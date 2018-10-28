package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FormatConverter;
import edu.stanford.nlp.sempre.freebase.utils.FreebaseUtils;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Goes over a datadump file (.ttl format) and generates a file with 1. unary
 * made-up MID 2. unary ID 3. unary popularity - how many instances in the
 * datadump 4. Names
 *
 * @author jonathanberant
 */
public final class GenerateUnaryInfoFile {
  private GenerateUnaryInfoFile() { }

  public static void main(String[] args) throws IOException {

    LogInfo.begin_track("Main");
    generateUnaryStringInfo(args[0], args[1]);
    LogInfo.end_track("Main");
  }

  private static void generateUnaryStringInfo(String infile, String outfile) throws IOException {

    Counter<Pair<String, String>> unaryPopularityCounter = new ClassicCounter<Pair<String, String>>();
    Set<String> unaryValues = new HashSet<String>();
    // get the unaries and their popularity
    int i = 0;
    for (String line : IOUtils.readLines(infile)) {

      String[] tokens = line.split("\t");
      if (tokens.length < 3) // skip prefixes
        continue;

      if (FreebaseUtils.isUnary(tokens[1])) {
        String value = tokens[2].substring(0, tokens[2].length() - 1);
        if (FreebaseUtils.isValidTypePrefix(FormatConverter.fromDotToSlash(value))) {
          Pair<String, String> pair = new Pair<String, String>(tokens[1], value);

          if (unaryPopularityCounter.getCount(pair) == 0)
            LogInfo.log("Found unary: " + pair);

          unaryPopularityCounter.incrementCount(pair);
          unaryValues.add(value);
        }
      }
      i++;
      if (i % 1000000 == 0)
        LogInfo.log("Number of lines: " + i);
    }
    i = 0;
    Map<String, Set<String>> idToAliasesMap = new HashMap<String, Set<String>>();
    // get the names and aliases
    for (String line : IOUtils.readLines(infile)) {

      String[] tokens = line.split("\t");
      if (tokens.length < 3) continue;

      if (unaryValues.contains(tokens[0]) && FreebaseUtils.isNameProperty(tokens[1]) && isEnglishName(tokens[2])) {

        String name = extractName(tokens[2]);
        Set<String> names = idToAliasesMap.get(tokens[0]);
        if (names == null) {
          names = new HashSet<String>();
          idToAliasesMap.put(tokens[0], names);
          LogInfo.log("Found name for id: " + tokens[0] + " name: " + name);
        }
        names.add(name);
      }
      i++;
      if (i % 1000000 == 0)
        LogInfo.log("Number of lines: " + i);
    }
    // print out
    PrintWriter writer = IOUtils.getPrintWriter(outfile);
    for (Pair<String, String> unary : unaryPopularityCounter.keySet()) {

      Set<String> names = idToAliasesMap.get(unary.second());
      if (names == null) {
        LogInfo.log("type with no names: " + unary);
      } else {
        for (String name : names) {
          writer.println("NO_MID\t(" + unary.first() + " " + unary.second + ")\t" + unaryPopularityCounter.getCount(unary) + "\t" + name);

        }
      }
    }
    writer.close();
  }

  private static boolean isEnglishName(String string) {
    return string.endsWith("@en.");
  }

  private static String extractName(String string) {

    int start = string.indexOf('"') + 1;
    int end = string.lastIndexOf('"');
    return string.substring(start, end);
  }

}
