package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.Json;
import fig.basic.LispTree;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by joberant on 7/1/14.
 * I am writing this so that we can compute the results according
 * to Xuchen's scheme
 */
public final class ExtractResultsFromLogFile {
  private ExtractResultsFromLogFile() { }

  public static void main(String[] args) throws IOException {

    String iter = null;
    String utterance = null;
    List<String> targetValues = null;
    List<String> predictedValues = new LinkedList<>();
    double comp = 0d;

    List<ResultEntry> results = new LinkedList<>();

    for (String line : IOUtils.readLines(args[0])) {
      if (line.contains("Processing") && line.contains("iter=")) {
        iter = extractIter(line);
      } else if (line.contains("Example: ")) {
        utterance = extractUtterance(line);
        targetValues = null;
        predictedValues = new LinkedList<>();
        comp = 0d;
      } else if (line.contains("targetValue: (list")) {
        targetValues = extractTargetValues(line);
      } else if (line.contains("Pred@0000")) {
        predictedValues = extractPredictedValues(line);
        comp = extractCompatibility(line);
      } else if (line.contains("Current: ")) {
        results.add(new ResultEntry(iter, utterance, targetValues, predictedValues, comp));
      }
    }

    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    fig.basic.LogInfo.logs("Writing");
    for (ResultEntry res : results) {
      writer.println(res);
    }
    writer.close();
  }

  private static double extractCompatibility(String line) {
    int startIndex = line.indexOf("comp=");
    int endIndex = line.lastIndexOf(']');
    return Double.parseDouble(line.substring(startIndex + 5, endIndex));
  }

  private static List<String> extractPredictedValues(String line) {
    List<String> res = new LinkedList<>();
    int startIndex = line.indexOf("(value ");
    int endIndex = line.indexOf("(proof ");
    if (endIndex == -1)
      endIndex = line.indexOf("(type");
    if (endIndex == -1)
      throw new RuntimeException("Could not find predicted values: " + line);

    LispTree tree = LispTree.proto.parseFromString(line.substring(startIndex, endIndex));
    assert tree.child(0).value.equals("value") : "not a value tree: " + tree;
    LispTree listTree = tree.child(1);
    if (!listTree.child(0).value.equals("error")) {
      assert listTree.child(0).value.equals("list") : "not a list tree: " + listTree;
      for (int i = 1; i < listTree.children.size(); ++i) {
        boolean isEntityOrNumberOrStringOrDateOrBoolean = listTree.child(i).child(0).value.equals("name") ||
                listTree.child(i).child(0).value.equals("number") ||
                listTree.child(i).child(0).value.equals("date") ||
                listTree.child(i).child(0).value.equals("boolean") ||
                listTree.child(i).child(0).value.equals("string");
        assert isEntityOrNumberOrStringOrDateOrBoolean : "not a name/number/string/date/boolean tree: " + listTree;
        if (listTree.child(i).children.size() == 2)
          res.add(listTree.child(i).child(1).value); // no name just an id
        else
          res.add(listTree.child(i).child(2).value);
      }
    }
    return res;
  }

  private static List<String> extractTargetValues(String line) {
    List<String> res = new LinkedList<>();
    LispTree tree = LispTree.proto.parseFromString(line.substring(line.indexOf("(list")));
    assert tree.child(0).value.equals("list");
    for (int i = 1; i < tree.children.size(); ++i) {
      assert tree.child(i).child(0).value.equals("description");
      res.add(tree.child(i).child(1).value);
    }
    return res;
  }

  private static String extractUtterance(String line) {
    return line.substring(line.indexOf("Example: ") + 9, line.indexOf("{")).trim();
  }

  private static String extractIter(String line) {
    return line.substring(line.indexOf('=') + 1, line.indexOf(':')).trim();
  }

  public static class ResultEntry {
    public final String iter;
    public final String utterance;
    public final List<String> targetValues;
    public final List<String> predictedValues;
    public final double comp;

    public ResultEntry(String iter, String utterance, List<String> targetValues,
                       List<String> predictedValues, double comp) {
      assert iter != null : "iter is null";
      assert  utterance != null : "utterance is null";
      assert  targetValues != null : "target values are null";
      assert predictedValues != null : "predicted values are null";
      assert comp >= 0d : "comp is not positive";
      this.iter = iter;
      this.utterance = utterance;
      this.targetValues = targetValues;
      this.predictedValues = predictedValues;
      this.comp = comp;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(iter + "\t");
      sb.append(utterance + "\t");
      sb.append(Json.writeValueAsStringHard(targetValues) + "\t");
      sb.append(Json.writeValueAsStringHard(predictedValues) + "\t");
      sb.append(comp);
      return sb.toString();
    }

    public String toNonJsonString() {
      StringBuilder sb = new StringBuilder();
      sb.append(iter + "\t");
      sb.append(utterance + "\t");
      sb.append(targetValues + "\t");
      sb.append(predictedValues + "\t");
      sb.append(comp);
      return sb.toString();
    }
  }
}
