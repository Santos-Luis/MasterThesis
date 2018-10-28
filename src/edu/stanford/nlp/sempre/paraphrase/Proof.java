package edu.stanford.nlp.sempre.paraphrase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;

import edu.stanford.nlp.sempre.BooleanValue;
import edu.stanford.nlp.sempre.FeatureMatcher;
import edu.stanford.nlp.sempre.FeatureVector;
import edu.stanford.nlp.sempre.LanguageInfo;
import edu.stanford.nlp.sempre.Params;
import edu.stanford.nlp.sempre.paraphrase.rules.RuleApplication;
import edu.stanford.nlp.sempre.paraphrase.rules.RuleApplier;
import fig.basic.LispTree;
import fig.basic.NumUtils;

/**
 * A sequence of transformation from some source
 * @author jonathanberant
 *
 */
public class Proof {

  private static Proof nullProof;
  public static final FeatureMatcher featureMatcher = new ParaphraseFeatureMatcher();
  public static final ProofScorer scorer = new DotProductScorer();

  public final LanguageInfo source;
  public final LanguageInfo target;
  private List<RuleApplication> ruleApplications = new ArrayList<RuleApplication>();
  private boolean isCompleted;

  FeatureVector featureVector = new FeatureVector(); // features
  double score = Double.NaN;
  double prob = Double.NaN;
  double compatibility = Double.NaN;

  public static void initNullProof() {
    nullProof = new Proof(new LanguageInfo(), new LanguageInfo());
    // nullProof.featureVector.add("NullProof", "bias");
    nullProof.score = 0d;
  }

  public static Proof getNullProof() {
    if (nullProof == null)
      initNullProof();
    return nullProof;
  }

  public Proof(LanguageInfo source, LanguageInfo target) {
    this.source = source;
    this.target = target;
    isCompleted = setCompleted();
    score = scorer.scoreProof(this, new Params()); // all features are zero at this point so it doesn't matter what parameters we have
  }

  public Proof copy() {
    Proof res = new Proof(this.source, this.target);
    for (int i = 0; i < ruleApplications.size(); ++i) {
      res.ruleApplications.add(ruleApplications.get(i));
    }
    res.featureVector.add(this.featureVector);
    res.setCompleted();
    return res;
  }

  public LanguageInfo currConsequent() {
    if (ruleApplications.size() == 0)
      return source;
    return ruleApplications.get(ruleApplications.size() - 1).consequent;
  }

  private void add(RuleApplication application, Params params) {
    ruleApplications.add(application);
    featureVector.add(application.features(), featureMatcher);
    // For dot product we could make this more efficient by only considering the features added and not the entire vector
    isCompleted = setCompleted();
    score = scorer.scoreProof(this, params);
  }

  private boolean setCompleted() {
    return currConsequent().equalLemmas(target);
  }

  public boolean isCompleted() {
    return isCompleted;
  }

  public static class ProofComparator implements Comparator<Proof> {
    public int compare(Proof o1, Proof o2) {
      if (o1.score > o2.score) return -1;
      if (o2.score > o1.score) return 1;
      return 0;
    }
  }

  public List<Proof> expandProof(RuleApplier rule, Params params) {
    List<Proof> res = new ArrayList<Proof>();

    for (RuleApplication application : rule.apply(currConsequent(), target)) {
      if (validApplication(application)) {
        Proof newProof = this.copy();
        newProof.add(application, params);
        res.add(newProof);
      }
    }
    return res;
  }

  /**
   * Whether it is a valid application in this proof.
   * Not valid: (a) moving the same phrase twice (b) substituting and then deleting
   * @param proof
   * @param application
   * @return
   */
  private boolean validApplication(RuleApplication application) {

    if (application.appInfo.type.equals(RuleApplier.PHRASE_TABLE) ||
        application.appInfo.type.equals(RuleApplier.RULE)) {
      return true;
    }

    Set<String> movedPhrases = new HashSet<String>();
    Set<String> substitutingPhrases = new HashSet<String>();
    Set<String> deletedPhrases = new HashSet<String>();
    for (RuleApplication a : ruleApplications) {
      if (RuleApplier.MOVE.equals(a.appInfo.type))
        movedPhrases.add(a.appInfo.value);
      else if (RuleApplier.SUBST.equals(a.appInfo.type))
        substitutingPhrases.add(a.appInfo.value.split("-->")[1]);
      else if (RuleApplier.DELETE.equals(a.appInfo.type))
        deletedPhrases.add(a.appInfo.value);
    }
    if (RuleApplier.MOVE.equals(application.appInfo.type)) {
      if (movedPhrases.contains(application.appInfo.value)) {
        return false;
      }
    } else if (RuleApplier.DELETE.equals(application.appInfo.type)) {
      if (substitutingPhrases.contains(application.appInfo.value)) {
        // LogInfo.logs("Proof.validApplication: illegal - deleting=%s, proof=%s",application.appInfo.value,this);
        return false;
      }
    } else if (RuleApplier.INSERT.equals(application.appInfo.type)) {
      if (deletedPhrases.contains(application.appInfo.value)) {
        //        LogInfo.logs("Proof.validApplication: illegal - inserting=%s, proof=%s",application.appInfo.value,this);
        return false;
      }
    }
    return true;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[" + Joiner.on(' ').join(source.lemmaTokens));
    for (int i = 0; i < ruleApplications.size(); ++i) {
      sb.append(",");
      sb.append(Joiner.on(' ').join(ruleApplications.get(i).consequent.lemmaTokens));
    }
    sb.append("], [target=" + Joiner.on(' ').join(target.lemmaTokens) + "], score=" + score);
    return sb.toString();
  }

  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    tree.addChild("proof");
    tree.addChild(Joiner.on(' ').join(source.lemmaTokens));
    for (RuleApplication application : ruleApplications) {
      tree.addChild(Joiner.on(' ').join(application.consequent.lemmaTokens));
    }
    return tree;
  }

  /**
   * Add a backward proof to a forward proof - add rules, utterances, features and re-score
   * @param backwardProof
   * @param params
   */
  public boolean completeProof(Proof backwardProof, Params params) {
    for (int i = backwardProof.ruleApplications.size() - 1; i >= 0; i--) {
      RuleApplication reverseApplication = backwardProof.ruleApplications.get(i).reverse();
      if (!validApplication(reverseApplication))
        return false;
      ruleApplications.add(reverseApplication);
    }
    isCompleted = setCompleted(); // danger - need to remember to set this...

    // add features and score
    featureVector.add(backwardProof.featureVector);
    // add length feature
    if (ParaphraseFeatureMatcher.containsDomain("Global"))
      featureVector.add("Global", "length=" + this.ruleApplications.size());
    score = scorer.scoreProof(this, params);
    return true;
  }

  public BooleanValue value() {
    if (this.equals(nullProof))
      return new BooleanValue(false);
    return new BooleanValue(true);
  }

  public void incrementAllFeatureVector(double incr, Map<String, Double> counts) {
    featureVector.increment(incr, counts);
  }

  public static double[] getProbs(List<Proof> proofs) {
    double[] probs = new double[proofs.size()];
    for (int i = 0; i < proofs.size(); i++)
      probs[i] = proofs.get(i).score;
    if (probs.length > 0)
      NumUtils.expNormalize(probs);
    return probs;
  }

  public void clear() {
    ruleApplications.clear();
    featureVector.clear();
  }
}
