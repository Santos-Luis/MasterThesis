package edu.stanford.nlp.sempre.paraphrase;

import edu.stanford.nlp.sempre.BooleanValue;
import edu.stanford.nlp.sempre.Params;
import edu.stanford.nlp.sempre.freebase.utils.CollectionUtils;
import edu.stanford.nlp.sempre.freebase.utils.DoubleContainer;
import edu.stanford.nlp.sempre.freebase.utils.MathUtils;
import fig.basic.LogInfo;
import fig.basic.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProofTransformationModel {

  public static class Options {
    @Option public boolean loadParams = false;
    @Option public String inParamsPath;
  }
  public static Options opts = new Options();

  private static Transformer transformer = getInstance();

  private static Transformer getInstance() {
    return new Transformer();
  }

  private Params params = new Params();

  public ProofTransformationModel() {
    if (opts.loadParams)
      params.read(opts.inParamsPath);
  }

  public Map<Context, Double> getContextDist(Set<Context> candidates,
      Context questionContext) {

    Map<Context, DoubleContainer> res = new HashMap<Context, DoubleContainer>();

    for (Context candidate : candidates) {
      if (candidate.equals(questionContext)) {
        LogInfo.logs("RuleTransformationModel: Exact match", questionContext, candidate);
        res.clear();
        res.put(candidate, new DoubleContainer(1.0));
        break;
      }
      ParaphraseExample example = new ParaphraseExample(questionContext.toUtteranceString(), candidate.toUtteranceString(),
          new BooleanValue(false));
      transformer.transform(example, params);
      double score = example.computeExampleScore();
      if (score > 0.0) {
        LogInfo.logs("RuleTransformationModel: question=%s, match=%s, score=%s", questionContext, candidate, score);
        res.put(candidate, new DoubleContainer(score));
      }
    }
    MathUtils.normalizeDoubleMap(res);
    return CollectionUtils.doubleContainerToDoubleMap(res);
  }
}
