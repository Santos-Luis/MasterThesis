package edu.stanford.nlp.sempre.paraphrase;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.freebase.*;
import edu.stanford.nlp.sempre.paraphrase.Aligner.Alignment;
//BEGIN_HIDE
import edu.stanford.nlp.sempre.paraphrase.Proof.ProofComparator;
//END_HIDE
import edu.stanford.nlp.sempre.paraphrase.rules.ParaphraseAlignment;
import edu.stanford.nlp.sempre.paraphrase.rules.LemmaPosRule;
import edu.stanford.nlp.sempre.paraphrase.rules.LemmaPosSequence;
import edu.stanford.nlp.sempre.paraphrase.rules.LemmaPosSequence.LemmaAndPos;
import fig.basic.Fmt;
import fig.basic.LogInfo;
import fig.basic.MemUsage;
import fig.basic.Evaluation;

public class ParaphraseExample {

  public String id = null;
  @JsonProperty public final String source;
  @JsonProperty public final String target;
  @JsonProperty private Formula formula; // formula from which the paraphrase was generated
  @JsonProperty Value goldValue;

  public LanguageInfo sourceInfo;
  public LanguageInfo targetInfo;

  // BEGIN_HIDE
  private List<Proof> predictedProofs;
  // END_HIDE
  FeatureSimilarity featureSimilarity;
  Alignment alignment;

  Evaluation eval = new Evaluation();

  private static Map<String, LanguageInfo> annotationCache = new HashMap<>();

  public ParaphraseExample(String source, String target, BooleanValue value) {
    this.source = source;
    this.target = target;
    this.goldValue = value;
    synchronized (annotationCache) {
      this.sourceInfo = annotationCache.get(source); // null if not there
      this.targetInfo = annotationCache.get(target); // null if not there
    }
    // BEGIN_HIDE
    predictedProofs = new ArrayList<>();
    predictedProofs.add(Proof.getNullProof());
    // END_HIDE
  }

  @JsonCreator
  public ParaphraseExample(@JsonProperty("source") String source, @JsonProperty("target") String target,
      @JsonProperty("formula") Formula f, @JsonProperty("value") BooleanValue value) {
    this.source = source;
    this.target = target;
    this.formula = f;
    this.goldValue = value;
    // BEGIN_HIDE
    predictedProofs = new ArrayList<Proof>();
    predictedProofs.add(Proof.getNullProof());
    // END_HIDE
  }

  // IF WE HAVE A NULL PROOF IT SEEMS THAT THIS IS BROKEN
  public double computeExampleScore() {
    throw new RuntimeException("This method might be broken because there is now a null proof that" +
        "can on the predicted proofs");
    //    if (predictedProofs == null)
    //      throw new RuntimeException("Transformation was not performed. source: " + source + " target; " + target);
    //    if (predictedProofs.size() == 0)
    //      return 0.0;
    //    if (predictedProofs.get(0).score<0.0)
    //      throw new RuntimeException("Supporting only positive scores for proofs");
    //    return 1.0;
  }

  // BEGIN_HIDE
  public List<Proof> predictedProofs() {
    return predictedProofs;
  }
  // END_HIDE

  /**
   * Aligns from left to right without any crossing alignments
   */
  public ParaphraseAlignment align() {

    ensureAnnotated();
    int[] sourceAlignment = new int[sourceInfo.tokens.size()];
    int[] targetAlignment = new int[targetInfo.tokens.size()];
    Arrays.fill(sourceAlignment, -1);
    Arrays.fill(targetAlignment, -1);

    int lastAlignedIndex = -1;
    for (int i = 0; i < sourceInfo.tokens.size(); ++i) {
      for (int j = lastAlignedIndex + 1; j < targetInfo.tokens.size(); ++j) {
        if (sourceInfo.lemmaTokens.get(i).equals(targetInfo.lemmaTokens.get(j))) {
          lastAlignedIndex = sourceAlignment[i] = j;
          targetAlignment[j] = i;
          break;
        }
      }
    }
    return new ParaphraseAlignment(sourceAlignment, targetAlignment);
  }

 // TODO(alex)
  public void ensureAnnotated() {
    if (sourceInfo == null) {
      sourceInfo = new LanguageInfo();
     // sourceInfo.analyze(this.source);
      synchronized (annotationCache) {
        annotationCache.put(source, sourceInfo);
      }
    }
    if (targetInfo == null) {
      this.targetInfo = new LanguageInfo();
     // this.targetInfo.analyze(this.target);
      synchronized (annotationCache) {
        annotationCache.put(source, sourceInfo);
      }
    }
  }

  public String toJson() {
    return Json.writeValueAsStringHard(this);
  }

  public LemmaPosRule getRule(Interval sourceInterval, Interval targetInterval) {
    return new LemmaPosRule(computeTemplate(sourceInfo, sourceInterval),
        computeTemplate(targetInfo, targetInterval));
  }

  public LemmaPosSequence computeTemplate(LanguageInfo info, Interval interval) {

    List<LemmaAndPos> res = new ArrayList<>();
    for (int i = interval.start; i < interval.end; ++i) {
      res.add(new LemmaAndPos(info.lemmaTokens.get(i), info.posTags.get(i)));
    }
    return new LemmaPosSequence(res);
  }

  // BEGIN_HIDE
  public void addPredictedProofs(List<Proof> predictedProofs) {
    this.predictedProofs.addAll(predictedProofs);
  }
  // END_HIDE

  public void setVectorSpaceSimilarity(FeatureSimilarity vsSimilarity) {
    this.featureSimilarity = vsSimilarity;
  }

  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  // BEGIN_HIDE
  /**
   *
   * @return highest scoring proof that is not the null proof is it exists
   *
   */
  // END_HIDE
  // BEGIN_HIDE
  public Proof getTopProof() {
    if (predictedProofs == null || predictedProofs.isEmpty())
      throw new RuntimeException("No predicted proofs");
    Proof res = predictedProofs.get(0);
    if (res != Proof.getNullProof())
      return res;
    if (predictedProofs.size() <= 1)
      return Proof.getNullProof();
    return predictedProofs.get(1);
  }
  // END_HIDE

  public void log() {
    ensureAnnotated();
    LogInfo.begin_track_printAll("Example");
    LogInfo.log("Id: " + id);
    LogInfo.log("Source: " + source);
    LogInfo.logs("Source lemmas: %s", sourceInfo.lemmaTokens);
    LogInfo.logs("Source POS tags: %s", sourceInfo.posTags);
    LogInfo.logs("Source NER tags: %s", sourceInfo.nerTags);
    LogInfo.log("Target: " + target);
    LogInfo.logs("Target lemmas: %s", targetInfo.lemmaTokens);
    LogInfo.logs("Target POS tags: %s", targetInfo.posTags);
    LogInfo.logs("Target NER tags: %s", targetInfo.nerTags);
    LogInfo.log("Value: " + goldValue);
    LogInfo.end_track();
  }

  public void setEvaluation(Params params) {
    setEvaluation(params, false);
  }

  public void setEvaluation(Params params, boolean print) {
    // BEGIN_HIDE

    ValueEvaluator valueEvaluator = new FreebaseValueEvaluator();

    int numCandidates = predictedProofs().size();
    LogInfo.begin_track_printAll("Transformer.setEvaluation: %d candidates", numCandidates);

    List<Proof> predictedProofs = predictedProofs();
    // score and sort
    for (Proof p : predictedProofs) {
      Proof.scorer.scoreProof(p, params);
    }
    Collections.sort(predictedProofs, new ProofComparator());
    boolean posEx = ((BooleanValue) goldValue).value;
    boolean posPrediction = !(predictedProofs.get(0) == Proof.getNullProof());

    // Did we get the answer correct?
    int correctIndex = -1;  // Index of first correct derivation
    double[] compatibilities = new double[numCandidates];

    for (int i = 0; i < numCandidates; i++) {

      Proof proof = predictedProofs.get(i);
      compatibilities[i] = proof.compatibility = valueEvaluator.getCompatibility(goldValue, proof.value());

      // Must be fully compatible to count as correct.
      if (compatibilities[i] == 1 && correctIndex == -1)
        correctIndex = i;
    }

    // Compute probabilities
    double[] probs = Proof.getProbs(predictedProofs);
    for (int i = 0; i < numCandidates; i++) {
      Proof proof = predictedProofs.get(i);
      proof.prob = probs[i];
    }

    // Number of derivations which have the same top score
    int numTop = 0;
    double topMass = 0;

    while (numTop < numCandidates &&
        compatibilities[numTop] > 0.0d &&
        Math.abs(predictedProofs.get(numTop).score - predictedProofs.get(0).score) < 1e-10) {
      topMass += probs[numTop];
      numTop++;
    }

    double correct = 0;
    for (int i = 0; i < numTop; i++)
      if (compatibilities[i] == 1) correct += probs[i] / topMass;

    // Print features (note this is only with respect to the first correct, is NOT the gradient).
    if (print) {
      if (correctIndex != -1 && correct != 1) {
        Proof trueProof = predictedProofs.get(correctIndex);
        Proof predProof = predictedProofs.get(0);
        HashMap<String, Double> featureDiff = new HashMap<>();
        trueProof.incrementAllFeatureVector(+1, featureDiff);
        predProof.incrementAllFeatureVector(-1, featureDiff);
        String heading = String.format("TopTrue (%d) - Pred (%d) = Diff", correctIndex, 0);
        FeatureVector.logFeatureWeights(heading, featureDiff, params);
      }


      // Fully correct
      for (int i = 0; i < predictedProofs.size(); i++) {
        Proof proof = predictedProofs.get(i);
        if (compatibilities != null && compatibilities[i] == 1) {
          if (i < 10) {
            LogInfo.logs(
                "True@%04d: %s [score=%s, prob=%s%s]", i, proof.toString(),
                Fmt.D(proof.score), Fmt.D(probs[i]), compatibilities != null ? ", comp=" + Fmt.D(compatibilities[i]) : "");
          }
        }
      }
      // Anything that's predicted.
      for (int i = 0; i < predictedProofs.size(); i++) {
        Proof proof = predictedProofs.get(i);
        if (i < 10) {
          LogInfo.logs(
              "Pred@%04d: %s [score=%s, prob=%s%s]", i, proof.toString(),
              Fmt.D(proof.score), Fmt.D(probs[i]), compatibilities != null ? ", comp=" + Fmt.D(compatibilities[i]) : "");
        }
      }
    }

    eval.add("correct", correct);
    eval.add("oracle", correctIndex != -1);
    if (posEx)
      eval.add("recall", correct);
    if (posPrediction)
      eval.add("precision", correct);
    eval.add("numCandidates", numCandidates);
    LogInfo.end_track();
    // END_HIDE
  }

  // BEGIN_HIDE
  public void clearProofs() {
    predictedProofs.clear();
  }
  // END_HIDE

  public static long cacheSize() {
    return MemUsage.getBytes(annotationCache);
  }
}
