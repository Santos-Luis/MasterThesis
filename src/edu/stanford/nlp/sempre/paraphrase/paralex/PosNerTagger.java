package edu.stanford.nlp.sempre.paraphrase.paralex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Joiner;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import fig.basic.LogInfo;

/**
 * Takes the questions file and spits out a file with pos tags and ner tags
 * @author jonathanberant
 *
 */
public final class PosNerTagger {
  private PosNerTagger() { }

  public static void main(String[] args) throws IOException {
    Properties props = new Properties();
    props.put("annotators", "tokenize,ssplit,pos,lemma,ner");
    props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-caseless-left3words-distsim.tagger");
    props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.caseless.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.conll.4class.caseless.distsim.crf.ser.gz");

    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    PrintWriter writer = IOUtils.getPrintWriter(args[1]);
    int i = 0;
    for (String line : IOUtils.readLines(args[0])) {
      String[] parts = line.split("\t");
      if (parts.length != 4) continue;
      String utterance = parts[0];
      Annotation doc = new Annotation(utterance);
      pipeline.annotate(doc);

      List<String> tokens = new ArrayList<String>();
      List<String> posTags = new ArrayList<String>();
      List<String> lemmas = new ArrayList<String>();
      List<String> nerTags = new ArrayList<String>();
      for (CoreLabel label : doc.get(TokensAnnotation.class)) {
        tokens.add(label.get(TextAnnotation.class));
        posTags.add(label.get(PartOfSpeechAnnotation.class));
        lemmas.add(label.get(LemmaAnnotation.class));
        nerTags.add(label.get(NamedEntityTagAnnotation.class));
      }
      String tokenDesc = Joiner.on(' ').join(tokens);
      String posDesc = Joiner.on(' ').join(posTags);
      String lemmaDesc = Joiner.on(' ').join(lemmas);
      String nerDesc = Joiner.on(' ').join(nerTags);
      writer.println(Joiner.on('\t').join(parts[0], tokenDesc, posDesc, lemmaDesc, nerDesc));

      if (++i % 100000 == 0)
        LogInfo.logs("Number of lines=%s", i);
    }
    writer.close();
  }


}
