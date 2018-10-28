package edu.stanford.nlp.sempre.fbalignment.unary;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import edu.stanford.nlp.sempre.freebase.utils.FormatConverter;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * handles a partially-linked file where there is <i>arg1, pred, arg2, mid</i> and
 * creates a unary tuples file <i>arg1, is-a, type</i>
 *
 * @author jonathanberant
 */
public class UnaryTupleGenerator {

  private static final Pattern unaryPattern = Pattern.compile("(is|was) (a|the) (.+) (in|at|for|from|about|of|under|on|near|around)");
  private static final Pattern isAPattern = Pattern.compile("(is|was) (a|the) ");
  private static final Pattern prepositionPattern = Pattern.compile(" (in|at|for|from|about|of|under|on|near|around)");


  public void generateUnaryTuples(String linkedExtractionFile, String unaryExtractionsFile, Map<String, String> midToIdMap) throws IOException {

    PrintWriter writer = IOUtils.getPrintWriter(unaryExtractionsFile);
    int i = 0;

    for (String line : IOUtils.readLines(linkedExtractionFile)) {

      String[] tokens = line.split("\t");
      String predicate = tokens[1];
      String mid = tokens[3];
      if (isUnaryPredicate(predicate)) {

        String unaryDesc = extractUnaryDescription(predicate);
        String id = midToIdMap.get(FormatConverter.fromNoPrefixMidToDot(mid));
        if (id != null) {
          writer.println(tokens[0] + "\tis-a\t" + unaryDesc + "\t" + id);
        }
      }
      i++;
      if (i % 10000 == 0) {
        LogInfo.log("Line: " + line);
        LogInfo.log("Number of lines: " + i);
      }
    }
    writer.close();
  }


  /** @throws Exception  */
  public String extractUnaryDescription(String unaryPredicate) {

    Matcher unaryMatcher = unaryPattern.matcher(unaryPredicate);
    boolean matches = unaryMatcher.find();

    if (!matches)
      throw new RuntimeException("Not a unary predicate: " + unaryPredicate);

    String unary = unaryMatcher.group();
    Matcher isAMatcher = isAPattern.matcher(unary);
    Matcher prepositionMatcher = prepositionPattern.matcher(unary);

    isAMatcher.find();
    int startIndex = isAMatcher.end();

    int endIndex = -1;
    while (prepositionMatcher.find()) {
      endIndex = prepositionMatcher.start();
    }
    return unary.substring(startIndex, endIndex);
  }

  public boolean isUnaryPredicate(String predicate) {

    Matcher m = unaryPattern.matcher(predicate);
    return m.matches();
  }

  public static void main(String[] args) throws Exception {

    LogInfo.begin_track("Loading mid to id map");
    LogInfo.log("Starting upload");
    Map<String, String> midToIdMap = FileUtils.loadStringToStringMap(args[2], 0, 1);
    LogInfo.end_track("Loading mid to id map");

    UnaryTupleGenerator utg = new UnaryTupleGenerator();
    utg.generateUnaryTuples(args[0], args[1], midToIdMap);
  }


}
