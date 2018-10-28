package edu.stanford.nlp.sempre.fbalignment.preprocess_openie;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sempre.freebase.utils.FormatConverter;

public class UnaryExtractionsPreprocessor extends ExtractionsPreprocessor {

  public static final TokenSequencePattern p = TokenSequencePattern.compile(
          "[ is|was ] [ { tag:\"RB\" } ]* [ { tag:\"DT\" } ] [ { tag:\"RB\" } | { tag:\"RBS\" } | { tag:\"RBR\" } ]* " +
              "[ { tag:\"JJ\" }|{ tag:\"JJS\" }|{ tag:\"JJR\" } ]* " +
              "( [ { tag:\"NN\" } | { tag:\"NNS\" } ]+ ) [ { tag:\"IN\" } | { tag:\"TO\" } ] ");

  @Override
  public String preprocessLine(String line) {

    String[] tokens = line.split("\t");
    String sentence = tokens[0] + " " + tokens[1] + " " + tokens[2];
    Annotation annotation = new Annotation(sentence);
    pipeline.annotate(annotation);

    TokenSequenceMatcher m = p.getMatcher(annotation.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    String id = midToIdMap.get(FormatConverter.fromNoPrefixMidToDot(tokens[3]));
    if (match && id != null) {
      return tokens[0] + "\tis-a\t" + m.group(1).toLowerCase() + "\t" + id;
    }

    return null;
  }

  public static void main(String[] args) {
    UnaryExtractionsPreprocessor preprocessor = new UnaryExtractionsPreprocessor();
    preprocessor.preprocessLine("");
  }

}
