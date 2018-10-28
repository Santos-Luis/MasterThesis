package edu.stanford.nlp.sempre.fbalignment.scripts;

import edu.stanford.nlp.io.IOUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

/**
 * Takes a freebase file or freebase schema file (faster) and finds Cvts
 *
 * @author jonathanberant
 */
public final class GenerateCvts {
  private GenerateCvts() { }

  public static void main(String[] args) throws IOException {

    String freebaseFile = args[0];
    Set<String> cvts = new TreeSet<String>();
    for (String line : IOUtils.readLines(freebaseFile)) {

      String[] tokens = edu.stanford.nlp.sempre.freebase.Utils.parseTriple(line);
      if (tokens == null) continue;
      if (tokens[1].equals("fb:freebase.type_hints.mediator")) {
        if (tokens[2].equals("\"true\"^^xsd:boolean")) cvts.add(tokens[0]);
      }

    }
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (String cvt : cvts)
      writer.println(cvt);
    writer.close();
  }
}
