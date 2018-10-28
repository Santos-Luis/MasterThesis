package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.sempre.fbalignment.bipartite.scorers.NodePairScorer;
import edu.stanford.nlp.sempre.fbalignment.bipartite.scorers.ScorerFactory;
import edu.stanford.nlp.util.Pair;
import fig.basic.Option;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public abstract class NlIterLearner implements AlignmentLearner {

  public static class Options {
    @Option(gloss = "Cost of alignment edge") public double edgeCost;
  }

  public static Options opts = new Options();

  protected FourPartiteGraph graph;
  protected double edgeCost;
  protected NodePairScorer nodePairScorer;

  public NlIterLearner(FourPartiteGraph fourPartiteGraph) throws IOException, ClassNotFoundException {
    this.graph = fourPartiteGraph;
    edgeCost = opts.edgeCost;
    nodePairScorer = ScorerFactory.createScorer();
  }
  @Override
  /**
   * Go over each of the natural language nodes and handle that node
   */
  public void learn() {

    Queue<NlTypedBipartiteNode> nodeQueue = new LinkedList<NlTypedBipartiteNode>();
    nodeQueue.addAll(graph.getNlTypedNodes());
    while (!nodeQueue.isEmpty()) {


      NlTypedBipartiteNode currNode = nodeQueue.poll();
      /*
      if (currNode.getDescription().equals("Add to"))
        System.out.println();
      else continue;
      */
      handleNode(currNode, nodeQueue);
    }
    finalizeGraph();

  }

  protected CandidateScore getBestCandidate(NlTypedBipartiteNode currNode, Collection<BipartiteNode> candidates) {

    double bestScore = 0;
    FbTypedBipartiteNode bestFbNode = new FbTypedBipartiteNode(new FbBipartiteNode());

    for (BipartiteNode candidate : candidates) {

      FbTypedBipartiteNode toNode = (FbTypedBipartiteNode) candidate;
      double currScore = scoreNodePair(currNode, toNode);
      if (currScore > bestScore) {
        bestScore = currScore;
        bestFbNode = toNode;
      }
    }
    return new CandidateScore(bestFbNode, bestScore);
  }

  protected double scoreNodePair(BipartiteNode node1, BipartiteNode node2) {
    return nodePairScorer.scoreNodePair(graph, node1, node2) - edgeCost;
  }

  // ABSTRACT
  protected abstract boolean handleNode(NlTypedBipartiteNode fromNlTypedNode, Queue<NlTypedBipartiteNode> nodeQueue);
  protected abstract void finalizeGraph();

  protected void populateTypeMap(BipartiteNode currentNode, BipartiteNode splitNode) {

    // add to arg1TypeMap
    for (int typeId : currentNode.getArg1TypeMap().keySet()) {
      for (Pair<Integer, Integer> idPair : currentNode.getArg1IdPairs(typeId)) {
        if (splitNode.getMidIdPairSet().contains(idPair)) {
          splitNode.addIdPairToArg1TypeMap(typeId, idPair);
        }
      }
    }
    // add to arg2TypeMap
    for (int typeId : currentNode.getArg2TypeMap().keySet()) {
      for (Pair<Integer, Integer> idPair : currentNode.getArg2IdPairs(typeId)) {

        if (splitNode.getMidIdPairSet().contains(idPair)) {
          splitNode.addIdPairToArg2TypeMap(typeId, idPair);
        }
      }
    }
  }


  public static class CandidateScore {

    private FbTypedBipartiteNode fbNode;
    private double score;

    public CandidateScore(FbTypedBipartiteNode fbNode, double score) {
      this.fbNode = fbNode;
      this.score = score;
    }

    public FbTypedBipartiteNode getFbNode() {
      return fbNode;
    }
    public double getScore() {
      return score;
    }
  }

}
