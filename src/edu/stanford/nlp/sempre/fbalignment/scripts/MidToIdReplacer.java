package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Takes a file with MIDs and converts them to ID args[0] - input file args[1] -
 * output file args[2] - mid-to-id file
 *
 * @author jonathanberant
 */
public final class MidToIdReplacer {
  private MidToIdReplacer() { }

  public static void main(String[] args) throws IOException {

    LogInfo.begin_track("Loading mid to id map");
    LogInfo.log("Starting upload");
    Map<String, String> midToIdMap = FileUtils.loadStringToStringMap(args[2], 0, 1);
    LogInfo.end_track("Loading mid to id map");
    Set<Character> endIndexDeimiter = new HashSet<Character>();
    endIndexDeimiter.add(' ');
    endIndexDeimiter.add(')');
    endIndexDeimiter.add('(');
    endIndexDeimiter.add('\t');

    BufferedReader reader = IOUtils.getBufferedFileReader(args[0]);
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    String line;
    int startIndex;
    while ((line = reader.readLine()) != null) {

      String newLine = line;
      do {
        startIndex = newLine.indexOf("fb:m.");
        int endIndex = newLine.length();

        if (startIndex != -1) {
          for (int i = startIndex; i < newLine.length(); ++i) {
            Character currChar = newLine.charAt(i);
            if (endIndexDeimiter.contains(currChar)) {
              endIndex = i;
              break;
            }
          }

          String mid = newLine.substring(startIndex, endIndex);
          String id = midToIdMap.get(mid);
          if (id == null) {
            throw new RuntimeException("could not find id for mid: " + mid + ", in line: " + line);
          }
          newLine = newLine.replace(mid, id);
        }
      }
      while (startIndex != -1);

      writer.println(newLine);
    }
    writer.close();
    reader.close();
  }

}
