package edu.stanford.nlp.sempre.fbalignment.testers;

import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.sempre.fbalignment.bipartite.classify.ClassifierUtils;
import edu.stanford.nlp.sempre.fbalignment.bipartite.classify.DatumGenerator;
import edu.stanford.nlp.util.StringUtils;
import fig.basic.LogInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class LinearClassifierFromAnnotationFile {
  private LinearClassifierFromAnnotationFile() { }

  public static GeneralDataset<Boolean, String> loadDataset(String trainFile) throws IOException {

    GeneralDataset<Boolean, String> trainSet = new RVFDataset<Boolean, String>();

    int count = 0;
    for (String line : ObjectBank.getLineIterator(new File(trainFile))) {
      if (line.startsWith("pred\t")) { // skip title
        continue;
      }
      Datum<Boolean, String> datum = DatumGenerator.lineToDatum(line);
      trainSet.add(datum);
      count++;
    }
    System.err.println("Loaded " + count + " datums from " + trainFile);
    return trainSet;
  }

  public static List<String> loadAnnotations(String annotationFile) {

    List<String> res = new ArrayList<String>();
    for (String line : ObjectBank.getLineIterator(new File(annotationFile))) {
      res.add(line);
    }
    return res;
  }

  public static void main(String[] args) throws IOException {

    Properties props = StringUtils.argsToProperties(args);
    LogInfo.begin_track("main");
    LogInfo.logs("%s", props);

    LogInfo.begin_track("train");
    LogInfo.log("train and cross validate");
    LinearClassifier<Boolean, String> classifier = trainAndCrossValidate(props.getProperty("train-set-file"));
    LogInfo.log("saving");
    IOUtils.writeObjectToFile(classifier, props.getProperty("classifier-file"));
    LogInfo.log("printing tp tn fp fn");
    printFpFnTpTn(props.getProperty("train-set-file"), classifier);
    LogInfo.end_track("train");
  }

  public static void trainAndSave(String[] args) throws IOException {

    GeneralDataset<Boolean, String> trainSet = loadDataset(args[0]);
    LinearClassifier<Boolean, String> classifier = train(trainSet);
    IOUtils.writeObjectToFile(classifier, args[1]);
  }

  public static LinearClassifier<Boolean, String> trainAndCrossValidate(String trainSetFile) throws IOException {
    // load the training dataset
    GeneralDataset<Boolean, String> trainSet = loadDataset(trainSetFile);
    LinearClassifier<Boolean, String> classifier = train(trainSet);
    ClassifierUtils.crossValidate(trainSet, new LinearClassifierFactory<Boolean, String>(), 5);
    return classifier;
  }

  public static LinearClassifier<Boolean, String> train(GeneralDataset<Boolean, String> trainSet) {

    trainSet.summaryStatistics();

    LinearClassifierFactory<Boolean, String> factory = new LinearClassifierFactory<Boolean, String>();
    LinearClassifier<Boolean, String> classifier = ClassifierUtils.printTrainTestResults(trainSet, trainSet, factory);

    LogInfo.log("Trained weights:");

    for (String feature : classifier.features()) {
      for (Boolean label : classifier.labels()) {
        LogInfo.log(feature + "\t" + label + "\t" + classifier.weight(feature, label));
      }
    }
    return classifier;
  }

  public static void printFpFnTpTn(String trainSetFile, LinearClassifier<Boolean, String> classifier) throws IOException {

    GeneralDataset<Boolean, String> trainSet = loadDataset(trainSetFile);
    List<String> annotations = loadAnnotations(trainSetFile);

    for (int i = 0; i < trainSet.size(); i++) {

      Datum<Boolean, String> datum = trainSet.getDatum(i);
      Boolean predictedLabel = classifier.classOf(datum);
      double scoreOfLabel = classifier.scoreOf(datum, true);
      Boolean goldLabel = datum.label();
      if (predictedLabel && !goldLabel) {
        LogInfo.log("FP:\t" + annotations.get(i) + "\t" + scoreOfLabel);
      } else if (!predictedLabel && goldLabel) {
        LogInfo.log("FN:\t" + annotations.get(i) + "\t" + scoreOfLabel);
      } else if (predictedLabel && goldLabel) {
        LogInfo.log("TP:\t" + annotations.get(i) + "\t" + scoreOfLabel);
      } else {
        LogInfo.log("TN:\t" + annotations.get(i) + "\t" + scoreOfLabel);
      }
    }
  }


}
