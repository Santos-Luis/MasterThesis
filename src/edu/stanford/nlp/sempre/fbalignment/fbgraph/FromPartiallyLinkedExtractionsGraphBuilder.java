package edu.stanford.nlp.sempre.fbalignment.fbgraph;

import com.google.common.collect.BiMap;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.utils.FreebaseUtils;
import edu.stanford.nlp.sempre.freebase.utils.LinkedExtractionFileUtils;
import edu.stanford.nlp.sempre.freebase.utils.ShortContainer;
import edu.stanford.nlp.util.SystemUtils;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import fig.basic.LogInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Creates a Freebase graph from an extraction file where arg1 is linked to a
 * MID. This is done by performing two steps of BFS from the MIDs that appear in
 * the extraction file
 *
 * @author jonathanberant
 */
public class FromPartiallyLinkedExtractionsGraphBuilder implements FbGraphBuilder<FbEntity, ShortContainer> {

  private LinkedExtractionFileUtils extractionUtils;
  private String datadumpFile;
  // private String mid2NameFile;
  private String propertyFile;


  public FromPartiallyLinkedExtractionsGraphBuilder(Properties props) {
    extractionUtils = new LinkedExtractionFileUtils(props.getProperty("extraction-file"));
    datadumpFile = props.getProperty("datadump-file");
    //  mid2NameFile = props.getProperty("mid-to-name-file");
    propertyFile = props.getProperty("property-file");

  }
  public FromPartiallyLinkedExtractionsGraphBuilder(String extractionFile, String datadumpFile, String propertyFile) {
    extractionUtils = new LinkedExtractionFileUtils(extractionFile);
    datadumpFile = datadumpFile;
    // mid2NameFile = mid2NameFile;
    propertyFile = propertyFile;

  }


  @Override
  public DirectedSparseMultigraph<FbEntity, ShortContainer> constructFbGraph() throws IOException {

    DirectedSparseMultigraph<FbEntity, ShortContainer> res = new DirectedSparseMultigraph<FbEntity, ShortContainer>();
    // 1. upload mid2NameMap
    /*  LogInfo.begin_track("Loading MID-to-name map");
    Map<String, String> mid2NameMap = FreebaseUtils.loadMid2NameMap(mid2NameFile);
    int memory = SystemUtils.getMemoryInUse();
    LogInfo.log(Redwood.DBG,"Memroy: " + memory);
    LogInfo.end_track("Loading MID-to-name map");*/

    // 2. construct extraction set
    LogInfo.begin_track("Loading mid set");
    Set<String> extractionMids = extractionUtils.getLinkedIdSet();
    LogInfo.log("Number of MIDs in extraction file: " + extractionMids.size());
    int memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memroy: " + memory);
    LogInfo.end_track("Loading mid set");

    // 3. upload property file
    BiMap<Short, String> propertyMap = FreebaseUtils.loadProperties(propertyFile);

    // 4. go over dumpfile for the first time
    LogInfo.begin_track("First pass");
    Set<String> newMids = addEdgesInGraphForMids(extractionMids, res, propertyMap);
    LogInfo.log("Number of new MIDs: " + newMids.size());
    LogInfo.log("Number of nodes: " + res.getVertexCount());
    LogInfo.log("Number of edges: " + res.getEdgeCount());
    memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memroy: " + memory);
    LogInfo.end_track("First pass");

    // 5. go over dumpfile for the second time
    LogInfo.begin_track("Second pass");
    addEdgesInGraphForMids(newMids, res, propertyMap);
    LogInfo.log("Number of nodes: " + res.getVertexCount());
    LogInfo.log("Number of edges: " + res.getEdgeCount());
    memory = SystemUtils.getMemoryInUse();
    LogInfo.log("Memroy: " + memory);
    LogInfo.end_track("Second pass");

    return res;
  }


  private Set<String> addEdgesInGraphForMids(Set<String> currMids,
                                             DirectedSparseMultigraph<FbEntity, ShortContainer> oGraph,
                                             BiMap<Short, String> propertyMap) throws IOException {

    Set<String> newMids = new HashSet<String>();
    BufferedReader reader = IOUtils.getBufferedFileReader(datadumpFile);
    String line;
    int i = 0;
    while ((line = reader.readLine()) != null) {

      String mid1 = FreebaseUtils.getNoPrefixMid(line);

      if (currMids.contains(mid1) && FreebaseUtils.isValidPropertyLineWithDate(line)) {

        FbEntity source = new FbEntity(mid1);
        String arg2 = getArg2(line);
        if (arg2.equals(""))
          continue;
        String mid2 = chopMidPrefix(arg2);
        FbEntity dest = new FbEntity(mid2);
        int propertyIndex = propertyMap.inverse().get(FreebaseUtils.getProperty(line)).intValue();
        short edgeId = new Integer(propertyIndex).shortValue();
        ShortContainer edge = new ShortContainer(edgeId);

        if (!mid1.equals(mid2)) { // no need to insert loops into the graph
          oGraph.addEdge(edge, source, dest);
        }

        if (!currMids.contains(mid2) && FreebaseUtils.isMid(arg2)) {
          newMids.add(mid2);
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
    reader.close();
    return newMids;
  }

  private String chopMidPrefix(String mid) {
    if (mid.startsWith(FreebaseUtils.MID_PREFIX))
      return mid.substring(3);
    return mid;
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

  private String getArg2(String fbLine) {

    String value = FreebaseUtils.getValue(fbLine);
    if (FreebaseUtils.isMid(value))
      return chopMidPrefix(value);
    else {
      String[] fbLineTokens = fbLine.split("\t");
      if (fbLineTokens.length > 3 && FreebaseUtils.isDate(fbLineTokens[3]))
        return FreebaseUtils.getDateValue(fbLine);
    }
    return "";
  }

}
