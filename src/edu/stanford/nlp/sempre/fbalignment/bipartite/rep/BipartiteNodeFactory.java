package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;

import java.util.Set;

/**
 * Creates a node without populating the type map
 *
 * @author jonathanberant
 */
public final class BipartiteNodeFactory {
  private BipartiteNodeFactory() { }

  public static BipartiteNode createTypedBipartiteNodeCandidate(int arg1Type, int arg2Type, BipartiteNodeType nodeType, String description, Set<Pair<Integer, Integer>> midIdPairs) {

    BipartiteNode result;
    if (nodeType == BipartiteNodeType.FB_TYPED) {
      result = new FbTypedBipartiteNode(
          FbBipartiteNode.fromCompositePredicateDescription(description),
          arg1Type, arg2Type);
      result.addAllPairs(midIdPairs);

    } else if (nodeType == BipartiteNodeType.NL_TYPED) {
      result = new NlTypedBipartiteNode(description, arg1Type, arg2Type);
      result.addAllPairs(midIdPairs);
    } else
      throw new IllegalArgumentException("The following type is not a typed bipartite node: " + nodeType);
    return result;
  }

  public static BipartiteNode createTypedBipartiteNodeCandidate(BipartiteNode originalNode, Set<Pair<Integer, Integer>> midIdPairs) {

    BipartiteNode result;
    if (originalNode.getType() == BipartiteNodeType.FB_TYPED) {
      FbTypedBipartiteNode fbTypedNode = (FbTypedBipartiteNode) originalNode;
      result = new FbTypedBipartiteNode(
          FbBipartiteNode.fromCompositePredicateDescription(originalNode.description),
          fbTypedNode.getArg1Type(), fbTypedNode.getArg2Type());
      result.addAllPairs(midIdPairs);

    } else if (originalNode.getType() == BipartiteNodeType.NL_TYPED) {
      NlTypedBipartiteNode nlTypedNode = (NlTypedBipartiteNode) originalNode;
      result = new NlTypedBipartiteNode(originalNode.getDescription(), nlTypedNode.getArg1Type(), nlTypedNode.getArg2Type());
      result.addAllPairs(midIdPairs);
    } else
      throw new IllegalArgumentException("The following type is not a typed bipartite node: " + originalNode.getType());
    return result;
  }


}
