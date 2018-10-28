package edu.stanford.nlp.sempre.fbalignment.bipartite.rep;

import edu.stanford.nlp.util.Pair;

import java.io.Serializable;
import java.util.*;

public abstract class BipartiteNode implements Serializable {

  private static final long serialVersionUID = 8382340391207284957L;
  protected String description;
  protected BipartiteNodeType nodeType;

  protected Set<Pair<Integer, Integer>> midIdPairSet; // pairs of mid-ids that occur with this node
  protected Map<Integer, TypeIdPairs> arg1TypeIdToPairsMap; // mapping from type-id to pairs of mid-ids whose arg1 is of the type-id
  protected Map<Integer, TypeIdPairs> arg2TypeIdToPairsMap; // mapping from type-id to pairs of mid-ids whose arg2 is of the type-id

  // abstract methods

  public abstract BipartiteNodeType getType();

  // non-abstract methods

  public BipartiteNode() {
    midIdPairSet = new TreeSet<Pair<Integer, Integer>>();
    arg1TypeIdToPairsMap = new HashMap<Integer, TypeIdPairs>();
    arg2TypeIdToPairsMap = new HashMap<Integer, TypeIdPairs>();
  }

  public Set<Pair<Integer, Integer>> getMidIdPairSet() {
    return midIdPairSet;
  }

  public int getMidIdPairsCount() { return midIdPairSet.size(); }

  public void sortTypeMapsBySetSize() {

    // sort
    List<TypeIdPairs> arg1IdPairs = new LinkedList<TypeIdPairs>();
    arg1IdPairs.addAll(arg1TypeIdToPairsMap.values());
    Collections.sort(arg1IdPairs);
    // re-insert
    arg1TypeIdToPairsMap = new LinkedHashMap<Integer, BipartiteNode.TypeIdPairs>();
    for (TypeIdPairs typeIdPairs : arg1IdPairs) {
      arg1TypeIdToPairsMap.put(typeIdPairs.getTypeId(), typeIdPairs);
    }

    // sort
    List<TypeIdPairs> arg2IdPairs = new LinkedList<TypeIdPairs>();
    arg2IdPairs.addAll(arg2TypeIdToPairsMap.values());
    Collections.sort(arg2IdPairs);
    // re-insert
    arg2TypeIdToPairsMap = new LinkedHashMap<Integer, BipartiteNode.TypeIdPairs>();
    for (TypeIdPairs typeIdPairs : arg2IdPairs) {
      arg2TypeIdToPairsMap.put(typeIdPairs.getTypeId(), typeIdPairs);
    }
  }


  public Map<Integer, TypeIdPairs> getArg1TypeMap() {
    return arg1TypeIdToPairsMap;
  }
  public Map<Integer, TypeIdPairs> getArg2TypeMap() {
    return arg2TypeIdToPairsMap;
  }

  public void addIdPairToArg1TypeMap(int type, Pair<Integer, Integer> idPair) {

    TypeIdPairs typeIdPairs = arg1TypeIdToPairsMap.get(type);
    if (typeIdPairs == null) {
      Set<Pair<Integer, Integer>> idPairs = new TreeSet<Pair<Integer, Integer>>();
      typeIdPairs = new TypeIdPairs(type, idPairs);
      arg1TypeIdToPairsMap.put(type, typeIdPairs);
    }
    typeIdPairs.addPair(idPair);
  }

  public void addIdPairToArg2TypeMap(int type, Pair<Integer, Integer> idPair) {

    TypeIdPairs typeIdPairs = arg2TypeIdToPairsMap.get(type);
    if (typeIdPairs == null) {
      Set<Pair<Integer, Integer>> idPairs = new TreeSet<Pair<Integer, Integer>>();
      typeIdPairs = new TypeIdPairs(type, idPairs);
      arg2TypeIdToPairsMap.put(type, typeIdPairs);
    }
    typeIdPairs.addPair(idPair);
  }

  public Set<Pair<Integer, Integer>> getArg1IdPairs(int type) {
    if (type == -1)
      return midIdPairSet;
    if (!arg1TypeIdToPairsMap.containsKey(type))
      return new HashSet<Pair<Integer, Integer>>();
    return arg1TypeIdToPairsMap.get(type).getIdPairs();
  }

  public Set<Pair<Integer, Integer>> getArg2IdPairs(int type) {
    if (type == -1)
      return midIdPairSet;
    if (!arg2TypeIdToPairsMap.containsKey(type))
      return new HashSet<Pair<Integer, Integer>>();
    return arg2TypeIdToPairsMap.get(type).getIdPairs();
  }

  public boolean addPair(int mid1, int mid2) {
    return midIdPairSet.add(new Pair<Integer, Integer>(mid1, mid2));
  }

  public boolean addPair(Pair<Integer, Integer> pair) {
    return midIdPairSet.add(pair);
  }

  public void addAllPairs(Set<Pair<Integer, Integer>> pairs) {
    for (Pair<Integer, Integer> pair : pairs) {
      midIdPairSet.add(pair);
    }
  }

  public String getDescription() { return description; }

  public String toString() { return description + "\t" + midIdPairSet; }
  public String toShortString() { return description; }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((description == null) ? 0 : description.hashCode());
    result = prime * result
        + ((nodeType == null) ? 0 : nodeType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BipartiteNode other = (BipartiteNode) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (nodeType != other.nodeType)
      return false;
    return true;
  }

  public static BipartiteNode copyNode(BipartiteNode otherNode) {

    BipartiteNode res;
    if (otherNode instanceof NlBipartiteNode) {
      res = new NlBipartiteNode(otherNode.description);
    } else if (otherNode instanceof FbBipartiteNode) { // it is an FbBipartiteNode
      FbBipartiteNode fbOriginal = (FbBipartiteNode) otherNode;
      res = new FbBipartiteNode(fbOriginal.getCompositePredicate(), fbOriginal.reversed);
    } else
      throw new RuntimeException("The node being cloned has an illegal type: " + otherNode.getClass());
    // copy pairs of IDs
    for (Pair<Integer, Integer> pair : otherNode.midIdPairSet) {
      res.addPair(new Pair<Integer, Integer>(pair.first, pair.second));
    }
    // copy mapping of types to pairs with arg1 of that type
    for (int typeId : otherNode.arg1TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : otherNode.getArg1IdPairs(typeId)) {
        res.addIdPairToArg1TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
    // copy mapping of types to pairs with args2 of that type
    for (int typeId : otherNode.arg2TypeIdToPairsMap.keySet()) {
      for (Pair<Integer, Integer> pair : otherNode.getArg2IdPairs(typeId)) {
        res.addIdPairToArg2TypeMap(typeId, new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }
    return res;
  }

  public class TypeIdPairs implements Comparable<TypeIdPairs>, Serializable {


    private static final long serialVersionUID = -4002959569790723021L;
    private int typeId;
    private Set<Pair<Integer, Integer>> idPairs;

    public TypeIdPairs(int typeId, Set<Pair<Integer, Integer>> idPairs) {
      typeId = typeId;
      idPairs = idPairs;
    }

    public TypeIdPairs(TypeIdPairs other) {

      typeId = other.typeId;
      for (Pair<Integer, Integer> pair : other.idPairs) {
        // no need to copy the integers since they are immutable
        idPairs.add(new Pair<Integer, Integer>(pair.first, pair.second));
      }
    }

    public void addPair(Pair<Integer, Integer> pair) {
      idPairs.add(pair);
    }

    public Set<Pair<Integer, Integer>> getIdPairs() {
      return idPairs;
    }

    public int getTypeId() {
      return typeId;
    }

    public String toString() {
      return typeId + "\t" + idPairs;
    }

    @Override
    public int compareTo(TypeIdPairs other) {
      return (new Integer(other.idPairs.size())).compareTo(idPairs.size());
    }
  }
}
