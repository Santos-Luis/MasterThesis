package edu.stanford.nlp.sempre.fbalignment.bipartite.scorers;

import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.sempre.fbalignment.bipartite.classify.DatumGenerator;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;

import java.io.IOException;

public class MaxentScorer implements NodePairScorer {

  LinearClassifier<Boolean, String> classifier;

  public MaxentScorer(String classifierFile) throws IOException, ClassNotFoundException {
    classifier = IOUtils.readObjectFromFile(classifierFile);
  }

  @Override
  public double scoreNodePair(FourPartiteGraph graph, BipartiteNode nlTypedNode, BipartiteNode fbTypedNode) {

    Datum<Boolean, String> example = DatumGenerator.nodePairToDatum(graph, nlTypedNode, fbTypedNode);
    return classifier.scoreOf(example, true);
  }


}
