package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import fig.basic.LogInfo;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Go over all natural language predicates and find the FB relation that has
 * maximal score with it
 */

public class ChooseBestRelation extends NlIterLearner {

  public ChooseBestRelation(FourPartiteGraph fourPartiteGraph) throws IOException, ClassNotFoundException {
    super(fourPartiteGraph);
  }

  protected boolean handleNode(NlTypedBipartiteNode fromNlTypedNode, Queue<NlTypedBipartiteNode> queue) {

    double bestScore = 0;
    BipartiteEdge bestEdge = null;
    BipartiteNode bestFbNode = null;

    // delete edges and determine best node
    List<BipartiteEdge> toDeleteEdges = new LinkedList<BipartiteEdge>();

    if (graph.getGraph().outDegree(fromNlTypedNode) == 0) {
      LogInfo.log("NL typed node with out degree zero: " + fromNlTypedNode);
    }

    for (BipartiteEdge currOutEdge : graph.getGraph().getOutEdges(fromNlTypedNode)) {

      BipartiteNode toNode = graph.getGraph().getDest(currOutEdge);
      double currScore = nodePairScorer.scoreNodePair(graph, fromNlTypedNode, toNode) - edgeCost;
      LogInfo.logs("DBG", "SCORE:\t" + fromNlTypedNode.getDescription() + "\t" + toNode.getDescription() + "\t" + currScore);
      if (currScore > bestScore) {
        bestScore = currScore;
        bestEdge = currOutEdge;
        bestFbNode = toNode;
      }
      toDeleteEdges.add(currOutEdge);
    }

    for (BipartiteEdge edge : toDeleteEdges) {

      boolean delete = graph.getGraph().removeEdge(edge);
      if (!delete)
        throw new IllegalArgumentException("Edges to be deleted must be in the graph");
    }

    // add the best edge
    if (bestScore > 0) {
      LogInfo.log("Adding edge:\t" + fromNlTypedNode.getDescription() + "\t" + bestFbNode.getDescription() + "\t" + bestScore);
      graph.getGraph().addEdge(new ScoreEdge(bestEdge.value(), bestScore), fromNlTypedNode, bestFbNode);
    }
    // this never changes the nodes so always "false"
    return false;
  }

  @Override
  /**
   * This learner does not require any finalization
   */
  protected void finalizeGraph() {
  }
}
