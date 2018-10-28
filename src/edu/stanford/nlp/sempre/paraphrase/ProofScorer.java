package edu.stanford.nlp.sempre.paraphrase;

import edu.stanford.nlp.sempre.Params;
import edu.stanford.nlp.sempre.freebase.utils.MathUtils;

public interface ProofScorer {
  double scoreProof(Proof p, Params params);
}

class CosineToTargetScorer implements ProofScorer {
  @Override
  public double scoreProof(Proof p, Params params) {
    return MathUtils.tokensCosine(p.currConsequent().lemmaTokens, p.target.lemmaTokens);
  }
}

class DotProductScorer implements ProofScorer {

  @Override
  public double scoreProof(Proof p, Params params) {
    return p.featureVector.dotProduct(params);
  }
}
