package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;

public class NlTypedBipartiteNode extends NlBipartiteNode {

  private static final long serialVersionUID = -8766812334978956800L;
  private int arg1Type = -1;
  private int arg2Type = -1;

  public NlTypedBipartiteNode(String predicate) {
    super(predicate);
    nodeType = BipartiteNodeType.NL_TYPED;
  }

  public NlTypedBipartiteNode(String predicate, int arg1Type, int arg2Type) {
    super(predicate);
    nodeType = BipartiteNodeType.NL_TYPED;
    this.arg1Type = arg1Type;
    this.arg2Type = arg2Type;
  }

  public NlTypedBipartiteNode(NlBipartiteNode nlNode) {
    super(nlNode.description);
    nodeType = BipartiteNodeType.NL_TYPED;

    for (Pair<Integer, Integer> pair : nlNode.midIdPairSet) {
      this.addPair(new Pair<Integer, Integer>(pair.first, pair.second));
    }
    // copy mapping of types to pairs with arg1 of that type
    for (int typeId : nlNode.arg1TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : nlNode.getArg1IdPairs(typeId)) {
        this.addIdPairToArg1TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
    // copy mapping of types to pairs with args2 of that type
    for (int typeId : nlNode.arg2TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : nlNode.getArg2IdPairs(typeId)) {
        this.addIdPairToArg2TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + arg1Type;
    result = prime * result + arg2Type;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    NlTypedBipartiteNode other = (NlTypedBipartiteNode) obj;
    if (arg1Type != other.arg1Type)
      return false;
    if (arg2Type != other.arg2Type)
      return false;
    return true;
  }

  public String toString() {
    return super.toString() + "\t" + "Arg1 Type:\t" + arg1Type + "\tArg2 Type:\t" + arg2Type;
  }

  public String toShortString() {
    return super.toShortString() + "\t" + arg1Type + "\t" + arg2Type;
  }

  public NlTypedBipartiteNode createTypedCandidate(int typeId, boolean arg1) {

    // error checking
    if (arg1 && isArg1Typed())
      throw new IllegalStateException("Error: arg1 of this node is already typed: " + this);
    if (!arg1 && isArg2Typed())
      throw new IllegalStateException("Error: arg2 of this node is already typed: " + this);
    if ((arg1 && getArg1IdPairs(typeId) == null) || (!arg1 && getArg2IdPairs(typeId) == null))
      throw new IllegalArgumentException("Error: type map doesn't contain the type " + typeId + " for arg1 = " + arg1);
    // creating new node
    NlTypedBipartiteNode newNode;
    if (arg1) {
      newNode = new NlTypedBipartiteNode(this.description, typeId, this.arg2Type);
      newNode.addAllPairs(getArg1IdPairs(typeId));
    } else { // arg2
      newNode = new NlTypedBipartiteNode(this.description, this.arg1Type, typeId);
      newNode.addAllPairs(getArg2IdPairs(typeId));
    }
    return newNode;
  }

  public boolean isArg1Typed() { return arg1Type != -1; }
  public boolean isArg2Typed() { return arg2Type != -1; }
  public boolean isFullyTyped() { return arg1Type != -1 && arg2Type != -1; }
  public int getArg1Type() { return arg1Type; }
  public int getArg2Type() { return arg2Type; }

}
