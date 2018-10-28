package edu.stanford.nlp.sempre.fbalignment.fbgraph;

import com.google.common.collect.BiMap;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FileUtils;
import edu.stanford.nlp.sempre.freebase.utils.FreebaseUtils;
import edu.stanford.nlp.sempre.freebase.utils.LinkedExtractionFileUtils;
import edu.stanford.nlp.sempre.freebase.utils.ShortContainer;
import edu.stanford.nlp.util.SystemUtils;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import fig.basic.LogInfo;
import fig.basic.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Takes text tuples where arg1 is linked to a freebase ID and generates an Fb
 * graph. We would like to keep the FB graph small so add edges to the graph by
 * starting a walk from the IDs that are in the text tuples and (1) the edge is
 * a legal property (2) we add length 2 paths only for things that pass through
 * a CVT (otherwise, only length 1) (3) we go from every ID that is in the
 * linked files both to predecessors and successors so that we are able to match
 * both <i>(id1, pred, id2)</i> and <i>id2, pred, id1</i>
 *
 * @author jonathanberant
 */
public class FromPartiallyLinkedThroughCvtFbGraphBuilder implements FbGraphBuilder<FbEntity, ShortContainer> {

  public static class Options {
    @Option(gloss = "Path to linked extraction file")
    public String extractionFilePath;
    @Option(gloss = "Path to freebase datadump file")
    public String datadumpFilePath;
    @Option(gloss = "Path to valid properties file")
    public String validPropertiesFilePath;
    @Option(gloss = "Path to CVT file") public String cvtFilePath;
  }

  public static Options opts = new Options();

  private LinkedExtractionFileUtils extractionUtils;
  private Set<String> forwardIds;
  private Set<String> backwardIds;

  public FromPartiallyLinkedThroughCvtFbGraphBuilder() throws IOException {
    extractionUtils = new LinkedExtractionFileUtils(opts.extractionFilePath);
    LogInfo.begin_track("Loading id set");
    forwardIds = extractionUtils.getLinkedIdSet();
    LogInfo.log("Number of IDs in extraction file: " + forwardIds.size());
    int memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memroy: " + memory);
    LogInfo.end_track();

  }

  @Override
  public DirectedSparseMultigraph<FbEntity, ShortContainer> constructFbGraph() throws IOException {

    DirectedSparseMultigraph<FbEntity, ShortContainer> res = new DirectedSparseMultigraph<FbEntity, ShortContainer>();

    LogInfo.begin_track("Loading cvt set");
    Set<String> cvts = FileUtils.loadSet(opts.cvtFilePath);
    LogInfo.log("Number of cvts: " + cvts.size());
    int memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memroy: " + memory);
    LogInfo.end_track();

    // 3. upload property file
    BiMap<Short, String> propertyMap = FreebaseUtils.loadProperties(opts.validPropertiesFilePath);

    // 4. go over dumpfile for the first time
    LogInfo.begin_track("First pass");
    addEdgesForSeedIds(forwardIds, res, propertyMap, cvts);
    LogInfo.log("Number of forward IDs: " + forwardIds.size());
    LogInfo.log("Number of backward IDs: " + backwardIds.size());
    LogInfo.log("Number of nodes: " + res.getVertexCount());
    LogInfo.log("Number of edges: " + res.getEdgeCount());
    memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memroy: " + memory);
    LogInfo.end_track();

    // 5. go over dumpfile for the second time
    LogInfo.begin_track("Second pass");
    addEdgesInGraphForIds(forwardIds, backwardIds, res, propertyMap, cvts);
    LogInfo.log("Number of nodes: " + res.getVertexCount());
    LogInfo.log("Number of edges: " + res.getEdgeCount());
    memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memroy: " + memory);
    LogInfo.end_track();

    return res;
  }

  private void addEdgesInGraphForIds(Set<String> forwardIds,
                                     Set<String> backwardIds,
                                     DirectedSparseMultigraph<FbEntity, ShortContainer> oGraph,
                                     BiMap<Short, String> propertyMap, Set<String> cvts) {

    int i = 0;
    for (String line : IOUtils.readLines(opts.datadumpFilePath)) {

      String[] tokens = line.split("\t");
      if (tokens.length != 3)
        continue;

      String property = tokens[1];
      if (propertyMap.inverse().containsKey(property)) { // check property is valid
        String id1 = tokens[0];
        String id2 = getArg2(tokens[2]);

        if (forwardIds.contains(id1)) {
          if (cvts.contains(extractTypeFromProperty(property))) {
            FbEntity source = new FbEntity(id1);
            FbEntity dest = new FbEntity(id2);
            ShortContainer edge = new ShortContainer(new Integer(propertyMap.inverse().get(property).intValue()).shortValue());

            if (!id1.equals(id2)) {
              if (!containsEdge(oGraph, source, dest, edge)) { // no need to insert loops into the graph or the same edge twice
                oGraph.addEdge(edge, source, dest);
              }
            }
          }
        }
        if (backwardIds.contains(id2)) {
          FbEntity source = new FbEntity(id1);
          FbEntity dest = new FbEntity(id2);
          ShortContainer edge = new ShortContainer(new Integer(propertyMap.inverse().get(property).intValue()).shortValue());
          if (!id1.equals(id2)) {
            if (!containsEdge(oGraph, source, dest, edge)) { // no need to insert loops into the graph or the same edge twice
              oGraph.addEdge(edge, source, dest);
            }
          }
        }
      }
      if (++i % 1000000 == 0) {
        LogInfo.log("Number of lines: " + i);
        if (i % 10000000 == 0) {
          int memory = SystemUtils.getMemoryInUse();
          LogInfo.log("Memroy: " + memory);
          LogInfo.log("Number of nodes: " + oGraph.getVertexCount());
          LogInfo.log("Number of edges: " + oGraph.getEdgeCount());
        }
      }
    }
  }


  private boolean containsEdge(
      DirectedSparseMultigraph<FbEntity, ShortContainer> oGraph,
      FbEntity source, FbEntity dest, ShortContainer edge) {

    if (oGraph.containsVertex(source) && oGraph.containsVertex(dest)) {
      Collection<ShortContainer> inGraphEdges = oGraph.findEdgeSet(source, dest);
      for (ShortContainer inGraphEdge : inGraphEdges) {
        if (inGraphEdge.value() == edge.value()) {
          return true;
        }
      }
    }
    return false;
  }

  private void addEdgesForSeedIds(Set<String> currIds,
                                  DirectedSparseMultigraph<FbEntity, ShortContainer> oGraph,
                                  BiMap<Short, String> propertyMap, Set<String> cvts) {

    Set<String> newForwardIds = new HashSet<String>();
    Set<String> newBackwardIds = new HashSet<String>();

    int i = 0;
    for (String line : IOUtils.readLines(opts.datadumpFilePath)) {

      String[] tokens = line.split("\t");
      if (tokens.length != 3)
        continue;
      String property = tokens[1];

      if (propertyMap.inverse().containsKey(property)) { // check property is valid
        String id1 = tokens[0];
        String id2 = getArg2(tokens[2]);
        if (currIds.contains(id1) || currIds.contains(id2)) {
          // add to the graph
          FbEntity source = new FbEntity(id1);
          FbEntity dest = new FbEntity(id2);
          ShortContainer edge = new ShortContainer(new Integer(propertyMap.inverse().get(property).intValue()).shortValue());
          if (!id1.equals(id2)) { // no need to insert loops into the graph
            oGraph.addEdge(edge, source, dest);
          }

          if (!currIds.contains(id2)) { // so id1 is contained and id2 is not
            newForwardIds.add(id2);
          }
          if (!currIds.contains(id1)) { // so id2 is contained and is1 is not
            if (cvts.contains(extractTypeFromProperty(property)))
              newBackwardIds.add(id1);
          }
        }
      }
      if (++i % 1000000 == 0) {
        LogInfo.log("Number of lines: " + i);
        if (i % 10000000 == 0) {
          int memory = SystemUtils.getMemoryInUse();
          LogInfo.log("Memroy: " + memory);
          LogInfo.log("Number of nodes: " + oGraph.getVertexCount());
          LogInfo.log("Number of edges: " + oGraph.getEdgeCount());
        }
      }
    }
    forwardIds = newForwardIds;
    backwardIds = newBackwardIds;
  }

  /** handle both IDs and dates as arg2 */
  private String getArg2(String str) {
    if (str.endsWith("xsd:datetime."))
      return str.substring(str.indexOf('"') + 1, str.lastIndexOf('"'));
    return str.substring(0, str.length() - 1); // remove final period
  }

  @Override
  public void saveGraph(DirectedSparseMultigraph<FbEntity, ShortContainer> graph, String graphFile) throws IOException {

    LogInfo.log("writing to file...");
    PrintWriter pw = IOUtils.getPrintWriter(graphFile);
    int i = 0;
    for (ShortContainer edge : graph.getEdges()) {
      if (++i % 1000000 == 0)
        LogInfo.log("Number of edges written: " + i);
      pw.println(graph.getSource(edge) + "\t" + edge.value() + "\t" + graph.getDest(edge));
    }
    pw.close();
  }

  public static DirectedSparseMultigraph<FbEntity, ShortContainer> loadGraphFromFile(String graphFile) throws IOException {

    DirectedSparseMultigraph<FbEntity, ShortContainer> graph = new DirectedSparseMultigraph<FbEntity, ShortContainer>();
    BufferedReader reader = IOUtils.getBufferedFileReader(graphFile);
    String line;
    int i = 0;
    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split("\t");
      FbEntity source = new FbEntity(tokens[0]);
      FbEntity destination = new FbEntity(tokens[2]);
      ShortContainer edge = new ShortContainer(Short.parseShort(tokens[1]));
      graph.addEdge(edge, source, destination);
      if (++i % 1000000 == 0)
        LogInfo.log("Number of edges read: " + i);
    }
    reader.close();
    LogInfo.log("Memory in use: " + SystemUtils.getMemoryInUse());
    return graph;
  }

  public String extractTypeFromProperty(String property) {
    return property.substring(0, property.lastIndexOf('.'));
  }
}
