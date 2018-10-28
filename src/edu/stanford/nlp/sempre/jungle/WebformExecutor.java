package edu.stanford.nlp.sempre.jungle;

import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.cache.StringCache;
import edu.stanford.nlp.sempre.cache.StringCacheUtils;
import fig.basic.LogInfo;
import fig.basic.Option;
import fig.basic.StrUtils;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert a Formula into a form-filling query on a website. Needs to get
 * updated like SparqlExecutor.
 * TODO(pliang): this should be a normal function, not an executor.
 *
 * @author Percy Liang
 */
public class WebformExecutor extends Executor {
  public static class Options {
    @Option(gloss = "Milliseconds to wait until opening connection times out")
    public int connectTimeoutMs = 20000;
    @Option(gloss = "Milliseconds to wait until reading connection times out")
    public int readTimeoutMs = 20000;
    @Option(gloss = "Cache documents based on URL") public String cachePath;
    @Option public int verbose = 0;
    @Option public int numRequestTrials = 1;
    @Option public boolean fetchUrl = true;
  }

  public static Options opts = new Options();
  private StringCache url2docCache;

  public static final String timeoutResponse = "TIMEOUT";
  public static final String errorResponse = "ERROR";

  public WebformExecutor() {
    if (opts.cachePath != null)
      this.url2docCache = StringCacheUtils.create(opts.cachePath);
  }

  class Query {
    // Input
    String uri;
    Map<String, String> params = new LinkedHashMap<String, String>();
    String fullUrl;  // Based on input

    // Output
    String outFormat;  // "json" or "html"
    // Specification of what to extract
    String outRoot;  // for JSON: field pointing to list of results
                     // for HTML: used to find items on the page and extract the URLs
    List<String> outFields = new ArrayList<String>(); // for JSON: Which fields to display
  }

  public Response execute(Formula formula, ContextValue context) {
    formula = Formulas.betaReduction(formula);
    LogInfo.logs("Execute: %s", formula);

    Map<String, String> properties = new LinkedHashMap<String, String>();
    Query query = new Query();
    fillQuery(formula, query);

    String url = query.fullUrl = queryToUrl(query);

    if (!opts.fetchUrl) {
      return new Response(new UriValue(url));
    }

    String doc = url2docCache == null ? null : url2docCache.get(url);
    boolean print = false;
    if (doc == null) {
      print = true;
      LogInfo.begin_track("WebformExecutor.execute: convert formula to query");
      if (formula != null) LogInfo.logs("%s", formula);
      LogInfo.logs("%s", url);
      doc = makeRequest(url);
      if (url2docCache != null)
        url2docCache.put(url, doc);
    }

    // Parse the response: hack: just search for instances of outFilter, and select the string.
    // Example: <a id="bizTitleLink0" href="/biz/indochine-palo-alto-2">1. Indochine
    Value value = parseValue(query, doc);

    if (print) LogInfo.end_track();
    return new Response(value);
  }

  Value parseValue(Query query, String doc) {
    if (query.outFormat.equals("json")) return parseJson(query, doc);
    if (query.outFormat.equals("html")) return parseHtml(query, doc);
    throw new RuntimeException("Unknown format: " + query.outFormat);
  }

  @SuppressWarnings("unchecked")
  Value parseJson(Query query, String doc) {
    Map<String, Object> map = Json.readMapHard(doc);

    // Find root
    List<Object> results = (List<Object>) map.get(query.outRoot);
    // if (value == null) throw new RuntimeException("Field " + field + " of " + map + " is not good");

    // Extract children
    List<Value> values = new ArrayList<Value>();
    for (Object result : results) {
      List<Value> rowValues = new ArrayList<Value>();
      Map<String, Object> row = (Map<String, Object>) result;
      for (String field : query.outFields) {
        Object rowValue = row.get(field);
        // if (rowValue == null) throw new RuntimeException("Row missing " + field + ": " + row);
        rowValues.add(toValue(rowValue));
      }
      values.add(new ListValue(rowValues));
    }
    return new ListValue(values);
  }

  private static Value toValue(Object obj) {
    if (obj == null) return new StringValue("");
    if (obj instanceof Boolean) return new BooleanValue((Boolean) obj);
    if (obj instanceof Integer) return new NumberValue(((Integer) obj).intValue());
    if (obj instanceof Double) return new NumberValue(((Double) obj).doubleValue());
    if (obj instanceof String) return new StringValue((String) obj);
    throw new RuntimeException("Invalid object: " + obj);
  }

  Value parseHtml(Query query, String doc) {
    List<Value> values = new ArrayList<Value>();
    int i = 0;
    while (true) {
      i = doc.indexOf(query.outRoot, i);
      if (i == -1) break;
      int j = doc.indexOf(">", i);
      if (j == -1) break;
      int k = doc.indexOf("</a>", j);
      if (k == -1) break;
      String str = doc.substring(j + 1, k);
      str = str.trim();
      str = str.replaceAll("<[^>]+>", "");  // Remove HTML tags
      str = str.replaceAll("\\s+", " ");
      str = str.replaceAll("&amp;", "&");
      String uri = null;
      int h1 = doc.lastIndexOf("href=", k);
      if (h1 >= i) {
        int h2 = doc.indexOf("\"", h1 + 6);
        if (h2 != -1)
          uri = doc.substring(h1 + 6, h2);
      }
      try {
        uri = new URL(new URL(query.fullUrl), uri).toString();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      values.add(new NameValue(str, uri));
      i = k;
    }
    return new ListValue(values);
  }

  public String queryToUrl(Query query) {
    StringBuilder out = new StringBuilder();
    out.append(query.uri);
    boolean first = true;
    for (Map.Entry<String, String> e : query.params.entrySet()) {
      out.append(first ? "?" : "&");
      first = false;
      try {
        out.append(e.getKey() + "=" + URLEncoder.encode(e.getValue(), "UTF-8"));
      } catch (UnsupportedEncodingException ex) {
        throw new RuntimeException(ex);
      }
    }
    return out.toString();
  }

  public String makeRequest(String url) {
    for (int trial = 0; true; trial++) {
      try {
        URLConnection conn = new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Firefox");
        conn.setConnectTimeout(opts.connectTimeoutMs);
        conn.setReadTimeout(opts.readTimeoutMs);
        InputStream in = conn.getInputStream();

        StringBuilder buf = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null)
          buf.append(line);
        return buf.toString();
      } catch (SocketTimeoutException e) {
        return timeoutResponse;
      } catch (IOException e) {
        LogInfo.errors("Server exception: %s", e);
        if (trial == opts.numRequestTrials - 1)
          throw new RuntimeException(e);
      }
    }
  }

  private void fillQuery(Formula rawFormula, Query query) {
    if (rawFormula instanceof MergeFormula) {
      MergeFormula formula = (MergeFormula) rawFormula;
      fillQuery(formula.child1, query);
      fillQuery(formula.child2, query);
    } else if (rawFormula instanceof JoinFormula) {
      JoinFormula formula = (JoinFormula) rawFormula;
      String key = Formulas.getString(formula.relation);
      if (key == null) throw new RuntimeException("Couldn't extract string from " + formula.relation);
      String value = Formulas.getString(formula.child);
      if (value == null) throw new RuntimeException("Couldn't extract string from " + formula.child);
      LogInfo.logs("%s => %s", key, value);
      if (key.equals(".uri")) query.uri = value;
      else if (key.equals(".outFormat")) query.outFormat = value;
      else if (key.equals(".outRoot")) query.outRoot = value;
      else if (key.equals(".outFields")) query.outFields = Arrays.asList(StrUtils.split(value, " "));
      else query.params.put(key, value);
    }
  }
}
