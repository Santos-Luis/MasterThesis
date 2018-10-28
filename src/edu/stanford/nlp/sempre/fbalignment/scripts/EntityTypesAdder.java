package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EntityTypesAdder {
  private EntityTypesAdder() { }

  public static void main(String[] args) throws IOException {

    LogInfo.log("loading ids from file: " + args[0]);
    Set<String> ids = FileUtils.loadSetFromTabDelimitedFile(args[0], 1);

    LogInfo.log("Loading id to types set");
    Map<String, Set<String>> idToTypeSet = loadIdToTypeSet(args[2], ids);
    LogInfo.log("writing new file");
    BufferedReader reader = IOUtils.getBufferedFileReader(args[0]);
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    String line;
    int numOfUntyped = 0;
    while ((line = reader.readLine()) != null) {

      String[] tokens = line.split("\t");

      String mid = tokens[0];
      String id = tokens[1];
      String popularity = tokens[2];
      String name = tokens[3];

      Set<String> types = idToTypeSet.get(id);

      if (types == null) {
        types = new HashSet<String>();
      }
      if (types.size() == 0) {
        numOfUntyped++;
        if (numOfUntyped % 100 == 0)
          LogInfo.log("could not find type for line: " + line);
      }

      StringBuilder sb = new StringBuilder();
      sb.append(mid + "\t" + id + "\t" + popularity + "\t" + name + "\t");
      for (String type : types) {
        sb.append(type + ",");
      }
      sb.deleteCharAt(sb.length() - 1);
      writer.println(sb.toString());
    }
    LogInfo.log("Number of untyped: " + numOfUntyped);
    writer.close();
    reader.close();
  }

  private static Map<String, Set<String>> loadIdToTypeSet(String string, Set<String> ids) throws IOException {

    Map<String, Set<String>> res = new HashMap<String, Set<String>>();
    BufferedReader reader = IOUtils.getBufferedFileReader(string);
    String line;
    int i = 0;
    while ((line = reader.readLine()) != null) {

      String[] tokens = line.split("\t");

      String entity = tokens[0];
      String type = tokens[2];
      type = type.substring(0, type.length() - 1);
      if (ids.contains(entity)) {
        Set<String> types = res.get(entity);
        if (types == null) {
          types = new HashSet<String>();
          res.put(entity, types);
        }
        types.add(type);
      }

      i++;
      if (i % 1000000 == 0) {
        LogInfo.log("Creating id to types, line: " + i);
      }
    }

    reader.close();
    return res;
  }


}
