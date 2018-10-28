package edu.stanford.nlp.sempre.fbalignment;

import edu.stanford.nlp.sempre.fbalignment.bipartite.BipartiteBuilder;
import edu.stanford.nlp.sempre.fbalignment.bipartite.FromFbGraphBuilder;
import edu.stanford.nlp.sempre.fbalignment.bipartite.learner.AlignmentLearnerFactory;
import edu.stanford.nlp.sempre.fbalignment.bipartite.learner.NlIterLearner;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteEdge;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.scorers.ScorerFactory;
import edu.stanford.nlp.sempre.fbalignment.fbgraph.FbEntity;
import edu.stanford.nlp.sempre.fbalignment.fbgraph.FbGraphBuilder;
import edu.stanford.nlp.sempre.fbalignment.fbgraph.FromPartiallyLinkedThroughCvtFbGraphBuilder;
import edu.stanford.nlp.sempre.fbalignment.scripts.AddAlignmentScoresToPropertyInfoFile;
import edu.stanford.nlp.sempre.fbalignment.testers.FourPartiteLearnerTester;
import edu.stanford.nlp.sempre.freebase.utils.ShortContainer;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import fig.basic.LogInfo;
import fig.basic.Option;
import fig.exec.Execution;

import java.io.IOException;

public class BinaryLexiconConstructionMain implements Runnable {

  @Option(gloss = "Path to output freebase graph file")
  public String outFbGraphFile;

  @Override
  public void run() {

    try {

      LogInfo.begin_track("Generating FB graph");
      FbGraphBuilder<FbEntity, ShortContainer> graphBuilder = new FromPartiallyLinkedThroughCvtFbGraphBuilder();
      DirectedSparseMultigraph<FbEntity, ShortContainer> fbGraph = graphBuilder.constructFbGraph();
      graphBuilder.saveGraph(fbGraph, outFbGraphFile);
      System.gc();
      LogInfo.end_track();


      LogInfo.begin_track("Match text and freebase");
      BipartiteBuilder<BipartiteNode, BipartiteEdge> bigraphBuilder = new FromFbGraphBuilder();
      bigraphBuilder.constructBipartiteGraph();
      System.gc();
      LogInfo.end_track();

      LogInfo.begin_track("Generate bipartite graph");
      FreebaseAlignmentDataManager fadm = new FreebaseAlignmentDataManager();
      fadm.genrateBigraph();
      System.gc();
      LogInfo.end_track("");

      LogInfo.begin_track("Aligning typed NL phrases");
      FourPartiteLearnerTester fplt = new FourPartiteLearnerTester();
      fplt.learnAndPrintEdges();
      System.gc();
      LogInfo.end_track("");

      LogInfo.begin_track("Unifying string match file and alignment file");
      AddAlignmentScoresToPropertyInfoFile unifier = new AddAlignmentScoresToPropertyInfoFile();
      unifier.run();
      LogInfo.end_track("");


    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws IOException {

    Execution.run(
        args,
        "BinaryLexiconConstructionMain", new BinaryLexiconConstructionMain(),
        "FromPartiallyLinkedThroughCvtFbGraphBuilder", FromPartiallyLinkedThroughCvtFbGraphBuilder.opts,
        "FromFbGraphBuilder", FromFbGraphBuilder.opts,
        "FreebaseAlignmentDataManager", FreebaseAlignmentDataManager.opts,
        "FourPartiteLearnerTester", FourPartiteLearnerTester.opts,
        "NlIterLearner", NlIterLearner.opts,
        "AlignmentLearnerFactory", AlignmentLearnerFactory.opts,
        "ScorerFactory", ScorerFactory.opts,
        "AddAlignmentScoresToPropertyInfoFile", AddAlignmentScoresToPropertyInfoFile.opts);
  }
}
