package edu.stanford.nlp.sempre.logicpuzzles;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.sempre.*;
import fig.basic.*;

/**
 * A knowledge graph for a logic puzzle.
 *
 * For each entity, create fb:entity.[type]_[name].
 * For each entity type, create fb:unary.[type]
 * For each known relation, create fb:unary.[type]_[relation]
 *   Only unary known relations are supported currently.
 * For an unknown n-ary relation, create fb:record.[relation]_[number]
 *   for each possible record.  Also create fb:binary.[relation].[type]
 *   for each of the n types.
 *
 * e.g. (graph logicpuzzles.PuzzleKnowledgeGraph (type person (A B C)) (relation hired (person)) (relation hardworking (person) (A C)))
 * yields:
 *   - fb:entity.person_a
 *   - fb:entity.person_b
 *   - fb:entity.person_c
 *   - fb:unary.person  # (person.a person.b person.c)
 *   - fb:record.hired_a
 *   - fb:record.hired_b
 *   - fb:record.hired_c
 *   - fb:binary.hired.person  # ((hired.a person.a) ...)
 *   - fb:unary.person_hardworking  # (person.a person.c)
 *
 * The associated types will be:
 *   - fb:type.entity_person
 *   - fb:type.record_hired
 *   - (-> fb:type.entity_person fb:type.record_hired)
 *
 * If an entity type is additionally specified as "ordered", the additional binaries
 * will be constructed:
 *  - fb:order.before.[entityType]
 *  - fb:order.after.[entityType]
 *  - fb:order.next.[entityType]
 *  - fb:order.prev.[entityType]
 *  - fb:order.adjacent.[entityType]
 *
 * @author Robin Jia
 */
public class PuzzleKnowledgeGraph extends KnowledgeGraph {
  public static class Options {
    @Option(gloss = "Base directory for graph files") public String baseDir = null;
    @Option(gloss = "Verbosity") public int verbosity = 0;
  }
  public static Options opts = new Options();

  private LispTree fullTree;

  /* Map type name to list of entities (useful when creating FiniteUnary/FiniteBinary */
  public Map<String, List<NameValue>> typeToDomain = new HashMap<String, List<NameValue>>();

  /* Map unknown relation name to list of types */
  public Map<String, List<String>> relationTypeMap = new HashMap<String, List<String>>();

  /* For FuzzyMatchFn: all formulas
   * Note: by convention, all of these sets are disjoint.
   * Therefore, none of the order-related formulas are present in allBinaryFormulas. */
  private Set<Formula> allEntityFormulas = new HashSet<Formula>();
  private Set<Formula> allUnaryFormulas = new HashSet<Formula>();
  private Set<Formula> allBinaryFormulas = new HashSet<Formula>();
  private Set<Formula> allBeforeFormulas = new HashSet<Formula>();
  private Set<Formula> allAfterFormulas = new HashSet<Formula>();
  private Set<Formula> allNextFormulas = new HashSet<Formula>();
  private Set<Formula> allPrevFormulas = new HashSet<Formula>();
  private Set<Formula> allAdjacentFormulas = new HashSet<Formula>();

  /* For FuzzyMatchFn: map words in utterance to ValueFormula's */
  private Map<String, Set<Formula>> phraseToEntityFormulas = new TreeMap<String, Set<Formula>>();
  private Map<String, Set<Formula>> phraseToUnaryFormulas = new TreeMap<String, Set<Formula>>();
  private Map<String, Set<Formula>> phraseToBinaryFormulas = new TreeMap<String, Set<Formula>>();

  /* For the executor: map from id's to unaries and binaries */
  private Map<String, FiniteUnary> unaries = new TreeMap<String, FiniteUnary>();
  private Map<String, FiniteBinary> binaries = new TreeMap<String, FiniteBinary>();

  public PuzzleKnowledgeGraph(LispTree tree) {
    init(tree);
  }

  private void init(LispTree tree) {
    this.fullTree = tree;
    LispTree head = tree.head();
    LispTree tail = tree.tail();
    if (!head.value.equals("logicpuzzles.PuzzleKnowledgeGraph")) {
      throw new RuntimeException("Received malformatted logicpuzzles.PuzzleKnowledgeGraph");
    }

    /* Check if we're supposed to read from file */
    LispTree child0 = tail.children.get(0);
    if (child0.value != null) {
      String filename = new File(opts.baseDir, tail.children.get(0).value).getPath();
      LispTree newTree = LispTree.proto.parseFromFile(filename).next();
      init(newTree);
      return;
    }

    for (LispTree child: tail.children) {
      parseTree(child);
    }

    /* Log results */
    if (opts.verbosity >= 2) {
      for (Map.Entry<String, FiniteUnary> entry: unaries.entrySet()) {
        String id = entry.getKey();
        FiniteUnary unary = entry.getValue();
        LogInfo.begin_track(id);
        unary.log();
        LogInfo.end_track();
      }
      for (Map.Entry<String, FiniteBinary> entry: binaries.entrySet()) {
        String id = entry.getKey();
        FiniteBinary binary = entry.getValue();
        LogInfo.begin_track(id);
        binary.log();
        LogInfo.end_track();
      }

      LogInfo.begin_track("FuzzyMatch phrases");
      logPhraseMap(phraseToEntityFormulas, "entity");
      logPhraseMap(phraseToUnaryFormulas, "unary");
      logPhraseMap(phraseToBinaryFormulas, "binary");
      LogInfo.end_track();
    }
  }

  private void logPhraseMap(Map<String, Set<Formula>> map, String name) {
    LogInfo.begin_track(name);
    for (Map.Entry<String, Set<Formula>> entry: map.entrySet()) {
      String key = entry.getKey();
      Set<Formula> value = entry.getValue();
      LogInfo.logs(key + "-> " + value.toString());
    }
    LogInfo.end_track();
  }

  public static PuzzleKnowledgeGraph fromLispTree(LispTree tree) {
    return new PuzzleKnowledgeGraph(tree.tail());
  }

  public NameValue findNameValue(String type, String entity) {
    for (NameValue value: typeToDomain.get(type)) {
      if (value.description.equals(entity)) return value;
    }
    return null;
  }

  private void parseTree(LispTree tree) {
    LispTree head = tree.head();
    LispTree tail = tree.tail();
    if (head.value.equals("type")) {
      parseEntityType(tail);
    } else if (head.value.equals("relation")) {
      parseRelation(tail);
    } else {
      throw new RuntimeException("Received unknown puzzle.PuzzleKnowledgeGraph expression \""
          + head.value + "\".");
    }
  }

  private void addBeforeRelation(String typeName, List<NameValue> entityList) {
    NameValue value = PuzzleTypeSystem.createOrderRelation("before", typeName);
    Formula formula = new ValueFormula<NameValue>(value);
    allBeforeFormulas.add(formula);
    FiniteBinary binary = new FiniteBinary(entityList, entityList);
    for (int i = 0; i < entityList.size(); ++i) {
      NameValue e1 = entityList.get(i);
      for (int j = 0; j < i; ++j) {
        NameValue e2 = entityList.get(j);
        binary.put(e1, e2, BooleanExpression.TRUE);
      }
    }
    binaries.put(value.id, binary);
  }

  private void addAfterRelation(String typeName, List<NameValue> entityList) {
    NameValue value = PuzzleTypeSystem.createOrderRelation("after", typeName);
    Formula formula = new ValueFormula<NameValue>(value);
    allAfterFormulas.add(formula);
    FiniteBinary binary = new FiniteBinary(entityList, entityList);
    for (int i = 0; i < entityList.size(); ++i) {
      NameValue e1 = entityList.get(i);
      for (int j = i + 1; j < entityList.size(); ++j) {
        NameValue e2 = entityList.get(j);
        binary.put(e1, e2, BooleanExpression.TRUE);
      }
    }
    binaries.put(value.id, binary);
  }

  private void addNextRelation(String typeName, List<NameValue> entityList) {
    NameValue value = PuzzleTypeSystem.createOrderRelation("next", typeName);
    Formula formula = new ValueFormula<NameValue>(value);
    allNextFormulas.add(formula);
    FiniteBinary binary = new FiniteBinary(entityList, entityList);
    for (int i = 0; i < entityList.size() - 1; ++i) {
      NameValue e1 = entityList.get(i);
      NameValue e2 = entityList.get(i + 1);
      binary.put(e1, e2, BooleanExpression.TRUE);
    }
    binaries.put(value.id, binary);
  }

  private void addPrevRelation(String typeName, List<NameValue> entityList) {
    NameValue value = PuzzleTypeSystem.createOrderRelation("prev", typeName);
    Formula formula = new ValueFormula<NameValue>(value);
    allPrevFormulas.add(formula);
    FiniteBinary binary = new FiniteBinary(entityList, entityList);
    for (int i = 1; i < entityList.size(); ++i) {
      NameValue e1 = entityList.get(i);
      NameValue e2 = entityList.get(i - 1);
      binary.put(e1, e2, BooleanExpression.TRUE);
    }
    binaries.put(value.id, binary);
  }

  private void addAdjacentRelation(String typeName, List<NameValue> entityList) {
    NameValue value = PuzzleTypeSystem.createOrderRelation("adjacent", typeName);
    Formula formula = new ValueFormula<NameValue>(value);
    allAdjacentFormulas.add(formula);
    FiniteBinary binary = new FiniteBinary(entityList, entityList);
    for (int i = 0; i < entityList.size() - 1; ++i) {
      NameValue e1 = entityList.get(i);
      NameValue e2 = entityList.get(i + 1);
      binary.put(e1, e2, BooleanExpression.TRUE);
      binary.put(e2, e1, BooleanExpression.TRUE);
    }
    binaries.put(value.id, binary);
  }

  private void parseEntityType(LispTree tree) {
    String typeName = tree.child(0).value.toLowerCase();
    LispTree entities = tree.child(1);
    boolean ordered = tree.children.size() > 2 && tree.child(2).value.equals("ordered");
    if (opts.verbosity >= 3) {
      if (ordered) {
        LogInfo.logs("Found ordered relation " + tree);
      } else {
        LogInfo.logs("Found unordered relation " + tree);
      }
    }

    /* Read the list of all entities of this type */
    List<NameValue> entityList = new ArrayList<NameValue>();
    for (LispTree child: entities.children) {
      NameValue value = PuzzleTypeSystem.createEntity(typeName, child.value);
      entityList.add(value);
    }
    typeToDomain.put(typeName, entityList);

    /* Map entity name (e.g. "Percy") to an entity for fuzzy matching.
     * Also try "typeName entityName" and "entityName typeName"
     * e.g. "Professor Percy" or "front position" */
    for (NameValue entity: entityList) {
      Formula curFormula = new ValueFormula<NameValue>(entity);
      allEntityFormulas.add(curFormula);
      String entityName = entity.description.toLowerCase();
      String s1 = entityName;
      String s2 = typeName + " " + entityName;
      String s3 = entityName + " " + typeName;
      MapUtils.addToSet(phraseToEntityFormulas, s1, curFormula);
      MapUtils.addToSet(phraseToEntityFormulas, s2, curFormula);
      MapUtils.addToSet(phraseToEntityFormulas, s3, curFormula);
    }

    /* Map type name (e.g. "person") to a unary for fuzzy matching */
    NameValue unaryValue = PuzzleTypeSystem.createUnary(typeName);
    Formula formula = new ValueFormula<NameValue>(unaryValue);
    allUnaryFormulas.add(formula);
    MapUtils.addToSet(phraseToUnaryFormulas, typeName.toLowerCase(), formula);

    /* Store unaries for CSPExecutor */
    for (NameValue value: entityList) {
      FiniteUnary unary = FiniteUnary.makeSingleton(entityList, value);
      unaries.put(value.id, unary);
    }
    String id = PuzzleTypeSystem.createUnaryId(typeName);
    unaries.put(id, FiniteUnary.makeAll(entityList));

    /* Deal with ordered entity types */
    if (ordered) {
      addBeforeRelation(typeName, entityList);
      addAfterRelation(typeName, entityList);
      addPrevRelation(typeName, entityList);
      addNextRelation(typeName, entityList);
      addAdjacentRelation(typeName, entityList);
    }
  }

  private void parseRelation(LispTree tree) {
    if (tree.children.size() == 2) {
      parseUnknownRelation(tree);
    } else {
      parseKnownRelation(tree);
    }
  }

  /** Converting between array and a single index */
  private int listToIndex(List<Integer> domainSizes, List<Integer> values) {
    int num = 0;
    for (int i = domainSizes.size() - 1; i >= 0; --i) {
      num = num * domainSizes.get(i) + values.get(i);
    }
    return num;
  }
  private List<Integer> indexToList(List<Integer> domainSizes, int value) {
    List<Integer> answer = new ArrayList<Integer>();
    int num = value;
    for (int i = 0; i < domainSizes.size(); ++i) {
      int curSize = domainSizes.get(i);
      answer.add(num % curSize);
      num /= curSize;
    }
    return answer;
  }

  private void parseUnknownRelation(LispTree tree) {
    String relationName = tree.child(0).value;
    LispTree types = tree.child(1);

    /* Read the list of types used by this relation */
    List<String> typeList = new ArrayList<String>();
    for (LispTree child: types.children) {
      typeList.add(child.value);
    }
    relationTypeMap.put(relationName, typeList);

    /* Map unknown relation name (e.g. "assigned") to binaries */
    for (String type: typeList) {
      NameValue curValue = PuzzleTypeSystem.createBinary(relationName, type);
      Formula curFormula = new ValueFormula<NameValue>(curValue);
      allBinaryFormulas.add(curFormula);
      MapUtils.addToSet(phraseToBinaryFormulas, relationName.toLowerCase(), curFormula);
    }

    /* Store binaries for CSPExecutor */
    int numTypes = typeList.size();
    int relationSize = 1;
    List<List<NameValue>> domains = new ArrayList<List<NameValue>>();
    for (int i = 0; i < numTypes; ++i) {
      String typeName = typeList.get(i);
      List<NameValue> domain = typeToDomain.get(typeName);
      relationSize *= domain.size();
      domains.add(domain);
    }

    /* Create the record NameValues (like rows in a table, one per CSP variable) */
    List<Integer> domainSizes = new ArrayList<Integer>();
    for (int i = 0; i < domains.size(); ++i) {
      domainSizes.add(domains.get(i).size());
    }
    List<NameValue> records = new ArrayList<NameValue>();
    for (int i = 0; i < relationSize; ++i) {
      List<Integer> indices = indexToList(domainSizes, i);
      List<String> curEntityNames = new ArrayList<String>();
      for (int j = 0; j < indices.size(); ++j) {
        curEntityNames.add(domains.get(j).get(indices.get(j)).description.toLowerCase());
      }
      records.add(PuzzleTypeSystem.createRecord(relationName, curEntityNames));
    }

    /* Create the binaries that map between entities and records */
    List<FiniteBinary> curBinaries = new ArrayList<FiniteBinary>();
    for (int i = 0; i < numTypes; ++i) {
      String curType = typeList.get(i);
      List<NameValue> domain = domains.get(i);
      FiniteBinary binary = new FiniteBinary(domain, records);
      String id = PuzzleTypeSystem.createBinaryId(relationName, curType);
      curBinaries.add(binary);
      binaries.put(id, binary);
    }
    for (int i = 0; i < relationSize; ++i) {
      NameValue record = records.get(i);
      List<Integer> indices = indexToList(domainSizes, i);
      for (int j = 0; j < numTypes; ++j) {
        List<NameValue> domain = domains.get(j);
        FiniteBinary binary = curBinaries.get(j);
        int curIndex = indices.get(j);
        Value curValue = domain.get(curIndex);
        binary.put(curValue, record, BooleanExpression.var(record.description));
      }
    }
  }

  private void parseKnownRelation(LispTree tree) {
    String relationName = tree.child(0).value;
    LispTree types = tree.child(1);
    if (types.children.size() > 1) {
      throw new RuntimeException("Not supported: known relations on multiple types.");
    }
    String type = types.head().value;
    LispTree entities = tree.child(2);

    /* Read the list of all entities in this known relation */
    List<NameValue> entityList = new ArrayList<NameValue>();
    for (LispTree child: entities.children) {
      String entity = child.value;
      NameValue value = findNameValue(type, entity);
      if (value == null) {
        throw new RuntimeException("Entity " + entity + " not of required type " + type + ".");
      }
      entityList.add(value);
    }

    /* Map known relation name (e.g. "speaking") to a unary */
    NameValue unaryValue = PuzzleTypeSystem.createUnary(type, relationName);
    Formula formula = new ValueFormula<NameValue>(unaryValue);
    allUnaryFormulas.add(formula);
    MapUtils.addToSet(phraseToUnaryFormulas, relationName.toLowerCase(), formula);

    /* Store unaries for CSPExecutor */
    String id = PuzzleTypeSystem.createUnaryId(type, relationName);
    List<NameValue> domain = typeToDomain.get(type);
    unaries.put(id, FiniteUnary.makeSubset(domain, entityList));
  }

  /** Takes something like (seated, [Person A, Table 1]) and
   * returns the index corresponding to the CSP variable for that assignment */
  public int assignmentToIndex(String relationName, List<String> entityNames) {
    List<String> typeNames = relationTypeMap.get(relationName);
    List<Integer> domainSizes = new ArrayList<Integer>();
    List<Integer> indices = new ArrayList<Integer>();
    for (int i = 0; i < typeNames.size(); ++i) {
      String typeName = typeNames.get(i);
      List<NameValue> domain = typeToDomain.get(typeName);
      domainSizes.add(domain.size());
      int curIndex = -1;
      for (int j = 0; j < domain.size(); ++j) {
        NameValue entity = domain.get(j);
        if (entity.description.equals(entityNames.get(i))) {
          curIndex = j;
          break;
        }
      }
      if (curIndex == -1) {
        throw new RuntimeException(
            "Received out-of-dommain entity " + entityNames.get(i) +
            " for type " + typeName);
      }
      indices.add(curIndex);
    }
    return listToIndex(domainSizes, indices);
  }

  public FiniteUnary getUnary(String id) {
    return unaries.get(id);
  }

  public FiniteBinary getBinary(String id) {
    return binaries.get(id);
  }

  @Override public LispTree toLispTree() {
    return fullTree;
  }

  /** Return all y such that x in firsts and (x,r,y) in graph */
  @Override public List<Value> joinFirst(Value r, Collection<Value> firsts) {
    return joinSecond(getReversedPredicate(r), firsts);
  }

  /** Return all x such that y in seconds and (x,r,y) in graph */
  @Override public List<Value> joinSecond(Value r, Collection<Value> seconds) {
    List<Value> answer = new ArrayList<Value>();
    for (Pair<Value, Value> pair: filterSecond(r, seconds)) {
      answer.add(pair.getFirst());
    }
    return answer;
  }

  /** Return all (x,y) such that x in firsts and (x,r,y) in graph */
  @Override public List<Pair<Value, Value>> filterFirst(Value r, Collection<Value> firsts) {
    return getReversedPairs(filterSecond(getReversedPredicate(r), firsts));
  }

  /** Return all (x,y) such that y in seconds and (x,r,y) in graph */
  @Override public List<Pair<Value, Value>> filterSecond(Value r, Collection<Value> seconds) {
    throw new RuntimeException("Not implemented!");
  }

  /** Return all entities / unaries / binaries that approximately match the given term */
  @Override public Collection<Formula> getFuzzyMatchedFormulas(String term,
      FuzzyMatchFn.FuzzyMatchFnMode mode) {
    Set<Formula> answer = null;
    switch (mode) {
      case ENTITY: answer = phraseToEntityFormulas.get(term); break;
      case UNARY:  answer = phraseToUnaryFormulas.get(term);  break;
      case BINARY: answer = phraseToBinaryFormulas.get(term); break;
      default: throw new RuntimeException("Unknown FuzzyMatchMode " + mode);
    }
    return answer == null ? Collections.emptySet() : answer;
  }

  /** Return all entities / unaries / binaries */
  @Override public Collection<Formula> getAllFormulas(FuzzyMatchFn.FuzzyMatchFnMode mode) {
    switch (mode) {
      case ENTITY: return allEntityFormulas;
      case UNARY:  return allUnaryFormulas;
      case BINARY: return allBinaryFormulas;
      case ORDER_BEFORE: return allBeforeFormulas;
      case ORDER_AFTER: return allAfterFormulas;
      case ORDER_NEXT: return allNextFormulas;
      case ORDER_PREV: return allPrevFormulas;
      case ORDER_ADJACENT: return allAdjacentFormulas;
      default: throw new RuntimeException("Unknown FuzzyMatchMode " + mode);
    }
  }
}
