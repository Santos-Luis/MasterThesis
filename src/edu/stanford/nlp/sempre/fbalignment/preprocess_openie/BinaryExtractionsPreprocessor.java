package edu.stanford.nlp.sempre.fbalignment.preprocess_openie;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sempre.freebase.utils.FormatConverter;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * This preprocessor normalizes the predicate and handles time arguments in the
 * second argument
 *
 * @author jonathanberant
 */
public class BinaryExtractionsPreprocessor extends ExtractionsPreprocessor {

  private StanfordCoreNLP timePipeline;
  private StanfordCoreNLP tokenizer;


  public BinaryExtractionsPreprocessor() {
    super();
    props = new Properties();
    props.put("annotators", "tokenize, ssplit");
    tokenizer = new StanfordCoreNLP(props);
    timePipeline = new StanfordCoreNLP(props);
    timePipeline.addAnnotator(new TimeAnnotator("sutime", props));
  }

  @Override
  public String preprocessLine(String line) {

    String[] tokens = line.split("\t");

    Annotation arg1Annotation = new Annotation(tokens[0]);
    tokenizer.annotate(arg1Annotation);
    int numOfArg1Tokens = arg1Annotation.get(CoreAnnotations.TokensAnnotation.class).size();

    Annotation predAnnotation = new Annotation(tokens[1]);
    tokenizer.annotate(predAnnotation);
    int numOfPredTokens = predAnnotation.get(CoreAnnotations.TokensAnnotation.class).size();

    String sentence = tokens[0] + " " + tokens[1] + " " + tokens[2];
    Annotation sentenceAnnotation = new Annotation(sentence);
    pipeline.annotate(sentenceAnnotation);
    String normalizedPredicate = normalizePredicate(sentenceAnnotation, numOfArg1Tokens, numOfArg1Tokens + numOfPredTokens);
    String normalizedArg2 = normalizeArg2(tokens[2]);
    String id = "";
    if (tokens.length > 3) {
      id = FormatConverter.fromNoPrefixMidToDot(tokens[3]);
      if (midToIdMap.containsKey(id))
        id = midToIdMap.get(id);
    }
    return tokens[0] + "\t" + normalizedPredicate + "\t" + normalizedArg2 + "\t" + id;
  }

  private String normalizeArg2(String arg2) {
    Annotation timeCandidate = new Annotation(arg2);
    timePipeline.annotate(timeCandidate);
    List<CoreMap> timexAnns = timeCandidate.get(TimeAnnotations.TimexAnnotations.class);
    if (timexAnns.size() == 1) {
      String value = timexAnns.get(0).get(TimeAnnotations.TimexAnnotation.class).value();
      if (value != null)
        return "TIME:" + value;
    }
    return arg2;
  }

  private String normalizePredicate(Annotation sentenceAnnotation,
                                    int startIndex, int endIndex) {

    StringBuilder sb = new StringBuilder();

    boolean firstLemma = true;
    for (int i = startIndex; i < endIndex; ++i) {
      CoreLabel token = sentenceAnnotation.get(CoreAnnotations.TokensAnnotation.class).get(i);
      String pos = token.get(PartOfSpeechAnnotation.class);
      String lemma = token.getString(LemmaAnnotation.class);
      if (pos.equals("RB") || pos.equals("MD") || pos.equals("DT"))
        continue;

      if (firstLemma) {
        if (!lemma.equals("be")) {
          sb.append(lemma + " ");
          firstLemma = false;
        }
      } else
        sb.append(lemma + " ");
    }
    return sb.toString().toLowerCase().trim();
  }
}
