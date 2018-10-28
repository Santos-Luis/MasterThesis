package edu.stanford.nlp.sempre.fbalignment.testers;

import edu.stanford.nlp.sempre.freebase.EntityLexicon;
import edu.stanford.nlp.sempre.freebase.lexicons.LexicalEntry;
import edu.stanford.nlp.sempre.freebase.Lexicon;
import fig.basic.LogInfo;
import fig.basic.StopWatch;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

public final class LexiconTester {
  private LexiconTester() { }

  public static void main(String[] args) throws IOException, ParseException {

    LogInfo.begin_track("Main");
    Pattern quit =
        Pattern.compile("quit|exit|q|bye", Pattern.CASE_INSENSITIVE);

    Lexicon lexicon = Lexicon.getSingleton();

    BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
    StopWatch watch = new StopWatch();
    while (true) {
      watch.reset();
      System.out.print("Lookup> ");
      String question = is.readLine().trim();
      if (quit.matcher(question).matches()) {
        System.out.println("Quitting.");
        break;
      }
      if (question.equals(""))
        continue;

      int sep = question.indexOf(':');
      if (sep == -1) {
        usage();
      } else {
        String queryType = question.substring(0, sep);
        String query = question.substring(sep + 1);


        watch.start();
        if (queryType.equals("u")) {
          List<? extends LexicalEntry> entries = lexicon.lookupUnaryPredicates(query);
          for (LexicalEntry entry : entries)
            System.out.println(entry);
        } else if (queryType.equals("e")) {
          List<? extends LexicalEntry> entries = lexicon.lookupEntities(query, EntityLexicon.SearchStrategy.inexact);
          for (LexicalEntry entry : entries)
            System.out.println(entry);
        } else if (queryType.equals("b")) {
          List<? extends LexicalEntry> entries = lexicon.lookupBinaryPredicates(query);
          for (LexicalEntry entry : entries)
            System.out.println(entry);
        } else {
          usage();
        }
        watch.stop();
        LogInfo.log("Time: %s: " + watch);
      }
    }
    LogInfo.end_track("Main");
  }

  private static void usage() {
    System.out.println(
        "Query should start with either 'u' if looking for an FB unary or 'e' if looking" +
            " for an FB entity, followed by a colon, followed by the query itself");
  }

}
