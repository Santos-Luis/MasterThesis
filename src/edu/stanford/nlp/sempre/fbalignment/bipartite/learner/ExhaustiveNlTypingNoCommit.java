package edu.stanford.nlp.sempre.fbalignment.bipartite.learner;

import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.*;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs exhaustive typing but does not choose a single FB relation for every
 * alignment just keeps all edges where there is some overlap
 *
 * @author jonathanberant
 */
public class ExhaustiveNlTypingNoCommit extends ExhaustiveNlTyping {

  public ExhaustiveNlTypingNoCommit(FourPartiteGraph fourPartiteGraph) throws IOException, ClassNotFoundException {
    super(fourPartiteGraph);
  }

  @Override
  protected void finalizeGraph() {

    for (NlTypedBipartiteNode nlTypedNode : graph.getNlTypedNodes()) {

      List<BipartiteEdge> toDeleteEdges = new LinkedList<BipartiteEdge>();
      List<Triple<BipartiteNode, BipartiteNode, BipartiteEdge>> toAddEdges = new LinkedList<Triple<BipartiteNode, BipartiteNode, BipartiteEdge>>();
      toDeleteEdges.addAll(graph.getGraph().getOutEdges(nlTypedNode));

      for (BipartiteEdge outEdge : graph.getGraph().getOutEdges(nlTypedNode)) {
        double score = scoreNodePair(nlTypedNode, graph.getGraph().getDest(outEdge));
        toAddEdges.add(
            new Triple<BipartiteNode, BipartiteNode, BipartiteEdge>(
                nlTypedNode, graph.getGraph().getDest(outEdge),
                new ScoreEdge(outEdge.value(), score)));

      }

      for (BipartiteEdge toDeleteEdge : toDeleteEdges) {
        graph.getGraph().removeEdge(toDeleteEdge);
      }
      for (Triple<BipartiteNode, BipartiteNode, BipartiteEdge> triple : toAddEdges) {
        graph.addEdge(triple.third, triple.first, triple.second);
      }
    }

  }

}
