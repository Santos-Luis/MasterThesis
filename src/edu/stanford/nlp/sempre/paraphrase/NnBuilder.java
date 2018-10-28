package edu.stanford.nlp.sempre.paraphrase;

import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.freebase.*;
import fig.basic.Option;
import fig.basic.Utils;

import java.util.List;

/**
 * Basic components for transformation learning
 * @author jonathanberant
 */
public class NnBuilder {
  public static class Options {
    @Option public String packageName = "edu.stanford.nlp.sempre";
    @Option public String languageAnalyzer = "SimpleAnalyzer";
    @Option public String executor = "SparqlExecutor";
    @Option public List<String> trainGrammarPath;
    @Option public List<String> testGrammarPath;
  }

  public static Options opts = new Options();

  public LanguageAnalyzer languageAnalyzer;
  public Executor executor;
  public FeatureExtractor extractor;
  public Parser trainParser;
  public Parser testParser;

  public void build() {
    executor = null;
    extractor = null;
    trainParser = null;
    buildUnspecified();
  }

  public void buildLanguageAnalyzer() {
    if (languageAnalyzer == null)
      languageAnalyzer = (LanguageAnalyzer) Utils.newInstanceHard(opts.packageName + "." + opts.languageAnalyzer);
  }

  public void buildUnspecified() {

    // Train grammar
    Grammar.opts.inPaths = opts.trainGrammarPath;
    Grammar trainGrammar = new Grammar();
    trainGrammar.read();

    // Test grammar
    Grammar.opts.inPaths = opts.testGrammarPath;
    Grammar testGrammar = new Grammar();
    testGrammar.read();

    // LanguageAnalyzer
    buildLanguageAnalyzer();

    // Executor
    if (executor == null) {
      executor = (Executor) Utils.newInstanceHard(opts.packageName + "." + opts.executor);
    }

    // Feature extractors
    if (extractor == null)
      extractor = new FeatureExtractor(executor);

    Parser.Spec spec = new Parser.Spec(trainGrammar, extractor, executor, new FreebaseValueEvaluator());

    // Train Parser
    if (trainParser == null)
      trainParser = new BeamParser(spec);

    // Test parser
    if (testParser == null)
      testParser = new BeamParser(spec);
  }
}
