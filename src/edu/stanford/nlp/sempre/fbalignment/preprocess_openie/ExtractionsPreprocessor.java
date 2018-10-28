package edu.stanford.nlp.sempre.fbalignment.preprocess_openie;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import fig.basic.LogInfo;
import fig.basic.Option;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

/**
 * Takes the original linked exrtaction file and output a file that is unary
 * tuples
 *
 * @author jonathanberant
 */
public abstract class ExtractionsPreprocessor {

  public static class Options {

    @Option(gloss = "Path to linked extractions")
    public String linkedExtractionsPath;
    @Option(gloss = "Path to write preprocessed extraction")
    public String outPath;
    @Option(gloss = "Path to mid-to-id file") public String midToIdPath;
    @Option(gloss = "Verbosity") public int verbose = 0;
  }

  public static Options opts = new Options();

  protected StanfordCoreNLP pipeline;
  protected Map<String, String> midToIdMap;
  protected Properties props;

  /** Initialize Stanford Core NLP annotator */
  public ExtractionsPreprocessor() {
    props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma");
    pipeline = new StanfordCoreNLP(props);
  }


  public void preprocess() throws IOException {

    LogInfo.begin_track("Loading mid to id map");
    midToIdMap = FileUtils.loadStringToStringMap(opts.midToIdPath, 0, 1);
    LogInfo.end_track();

    LogInfo.begin_track("Preprocessing lines");
    PrintWriter writer = IOUtils.getPrintWriter(opts.outPath);
    int i = 0;
    for (String line : IOUtils.readLines(opts.linkedExtractionsPath)) {
      String preprocessedLine = preprocessLine(line);
      if (preprocessedLine != null)
        writer.println(preprocessedLine);
      else if (opts.verbose >= 1) {
        LogInfo.log("Line not written: " + line + ", preprocessed line: " + preprocessedLine);
      }
      i++;
      if (i % 10000 == 0)
        LogInfo.log("ExtractionPreprocessor: finished line " + i);
    }
    writer.close();
    LogInfo.end_track();
  }

  public abstract String preprocessLine(String line);
}
