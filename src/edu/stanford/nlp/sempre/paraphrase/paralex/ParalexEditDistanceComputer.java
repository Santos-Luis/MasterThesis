package edu.stanford.nlp.sempre.paraphrase.paralex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.paraphrase.ParaphraseUtils;


/**
 * Computes the edit distance between paralex sentences for various edit distance values
 * @author jonathanberant
 *
 */
public final class ParalexEditDistanceComputer {
  private ParalexEditDistanceComputer() { }

  public static void main(String[] args) throws IOException {

    List<List<String>> res = new ArrayList<List<String>>();
    for (int i = 0; i < 5; ++i) {
      res.add(new ArrayList<String>());
    }
    for (String line : IOUtils.readLines(args[0])) {
      String[] tokens = line.split("\t");
      List<String> utter1 = Arrays.asList(tokens[0].split("\\s+"));
      List<String> utter2 = Arrays.asList(tokens[1].split("\\s+"));
      int distance = ParaphraseUtils.editDistance(utter1, utter2);
      if (distance < 5)
        res.get(distance).add(line);
    }

    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (int i = 1; i < res.size(); ++i) {
      for (String line : res.get(i)) {
        writer.println(i + "\t" + line);
      }
    }
    writer.close();
  }

}
