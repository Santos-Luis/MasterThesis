package edu.stanford.nlp.sempre.fbalignment.testers;

import edu.stanford.nlp.sempre.fbalignment.unary.UnaryInfoData;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import fig.basic.LogInfo;

import java.io.IOException;
import java.util.Map;

public final class UnaryInfoTester {
  private UnaryInfoTester() { }

  public static void main(String[] args) throws IOException {
    // fixIdsInUnaryInfo(args[0],args[1],args[2]);
    addAliases(args[0], args[1], args[2]);
  }

  public static void fixIdsInUnaryInfo(String unaryInFile, String unaryOutFile, String midToIdFile) throws IOException {

    LogInfo.begin_track("Main");
    LogInfo.log("Loading mid to id file");
    Map<String, String> midToIdMap = FileUtils.loadStringToStringMap(midToIdFile, 0, 1);
    LogInfo.log("Loading unary info");
    UnaryInfoData unaryInfoData = UnaryInfoData.fromUnaryInfoFile(unaryInFile);
    LogInfo.log("Fixing bad IDs");
    unaryInfoData.fixBadIds(midToIdMap);
    LogInfo.log("Saving new info file");
    unaryInfoData.saveToUnaryInfoFile(unaryOutFile);
    LogInfo.end_track("Main");
  }

  public static void addAliases(String unaryInFile, String unaryOutFile, String nameFile) throws IOException {
    LogInfo.begin_track("Main");
    LogInfo.log("Loading unary info");
    UnaryInfoData unaryInfoData = UnaryInfoData.fromUnaryInfoFile(unaryInFile);
    LogInfo.log("Adding aliases");
    unaryInfoData.addAliases(nameFile);
    LogInfo.log("Saving new info file");
    unaryInfoData.saveToUnaryInfoFile(unaryOutFile);
    LogInfo.end_track("Main");
  }

}
