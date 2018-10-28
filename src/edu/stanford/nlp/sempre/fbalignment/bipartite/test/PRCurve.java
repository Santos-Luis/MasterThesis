package edu.stanford.nlp.sempre.fbalignment.bipartite.test;

import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Pair.ByFirstReversePairComparator;
import fig.basic.LogInfo;

import java.util.Collections;
import java.util.List;

/**
 * Computes a precision-recall curve Needs a list of all gold standard examples
 * with the score given by the system
 *
 * @author jonathanberant
 */
public class PRCurve {

  private Pair.ByFirstReversePairComparator<Double, HasLabel> comparator;
  private List<Pair<Double, HasLabel>> scoredGoldStandard;
  private int numOfGoldNegatives = 0;
  private int numOfGoldPositives = 0;

  public PRCurve(List<Pair<Double, HasLabel>> scoredGoldStandard) {
    comparator = new ByFirstReversePairComparator<Double, HasLabel>();
    this.scoredGoldStandard = scoredGoldStandard;
    Collections.sort(scoredGoldStandard, comparator);
    for (Pair<Double, HasLabel> pair : scoredGoldStandard) {
      if (pair.second().isPositive()) {
        numOfGoldPositives++;
      } else {
        numOfGoldNegatives++;
      }
    }
  }

  public void logPrecisionRecallCurve(int frequency) {

    int tp = 0, fp = 0, tn = numOfGoldNegatives, fn = numOfGoldPositives;
    int i = 0;

    LogInfo.log("recall\tprecision\tf1\taccuracy\tscore");

    for (Pair<Double, HasLabel> pair : scoredGoldStandard) {

      if (pair.second().isPositive()) {
        tp++;
        fn--;
      } else {
        tn--;
        fp++;
      }
      i++;
      if (i % frequency == 0) {
        LogInfo.log(recall(tp, fn) + "\t" + precision(tp, fp) + "\t" + f1(tp, fp, fn) + "\t" + accuracy(tp, fp, fn, tn) + "\t" + pair.first);
      }
    }
  }

  public static double precision(int tp, int fp) {
    if (tp + fp == 0)
      return 1;
    return (double) tp / (tp + fp);
  }

  public static double recall(int tp, int fn) {
    if (tp + fn == 0)
      return 1;
    return (double) tp / (tp + fn);
  }

  public static double f1(int tp, int fp, int fn) {

    double precision = precision(tp, fp);
    double recall = recall(tp, fn);
    return (2 * precision * recall) / (precision + recall);
  }

  public static double accuracy(int tp, int fp, int fn, int tn) {

    if (tn + fp + fn + tn == 0)
      throw new IllegalArgumentException("Gold standard size is zero");
    else {
      return (double) (tp + tn) / (tp + fp + fn + tn);
    }
  }

  public static String allMeasures(int tp, int fp, int fn, int tn) {

    StringBuilder sb = new StringBuilder();
    sb.append(recall(tp, fn) + "\t" + precision(tp, fp) + "\t" + f1(tp, fp, fn) + "\t" + accuracy(tp, fp, fn, tn));
    return sb.toString();
  }

}
