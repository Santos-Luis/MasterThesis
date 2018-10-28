package edu.stanford.nlp.sempre.fbalignment.bipartite.classify;

import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.sempre.fbalignment.bipartite.test.PRCurve;
import edu.stanford.nlp.util.Pair;
import fig.basic.LogInfo;

/**
 * Utils for printing the test performance and cross validation
 *
 * @author jonathanberant
 */
public final class ClassifierUtils {
  private ClassifierUtils() { }

  public static <F> LinearClassifier<Boolean, F> printTrainTestResults(GeneralDataset<Boolean, F> trainSet, GeneralDataset<Boolean, F> testSet,
                                                                       LinearClassifierFactory<Boolean, F> factory) {

    LinearClassifier<Boolean, F> classifier = factory.trainClassifier(trainSet);

    int tp = 0, tn = 0, fp = 0, fn = 0;
    for (int i = 0; i < testSet.size(); i++) {

      Datum<Boolean, F> datum = testSet.getDatum(i);
      Boolean predictedLabel = classifier.classOf(datum);
      Boolean goldLabel = datum.label();
      if (predictedLabel && goldLabel) {
        tp++;
      } else if (predictedLabel && !goldLabel) {
        fp++;
      } else if (!predictedLabel && goldLabel) {
        fn++;
      } else {
        tn++;
      }
    }

    LogInfo.log("Training stats:\t" + PRCurve.allMeasures(tp, fp, fn, tn));
    return classifier;
  }

  public static <F> void crossValidate(GeneralDataset<Boolean, F> trainSet, LinearClassifierFactory<Boolean, F> factory, int folds) {

    for (int i = 0; i < folds; ++i) {

      int start = trainSet.size() * i / folds;
      int end = trainSet.size() * (i + 1) / folds;

      Pair<GeneralDataset<Boolean, F>, GeneralDataset<Boolean, F>> trainTestPair = trainSet.split(start, end);
      trainTestPair.first().summaryStatistics();
      LogInfo.log("Iteration " + i);
      printTrainTestResults(trainTestPair.first(), trainTestPair.second(), factory);
    }
  }

}
