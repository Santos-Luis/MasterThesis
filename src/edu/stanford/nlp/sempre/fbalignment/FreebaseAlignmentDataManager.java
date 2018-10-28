package edu.stanford.nlp.sempre.fbalignment;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.fbalignment.bipartite.filters.FbRelationFilter;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode.TypeIdPairs;
import edu.stanford.nlp.sempre.fbalignment.bipartite.test.AlignmentExample;
import edu.stanford.nlp.sempre.fbalignment.fbgraph.FbPropertiesExpectedTypes;
import edu.stanford.nlp.sempre.fbalignment.jung.DirectedSparseGraphVertexAccess;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import edu.stanford.nlp.sempre.freebase.utils.FreebaseUtils;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Sets;
import edu.stanford.nlp.util.SystemUtils;
import fig.basic.LogInfo;
import fig.basic.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

public class FreebaseAlignmentDataManager implements Serializable {

  private static final long serialVersionUID = -5609147303008232223L;

  public static class Options {
    @Option(gloss = "Path to freebase datadump file")
    public String datadumpFilePath;
    @Option(gloss = "Type of edge in the bipartite graph")
    public String edgeType;
    @Option(gloss = "Path to type-to-id file") public String typeToIdFilePath;
    @Option(gloss = "Path to freebase properties expected types file")
    public String fbPropertiesExpectedTypesFilePath;
    @Option(gloss = "Path to match file") public String matchFilePath;
    @Option(gloss = "Path to out object file") public String objectFilePath;
  }

  public static Options opts = new Options();

  private BiMap<String, Integer> fbType2Id;
  private FbPropertiesExpectedTypes fbPropertiesExpectedTypes;
  private Map<Integer, IdInfo> id2IdInfo = new HashMap<Integer, IdInfo>();
  private FourPartiteGraph fourPartiteGraph;

  public void genrateBigraph() throws IOException {
    // 1. upload a one-to-one map that provides an internal ID for every type
    LogInfo.begin_track("Uploading type-to-id-map");
    fbType2Id = FileUtils.loadString2IntegerBiMap(opts.typeToIdFilePath);
    LogInfo.log("Number of types: " + fbType2Id.size());
    logTypeTable();
    LogInfo.end_track();

    // 1a. upload expected types
    fbPropertiesExpectedTypes = new FbPropertiesExpectedTypes(opts.fbPropertiesExpectedTypesFilePath);

    // 2. Create the initial undirected graph and as a side effect create an internal ID (integer) to every external ID (String)
    LogInfo.begin_track("Create bigraph nodes");
    BiMap<String, Integer> idDesc2IdMap = HashBiMap.create();
    DirectedSparseGraphVertexAccess<BipartiteNode, BipartiteEdge> graph = generateInitialBigraph(
        opts.matchFilePath,
        idDesc2IdMap);

    logIdDesc2IdMap(idDesc2IdMap);
    LogInfo.log("Number of IDs: " + idDesc2IdMap.size());
    LogInfo.log("Number of graph nodes: " + graph.getVertexCount());
    LogInfo.end_track();

    // 3. create map from internal ID to mid-info
    LogInfo.begin_track("Uploading id-to-mid-info");
    generateId2MidInfoMap(idDesc2IdMap, opts.datadumpFilePath);
    logIdId2IdInfo();
    LogInfo.end_track();
    idDesc2IdMap.clear();

    // 4. add type information to graph nodes
    LogInfo.begin_track("Adding type information");
    addTypeInformationToGraphNodes(graph);
    logGraph(graph);
    LogInfo.end_track();

    // 5. create bigraph
    fourPartiteGraph = new FourPartiteGraph(graph, fbType2Id, fbPropertiesExpectedTypes);
    LogInfo.log("Memory: " + SystemUtils.getMemoryInUse());
    IOUtils.writeObjectToFile(this, opts.objectFilePath);
  }

  public FourPartiteGraph getFourPartiteGraph() { return fourPartiteGraph; }

  /**
   * Go over a file containing all matches of predicates to relations and create
   * a node for every predicate and every relation with the list of pairs of
   * MIDs. Also assign an internal ID for every mid
   *
   * @throws IOException
   */
  private DirectedSparseGraphVertexAccess<BipartiteNode, BipartiteEdge> generateInitialBigraph(
      String matchFilename, BiMap<String, Integer> idDesc2IdMap) throws IOException {

    DirectedSparseGraphVertexAccess<BipartiteNode, BipartiteEdge> res = new DirectedSparseGraphVertexAccess<BipartiteNode, BipartiteEdge>();
    BufferedReader reader = IOUtils.getBufferedFileReader(matchFilename);
    String line;
    int currId = 1;
    FbRelationFilter filter = new FbRelationFilter();


    while ((line = reader.readLine()) != null) {

      String[] tokens = FreebaseUtils.DELIMITER_PATTERN.split(line);

      String nl = tokens[0];

      if (nl.equals(""))
        continue;

      String fb = tokens[1];
      String id1 = tokens[2];
      String id2 = tokens[3]; // might be a date
      if (FreebaseUtils.isDate(id2)) {
        id2 = "t:" + FreebaseUtils.extractDate(id2);
      }

      LogInfo.log(tokens[1] + "\t" + tokens[2] + "\t" + tokens[3]);

      // add to mid-desc to mid-id map
      if (!idDesc2IdMap.containsKey(id1)) {
        idDesc2IdMap.put(id1, currId++);
      }
      if (!idDesc2IdMap.containsKey(id2)) {
        idDesc2IdMap.put(id2, currId++);
      }
      Pair<Integer, Integer> encodedIdPair = new Pair<Integer, Integer>(idDesc2IdMap.get(id1), idDesc2IdMap.get(id2));

      LogInfo.log(id1 + "\t" + id2 + "\t" + encodedIdPair);

      // update initial graph with nodes and edges
      BipartiteNode nlNode = new NlBipartiteNode(nl);
      FbBipartiteNode fbNode = FbBipartiteNode.fromCompositePredicateDescription(fb);

      if (validFbNode(fbNode, filter)) {

        if (res.containsVertex(nlNode)) {
          nlNode = res.getRealVertex(nlNode);
        }

        if (res.containsVertex(fbNode)) {
          fbNode = (FbBipartiteNode) res.getRealVertex(fbNode);
        }

        BipartiteEdge edge = res.findEdge(nlNode, fbNode);
        if (edge == null) {
          edge = BipartiteEdgeFactory.createEdge(opts.edgeType);
          res.addEdge(edge, nlNode, fbNode);
        }

        edge.addMatch(new Pair<String, String>(id1, id2));
        edu.uci.ics.jung.graph.util.Pair<BipartiteNode> endPoints = res.getEndpoints(edge);
        endPoints.getFirst().addPair(encodedIdPair);
        endPoints.getSecond().addPair(encodedIdPair);

        // JONATHAN - adding this since FB sometimes does not explicitly specify the expected types for MIDs
        Pair<String, String> expectedTypes = fbPropertiesExpectedTypes.getExpectedTypes(fbNode);
        addTypeToId(id1, encodedIdPair.first, expectedTypes.first, fbType2Id);
        addTypeToId(id2, encodedIdPair.second, expectedTypes.second, fbType2Id);
      }
    }
    reader.close();
    return res;
  }

  private void logTypeTable() {
    int i = 0;
    for (Map.Entry<String, Integer> entry : fbType2Id.entrySet()) {
      LogInfo.log(entry.getKey() + "\t" + entry.getValue());
      i++;
      if (i == 20)
        break;
    }
  }

  private void logIdDesc2IdMap(BiMap<String, Integer> idDesc2IdMap) {

    int i = 0;
    for (Map.Entry<String, Integer> entry : idDesc2IdMap.entrySet()) {
      LogInfo.log(entry.getKey() + "\t" + entry.getValue());
      i++;
      if (i == 20)
        break;
    }
  }

  private void logIdId2IdInfo() {
    int i = 0;
    for (Integer id : id2IdInfo.keySet()) {
      LogInfo.log(id + "\t" + id2IdInfo.get(id));
      i++;
      if (i == 20)
        break;
    }
  }

  private void logGraph(
      DirectedSparseGraphVertexAccess<BipartiteNode, BipartiteEdge> graph) {

    int i = 0;
    for (BipartiteNode node : graph.getVertices()) {

      LogInfo.log("Node: " + node);
      for (Pair<Integer, Integer> pair : node.getMidIdPairSet()) {
        LogInfo.log("Pair:\t" + id2IdInfo.get(pair.first) + "\t" + id2IdInfo.get(pair.second));
      }

      for (TypeIdPairs typeIdPairs : node.getArg1TypeMap().values()) {
        LogInfo.log("Arg1 Type:\t" + typeIdPairs);
      }

      for (TypeIdPairs typeIdPairs : node.getArg2TypeMap().values()) {
        LogInfo.log("Arg2 Type:\t" + typeIdPairs);
      }

      i++;
      if (i == 30)
        break;
    }
  }


  /**
   * Fb node is valid if it is composed of valid properties and the types are
   * valid
   */
  private boolean validFbNode(FbBipartiteNode fbNode, FbRelationFilter filter) {

    if (filter.filterNode(fbNode))
      return false;

    Pair<String, String> expectedTypes = fbPropertiesExpectedTypes.getExpectedTypes(fbNode);
    return (isValidType(expectedTypes.first) &&
        isValidType(expectedTypes.second));
  }

  public static boolean isValidType(String type) {
    if (type.equals("fb:type.datetime"))
      return true;
    return !(type.startsWith("fb:base.") || type.startsWith("fb:common.") ||
        type.startsWith("fb:user.") ||
        type.startsWith("fb:dataworld.") ||
        type.startsWith("fb:freebase.") ||
        type.startsWith("fb:type."));
  }

  private void generateId2MidInfoMap(BiMap<String, Integer> idDesc2IdMap, String datadumpFile) throws IOException {


    int i = 0;
    for (String line : IOUtils.readLines(datadumpFile)) {

      String[] tokens = line.substring(0, line.length() - 1).split("\t");
      if (tokens.length != 3)
        continue;

      String id = tokens[0];
      String property = tokens[1];
      String value = tokens[2];

      // we are interested in relevant MIDs and either the name or type property
      if (idDesc2IdMap.containsKey(id) &&
          (property.equals("fb:type.object.name") || property.equals("fb:type.object.type"))) {

        // get the info
        IdInfo idInfo = id2IdInfo.get(idDesc2IdMap.get(id));

        // if no info, generate a new one
        if (idInfo == null) {
          idInfo = new IdInfo(id);
          id2IdInfo.put(idDesc2IdMap.get(id), idInfo);
        }
        // if this is a name in English add it to the info
        if (property.equals("fb:type.object.name") && value.endsWith("@en"))
          idInfo.setName(value.substring(1, value.lastIndexOf('"')));
          // if this is a type, add it to the list of types
        else if (property.equals("fb:type.object.type")) {
          if (isValidType(value)) {
            Integer typeId = fbType2Id.get(value);
            if (typeId == null) {
              throw new NullPointerException("Unable to find type ID for the type " + value);
            }
            idInfo.addType(typeId);
          }
        }
      }
      i++;
      if (i % 1000000 == 0)
        LogInfo.log("Number of lines: " + i);
    }
  }

  private void addTypeToId(String idDesc, Integer encodedId, String typeDesc, BiMap<String, Integer> fbType2Id) {

    if (isValidType(typeDesc)) {

      Integer typeId = fbType2Id.get(typeDesc);

      if (typeId == null) {
        throw new NullPointerException("Unable to find type ID for the type " + typeDesc);
      }
      IdInfo idInfo = id2IdInfo.get(encodedId);
      // if no info, generate a new one
      if (idInfo == null) {
        idInfo = new IdInfo(idDesc);
        id2IdInfo.put(encodedId, idInfo);
      }
      idInfo.addType(typeId);
    }
  }

  private void addTypeInformationToGraphNodes(
      DirectedSparseGraphVertexAccess<BipartiteNode, BipartiteEdge> graph) {

    for (BipartiteNode node : graph.getVertices()) {

      for (Pair<Integer, Integer> pair : node.getMidIdPairSet()) {

        int encodedId1 = pair.first;
        IdInfo idInfo1 = id2IdInfo.get(encodedId1);

        if (idInfo1 == null) {
          LogInfo.log("No id-info for encoded-id: " + encodedId1 + " node description: " + node);
        } else {
          Set<Integer> typeList1 = idInfo1.getTypeSet();
          for (int typeId : typeList1) {
            node.addIdPairToArg1TypeMap(typeId, pair);
          }
        }

        int encodedId2 = pair.second;
        IdInfo idInfo2 = id2IdInfo.get(encodedId2);
        if (idInfo2 == null) {
          LogInfo.log("No id-info for encoded-id: " + encodedId2 + " node description: " + node);
        } else if (idInfo2.getMid().startsWith("t:")) {
          node.addIdPairToArg2TypeMap(fbType2Id.get("fb:type.datetime"), pair);
        } else {
          Set<Integer> typeList2 = idInfo2.getTypeSet();
          for (int typeId : typeList2) {
            node.addIdPairToArg2TypeMap(typeId, pair);
          }
        }
      }
      node.sortTypeMapsBySetSize();
    }
  }

  public void printEdges(String outFile) throws IOException {

    PrintWriter pw = IOUtils.getPrintWriter(outFile);
    pw.println("NL_description\ttype1\ttype2\tFB_desc\tIntersection_size_untyped\tIntersection_size_typed\tNL_typed_size\tFB_typed_size\tNL-size\tscore\tsample");
    for (NlBipartiteNode nlNode : fourPartiteGraph.getNlNodes()) {
      for (BipartiteNode nlNodeSuccessor : fourPartiteGraph.getGraph().getSuccessors(nlNode)) {

        NlTypedBipartiteNode nlTypedNode = (NlTypedBipartiteNode) nlNodeSuccessor;

        for (BipartiteEdge outEdge : fourPartiteGraph.getGraph().getOutEdges(nlTypedNode)) {

          BipartiteNode fbTypedNode = fourPartiteGraph.getGraph().getDest(outEdge);
          pw.println(getEdgeDesc(nlNode, nlTypedNode, fbTypedNode, outEdge));
        }
      }
    }
    pw.close();
  }

  private String getEdgeDesc(NlBipartiteNode nlNode, NlTypedBipartiteNode nlTypedNode, BipartiteNode fbTypedNode, BipartiteEdge outEdge) {

    String nlDesc = nlNode.getDescription();
    String type1Desc = fbType2Id.inverse().get(nlTypedNode.getArg1Type());
    String type2Desc = fbType2Id.inverse().get(nlTypedNode.getArg2Type());
    String fbTypedDesc = fbTypedNode.getDescription();

    int untypedIntersectionSize = Sets.intersection(nlNode.getMidIdPairSet(), fbTypedNode.getMidIdPairSet()).size();
    Set<Pair<Integer, Integer>> typedIntersection = Sets.intersection(nlTypedNode.getMidIdPairSet(), fbTypedNode.getMidIdPairSet());

    int typedIntersectionSize = outEdge.value();
    if (typedIntersection.size() != typedIntersectionSize)
      throw new IllegalStateException("Count on edge is not equal to intersection size for nl node: " + nlTypedNode.toShortString() + " and fb node: " + fbTypedNode.toShortString());
    Set<Pair<String, String>> sample = sampleIntersection(typedIntersection, 3);

    int nlTypedSize = nlTypedNode.getMidIdPairsCount();
    int fbTypedSize = fbTypedNode.getMidIdPairsCount();
    int nlSize = nlNode.getMidIdPairsCount();
    double score = outEdge.score();
    return (nlDesc + "\t" + type1Desc + "\t" + type2Desc + "\t" + fbTypedDesc + "\t" + untypedIntersectionSize + "\t" + typedIntersectionSize + "\t" + nlTypedSize + "\t" + fbTypedSize + "\t" + nlSize + "\t" + score + "\t" + sample);
  }

  private Set<Pair<String, String>> sampleIntersection(
      Set<Pair<Integer, Integer>> typedIntersection, int sampleSize) {

    Set<Pair<String, String>> res = new HashSet<Pair<String, String>>();
    int i = 0;
    for (Pair<Integer, Integer> pair : typedIntersection) {
      res.add(new Pair<String, String>(id2IdInfo.get(pair.first).getName(), id2IdInfo.get(pair.second).getName()));
      i++;
      if (i == sampleSize)
        break;
    }
    return res;
  }

  public double scoreAlignmentExample(AlignmentExample example) {

    int type1 = fbType2Id.get(example.getType1Desc());
    int type2 = fbType2Id.get(example.getType2Desc());

    NlTypedBipartiteNode nlTypedNode = new NlTypedBipartiteNode(example.getNlNodeDesc(), type1, type2);
    FbTypedBipartiteNode fbTypedNode = new FbTypedBipartiteNode(FbBipartiteNode.fromCompositePredicateDescription(example.getFbNodeDesc()), type1, type2);
    BipartiteEdge edge = fourPartiteGraph.getGraph().findEdge(nlTypedNode, fbTypedNode);

    if (edge == null)
      return Integer.MIN_VALUE;
    return edge.score();
  }

  public boolean matchAlignmentExample(AlignmentExample example) {

    int type1 = fbType2Id.get(example.getType1Desc());
    int type2 = fbType2Id.get(example.getType2Desc());

    NlTypedBipartiteNode nlTypedNode = new NlTypedBipartiteNode(example.getNlNodeDesc(), type1, type2);
    FbTypedBipartiteNode fbTypedNode = new FbTypedBipartiteNode(FbBipartiteNode.fromCompositePredicateDescription(example.getFbNodeDesc()), type1, type2);
    BipartiteEdge edge = fourPartiteGraph.getGraph().findEdge(nlTypedNode, fbTypedNode);
    return edge != null;
  }

  public static class IdInfo implements Serializable {

    private static final long serialVersionUID = -8469683207720593086L;
    private String id;
    private String name;
    private Set<Integer> typeSet;

    public IdInfo(String id) {
      this.id = id;
      typeSet = new TreeSet<Integer>();
      // all MIDs are of type 0 which is /type/object
      typeSet.add(0);
    }

    public void setName(String name) {
      this.name = name;
    }

    public void addType(int typeId) {
      typeSet.add(typeId);
    }

    public String getMid() { return id; }
    public String getName() { return name; }
    public Set<Integer> getTypeSet() { return typeSet; }

    public String toString() {
      return id + "\t" + name + "\t" + typeSet;
    }
  }
}
