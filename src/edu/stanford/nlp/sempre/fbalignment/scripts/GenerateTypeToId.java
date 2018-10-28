package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates file with types and an ID for them for alignment
 *
 * @author jonathanberant
 */
public final class GenerateTypeToId {
  private GenerateTypeToId() { }

  public static void main(String[] args) throws IOException {
    Map<String, Integer> res = new TreeMap<String, Integer>();
    res.put("fb:type.object", 0);
    int currId = 1;

    int i = 0;
    for (String line : IOUtils.readLines(args[0])) {

      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;
      if (tokens[1].equals("fb:type.object.type")) {
        String type = tokens[2];
        if (validType(type)) {
          if (!res.containsKey(type)) {
            res.put(type, currId++);
          }
        }
      }

      i++;
      if (i % 1000000 == 0) {
        LogInfo.log("Number of lines: " + i);
      }
    }
    res.put("fb:type.datetime", currId++);
    res.put("fb:m.03wnggz", currId++);
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (String type : res.keySet()) {
      writer.println(type + "\t" + res.get(type));
    }
    writer.close();
  }

  private static boolean validType(String type) {
    return !(type.startsWith("fb:base.") ||
        type.startsWith("fb:user."));
  }
}
