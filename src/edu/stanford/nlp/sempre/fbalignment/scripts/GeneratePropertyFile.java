package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generate all properties in a freebase .ttl file that pass a certain filter
 *
 * @author jonathanberant
 */
public final class GeneratePropertyFile {
  private GeneratePropertyFile() { }

  public static void main(String[] args) throws IOException {

    String freebaseFile = args[0];
    Set<String> properties = new TreeSet<String>();
    int i = 0;
    for (String line : IOUtils.readLines(freebaseFile)) {

      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;
      if (validProperty(tokens[1]))
        properties.add(tokens[1]);
      i++;
      if (i % 1000000 == 0)
        System.out.println("Lines: " + i);
    }

    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (String property : properties)
      writer.println(property);
    writer.close();
  }

  private static boolean validProperty(String property) {

    if (property.equals("fb:common.topic.alias"))
      return true;

    return !(property.startsWith("fb:base.") || property.startsWith("fb:common.") ||
        property.startsWith("fb:user.") ||
        property.startsWith("fb:dataworld.") ||
        property.startsWith("fb:freebase.") ||
        property.startsWith("fb:type."));
  }


}
