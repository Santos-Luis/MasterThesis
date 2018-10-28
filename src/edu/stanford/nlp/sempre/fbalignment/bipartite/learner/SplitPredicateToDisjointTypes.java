package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Sets;
import fig.basic.LogInfo;

import java.io.IOException;
import java.util.*;

public class SplitPredicateToDisjointTypes extends NlIterLearner {

  public SplitPredicateToDisjointTypes(FourPartiteGraph fourPartiteGraph) throws IOException, ClassNotFoundException {
    super(fourPartiteGraph);
  }

  @Override
  protected boolean handleNode(NlTypedBipartiteNode fromNlTypedNode,
                               Queue<NlTypedBipartiteNode> nodeQueue) {


    Pair<NlTypedBipartiteNode, CandidateScore> bestTypedNodeAndCandidate = getBestTypedNode(fromNlTypedNode);

    if (bestTypedNodeAndCandidate != null && bestTypedNodeAndCandidate.second.getScore() > 0) {

      LogInfo.log("Splitting node: " + fromNlTypedNode + " with node " + bestTypedNodeAndCandidate.first);
      NlTypedBipartiteNode candidate2 = splitTypedNodeWithTypedNodeCandidate(fromNlTypedNode, bestTypedNodeAndCandidate);
      if (candidate2.getMidIdPairsCount() > 0)
        handleNode(candidate2, nodeQueue);

    }
    return false;
  }

  private NlTypedBipartiteNode splitTypedNodeWithTypedNodeCandidate(NlTypedBipartiteNode currentNode, Pair<NlTypedBipartiteNode, CandidateScore> bestTypedNodeAndCandidate) {

    // create the two new nodes
    Set<Pair<Integer, Integer>> candidate2IdPairs = new TreeSet<Pair<Integer, Integer>>(currentNode.getMidIdPairSet());
    candidate2IdPairs.removeAll(bestTypedNodeAndCandidate.first().getMidIdPairSet());
    BipartiteNode candidateNode2 = BipartiteNodeFactory.createTypedBipartiteNodeCandidate(currentNode, candidate2IdPairs);
    // populate the types of the node candidates
    populateTypeMap(currentNode, candidateNode2);

    // find the edges for the two new nodes
    if (currentNode.getType() == BipartiteNodeType.NL_TYPED) {
      splitNlTypedNodeWithCandidates(currentNode, bestTypedNodeAndCandidate, candidateNode2);
    } else {
      throw new RuntimeException("Method is not supported for nodes of type: " + currentNode.getType());
    }

    return (NlTypedBipartiteNode) candidateNode2;

  }


  private void splitNlTypedNodeWithCandidates(NlTypedBipartiteNode currentNode,
                                              Pair<NlTypedBipartiteNode, CandidateScore> bestTypedNodeAndCandidate,
                                              BipartiteNode candidateNode2) {

    // get the Nl node
    Collection<BipartiteNode> nlNodes = graph.getGraph().getPredecessors(currentNode);
    if (nlNodes.size() != 1)
      throw new IllegalStateException("Nl typed nodes must have exactly one predecessor. The node: " + currentNode + " has " + nlNodes.size() + " predecessors.");
    BipartiteNode nlNode = nlNodes.iterator().next();

    // get the successors and assign each successor to candidate1 or to candidate2
    Collection<BipartiteNode> successors = graph.getGraph().getSuccessors(currentNode);
    Map<BipartiteNode, Integer> node2Successors = new HashMap<BipartiteNode, Integer>();
    for (BipartiteNode successor : successors) {
      int intersection2 = Sets.intersection(successor.getMidIdPairSet(), candidateNode2.getMidIdPairSet()).size();

      if (intersection2 > 0) {
        node2Successors.put(successor, intersection2);
      }
    }

    // adding nodes to graph and queue
    graph.removeNode(currentNode);

    // add the typed node
    graph.addNode(bestTypedNodeAndCandidate.first());
    graph.addEdge(new UnlabeledEdge(), nlNode, bestTypedNodeAndCandidate.first());
    int count = Sets.intersection(bestTypedNodeAndCandidate.first().getMidIdPairSet(), bestTypedNodeAndCandidate.second().getFbNode().getMidIdPairSet()).size();
    double score = bestTypedNodeAndCandidate.second().getScore();
    graph.addEdge(new ScoreEdge(count, score), bestTypedNodeAndCandidate.first(), bestTypedNodeAndCandidate.second().getFbNode());

    addSplitNodeToGraph(nlNode, candidateNode2, node2Successors);

  }

  private void addSplitNodeToGraph(BipartiteNode nlNode,
                                   BipartiteNode candidateNode, Map<BipartiteNode, Integer> nodeSuccessors) {

    graph.addNode(candidateNode);
    graph.addEdge(new CountEdge(), nlNode, candidateNode);

    for (BipartiteNode nodeSuccessor : nodeSuccessors.keySet()) {

      int count = nodeSuccessors.get(nodeSuccessor);
      double score = nodePairScorer.scoreNodePair(graph, candidateNode, nodeSuccessor);
      LogInfo.logs("Verbose", "Adding edge from: " + candidateNode + " to " + nodeSuccessor + " count: " + count + " score: " + score);
      graph.addEdge(new ScoreEdge(count, score), candidateNode, nodeSuccessor);
    }
  }

  private Pair<NlTypedBipartiteNode, CandidateScore> getBestTypedNode(
      NlTypedBipartiteNode fromNlTypedNode) {

    Pair<NlTypedBipartiteNode, CandidateScore> res = null;

    // Find all of the pairs of types
    Set<Pair<Integer, Integer>> expectedTypesSet = new HashSet<Pair<Integer, Integer>>();
    for (BipartiteNode successor : graph.getGraph().getSuccessors(fromNlTypedNode)) {
      FbTypedBipartiteNode fbTypedSuccessor = (FbTypedBipartiteNode) successor;
      expectedTypesSet.add(new Pair<Integer, Integer>(fbTypedSuccessor.getArg1Type(), fbTypedSuccessor.getArg2Type()));
    }

    // for each pair of type create a node and the necessary edges
    for (Pair<Integer, Integer> expectedTypes : expectedTypesSet) {
      Pair<NlTypedBipartiteNode, CandidateScore> currCandidate = findScoreForPairOfTypes(fromNlTypedNode, expectedTypes);
      if (currCandidate != null) {

        if (res == null || currCandidate.second().getScore() > res.second().getScore()) {
          res = currCandidate;
        }
      }
    }

    return res;
  }

  private Pair<NlTypedBipartiteNode, CandidateScore> findScoreForPairOfTypes(NlTypedBipartiteNode fromNlTypedNode,
                                                                             Pair<Integer, Integer> expectedTypes) {

    Pair<NlTypedBipartiteNode, NlIterLearner.CandidateScore> res = null;
    // create the set of MIDs
    Set<Pair<Integer, Integer>> splitNodeMidIdPairs = Sets.intersection(
        fromNlTypedNode.getArg1IdPairs(expectedTypes.first),
        fromNlTypedNode.getArg2IdPairs(expectedTypes.second));

    if (splitNodeMidIdPairs.size() == 0) {
      throw new IllegalStateException("The node " + fromNlTypedNode.toShortString() + " has no intersection with the types " + expectedTypes);
    }
    // create the type map
    BipartiteNode splitNode = BipartiteNodeFactory.createTypedBipartiteNodeCandidate(
        expectedTypes.first, expectedTypes.second, BipartiteNodeType.NL_TYPED,
        fromNlTypedNode.getDescription(), splitNodeMidIdPairs);
    populateTypeMap(fromNlTypedNode, splitNode);
    CandidateScore candidateScore = graph.findCandidateScoreForSplitNode(fromNlTypedNode, (NlTypedBipartiteNode) splitNode, nodePairScorer);

    if (candidateScore != null)
      res = new Pair<NlTypedBipartiteNode, NlIterLearner.CandidateScore>((NlTypedBipartiteNode) splitNode, candidateScore);
    return res;
  }

  @Override
  protected void finalizeGraph() {

    List<NlTypedBipartiteNode> toDelete = new LinkedList<NlTypedBipartiteNode>();

    for (NlTypedBipartiteNode nlTypedNode : graph.getNlTypedNodes()) {
      if (!nlTypedNode.isFullyTyped())
        toDelete.add(nlTypedNode);
    }

    for (NlTypedBipartiteNode nlTypedNode : toDelete) {
      graph.removeNode(nlTypedNode);
    }
  }

}
