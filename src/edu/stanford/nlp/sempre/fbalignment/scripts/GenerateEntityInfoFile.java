package edu.stanford.nlp.sempre.fbalignment.scripts;

import com.google.common.base.Joiner;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import fig.basic.LogInfo;
import fig.basic.MapUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Takes a filtered freebase file (0.ttl), popularity file, mid-to-id-file, and
 * outputs an entity info file with fields |mid| |id| |popularity| |name|
 * |types| assumes the freebase files contains only names in English
 *
 * @author jonathanberant
 */

public final class GenerateEntityInfoFile {
  private GenerateEntityInfoFile() { }

  /**
   * args 0 - freebase file 1 - popularity file  (scr/fb_data/MidPopularity.txt)
   * 2 - mid-to-id file 3 - out file
   *
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    generateEntityInfo(args);
  }
  /**
   * Input
   * (0) mid to id - /u/nlp/data/sempre/scr/freebase/freebase-rdf-2013-06-09-00-00.canonical-id-map
   * (1) original linked extractions file - /user/joberant/scr/alignment/data/linked-extractions.tsv
   * (2) freebase file
   * @param args
   * @throws IOException
   */
  private static void generateEntityInfo(String[] args) throws IOException {

    LogInfo.log("Loading mid to id");
    Map<String, String> midToIdMap = FileUtils.loadStringToStringMap(args[0]);
    LogInfo.log("Compute popularity");
    Counter<String> idToPopularity = new ClassicCounter<String>();
    int i = 0;
    for (String line : IOUtils.readLines(args[1])) {
      String[] tokens = line.split("\t");
      String mid = "fb:m." + tokens[3];
      String id = MapUtils.get(midToIdMap, mid, mid);
      idToPopularity.incrementCount(id);
      i++;
      if (i % 1000000 == 0)
        LogInfo.logs("Uploaing line %s: %s", i, line);
    }
    i = 0;
    LogInfo.log("Uploading types");
    Map<String, Set<String>> idToTypes = new HashMap<String, Set<String>>();
    for (String line : IOUtils.readLines(args[2])) {
      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;

      if (tokens[1].equals("fb:type.object.type")) {
        String type = tokens[2];
        if (legalType(type))
          MapUtils.addToSet(idToTypes, tokens[0], tokens[2]);
      }
      i++;
      if (i % 1000000 == 0)
        System.out.println("Lines: " + i);
    }
    i = 0;
    LogInfo.log("Printing names");
    PrintWriter writer = IOUtils.getPrintWriter(args[3]);
    for (String line : IOUtils.readLines(args[2])) {
      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;
      if (tokens[1].equals("fb:type.object.name")) {
        String id = tokens[0];
        String name = edu.stanford.nlp.sempre.freebase.Utils.parseStr(tokens[2]);
        double popularity = idToPopularity.getCount(id);
        Set<String> types = MapUtils.get(idToTypes, id, new HashSet<String>());
        types.add("fb:common.topic");
        writer.println(
            "MID\t" + id + "\t" + popularity + "\t" + name + "\t" +
                Joiner.on(',').join(types));
      }
      i++;
      if (i % 1000000 == 0)
        LogInfo.log("Lines: " + i);
    }
    writer.close();
  }

  private static boolean legalType(String type) {

    if (type.startsWith("fb:base.")) {
      if (type.startsWith("fb:base.dinosaur"))
        return true;
      if (type.startsWith("base.popstra"))
        return true;
      if (type.startsWith("fb:base.saturdaynightlive"))
        return true;
      if (type.startsWith("fb:base.academyawards"))
        return true;
      if (type.startsWith("base.politicalconventions"))
        return true;
      if (type.startsWith("base.famouspets"))
        return true;
      return false;
    }
    return true;
  }
}
