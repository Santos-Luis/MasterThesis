package edu.stanford.nlp.sempre.fbalignment.bipartite;

import com.google.common.collect.BiMap;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.sempre.fbalignment.fbgraph.FbEntity;
import edu.stanford.nlp.sempre.fbalignment.fbgraph.FromPartiallyLinkedThroughCvtFbGraphBuilder;
import edu.stanford.nlp.sempre.fbalignment.matchers.Matcher;
import edu.stanford.nlp.sempre.fbalignment.matchers.MatcherFactory;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import edu.stanford.nlp.sempre.freebase.utils.FreebaseUtils;
import edu.stanford.nlp.sempre.freebase.utils.LinkedExtractionFileUtils;
import edu.stanford.nlp.sempre.freebase.utils.ShortContainer;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.SystemUtils;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import fig.basic.LogInfo;
import fig.basic.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class FromFbGraphBuilder implements BipartiteBuilder<BipartiteNode, BipartiteEdge> {

  public static class Options {
    @Option(gloss = "Path to freebase graph file")
    public String fbGraphFilePath;
    @Option(gloss = "path to linked extractions file")
    public String linkedExtractionFilePath;
    @Option(gloss = "Path to datadump file") public String datadumpFilePath;
    @Option(gloss = "Type of matcher to use") public String matcherType;
    @Option(gloss = "Path to property file") public String propertyFilePath;
    @Option(gloss = "Path to output match file")
    public String outputMatchFilePath;
    @Option(gloss = "Path to cvt file") public String cvtFilePath;
    @Option(gloss = "Path to output extraction file with linked arg2")
    public String arg2LinkedExtractionFilePath;

  }

  public static Options opts = new Options();
  private LinkedExtractionFileUtils extractionUtils;
  private Matcher matcher;
  private BiMap<Short, String> propertyId2DescMap;
  private Set<String> cvts;
  private PrintWriter writer;
  private PrintWriter arg2LinkedWriter;


  public FromFbGraphBuilder() throws IOException {
    extractionUtils = new LinkedExtractionFileUtils(opts.linkedExtractionFilePath);
    matcher = MatcherFactory.create(opts.matcherType);
    propertyId2DescMap = FreebaseUtils.loadProperties(opts.propertyFilePath);
    cvts = FileUtils.loadSet(opts.cvtFilePath);
  }


  @Override
  public UndirectedSparseGraph<BipartiteNode, BipartiteEdge> constructBipartiteGraph()
      throws IOException,
             ClassNotFoundException {

    UndirectedSparseGraph<BipartiteNode, BipartiteEdge> res = new UndirectedSparseGraph<BipartiteNode, BipartiteEdge>();

    // 1. Generate graph
    LogInfo.begin_track("Generating graph");
    DirectedSparseMultigraph<FbEntity, ShortContainer> graph = FromPartiallyLinkedThroughCvtFbGraphBuilder.loadGraphFromFile(opts.fbGraphFilePath);
    int memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memory: " + memory);
    LogInfo.end_track();
    System.gc();

    // 2. names for relevant mids
    LogInfo.begin_track("Names for relevant MIDs");
    Map<String, Set<String>> relevantMids = loadRelevantMidNames(graph);
    memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memory: " + memory);
    LogInfo.end_track();
    System.gc();

    // 3. extractions grouped by MIDs
    LogInfo.begin_track("Load extractions");
    Map<String, Map<String, List<String>>> idToArg2ToPredicateList = extractionUtils.getIdToArg2ToPredicateListMap();
    LogInfo.log("Number of IDs: " + idToArg2ToPredicateList.keySet().size());
    memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memory: " + memory);
    LogInfo.end_track();
    System.gc();

    // 4. go over the graph nodes and find relevant matches
    LogInfo.begin_track("Construct bipartite graph");
    int i = 0;
    writer = IOUtils.getPrintWriter(opts.outputMatchFilePath);
    arg2LinkedWriter = IOUtils.getPrintWriter(opts.arg2LinkedExtractionFilePath);
    for (FbEntity fbEntity : graph.getVertices()) {

      // check that this entity is a relevant mid and not a time or a number
      if (relevantMids.get(fbEntity.getId()) == null)
        continue;

      // check if this node is a MID from an extraction
      Map<String, List<String>> arg2ToPredicateList = idToArg2ToPredicateList.get(fbEntity.getId());

      if (arg2ToPredicateList != null) {

        LogInfo.begin_track("Handling node");
        LogInfo.log("Handling entity: " + fbEntity.getId());
        handleFbEntitySuccessors(fbEntity, graph, arg2ToPredicateList, relevantMids, res);
        handleFbEntityPredecessors(fbEntity, graph, arg2ToPredicateList, relevantMids, res);
        LogInfo.end_track();
      }
      if (++i % 1000000 == 0)
        LogInfo.log("Number of vertices: " + i);

    }
    writer.close();
    arg2LinkedWriter.close();
    memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memory: " + memory);
    LogInfo.end_track();
    System.gc();
    return res;
  }

  private void handleFbEntityPredecessors(FbEntity destination, DirectedSparseMultigraph<FbEntity, ShortContainer> graph,
                                          Map<String, List<String>> arg2ToPredicateList, Map<String, Set<String>> relevantMids, UndirectedSparseGraph<BipartiteNode, BipartiteEdge> bipartiteGraph) {

    for (FbEntity predecessor : graph.getPredecessors(destination)) {

      // length 1 paths
      if (fbEntityMatchesArgs2(predecessor, arg2ToPredicateList, relevantMids)) {

        Collection<ShortContainer> edges = graph.findEdgeSet(predecessor, destination);
        for (ShortContainer edge : edges) {
          CompositeFbFact fact = new CompositeFbFact(destination.getId(), predecessor.getId(), propertyId2DescMap.get(edge.value()), true);

          LogInfo.log("handling fact: " + fact);
          handleFact(fact, bipartiteGraph, arg2ToPredicateList, relevantMids);

        }
      }

      if (validBackwardNode(predecessor, graph)) {

        // length 2 paths
        for (FbEntity predecessor2 : graph.getPredecessors(predecessor)) {

          if (!destination.equals(predecessor2) && fbEntityMatchesArgs2(predecessor2, arg2ToPredicateList, relevantMids)) {

            Collection<ShortContainer> edges1 = graph.findEdgeSet(predecessor2, predecessor);
            Collection<ShortContainer> edges2 = graph.findEdgeSet(predecessor, destination);

            for (ShortContainer edge1 : edges1) {
              for (ShortContainer edge2 : edges2) {

                List<String> propertyList = new LinkedList<String>();
                propertyList.add(propertyId2DescMap.get(edge1.value()));
                propertyList.add(propertyId2DescMap.get(edge2.value()));
                CompositeFbFact fact = new CompositeFbFact(destination.getId(), predecessor2.getId(), propertyList, true);
                LogInfo.log("handling fact: " + fact);
                handleFact(fact, bipartiteGraph, arg2ToPredicateList, relevantMids);
              }
            }
          }
        }
      }
    }
  }

  private void handleFbEntitySuccessors(FbEntity source, DirectedSparseMultigraph<FbEntity, ShortContainer> graph,
                                        Map<String, List<String>> arg2ToPredicateList, Map<String, Set<String>> relevantMids, UndirectedSparseGraph<BipartiteNode,
      BipartiteEdge> bipartiteGraph) {

    // generate all FbCompositeFacts and handle each fact
    for (FbEntity successor : graph.getSuccessors(source)) {

      // length 1 paths
      if (fbEntityMatchesArgs2(successor, arg2ToPredicateList, relevantMids)) {

        Collection<ShortContainer> edges = graph.findEdgeSet(source, successor);
        for (ShortContainer edge : edges) {
          CompositeFbFact fact = new CompositeFbFact(source.getId(), successor.getId(), propertyId2DescMap.get(edge.value()));

          LogInfo.log("handling fact: " + fact);
          handleFact(fact, bipartiteGraph, arg2ToPredicateList, relevantMids);

        }
      }

      if (validForwardNode(successor, graph)) {
        // length 2 paths
        for (FbEntity successor2 : graph.getSuccessors(successor)) {

          if (!source.equals(successor2) && fbEntityMatchesArgs2(successor2, arg2ToPredicateList, relevantMids)) {

            Collection<ShortContainer> edges1 = graph.findEdgeSet(source, successor);
            Collection<ShortContainer> edges2 = graph.findEdgeSet(successor, successor2);
            for (ShortContainer edge1 : edges1) {
              for (ShortContainer edge2 : edges2) {

                List<String> propertyList = new LinkedList<String>();
                propertyList.add(propertyId2DescMap.get(edge1.value()));
                propertyList.add(propertyId2DescMap.get(edge2.value()));
                CompositeFbFact fact = new CompositeFbFact(source.getId(), successor2.getId(), propertyList);
                LogInfo.log("handling fact : " + fact);
                handleFact(fact, bipartiteGraph, arg2ToPredicateList, relevantMids);
              }
            }
          }
        }
      }
    }
  }

  private void handleFact(CompositeFbFact fact, UndirectedSparseGraph<BipartiteNode, BipartiteEdge> bipartiteGraph, Map<String,
      List<String>> arg2ToPredicateList, Map<String, Set<String>> relevantMids) {

    if (fact.isReversedFact()) { // for reversed facts
      for (String arg2 : arg2ToPredicateList.keySet()) {
        if (fbEntityMatchesArg2(new FbEntity(fact.getMid2()), arg2, relevantMids)) {

          List<String> nlPredicates = arg2ToPredicateList.get(arg2);
          BipartiteNode fbNode = new FbBipartiteNode(fact.getProperties(), true);

          for (String nlPredicate : nlPredicates) {

            LogInfo.log("Incrementing edge:\t" + nlPredicate + "\t!" + fact.getProperties() + "\t" + fact.getMid1() + "\t" + fact.getMid2());
            writer.println(nlPredicate + "\t!" + fact.getProperties() + "\t" + fact.getMid1() + "\t" + fact.getMid2());
            BipartiteNode nlNode = new NlBipartiteNode(nlPredicate);
            BipartiteEdge edge = bipartiteGraph.findEdge(nlNode, fbNode);
            if (edge == null) {
              edge = new MatchSampleAndCountEdge();
              bipartiteGraph.addEdge(edge, nlNode, fbNode);
            }
            String mid1Desc = relevantMids.get(fact.getMid1()).iterator().next();
            String mid2Desc = LinkedExtractionFileUtils.isTimeArg(arg2) ? fact.getMid2() : relevantMids.get(fact.getMid2()).iterator().next();
            edge.addMatch(new Pair<String, String>(mid1Desc, mid2Desc));
          }
        }
      }
    } else {
      for (String arg2 : arg2ToPredicateList.keySet()) {
        if (fbEntityMatchesArg2(new FbEntity(fact.getMid2()), arg2, relevantMids)) {

          List<String> nlPredicates = arg2ToPredicateList.get(arg2);
          BipartiteNode fbNode = new FbBipartiteNode(fact.getProperties(), false);

          for (String nlPredicate : nlPredicates) {

            LogInfo.log("Incrementing edge:\t" + nlPredicate + "\t" + fact.getProperties() + "\t" + fact.getMid1() + "\t" + fact.getMid2());
            writer.println(nlPredicate + "\t" + fact.getProperties() + "\t" + fact.getMid1() + "\t" + fact.getMid2());
            arg2LinkedWriter.println(fact.getMid1() + "\t" + nlPredicate + "\t" + arg2 + "\t" + fact.getMid2());
            BipartiteNode nlNode = new NlBipartiteNode(nlPredicate);
            BipartiteEdge edge = bipartiteGraph.findEdge(nlNode, fbNode);
            if (edge == null) {
              edge = new MatchSampleAndCountEdge();
              bipartiteGraph.addEdge(edge, nlNode, fbNode);
            }
            String mid1Desc = relevantMids.get(fact.getMid1()).iterator().next();
            String mid2Desc = LinkedExtractionFileUtils.isTimeArg(arg2) ? fact.getMid2() : relevantMids.get(fact.getMid2()).iterator().next();
            edge.addMatch(new Pair<String, String>(mid1Desc, mid2Desc));
          }
        }
      }
    }
  }

  private boolean fbEntityMatchesArgs2(FbEntity entity, Map<String, List<String>> arg2ToPredicateList, Map<String, Set<String>> relevantMids) {

    for (String arg2 : arg2ToPredicateList.keySet()) {
      if (fbEntityMatchesArg2(entity, arg2, relevantMids))
        return true;
    }
    return false;
  }

  private boolean fbEntityMatchesArg2(FbEntity entity, String arg2, Map<String, Set<String>> relevantMids) {

    Set<String> entityNames = new HashSet<String>();
    if (LinkedExtractionFileUtils.isTimeArg(arg2)) {
      entityNames.add(entity.getId());
    } else {
      // LogInfo.log("Matching entity to arg2:" + entity.getId());
      if (relevantMids.containsKey(entity.getId()))
        entityNames.addAll(relevantMids.get(entity.getId()));
    }
    if (entityNames.size() > 0) {
      for (String idName : entityNames) {
        if (matcher.matchArgument2Entity(arg2, idName)) {
          LogInfo.log("matched argument: " + arg2 + " to id " + entity.getId() + " with name " + relevantMids.get(entity.getId()));
          return true;
        }
      }
    }
    return false;
  }

  private Map<String, Set<String>> loadRelevantMidNames(DirectedSparseMultigraph<FbEntity, ShortContainer> graph) throws IOException {

    Map<String, Set<String>> res = new HashMap<String, Set<String>>();
    BufferedReader reader = IOUtils.getBufferedFileReader(opts.datadumpFilePath);
    String line;
    int i = 0;
    LogInfo.log("Going over datadump file");
    while ((line = reader.readLine()) != null) {

      String[] tokens = FreebaseUtils.DELIMITER_PATTERN.split(line.substring(0, line.length() - 1));
      if (tokens.length < 3)
        continue;

      String id = tokens[0];
      String property = tokens[1];
      String arg2 = tokens[2];

      if ((property.equals("fb:type.object.name") || property.equals("fb:common.topic.alias")) &&
          arg2.endsWith("@en")) {

        if (graph.containsVertex(new FbEntity(id))) {
          Set<String> names = res.get(id);
          if (names == null) {
            names = new HashSet<String>();
            res.put(id, names);
          }
          names.add(arg2.substring(1, arg2.lastIndexOf('"')).toLowerCase());
        }
        i++;
        if (i % 1000000 == 0) {
          LogInfo.log("Number of id to names uploaded: " + i + ", id: " + id + ", name: " + arg2);
        }
      }
    }
    reader.close();
    return res;
  }

  private boolean validForwardNode(FbEntity node, DirectedSparseMultigraph<FbEntity, ShortContainer> graph) {
    if (graph.getOutEdges(node).iterator().hasNext())
      return validProperty(graph.getOutEdges(node).iterator().next());
    return false;
  }

  private boolean validBackwardNode(FbEntity node, DirectedSparseMultigraph<FbEntity, ShortContainer> graph) {
    if (graph.getInEdges(node).iterator().hasNext())
      return validProperty(graph.getOutEdges(node).iterator().next());
    return false;
  }


  private boolean validProperty(ShortContainer representativeEdge) {
    String property = propertyId2DescMap.get(representativeEdge.value());
    return cvts.contains(property.subSequence(0, property.lastIndexOf('.')));
  }

}
