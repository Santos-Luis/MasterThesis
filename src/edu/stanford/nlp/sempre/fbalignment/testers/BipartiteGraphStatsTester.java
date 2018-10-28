package edu.stanford.nlp.sempre.fbalignment.testers;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.logging.Redwood;
import edu.stanford.nlp.util.logging.StanfordRedwoodConfiguration;
import edu.uci.ics.jung.graph.util.Pair;
import fig.basic.LogInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

public final class BipartiteGraphStatsTester {
  private BipartiteGraphStatsTester() { }

  public static void main(String[] args) throws IOException, ClassNotFoundException {

    generalStats(args);
  }

  public static void generalStats(String[] args) throws IOException, ClassNotFoundException {
    Properties props = StringUtils.argsToProperties(args);
    StanfordRedwoodConfiguration.apply(props);
    LogInfo.begin_track("main");
    BipartiteGraph bigraph = new BipartiteGraph(props.getProperty("bigraph-file"));
    LogInfo.log("Number of natural language nodes: " + bigraph.getNlNodeCount());
    LogInfo.log("Number of Freebase nodes: " + bigraph.getFbNodeCount());
    LogInfo.log("Natural language nodes degree distribution");
    bigraph.getNlDegreeDistribution().prettyLog(new Redwood.RedwoodChannels(), "Natural language degree distribution");
    LogInfo.log("Freebase nodes degree distribution");
    bigraph.getFbDegreeDistribution().prettyLog(new Redwood.RedwoodChannels(), "Freebase degree distribution");
    LogInfo.log("Total number of matches from predicates to relations: " + bigraph.totalMatchesCount());

    Collection<NlBipartiteNode> nlSamples = bigraph.sampleNlNodes(50);
    for (NlBipartiteNode nlSample : nlSamples) {

      Collection<BipartiteEdge> outEdges = bigraph.getGraph().getOutEdges(nlSample);
      for (BipartiteEdge outEdge : outEdges) {

        Pair<BipartiteNode> pair = bigraph.getGraph().getEndpoints(outEdge);
        LogInfo.log(pair.getFirst().getDescription() + "\t" + pair.getSecond() + "\t" + outEdge);
      }

    }

    Collection<FbBipartiteNode> fbSamples = bigraph.sampleFbNodes(50);
    for (FbBipartiteNode fbSample : fbSamples) {

      Collection<BipartiteEdge> outEdges = bigraph.getGraph().getOutEdges(fbSample);
      for (BipartiteEdge outEdge : outEdges) {

        Pair<BipartiteNode> pair = bigraph.getGraph().getEndpoints(outEdge);
        LogInfo.log(pair.getFirst().getDescription() + "\t" + pair.getSecond() + "\t" + outEdge);
      }

    }
    LogInfo.end_track("main");
  }

  public static void logEdgesFromStrength(String[] args) throws IOException, ClassNotFoundException {
    Properties props = StringUtils.argsToProperties(args);
    StanfordRedwoodConfiguration.apply(props);
    LogInfo.begin_track("main");
    BipartiteGraph bigraph = new BipartiteGraph(props.getProperty("bigraph-file"));
    bigraph.logEdgesByCount(2);
    LogInfo.end_track("main");
  }

}
