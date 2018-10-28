package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.FbFormulasInfo;
import edu.stanford.nlp.sempre.freebase.FbFormulasInfo.BinaryFormulaInfo;
import edu.stanford.nlp.sempre.Formulas;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import fig.basic.LispTree;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Goes over freebase and finds for every atomic property: a) popularity b)
 * expected type 1 c) expected type 2 d) unit 1 e) unit description f) name *
 * @author jonathanberant
 */
public final class GenerateAtomicBinaryFormulaInfo {
  private GenerateAtomicBinaryFormulaInfo() { }

  public static boolean filter = true; // whether to filter some of the properties

  public static void main(String[] args) throws IOException {

    String freebaseFile = args[0];
    String schemaFile = args[1];
    String outFile = args[2];
    Counter<String> popularityMap = new ClassicCounter<String>();
    Map<String, FbFormulasInfo.BinaryFormulaInfo> formulaInfoMap = new HashMap<String, FbFormulasInfo.BinaryFormulaInfo>();
    // iteration 1: generate popularity map
    LogInfo.log("Counting popularity");
    int i = 0;
    for (String line : IOUtils.readLines(freebaseFile)) {
      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;
      popularityMap.incrementCount(tokens[1]);
      if (!formulaInfoMap.containsKey(tokens[1]))
        formulaInfoMap.put(
            tokens[1], new BinaryFormulaInfo(
                Formulas.fromLispTree(LispTree.proto.parseFromString(tokens[1])),
                "", "", new LinkedList<String>(), 0.0));
      i++;
      if (i % 1000000 == 0)
        LogInfo.log("Lines: " + i);
    }
    LogInfo.log("Getting info from schema");
    for (String line : IOUtils.readLines(schemaFile)) {
      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;
      BinaryFormulaInfo info = formulaInfoMap.get(tokens[0]);
      if (info != null) {
        if (tokens[1].equals("fb:type.object.name")) {
          String description = tokens[2].substring(tokens[2].indexOf('"') + 1, tokens[2].lastIndexOf('"')).toLowerCase();
          info.descriptions.add(description);
        } else if (tokens[1].equals("fb:type.property.schema")) {
          info.expectedType1 = tokens[2];
        } else if (tokens[1].equals("fb:type.property.expected_type")) {
          info.expectedType2 = tokens[2];
          // also fix popularity here for no particular reason
          info.popularity = popularityMap.getCount(tokens[0]);
        } else if (tokens[1].equals("fb:type.property.unit")) {
          info.unitId = tokens[2];
        }
      }
    }
    PrintWriter writer = IOUtils.getPrintWriter(outFile);
    for (String formula : Counters.toSortedListKeyComparable(popularityMap)) {

      if (filter && toFilter(formula))
        continue;

      BinaryFormulaInfo info = formulaInfoMap.get(formula);
      if (!info.isComplete()) {
        LogInfo.log("WARNING: skipping line where info is not complete: " + info);
        continue;
      }
      for (String description : info.descriptions) {
        writer.println("MID\t" + info.formula + "\t" + info.popularity + "\t" + description + "\t" + info.expectedType1 + "\t" + info.expectedType2 + "\t" + info.unitId + "\t");
        writer.println("MID\t!" + info.formula + "\t" + info.popularity + "\t" + description + "\t" + info.expectedType2 + "\t" + info.expectedType1 + "\t" + info.unitId + "\t");
      }
    }
    writer.close();
  }

  public static boolean toFilter(String property) {
    if (property.equals("fb:common.topic.alias"))
      return false;

    return (property.startsWith("fb:base.") || property.startsWith("fb:common.") ||
        property.startsWith("fb:user.") ||
        property.startsWith("fb:dataworld.") ||
        property.startsWith("fb:freebase.") ||
        property.startsWith("fb:type."));
  }
}
