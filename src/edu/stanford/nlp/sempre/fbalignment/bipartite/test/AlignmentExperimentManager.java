package edu.stanford.nlp.sempre.fbalignment.bipartite.test;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.fbalignment.FreebaseAlignmentDataManager;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.logging.PrettyLogger;
import edu.stanford.nlp.util.logging.StanfordRedwoodConfiguration;
import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class AlignmentExperimentManager {

  private String goldFile;
  private FreebaseAlignmentDataManager fadm;
  private static final int DEFAULT_FREQUENCY = 1;

  public AlignmentExperimentManager(Properties props) throws IOException, ClassNotFoundException {

    goldFile = props.getProperty("gold-file");
    fadm = IOUtils.readObjectFromFile(props.getProperty("alignment-data-manager-file"));
  }

  public List<AnnotatedAlignmentExample> generateGoldExamples() throws IOException {

    List<AnnotatedAlignmentExample> res = new LinkedList<AnnotatedAlignmentExample>();
    BufferedReader reader = IOUtils.getBufferedFileReader(goldFile);
    String line;
    while ((line = reader.readLine()) != null) {
      res.add(AnnotatedAlignmentExample.fromAnnotationLine(line));
    }
    reader.close();
    return res;
  }

  public void generatePRCurve(List<AnnotatedAlignmentExample> gold, int frequency) {

    List<Pair<Double, HasLabel>> scoredGoldStandard = new LinkedList<Pair<Double, HasLabel>>();

    for (AnnotatedAlignmentExample goldExample : gold) {

      double score = fadm.scoreAlignmentExample(goldExample);
      scoredGoldStandard.add(new Pair<Double, HasLabel>(score, goldExample));
    }

    PRCurve prCurve = new PRCurve(scoredGoldStandard);
    prCurve.logPrecisionRecallCurve(frequency);
  }

  public void generatePRCurve(List<AnnotatedAlignmentExample> gold) {
    generatePRCurve(gold, DEFAULT_FREQUENCY);
  }


  public void scoreGold(List<AnnotatedAlignmentExample> gold) {

    int tp = 0, tn = 0, fp = 0, fn = 0;

    for (AnnotatedAlignmentExample goldExample : gold) {

      boolean match = fadm.matchAlignmentExample(goldExample);
      boolean goldLabel = goldExample.isPositive();
      if (match && goldLabel)
        tp++;
      else if (!match && !goldLabel)
        tn++;
      else if (match && !goldLabel)
        fp++;
      else
        fn++;
    }

    LogInfo.log("Number of gold positives:\t" + (tp + fn));
    LogInfo.log("Number of labeled positives:\t" + (tp + fp));
    LogInfo.log("Number of gold negatives:\t" + (tn + fp));
    LogInfo.log("Accuracy:\t" + PRCurve.accuracy(tp, fp, fn, tn));
    LogInfo.log("Recall:\t " + PRCurve.recall(tp, fn));
    LogInfo.log("Precision:\t" + PRCurve.precision(tp, fp));
    LogInfo.log("F1:\t" + PRCurve.f1(tp, fp, fn));
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {

    Properties props = StringUtils.argsToProperties(args);
    StanfordRedwoodConfiguration.apply(props);

    LogInfo.begin_track("main");
    PrettyLogger.log(props);

    AlignmentExperimentManager aem = new AlignmentExperimentManager(props);
    List<AnnotatedAlignmentExample> gold = aem.generateGoldExamples();
    // aem.generatePRCurve(gold);
    aem.scoreGold(gold);
    LogInfo.end_track("main");
  }

}

