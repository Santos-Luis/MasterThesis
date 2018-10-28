package edu.stanford.nlp.sempre.paraphrase.scripts;

import edu.stanford.nlp.io.IOUtils;
import fig.basic.DoubleVec;
import fig.basic.LogInfo;

/**
 * Takes two log files and outputs where they disagree on some criteria
 * @author jonathanberant
 *
 */
public final class CompareLogFiles {
  private CompareLogFiles() { }

  public static void main(String[] args) {
    String logFile1 = args[0];
    String logFile2 = args[1];
    String criterion = args[2];
    DoubleVec results1 = getResults(logFile1, criterion);
    DoubleVec results2 = getResults(logFile2, criterion);
    for (int i = 0; i < results1.size(); ++i) {
      double score1 = results1.get(i);
      double score2 = results2.get(i);
      if (Math.abs(score2 - score1) > 0.00001) {
        LogInfo.logs("example\t%s\tscore1\t%s\tscore2\t%s", i, score1, score2);
      }
    }
  }

  private static DoubleVec getResults(String logFile, String criterion) {

    DoubleVec res = new DoubleVec();
    boolean record = false;
    int currExample = -1;
    for (String line : IOUtils.readLines(logFile)) {
      if (line.contains("dev: example")) {
        currExample = Integer.parseInt(line.substring(line.indexOf("dev: example") + 13, line.indexOf("/")));
        record = true;
      }
      if (line.contains("train: example"))
        record = false;
      if (record && line.contains("Current: ")) {
        String[] tokens = line.split("\\s+");
        for (String token : tokens) {
          if (token.startsWith(criterion + "=")) {
            double score  = Double.parseDouble(token.substring(token.indexOf("=") + 1));
            res.setGrow(currExample, score);
          }
        }
      }
    }
    res.trimToSize();
    return res;
  }

}
