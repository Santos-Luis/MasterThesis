package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;

public class FbTypedBipartiteNode extends FbBipartiteNode {

  private static final long serialVersionUID = -8782080020124583312L;
  private int arg1Type = -1;
  private int arg2Type = -1;

  public FbTypedBipartiteNode(FbBipartiteNode fbNode) {

    super();
    nodeType = BipartiteNodeType.FB_TYPED;
    compositePredicate.addAll(fbNode.compositePredicate);
    description = fbNode.description;
    reversed = fbNode.reversed;

    // copy pairs of IDs
    for (Pair<Integer, Integer> pair : fbNode.midIdPairSet) {
      this.addPair(new Pair<Integer, Integer>(pair.first, pair.second));
    }
    // copy mapping of types to pairs with arg1 of that type
    for (int typeId : fbNode.arg1TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : fbNode.getArg1IdPairs(typeId)) {
        this.addIdPairToArg1TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
    // copy mapping of types to pairs with args2 of that type
    for (int typeId : fbNode.arg2TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : fbNode.getArg2IdPairs(typeId)) {
        this.addIdPairToArg2TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
  }

  public FbTypedBipartiteNode(FbBipartiteNode fbNode, int arg1Typd, int arg2Type) {

    super();
    nodeType = BipartiteNodeType.FB_TYPED;
    compositePredicate.addAll(fbNode.compositePredicate);
    description = fbNode.description;
    reversed = fbNode.reversed;

    this.arg1Type = arg2Type;
    this.arg2Type = arg2Type;

    // copy pairs of IDs
    for (Pair<Integer, Integer> pair : fbNode.midIdPairSet) {
      this.addPair(new Pair<Integer, Integer>(pair.first, pair.second));
    }
    // copy mapping of types to pairs with arg1 of that type
    for (int typeId : fbNode.arg1TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : fbNode.getArg1IdPairs(typeId)) {
        this.addIdPairToArg1TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
    // copy mapping of types to pairs with args2 of that type
    for (int typeId : fbNode.arg2TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : fbNode.getArg2IdPairs(typeId)) {
        this.addIdPairToArg2TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
  }

  public String toString() {
    return super.toString() + "\t" + "Arg1 Type:\t" + arg1Type + "\tArg2 Type:\t" + arg2Type;
  }

  public String toShortString() {
    return super.toShortString() + "\t" + arg1Type + "\t" + arg2Type;
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
    FbTypedBipartiteNode other = (FbTypedBipartiteNode) obj;
    if (arg1Type != other.arg1Type)
      return false;
    if (arg2Type != other.arg2Type)
      return false;
    return true;
  }

  public boolean isArg1Typed() { return arg1Type != -1; }
  public boolean isArg2Typed() { return arg2Type != -1; }
  public int getArg1Type() { return arg1Type; }
  public int getArg2Type() { return arg2Type; }
  public void setTypes(Pair<Integer, Integer> pair) {
    arg1Type = pair.first; arg2Type = pair.second;
  }
}
