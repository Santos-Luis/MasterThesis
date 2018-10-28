package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public final class MidToIdFixer {
  private MidToIdFixer() { }

  /**
   * 0 - input file 1 - output file 2 - mid-to-id file 3 - mid column 4 - id
   * column
   *
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    LogInfo.begin_track("Loading mid to id map");
    LogInfo.log("Starting upload");
    Map<String, String> midToIdMap = FileUtils.loadStringToStringMap(args[2], 0, 1);
    LogInfo.end_track("Loading mid to id map");
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);

    int midColumn = Integer.parseInt(args[3]);
    int idColumn = Integer.parseInt(args[4]);

    int numOfCorrected = 0;
    int i = 0;
    for (String line : IOUtils.readLines(args[0])) {

      String[] tokens = line.split("\t");

      String correctId = midToIdMap.get(tokens[midColumn]);
      String currentId = tokens[idColumn];

      if (correctId != null && !correctId.equals(currentId)) {
        writer.println(line.replace(currentId, correctId));
        LogInfo.log("Current: " + currentId + ", Corrected: " + correctId);
        numOfCorrected++;
      } else
        writer.println(line);
      i++;
      if (i % 1000000 == 0) {
        LogInfo.log("Number of lines: " + i);
      }
    }
    LogInfo.log("Number of corrected:" + numOfCorrected);
    writer.close();

  }

}
