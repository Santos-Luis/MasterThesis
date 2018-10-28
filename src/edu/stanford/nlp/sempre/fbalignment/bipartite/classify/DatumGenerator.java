package edu.stanford.nlp.sempre.fbalignment.bipartite.classify;

import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.BipartiteNode;
import edu.stanford.nlp.sempre.fbalignment.bipartite.rep.FourPartiteGraph;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Sets;
import fig.basic.LogInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class DatumGenerator {
  private DatumGenerator() { }

  public static final int INTERSECTION_SIZE_INDEX = 5;
  public static final String INTERSECTION_FEATURE = "intersection";

  public static final int NL_TYPED_SIZE_INDEX = 6;
  public static final String NL_TYPED_SIZE_FEATURE = "nl_typed_size";

  public static final int FB_TYPED_SIZE_INDEX = 7;
  public static final String FB_TYPED_SIZE_FEATURE = "fb_typed_size";

  public static final int NL_SIZE_INDEX = 8;
  public static final String NL_SIZE_FEATURE = "nl_size";

  public static final int JACCARD_INDEX = 9;
  public static final String JACCARD_FEATURE = "jaccard";

  public static final String BIAS = "bias";

  public static final int LABLE_INDEX = 11;

  private static boolean squareFeatures = false;


  private static Map<String, Integer> features;

  static {
    features = new HashMap<String, Integer>();
    features.put(INTERSECTION_FEATURE, INTERSECTION_SIZE_INDEX);
    features.put(NL_TYPED_SIZE_FEATURE, NL_TYPED_SIZE_INDEX);
    features.put(FB_TYPED_SIZE_FEATURE, FB_TYPED_SIZE_INDEX);

//    features.put("intersection_arg_nl_count", 12);
//    features.put("intersection_arg_fb_count", 15);
//    features.put("nl_arg_nl_count", 18);
//    features.put("nl_arg_fb_count", 21);
//    features.put("fb_arg_nl_count", 24);
//    features.put("fb_arg_fb_count", 27);
    features.put("max_cover", 30);

    squareFeatures = true;
    LogInfo.logs("%s", features);
    LogInfo.log("Square features: " + squareFeatures);
    // features.put(NL_SIZE_FEATURE, NL_SIZE_INDEX);
  }


  public static Datum<Boolean, String> lineToDatum(String line) {


    String[] tokens = line.split("\t");
    Boolean label = tokens[DatumGenerator.LABLE_INDEX].equals("1") ? true : false;


    Counter<String> counter = new ClassicCounter<String>();

    for (String feature : features.keySet()) {
      counter.incrementCount(feature, Math.log(Double.parseDouble(tokens[features.get(feature)])));
    }
    counter.incrementCount(DatumGenerator.BIAS, 1);

    if (squareFeatures) {
      Counter<String> squareCounter = new ClassicCounter<String>();

      for (String key1 : counter.keySet()) {
        for (String key2 : counter.keySet()) {
          squareCounter.incrementCount(key1 + ":" + key2, counter.getCount(key1) * counter.getCount(key2));
        }
      }
      counter = squareCounter;
    }

    return new RVFDatum<Boolean, String>(counter, label);
  }

  public static Datum<Boolean, String> nodePairToDatum(FourPartiteGraph graph, BipartiteNode nlTypedNode, BipartiteNode fbTypedNode) {

    // unlabeled datum
    Boolean label = false;

    Set<Pair<Integer, Integer>> nlTypedMids = nlTypedNode.getMidIdPairSet();
    Set<Pair<Integer, Integer>> fbTypedMids = fbTypedNode.getMidIdPairSet();

    int nlTypedCount = nlTypedMids.size();
    int fbTypedCount = fbTypedMids.size();
    int intersectionCount = Sets.intersection(nlTypedMids, fbTypedMids).size();
    int nlCount = graph.getGraph().getPredecessors(nlTypedNode).iterator().next().getMidIdPairsCount();

    Counter<String> counter = new ClassicCounter<String>();

    if (features.containsKey(INTERSECTION_FEATURE))
      counter.incrementCount(INTERSECTION_FEATURE, Math.log(intersectionCount));
    if (features.containsKey(NL_TYPED_SIZE_FEATURE))
      counter.incrementCount(DatumGenerator.NL_TYPED_SIZE_FEATURE, Math.log(nlTypedCount));
    if (features.containsKey(FB_TYPED_SIZE_FEATURE))
      counter.incrementCount(DatumGenerator.FB_TYPED_SIZE_FEATURE, Math.log(fbTypedCount));
    if (features.containsKey(NL_SIZE_FEATURE))
      counter.incrementCount(DatumGenerator.NL_SIZE_FEATURE, Math.log(nlCount));
    counter.incrementCount(DatumGenerator.BIAS, 1);

    return new RVFDatum<Boolean, String>(counter, label);
  }

}
