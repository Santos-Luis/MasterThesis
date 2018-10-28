package edu.stanford.nlp.sempre.fbalignment.testers;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.fbalignment.FreebaseAlignmentDataManager;
import edu.stanford.nlp.sempre.fbalignment.bipartite.filters.NodeFrequencyOrFbRelationFilter;
import edu.stanford.nlp.sempre.fbalignment.bipartite.learner.AlignmentLearner;
import edu.stanford.nlp.sempre.fbalignment.bipartite.learner.AlignmentLearnerFactory;
import fig.basic.Option;

import java.io.IOException;

public class FourPartiteLearnerTester {

  public static class Options {
    @Option(gloss = "Path to out object file") public String objectFilePath;
    @Option(gloss = "Path to alignment file") public String alignmentFilePath;
  }

  public static Options opts = new Options();

  public void learnAndPrintEdges() throws IOException, ClassNotFoundException {
    FreebaseAlignmentDataManager fadm = IOUtils.readObjectFromFile(opts.objectFilePath);
    fadm.getFourPartiteGraph().filterNodes(new NodeFrequencyOrFbRelationFilter());
    AlignmentLearner learner = AlignmentLearnerFactory.createLearner(fadm.getFourPartiteGraph());
    learner.learn();
    fadm.printEdges(opts.alignmentFilePath);
  }
}
