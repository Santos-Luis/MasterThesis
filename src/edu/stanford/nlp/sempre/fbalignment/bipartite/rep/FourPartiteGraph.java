package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import com.google.common.collect.BiMap;
import edu.stanford.nlp.sempre.fbalignment.bipartite.filters.BipartiteNodeFilter;
import edu.stanford.nlp.sempre.fbalignment.bipartite.learner.NlIterLearner.CandidateScore;
import edu.stanford.nlp.sempre.fbalignment.bipartite.scorers.NodePairScorer;
import edu.stanford.nlp.sempre.fbalignment.fbgraph.FbPropertiesExpectedTypes;
import edu.stanford.nlp.util.Sets;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import fig.basic.LogInfo;

import java.io.Serializable;
import java.util.*;

/**
 * 4-partite graph with the following partitions: (a) natural language
 * predicates (b) typed natural language predicates (c) Freebase relations (d)
 * Typed Freebase relations
 *
 * @author jonathanberant
 */
public class FourPartiteGraph implements Serializable {

  private static final long serialVersionUID = 3279404659463634604L;
  private DirectedSparseGraph<BipartiteNode, BipartiteEdge> graph;
  Set<NlBipartiteNode> nlNodes;
  Set<FbBipartiteNode> fbNodes;
  Set<NlTypedBipartiteNode> nlTypedNodes;
  Set<FbTypedBipartiteNode> fbTypedNodes;

  /**
   * Inits the 4-partite graph by copying each natural language node and
   * connecting it to its typed copy, and copying each Freebase node and
   * connecting it to its typed copy.
   */
  public FourPartiteGraph(DirectedSparseGraph<BipartiteNode, BipartiteEdge> bigraph,
                          BiMap<String, Integer> fbType2Id,
                          FbPropertiesExpectedTypes fbPropertiesExpectedTypes) {

    graph = new DirectedSparseGraph<BipartiteNode, BipartiteEdge>();
    nlNodes = new HashSet<NlBipartiteNode>();
    nlTypedNodes = new HashSet<NlTypedBipartiteNode>();
    fbNodes = new HashSet<FbBipartiteNode>();
    fbTypedNodes = new HashSet<FbTypedBipartiteNode>();

    createNodes(bigraph, fbType2Id, fbPropertiesExpectedTypes);
    createEdges(bigraph);
    logNodeStats();
  }

  private void createNodes(DirectedSparseGraph<BipartiteNode, BipartiteEdge> bigraph,
                           BiMap<String, Integer> fbType2Id, FbPropertiesExpectedTypes fbPropertiesExpectedTypes) {

    for (BipartiteNode node : bigraph.getVertices()) {
      if (node instanceof NlBipartiteNode) {

        NlBipartiteNode nlNode = (NlBipartiteNode) node;
        NlTypedBipartiteNode nlTypedNode = new NlTypedBipartiteNode(nlNode);

        nlNodes.add(nlNode);
        nlTypedNodes.add(nlTypedNode);


        if (!graph.addVertex(nlNode))
          throw new IllegalStateException("While adding node " + nlNode + " of type " + nlNode.getClass() + ": Node already in graph");
        if (!graph.addVertex(nlTypedNode))
          throw new IllegalStateException("While adding node " + nlTypedNode + " of type " + nlTypedNode.getClass() + ": Node already in graph");


        graph.addEdge(new UnlabeledEdge(), nlNode, nlTypedNode);
      } else if (node instanceof FbBipartiteNode) {

        FbBipartiteNode fbNode = (FbBipartiteNode) node;
        FbTypedBipartiteNode fbTypedNode = new FbTypedBipartiteNode(fbNode);
        fbTypedNode.setTypes(fbPropertiesExpectedTypes.getExpectedTypeIds(fbTypedNode, fbType2Id));

        fbNodes.add(fbNode);
        fbTypedNodes.add(fbTypedNode);


        if (!graph.addVertex(fbNode))
          throw new IllegalStateException("While adding node " + fbNode + " of type " + fbNode.getClass() + ": Node already in graph");
        if (!graph.addVertex(fbTypedNode))
          throw new IllegalStateException("While adding node " + fbTypedNode + " of type " + fbTypedNode.getClass() + ": Node already in graph");

        graph.addEdge(new UnlabeledEdge(), fbTypedNode, fbNode);
      } else
        throw new RuntimeException("Nodes must be of class either NlBipartiteNode or FbBipartiteNode. This node is of class: " + node.getClass());
    }
  }

  private void createEdges(
      DirectedSparseGraph<BipartiteNode, BipartiteEdge> bigraph) {

    for (BipartiteEdge edge : bigraph.getEdges()) {

      edu.uci.ics.jung.graph.util.Pair<BipartiteNode> endPoints = bigraph.getEndpoints(edge);

      if (!(endPoints.getFirst() instanceof NlBipartiteNode) || !(endPoints.getSecond() instanceof FbBipartiteNode)) {
        throw new RuntimeException("First endpoint is of class: " + endPoints.getFirst().getClass() + ", Second endpoint class is: " + endPoints.getSecond().getClass());
      }

      // get the NlTypedBipartiteNode
      Collection<BipartiteNode> nlNeighbors = graph.getNeighbors(endPoints.getFirst());
      if (nlNeighbors.size() != 1)
        throw new IllegalStateException("The NlBipartite Node: " + endPoints.getFirst() + " must have exactly one neighbor. Number of neighbors is: " + nlNeighbors.size());
      BipartiteNode nlNeighbor = nlNeighbors.iterator().next();
      if (!(nlNeighbor instanceof NlTypedBipartiteNode))
        throw new RuntimeException("The class of neighbor of an NlBipartiteNode must be an NlTypedBipartiteNode. The class is " + nlNeighbor.getClass());

      // get the FBTypedBipartiteNode
      Collection<BipartiteNode> fbNeighbors = graph.getNeighbors(endPoints.getSecond());
      if (fbNeighbors.size() != 1)
        throw new IllegalStateException("The FbBipartite Node: " + endPoints.getSecond() + " must have exactly one neighbor. Number of neighbors is: " + fbNeighbors.size());
      BipartiteNode fbNeighbor = fbNeighbors.iterator().next();
      if (!(fbNeighbor instanceof FbTypedBipartiteNode))
        throw new RuntimeException("The class of neighbor of an FBBipartiteNode must be an FbTypedBipartiteNode. The class is " + fbNeighbor.getClass());

      graph.addEdge(edge, nlNeighbor, fbNeighbor);
    }
  }

  public DirectedSparseGraph<BipartiteNode, BipartiteEdge> getGraph() {
    return graph;
  }

  public Set<NlBipartiteNode> getNlNodes() {
    return nlNodes;
  }

  public Set<FbBipartiteNode> getFbNodes() {
    return fbNodes;
  }

  public Set<NlTypedBipartiteNode> getNlTypedNodes() {
    return nlTypedNodes;
  }

  public Set<FbTypedBipartiteNode> getFbTypedNodes() {
    return fbTypedNodes;
  }

  public void filterNodes(BipartiteNodeFilter filter) {

    List<BipartiteNode> nodesToRemove = new LinkedList<BipartiteNode>();

    // remove nodes using filter
    Iterator<BipartiteNode> nodeIter = graph.getVertices().iterator();
    while (nodeIter.hasNext()) {
      BipartiteNode node = nodeIter.next();
      if (filter.filterNode(node)) {
        nodesToRemove.add(node);
      }
    }

    for (BipartiteNode node : nodesToRemove) {
      LogInfo.log("Removing node " + node + " of class " + node.getClass());
      removeNode(node);
    }
    removeNodesWithNoMatches();
    logNodeStats();
  }

  private void removeNodesWithNoMatches() {

    // find nl-typed-nodes to remove
    List<NlTypedBipartiteNode> nlTypedNodesToRemove = new LinkedList<NlTypedBipartiteNode>();
    for (NlTypedBipartiteNode nlTypedNode : nlTypedNodes) {
      if (graph.getSuccessorCount(nlTypedNode) == 0) {
        nlTypedNodesToRemove.add(nlTypedNode);
      }
    }
    // remove them
    for (NlTypedBipartiteNode nlTypedNodeToRemove : nlTypedNodesToRemove) {
      BipartiteNode nlNodeToRemove = graph.getPredecessors(nlTypedNodeToRemove).iterator().next();
      LogInfo.log("Removing NL node with no successors: " + nlTypedNodeToRemove.getDescription());
      removeNode(nlTypedNodeToRemove);
      removeNode(nlNodeToRemove);
    }

    List<FbTypedBipartiteNode> fbTypedNodesToRemove = new LinkedList<FbTypedBipartiteNode>();
    for (FbTypedBipartiteNode fbTypedNode : fbTypedNodes) {
      if (graph.getPredecessorCount(fbTypedNode) == 0) {
        fbTypedNodesToRemove.add(fbTypedNode);
      }
    }
    // remove them
    for (FbTypedBipartiteNode fbTypedNodeToRemove : fbTypedNodesToRemove) {
      BipartiteNode fbNodeToRemove = graph.getSuccessors(fbTypedNodeToRemove).iterator().next();
      LogInfo.log("Removing FB node with no predecessors: " + fbTypedNodeToRemove.getDescription());
      removeNode(fbTypedNodeToRemove);
      removeNode(fbNodeToRemove);
    }
  }

  public boolean removeNode(BipartiteNode node) {

    boolean result = graph.removeVertex(node);
    if (result) {
      removeNodeFromPartitions(node);
    }
    return result;
  }

  public boolean addNode(BipartiteNode node) {

    boolean res = graph.addVertex(node);
    if (res) {
      addNodeToPartitions(node);
    }
    return res;
  }

  public void addSplitNlTypedNode(NlTypedBipartiteNode originalNode, NlTypedBipartiteNode splitNode, NodePairScorer scorer) {

    // get the Nl node
    Collection<BipartiteNode> nlNodes = graph.getPredecessors(originalNode);
    if (nlNodes.size() != 1)
      throw new IllegalStateException("Nl typed nodes must have exactly one predecessor. The node: " + originalNode + " has " + nlNodes.size() + " predecessors.");
    BipartiteNode nlNode = nlNodes.iterator().next();

    // get the successors and assign each successor to candidate1 or to candidate2
    Collection<BipartiteNode> successors = graph.getSuccessors(originalNode);
    Map<BipartiteNode, Integer> splitNodeSuccessors = new HashMap<BipartiteNode, Integer>();

    for (BipartiteNode successor : successors) {
      int intersection = Sets.intersection(successor.getMidIdPairSet(), splitNode.getMidIdPairSet()).size();

      if (intersection > 0 && areTypesCompatible(splitNode, (FbTypedBipartiteNode) successor)) {
        splitNodeSuccessors.put(successor, intersection);
      }
    }
    addSplitNodeToGraph(nlNode, splitNode, splitNodeSuccessors, scorer);
  }

  public CandidateScore findCandidateScoreForSplitNode(NlTypedBipartiteNode originalNode, NlTypedBipartiteNode splitNode, NodePairScorer scorer) {

    // get the successors and assign each successor to candidate1 or to candidate2


    CandidateScore candidateScore = null;
    BipartiteNode nlNode = graph.getPredecessors(originalNode).iterator().next();
    BipartiteEdge tempEdge = new UnlabeledEdge();
    graph.addEdge(tempEdge, nlNode, splitNode);

    Collection<BipartiteNode> successors = graph.getSuccessors(originalNode);

    for (BipartiteNode successor : successors) {

      if (areTypesCompatible(splitNode, (FbTypedBipartiteNode) successor)) {
        double score = scorer.scoreNodePair(this, splitNode, successor);
        if (candidateScore == null || candidateScore.getScore() < score) {
          candidateScore = new CandidateScore((FbTypedBipartiteNode) successor, score);
        }
      }
    }

    graph.removeEdge(tempEdge);
    return candidateScore;
  }

  private boolean areTypesCompatible(NlTypedBipartiteNode nlTypedNode, FbTypedBipartiteNode fbTypedNode) {
    return (nlTypedNode.getArg1Type() == fbTypedNode.getArg1Type() &&
        nlTypedNode.getArg2Type() == fbTypedNode.getArg2Type());
  }

  private void addSplitNodeToGraph(BipartiteNode nlNode,
                                   BipartiteNode splitNode, Map<BipartiteNode, Integer> nodeSuccessors, NodePairScorer scorer) {

    if (nodeSuccessors.size() > 0) {
      addNode(splitNode);
      graph.addEdge(new CountEdge(), nlNode, splitNode);

      for (BipartiteNode nodeSuccessor : nodeSuccessors.keySet()) {

        int count = nodeSuccessors.get(nodeSuccessor);
        double score = scorer.scoreNodePair(this, splitNode, nodeSuccessor);
        graph.addEdge(new ScoreEdge(count, score), splitNode, nodeSuccessor);
      }
    } else
      throw new IllegalStateException("No successors for candidate node: " + splitNode);
  }

  public boolean addEdge(BipartiteEdge edge, BipartiteNode node1, BipartiteNode node2) {
    return graph.addEdge(edge, node1, node2);
  }

  private void addNodeToPartitions(BipartiteNode node) {
    if (node instanceof NlTypedBipartiteNode)
      nlTypedNodes.add((NlTypedBipartiteNode) node);
    else if (node instanceof FbTypedBipartiteNode)
      fbTypedNodes.add((FbTypedBipartiteNode) node);
    else if (node instanceof NlBipartiteNode)
      nlNodes.add((NlBipartiteNode) node);
    else if (node instanceof FbBipartiteNode)
      fbNodes.add((FbBipartiteNode) node);
    else
      throw new RuntimeException("Node is of illegal class: " + node.getClass());
  }

  private void removeNodeFromPartitions(BipartiteNode node) {
    if (node instanceof NlTypedBipartiteNode)
      nlTypedNodes.remove(node);
    else if (node instanceof FbTypedBipartiteNode)
      fbTypedNodes.remove(node);
    else if (node instanceof NlBipartiteNode)
      nlNodes.remove(node);
    else if (node instanceof FbBipartiteNode)
      fbNodes.remove(node);
    else
      throw new RuntimeException("Node is of illegal class: " + node.getClass());
  }

  public void logNlNodeNeighbors() {

    for (NlTypedBipartiteNode nlTypedNode : nlTypedNodes) {

      Collection<BipartiteNode> successors = graph.getSuccessors(nlTypedNode);
      for (BipartiteNode successor : successors) {
        LogInfo.log(nlTypedNode.description + "\t" + successor.getDescription() + "\t" + graph.findEdge(nlTypedNode, successor).score());
      }

    }

  }

  public void logNodeStats() {
    LogInfo.log("Number of nodes in the graph: " + graph.getVertexCount());
    LogInfo.log("Number of nl nodes: " + nlNodes.size());
    LogInfo.log("Number of nl typed nodes: " + nlTypedNodes.size());
    LogInfo.log("Number of fb nodes: " + fbNodes.size());
    LogInfo.log("Number of fb typed nodes: " + fbTypedNodes.size());
  }
}
