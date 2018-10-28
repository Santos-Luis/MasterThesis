package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteEdge;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.NlTypedBipartiteNode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ChooseBestTypeCompatibleRelation extends FbBasedNlTyping {

  private static final int COUNT_THRESHOLD = 3;
  // private static final double SCORE_THRESHOLD = 0.001;
  private static final double SCORE_THRESHOLD = 0;

  public ChooseBestTypeCompatibleRelation(FourPartiteGraph fourPartiteGraph) throws IOException, ClassNotFoundException {
    super(fourPartiteGraph);
  }

  @Override
  protected void finalizeGraph() {

    for (NlTypedBipartiteNode nlTypedNode : graph.getNlTypedNodes()) {

      BipartiteEdge bestEdge = null;

      // find best edge

      for (BipartiteEdge outEdge : graph.getGraph().getOutEdges(nlTypedNode)) {

        if (outEdge.value() >= COUNT_THRESHOLD && outEdge.score() >= SCORE_THRESHOLD) {
          if (bestEdge == null || outEdge.score() > bestEdge.score()) {
            bestEdge = outEdge;
          }
        }
      }
      // collect edges to delete from immutable set
      List<BipartiteEdge> toDeleteEdges = new LinkedList<BipartiteEdge>();
      for (BipartiteEdge outEdge : graph.getGraph().getOutEdges(nlTypedNode)) {
        if (outEdge != bestEdge) {
          toDeleteEdges.add(outEdge);
        }
      }
      // delete edges
      for (BipartiteEdge toDeleteEdge : toDeleteEdges)
        graph.getGraph().removeEdge(toDeleteEdge);
    }
  }
}
