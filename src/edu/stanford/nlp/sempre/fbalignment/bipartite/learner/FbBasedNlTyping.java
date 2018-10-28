package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Sets;

import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Splits every node according to all types for which it has a match and
 * computes a score for every possible typing For example "write" will be split
 * to: <i>fb:people.person write fb:m.book.written_work</i> and also
 * <i>fb:people.person write fb:music.composition</i>
 *
 * @author jonathanberant
 */
public class FbBasedNlTyping extends NlIterLearner {

  public FbBasedNlTyping(FourPartiteGraph fourPartiteGraph) throws IOException, ClassNotFoundException {
    super(fourPartiteGraph);
  }

  @Override
  protected boolean handleNode(NlTypedBipartiteNode fromNlTypedNode,
                               Queue<NlTypedBipartiteNode> nodeQueue) {

    // Find all of the pairs of types
    Set<Pair<Integer, Integer>> expectedTypesSet = new HashSet<Pair<Integer, Integer>>();
    for (BipartiteNode successor : graph.getGraph().getSuccessors(fromNlTypedNode)) {
      FbTypedBipartiteNode fbTypedSuccessor = (FbTypedBipartiteNode) successor;
      expectedTypesSet.add(new Pair<Integer, Integer>(fbTypedSuccessor.getArg1Type(), fbTypedSuccessor.getArg2Type()));
    }
    // for each pair of type create a node and the necessary edges
    for (Pair<Integer, Integer> expectedTypes : expectedTypesSet) {
      splitNodeWithPairOfTypes(fromNlTypedNode, expectedTypes);
    }
    // delete the original node
    graph.removeNode(fromNlTypedNode);
    return false;
  }

  private void splitNodeWithPairOfTypes(NlTypedBipartiteNode fromNlTypedNode,
                                        Pair<Integer, Integer> expectedTypes) {

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
    graph.addSplitNlTypedNode(fromNlTypedNode, (NlTypedBipartiteNode) splitNode, nodePairScorer);
  }

  @Override
  // no need to do anything
  protected void finalizeGraph() {
  }
}
