package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;

import java.io.Serializable;

public interface BipartiteEdge extends Serializable {
  void addMatch(Pair<String, String> match);
  int value();
  double score();
}
