package edu.stanford.nlp.sempre.fbalignment.unary;


public final class FbUnaryNodeFilter {
  private FbUnaryNodeFilter() { }

  public static boolean filter(FbUnaryAlignmentNode unary) {

    String unaryNode = unary.getUnaryNode();
    if (unaryNode.startsWith("fb:user.") ||
        unaryNode.startsWith("fb:base.") ||
        unaryNode.startsWith("fb:common.") ||
        unaryNode.startsWith("fb:dataworld.") ||
        unaryNode.startsWith("fb:freebase.")) {
      return true;
    }
    return false;
  }
}
