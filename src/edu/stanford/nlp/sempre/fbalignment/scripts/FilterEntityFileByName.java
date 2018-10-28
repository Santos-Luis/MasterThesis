package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Takes an entity info file and a file with IDs and names and filters the
 * entity info file to contain only the entries that have the particular names
 * in the name file. e.g. if "fb:en.ted_bundy" has name "ted bundy" then an
 * entry saying that "fb:en.ted_bundy" is also called "tedd bundy" will be
 * omitted. arg0 - entity info file arg1 - new entity info file (filterd) arg2 -
 * name file (tab delimited - id property "name"@en
 *
 * @author jonathanberant
 */
public final class FilterEntityFileByName {
  private FilterEntityFileByName() { }

  public static void main(String[] args) throws IOException {
    // load names
    Map<String, Set<String>> id2Name = new HashMap<String, Set<String>>();
    for (String line : IOUtils.readLines(args[2])) {
      String[] tokens = line.split("\t");
      Set<String> names = id2Name.get(tokens[0]);
      if (names == null) {
        names = new HashSet<String>();
        id2Name.put(tokens[0], names);
      }
      names.add(tokens[2]);
    }
    // write the new entity file
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    int i = 0;
    for (String line : IOUtils.readLines(args[0])) {
      String[] tokens = line.split("\t");
      String id = tokens[1];
      String entityName = tokens[3];

      Set<String> idNames = id2Name.get(id);
      if (idNames != null) {
        for (String idName : idNames) {
          idName = idName.substring(idName.indexOf('"') + 1, idName.lastIndexOf('"'));
          if (idName.equals(entityName)) {
            writer.println(line);
            i++;
            if (i % 100000 == 0)
              LogInfo.log("Line: " + line + ", entity name " + entityName + ", id name: " + idName);
          }
        }
      }
    }
    writer.close();
  }
}
