package edu.stanford.nlp.sempre.fbalignment.preprocess_openie;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.util.CoreMap;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

/**
 * this is a simply script for taking a linked extraction file and writing a
 * file with all extractions where arg2 is a time
 *
 * @author jonathanberant
 */
public final class LinkedTimeExtractionCreator {
  private LinkedTimeExtractionCreator() { }

  public static void main(String[] args) throws IOException {

    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    pipeline.addAnnotator(new TimeAnnotator("sutime", props));

    // create an empty Annotation just with the given text
    int i = 0;
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    for (String line : IOUtils.readLines(args[0])) {

      String[] tokens = line.split("\t");
      Annotation timeCandidate = new Annotation(tokens[2]);
      pipeline.annotate(timeCandidate);
      List<CoreMap> timexAnns = timeCandidate.get(TimeAnnotations.TimexAnnotations.class);
      if (timexAnns.size() == 1) {

        String value = timexAnns.get(0).get(TimeAnnotations.TimexAnnotation.class).value();
        if (value == null)
          continue;

        writer.println(
            tokens[0] + "\t" + tokens[1] + "\t" + "TIME:" + value + "\t" +
                tokens[3] + "\t" + tokens[4] + "\t" + tokens[5]);
      }
      i++;
      if (i % 100000 == 0) {
        LogInfo.log("Number of lines: " + i);
      }
    }
    writer.close();
  }

}
