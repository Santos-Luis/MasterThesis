package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode.TypeIdPairs;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Sets;
import fig.basic.LogInfo;

import java.io.IOException;
import java.util.*;

/**
 * Go over NL predicates and find the best way to type until score can not be
 * improved by typing anymore
 *
 * @author jonathanberant
 */
public class ExhaustiveNlTyping extends NlIterLearner {

  private static final int MIN_ID_PAIRS = 3;

  public ExhaustiveNlTyping(FourPartiteGraph fourPartiteGraph) throws IOException, ClassNotFoundException {
    super(fourPartiteGraph);
  }

  @Override
  protected boolean handleNode(NlTypedBipartiteNode fromNlTypedNode,
                               Queue<NlTypedBipartiteNode> nodeQueue) {

    LogInfo.log("Handling node: " + fromNlTypedNode);

    boolean changed = false;
    // get best score without splitting
    CandidateScore bestNotTypedCandidate = getBestCandidate(fromNlTypedNode, graph.getGraph().getSuccessors(fromNlTypedNode));
    LogInfo.log(
        "Best score without further typing, source: " + fromNlTypedNode.getDescription() + ", destination: " + bestNotTypedCandidate.getFbNode().getDescription() + ", score: " +
            bestNotTypedCandidate.getScore());

    // get best typed
    Pair<NlTypedBipartiteNode, CandidateScore> bestTypedNodeAndCandidate = getBestTypedNode(fromNlTypedNode);

    LogInfo.log(
        "Best score with typing, source: " + fromNlTypedNode.getDescription() + ", destination: " +
            bestTypedNodeAndCandidate.second.getFbNode().getDescription() + ", score: " + bestTypedNodeAndCandidate.second.getScore());

    // if typing is better than we split the node and re-insert into the queue
    if (bestTypedNodeAndCandidate.second.getScore() > bestNotTypedCandidate.getScore() + 10e-10) {
      LogInfo.log("Splitting node: " + fromNlTypedNode + " with node " + bestTypedNodeAndCandidate.first);
      splitTypedNodeWithTypedNodeCandidate(fromNlTypedNode, bestTypedNodeAndCandidate.first, nodeQueue);
      changed = true;
    }
    return changed;
  }

  private Pair<NlTypedBipartiteNode, CandidateScore> getBestTypedNode(NlTypedBipartiteNode currNode) {

    Pair<NlTypedBipartiteNode, CandidateScore> res = null;

    if (!currNode.isArg1Typed()) {
      res = getBestTypedNodeForArg(currNode, currNode.getArg1TypeMap(), true);
    }
    if (!currNode.isArg2Typed()) { // check whether to type arg2

      Pair<NlTypedBipartiteNode, CandidateScore> arg2Res = getBestTypedNodeForArg(currNode, currNode.getArg2TypeMap(), false);
      if (res == null || arg2Res.second.getScore() > res.second.getScore() + 10e-10) {
        res = arg2Res;
      }
    }
    return res;
  }

  private Pair<NlTypedBipartiteNode, CandidateScore> getBestTypedNodeForArg(NlTypedBipartiteNode currNode, Map<Integer, TypeIdPairs> typeToIdPairsMap, boolean isArg1) {


    Pair<NlTypedBipartiteNode, NlIterLearner.CandidateScore> res =
        new Pair<NlTypedBipartiteNode, NlIterLearner.CandidateScore>(
            null,
            new CandidateScore(new FbTypedBipartiteNode(new FbBipartiteNode()), 0.0));

    for (int typeId : typeToIdPairsMap.keySet()) {

      Set<Pair<Integer, Integer>> idPairs = typeToIdPairsMap.get(typeId).getIdPairs();
      if (idPairs.size() >= MIN_ID_PAIRS) {

        NlTypedBipartiteNode currTypedNode = currNode.createTypedCandidate(typeId, isArg1);
        CandidateScore currCandidate = getBestCandidate(currTypedNode, graph.getGraph().getSuccessors(currNode));
        if (currCandidate.getScore() > res.second.getScore() + 10e-10) {
          res.second = currCandidate;
          res.first = currTypedNode;
        }
      }
    }
    return res;
  }

  public void splitTypedNodeWithTypedNodeCandidate(BipartiteNode currentNode, BipartiteNode candidateNode1, Queue<NlTypedBipartiteNode> nodeQueue) {

    // create the two new nodes
    Set<Pair<Integer, Integer>> candidate2IdPairs = new TreeSet<Pair<Integer, Integer>>(currentNode.getMidIdPairSet());
    candidate2IdPairs.removeAll(candidateNode1.getMidIdPairSet());
    BipartiteNode candidateNode2 = BipartiteNodeFactory.createTypedBipartiteNodeCandidate(currentNode, candidate2IdPairs);
    // populate the types of the node candidates
    populateTypeMaps(currentNode, candidateNode1, candidateNode2);

    // find the edges for the two new nodes
    if (currentNode.getType() == BipartiteNodeType.NL_TYPED) {
      splitNlTypedNodeWithCandidates(currentNode, candidateNode1, candidateNode2, nodeQueue);
    } else {
      throw new RuntimeException("Method is not supported for nodes of type: " + currentNode.getType());
    }
  }

  private void splitNlTypedNodeWithCandidates(BipartiteNode currentNode, BipartiteNode candidateNode1, BipartiteNode candidateNode2, Queue<NlTypedBipartiteNode> nodeQueue) {

    // get the Nl node
    Collection<BipartiteNode> nlNodes = graph.getGraph().getPredecessors(currentNode);
    if (nlNodes.size() != 1)
      throw new IllegalStateException("Nl typed nodes must have exactly one predecessor. The node: " + currentNode + " has " + nlNodes.size() + " predecessors.");
    BipartiteNode nlNode = nlNodes.iterator().next();

    // get the successors and assign each successor to candidate1 or to candidate2
    Collection<BipartiteNode> successors = graph.getGraph().getSuccessors(currentNode);
    Map<BipartiteNode, Integer> node1Successors = new HashMap<BipartiteNode, Integer>();
    Map<BipartiteNode, Integer> node2Successors = new HashMap<BipartiteNode, Integer>();
    for (BipartiteNode successor : successors) {
      int intersection1 = Sets.intersection(successor.getMidIdPairSet(), candidateNode1.getMidIdPairSet()).size();
      int intersection2 = Sets.intersection(successor.getMidIdPairSet(), candidateNode2.getMidIdPairSet()).size();

      if (intersection1 > 0) {
        node1Successors.put(successor, intersection1);
      }
      if (intersection2 > 0) {
        node2Successors.put(successor, intersection2);
      }
    }

    // adding nodes to graph and queue
    graph.removeNode(currentNode);
    addSplitNodeToGraph(nlNode, candidateNode1, node1Successors, nodeQueue);
    addSplitNodeToGraph(nlNode, candidateNode2, node2Successors, nodeQueue);
  }

  private void addSplitNodeToGraph(BipartiteNode nlNode,
                                   BipartiteNode candidateNode, Map<BipartiteNode, Integer> nodeSuccessors, Queue<NlTypedBipartiteNode> nodeQueue) {

    if (nodeSuccessors.size() > 0) {
      graph.addNode(candidateNode);
      LogInfo.logs("Verbose", "Adding edge from: " + nlNode + " to " + candidateNode);
      graph.addEdge(new CountEdge(), nlNode, candidateNode);

      for (BipartiteNode nodeSuccessor : nodeSuccessors.keySet()) {

        int count = nodeSuccessors.get(nodeSuccessor);
        double score = nodePairScorer.scoreNodePair(graph, candidateNode, nodeSuccessor);
        LogInfo.logs("Verbose", "Adding edge from: " + candidateNode + " to " + nodeSuccessor + " count: " + count + " score: " + score);
        graph.addEdge(new ScoreEdge(count, score), candidateNode, nodeSuccessor);
      }
      NlTypedBipartiteNode nlTypedCandidate = (NlTypedBipartiteNode) candidateNode;
      if (!nlTypedCandidate.isFullyTyped()) {
        nodeQueue.offer(nlTypedCandidate);
      }

    }
  }

  private void populateTypeMaps(BipartiteNode currentNode, BipartiteNode candidateNode1, BipartiteNode candidateNode2) {

    // add to arg1TypeMap
    for (int typeId : currentNode.getArg1TypeMap().keySet()) {
      for (Pair<Integer, Integer> idPair : currentNode.getArg1IdPairs(typeId)) {

        if (candidateNode1.getMidIdPairSet().contains(idPair)) {
          candidateNode1.addIdPairToArg1TypeMap(typeId, idPair);
        }
        if (candidateNode2.getMidIdPairSet().contains(idPair)) {
          candidateNode2.addIdPairToArg1TypeMap(typeId, idPair);
        }
      }
    }
    // add to arg2TypeMap
    for (int typeId : currentNode.getArg2TypeMap().keySet()) {
      for (Pair<Integer, Integer> idPair : currentNode.getArg2IdPairs(typeId)) {

        if (candidateNode1.getMidIdPairSet().contains(idPair)) {
          candidateNode1.addIdPairToArg2TypeMap(typeId, idPair);
        }
        if (candidateNode2.getMidIdPairSet().contains(idPair)) {
          candidateNode2.addIdPairToArg2TypeMap(typeId, idPair);
        }
      }
    }
  }

  @Override
  protected void finalizeGraph() {

    for (NlTypedBipartiteNode nlTypedNode : graph.getNlTypedNodes()) {

      CandidateScore bestCandidate = getBestCandidate(nlTypedNode, graph.getGraph().getSuccessors(nlTypedNode));
      BipartiteEdge bestEdge = graph.getGraph().findEdge(nlTypedNode, bestCandidate.getFbNode());

      List<BipartiteEdge> toDeleteEdges = new LinkedList<BipartiteEdge>();
      toDeleteEdges.addAll(graph.getGraph().getOutEdges(nlTypedNode));
      for (BipartiteEdge toDeleteEdge : toDeleteEdges) {
        graph.getGraph().removeEdge(toDeleteEdge);
      }
      if (bestCandidate.getScore() > 0)
        graph.addEdge(new ScoreEdge(bestEdge.value(), bestCandidate.getScore()), nlTypedNode, bestCandidate.getFbNode());
    }

  }


}
