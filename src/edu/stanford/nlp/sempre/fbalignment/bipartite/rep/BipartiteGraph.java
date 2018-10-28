package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.util.CollectionUtils;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains a bi-partite graph from Natrual language predicate to Freebase
 * relations. Allows to put various stats about the graph and sample from it.
 *
 * @author jonathanberant
 */
public class BipartiteGraph implements Serializable {
  private static final long serialVersionUID = 6386130890563548472L;
  private UndirectedSparseGraph<BipartiteNode, BipartiteEdge> graph;
  Set<NlBipartiteNode> nlNodes;
  Set<FbBipartiteNode> fbNodes;

  public BipartiteGraph(String fileName) throws IOException, ClassNotFoundException {
    graph = IOUtils.readObjectFromFile(fileName);
    nlNodes = new HashSet<NlBipartiteNode>();
    fbNodes = new HashSet<FbBipartiteNode>();
    for (BipartiteNode node : graph.getVertices()) {

      if (node instanceof NlBipartiteNode) {
        NlBipartiteNode nlNode = (NlBipartiteNode) node;
        nlNodes.add(nlNode);
      } else if (node instanceof FbBipartiteNode) {
        FbBipartiteNode fbNode = (FbBipartiteNode) node;
        fbNodes.add(fbNode);
      } else
        throw new RuntimeException("Node must be either NlBipartiteNode or FbBipartiteNode. It is: " + node.getClass().toString());
    }
  }

  public BipartiteGraph(UndirectedSparseGraph<BipartiteNode, BipartiteEdge> graph) {
    graph = graph;
    nlNodes = new HashSet<NlBipartiteNode>();
    fbNodes = new HashSet<FbBipartiteNode>();
    for (BipartiteNode node : graph.getVertices()) {

      if (node instanceof NlBipartiteNode) {
        NlBipartiteNode nlNode = (NlBipartiteNode) node;
        nlNodes.add(nlNode);
      } else if (node instanceof FbBipartiteNode) {
        FbBipartiteNode fbNode = (FbBipartiteNode) node;
        fbNodes.add(fbNode);
      } else
        throw new RuntimeException("Node must be either NlBipartiteNode or FbBipartiteNode. It is: " + node.getClass().toString());
    }
  }

  public Set<NlBipartiteNode> getNlNodes() { return nlNodes; }
  public Set<FbBipartiteNode> getFbNodes() { return fbNodes; }

  public int getNlNodeCount() {
    return nlNodes.size();
  }

  public int getFbNodeCount() {
    return fbNodes.size();
  }

  /** Counts the number of matches between predicates and relations */
  public int totalMatchesCount() {

    int sum = 0;
    for (BipartiteEdge edge : graph.getEdges()) {
      sum += edge.value();
    }
    return sum;
  }

  public ClassicCounter<Integer> getNlDegreeDistribution() {

    ClassicCounter<Integer> res = new ClassicCounter<Integer>();
    for (NlBipartiteNode nlNode : nlNodes) {
      res.incrementCount(graph.getNeighborCount(nlNode));
    }
    return res;
  }

  public ClassicCounter<Integer> getFbDegreeDistribution() {

    ClassicCounter<Integer> res = new ClassicCounter<Integer>();
    for (FbBipartiteNode fbNode : fbNodes) {
      res.incrementCount(graph.getNeighborCount(fbNode));
    }
    return res;
  }

  public Collection<NlBipartiteNode> sampleNlNodes(int numOfSamples) {

    return CollectionUtils.sampleWithoutReplacement(nlNodes, numOfSamples);
  }

  public Collection<FbBipartiteNode> sampleFbNodes(int numOfSamples) {

    return CollectionUtils.sampleWithoutReplacement(fbNodes, numOfSamples);
  }

  public UndirectedSparseGraph<BipartiteNode, BipartiteEdge> getGraph() {
    return graph;
  }

  public void logEdgesByCount(int strength) {

    for (NlBipartiteNode nlNode : nlNodes) {

      Collection<BipartiteNode> neighbors = graph.getNeighbors(nlNode);
      for (BipartiteNode neighbor : neighbors) {

        BipartiteEdge edge = graph.findEdge(nlNode, neighbor);
        if (edge.value() >= strength) {
          LogInfo.log(nlNode + "\t" + neighbor + "\t" + edge);
        }
      }
    }
  }
}
