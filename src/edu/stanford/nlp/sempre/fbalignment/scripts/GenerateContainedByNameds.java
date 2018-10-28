package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public final class GenerateContainedByNameds {
  private GenerateContainedByNameds() { }

  public static void main(String[] args) throws IOException {

    Map<String, String> id2Name = FileUtils.loadStringToStringMap(args[2], 1, 3);
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (String line : IOUtils.readLines(args[0])) {
      String[] tokens = line.split("\t");
      String id = tokens[0];
      String container = tokens[1].substring(0, tokens[1].length() - 1);
      String containerName = id2Name.get(container);
      if (containerName != null) {
        writer.println(id + "\t" + container + "\t" + containerName);
      } else {
        LogInfo.log("no name for container:" + container);
      }

    }
    writer.close();
  }
}
