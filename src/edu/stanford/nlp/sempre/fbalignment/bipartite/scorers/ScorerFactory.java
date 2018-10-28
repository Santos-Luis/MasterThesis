package edu.stanford.nlp.sempre.fbalignment.bipartite.scorers;

import fig.basic.Option;

import java.io.IOException;

public final class ScorerFactory {
  private ScorerFactory() { }

  public static class Options {
    @Option(gloss = "Name of scorer type to use") public String scorerName;
    @Option(gloss = "Path to classifier file") public String classifierFilePath;
  }

  public static Options opts = new Options();

  public static NodePairScorer createScorer() throws IOException, ClassNotFoundException {

    String desc = opts.scorerName;

    if (opts.scorerName.equals("smoothed-jaccard"))
      return new SmoothedJaccardScorer();
    else if (opts.scorerName.equals("count"))
      return new CountScorer();
    else if (opts.scorerName.equals("jaccard-and-count"))
      return new SmoothedJaccardAndCountThresholdScorer();
    else if (opts.scorerName.equals("classifier"))
      return new MaxentScorer(opts.classifierFilePath);
    else
      throw new IllegalArgumentException("Scorer description is illegal: " + desc);
  }

}
