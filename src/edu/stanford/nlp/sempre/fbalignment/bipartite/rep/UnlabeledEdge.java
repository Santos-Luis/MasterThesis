package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;

public class UnlabeledEdge implements BipartiteEdge {


  private static final long serialVersionUID = -2882056570038865589L;

  @Override
  public void addMatch(Pair<String, String> match) {
  }

  @Override
  public int value() {
    return -1;
  }

  @Override
  public double score() {
    return -1;
  }
}
