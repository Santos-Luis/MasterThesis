package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * for each property generates the expected types either from a free base file
 * or from a schema
 *
 * @author jonathanberant
 */
public final class GenerateExpectedTypes {
  private GenerateExpectedTypes() { }

  public static void main(String[] args) throws IOException {

    String freebaseFile = args[0];
    Map<String, String> propetyToExtype1 = new HashMap<String, String>();
    Map<String, String> propetyToExtype2 = new HashMap<String, String>();
    for (String line : IOUtils.readLines(freebaseFile)) {

      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;

      String property = tokens[1];
      String arg1 = tokens[0];
      String arg2 = tokens[2];

      if (validProperty(arg1)) {
        if (property.equals("fb:type.property.schema")) {
          propetyToExtype1.put(arg1, arg2);
        } else if (property.equals("fb:type.property.expected_type")) {
          propetyToExtype2.put(arg1, arg2);
        }
      }
    }
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (String property : propetyToExtype2.keySet()) {
      String type1 = propetyToExtype1.get(property);
      String type2 = propetyToExtype2.get(property);
      if (type1 == null)
        continue;
      writer.println(property + "\t" + type1 + "\t" + type2);
    }
    writer.close();
  }

  private static boolean validProperty(String property) {
    return !(property.startsWith("fb:base.") || property.startsWith("fb:common.") ||
        property.startsWith("fb:user.") ||
        property.startsWith("fb:dataworld.") ||
        property.startsWith("fb:freebase.") ||
        property.startsWith("fb:m.") ||
        property.startsWith("fb:type."));
  }
}
