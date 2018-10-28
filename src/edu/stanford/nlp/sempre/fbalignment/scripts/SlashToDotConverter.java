package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FormatConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * takes an infile as args[0] and an outfile as args[1] and a list of columns as
 * args[2] and converts the columns from slash-notation to dot notation
 *
 * @author jonathanberant
 */
public final class SlashToDotConverter {
  private SlashToDotConverter() { }

  public static void main(String[] args) throws IOException {

    BufferedReader reader = IOUtils.getBufferedFileReader(args[0]);
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    String[] cols = args[2].split(",");
    Set<Integer> columns = new HashSet<Integer>();
    for (int i = 0; i < cols.length; ++i)
      columns.add(new Integer(cols[i]));

    String line;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("\t");
      for (int i = 0; i < tokens.length; ++i) {
        if (columns.contains(i)) {
          writer.print(FormatConverter.fromSlashToDot(tokens[i], true));
        } else {
          writer.print(tokens[i]);
        }
        if (i < tokens.length - 1)
          writer.print("\t");
      }
      writer.println();
    }
    reader.close();
    writer.close();
  }

}
