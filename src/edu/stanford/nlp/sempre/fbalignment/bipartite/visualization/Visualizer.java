package edu.stanford.nlp.sempre.fbalignment.bipartite.visualization;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteEdge;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.NlBipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.UnlabeledEdge;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public final class Visualizer {
  private Visualizer() { }

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    graphFromUnaryLexicon(args);
  }

  public static void graphFileFromObject(String[] args) throws IOException, ClassNotFoundException {
    PrintWriter pw = IOUtils.getPrintWriter(args[1]);
    UndirectedSparseGraph<BipartiteNode, BipartiteEdge> graph = IOUtils.readObjectFromFile(args[0]);
    Map<String, Integer> desc2IdMap = new HashMap<String, Integer>();

    int currId = 0;

    pw.println("Graph G { ");
    for (BipartiteEdge edge : graph.getEdges()) {
      if (edge.value() >= 10) {
        Pair<BipartiteNode> pair = graph.getEndpoints(edge);

        Integer firstId = desc2IdMap.get(pair.getFirst().getDescription());
        Integer secondId = desc2IdMap.get(pair.getSecond().getDescription());
        if (firstId == null) {
          firstId = ++currId;
          desc2IdMap.put(pair.getFirst().getDescription(), firstId);
        }
        if (secondId == null) {
          secondId = ++currId;
          desc2IdMap.put(pair.getSecond().getDescription(), secondId);
        }

        pw.print(firstId);
        pw.print(" -- ");
        pw.println(secondId);
      }
    }
    pw.println("}");
    pw.close();
  }

  public static <V, E> void visualizeUndirectedGraph(UndirectedSparseGraph<V, E> undirectedGraph, PrintWriter writer) throws IOException {

    writer.println("Graph G { ");
    writer.println("rankdir=LR;");
    for (E edge : undirectedGraph.getEdges()) {

      Pair<V> pair = undirectedGraph.getEndpoints(edge);

      writer.print("\t" + ridNonDotCharacters(pair.getFirst().toString()));
      writer.print(" -- ");
      writer.println(ridNonDotCharacters(pair.getSecond().toString()) + " [label=\"" + edge.toString() + "\"];");
    }
    writer.println("}");
  }

  public static void graphFromBinaryLexicon(String[] args) throws IOException {

    PrintWriter pw = IOUtils.getPrintWriter(args[1]);
    UndirectedSparseGraph<BipartiteNode, BipartiteEdge> graph = new UndirectedSparseGraph<BipartiteNode, BipartiteEdge>();
    Map<String, Integer> desc2IdMap = new HashMap<String, Integer>();

    int i = 0;
    int currId = 0;

    for (String line : IOUtils.readLines(args[0])) {

      if (i++ == 0) continue;

      String[] tokens = line.split("\t");

      String nlDesc = ridNonDotCharacters(tokens[0] + " " + tokens[1].substring(3) + " " + tokens[2].substring(3));
      String fbDesc = ridNonDotCharacters(tokens[3]);
      // System.out.println(nlDesc+"\t"+fbDesc);
      if (desc2IdMap.get(nlDesc) == null) {
        desc2IdMap.put(nlDesc, ++currId);
      }
      if (desc2IdMap.get(fbDesc) == null) {
        desc2IdMap.put(fbDesc, ++currId);
      }

      BipartiteNode nlNode = new NlBipartiteNode(nlDesc);
      BipartiteNode fbNode = new NlBipartiteNode(fbDesc);
      BipartiteEdge edge = new UnlabeledEdge();
      double score = Double.parseDouble(tokens[9]);
      if (score >= 0.01)
        graph.addEdge(edge, nlNode, fbNode);
    }

    pw.println("Graph G { ");
    for (BipartiteEdge edge : graph.getEdges()) {
      Pair<BipartiteNode> pair = graph.getEndpoints(edge);

      String firstId = pair.getFirst().getDescription();
      String secondId = pair.getSecond().getDescription();
      pw.print(firstId);
      pw.print(" -- ");
      pw.println(secondId);
    }

    pw.println("}");
    pw.close();
  }

  public static void graphFromUnaryLexicon(String[] args) throws IOException {

    PrintWriter pw = IOUtils.getPrintWriter(args[1]);
    UndirectedSparseGraph<BipartiteNode, BipartiteEdge> graph = new UndirectedSparseGraph<BipartiteNode, BipartiteEdge>();
    Map<String, Integer> desc2IdMap = new HashMap<String, Integer>();

    int i = 0;
    int currId = 0;

    for (String line : IOUtils.readLines(args[0])) {

      if (i++ == 0) continue;

      String[] tokens = line.split("\t");

      String nlDesc = ridNonDotCharacters(tokens[0]);
      String fbDesc = ridNonDotCharacters(tokens[1]);
      if (desc2IdMap.get(nlDesc) == null) {
        desc2IdMap.put(nlDesc, ++currId);
      }
      if (desc2IdMap.get(fbDesc) == null) {
        desc2IdMap.put(fbDesc, ++currId);
      }

      BipartiteNode nlNode = new NlBipartiteNode(nlDesc);
      BipartiteNode fbNode = new NlBipartiteNode(fbDesc);
      BipartiteEdge edge = new UnlabeledEdge();
      double score = Double.parseDouble(tokens[5]);
      if (score >= 0.01) {
        graph.addEdge(edge, nlNode, fbNode);
      }
    }

    int j = 0;
    pw.println("Graph G { ");
    for (BipartiteEdge edge : graph.getEdges()) {
      j++;
      if (j % 100 == 0)
        System.out.println(j);
      Pair<BipartiteNode> pair = graph.getEndpoints(edge);

      String firstId = pair.getFirst().getDescription();
      String secondId = pair.getSecond().getDescription();
      pw.print(firstId);
      pw.print(" -- ");
      pw.println(secondId);
    }

    pw.println("}");
    pw.close();
  }

  public static String ridNonDotCharacters(String str) {

    String res = str.replace(".", " ");
    res = res.replace("?", "");
    res = res.replace("__", "_E_");
    res = res.replace("[", "");
    res = res.replace("]", "");
    res = res.replace("-", " ");
    res = res.replace("!", "REV");
    res = res.replace("'", "");
    res = res.replace(",", "_");
    res = res.replace(' ', '_');
    res = res.replace("fb:", "");
    res = res.replace("`", "");
    res = res.replace("(", "");
    res = res.replace(")", "");
    return res;
  }
}
