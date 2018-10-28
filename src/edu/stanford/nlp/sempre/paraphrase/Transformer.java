package edu.stanford.nlp.sempre.paraphrase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.sempre.BooleanValue;
import edu.stanford.nlp.sempre.Params;
import edu.stanford.nlp.sempre.paraphrase.rules.RuleApplier;
import edu.stanford.nlp.sempre.paraphrase.rules.Rulebase;
import fig.basic.LogInfo;
import fig.basic.MapUtils;
import fig.basic.Option;

/**
 * finds proofs that transform a source utterance to a target utterance
 * @author jonathanberant
 *
 */
public class Transformer {

  public static class Options {
    @Option(gloss = "Maximal number of predictions to print") public int maxPrintedProofs = Integer.MAX_VALUE;
    @Option(gloss = "How much output to print") public int verbose = 0;
    @Option public int beamSize = 40;
    @Option public int maxNumOfSteps = 3;
    @Option public boolean bow = false;
  }
  public static final Options opts = new Options();
  private final Rulebase rulebase;

  public Transformer() {
    rulebase = new Rulebase();
  }

  public void transform(ParaphraseExample example, Params params) {

    LogInfo.begin_track_printAll("Transformer.transform");
    ParaphraseExample forwardEx = example;
    ParaphraseExample backwardEx = new ParaphraseExample(example.target, example.source, new BooleanValue(false));

    if (opts.verbose >= 1)
      LogInfo.begin_track("Forward proof: source=%s, target=%s", forwardEx.source, forwardEx.target);
    TransformerState state = new TransformerState(forwardEx);
    state.generate(params); // get proofs
    Map<String, List<Proof>> forwardProofs = state.proofs;
    if (opts.verbose >= 1) {
      LogInfo.logs("Transformer.transform: Number of forward proofs=%s", state.proofCount);
      LogInfo.end_track();
    }

    if (opts.verbose >= 1)
      LogInfo.begin_track("Backward proof: source=%s, target=%s", backwardEx.source, backwardEx.target);
    state = new TransformerState(backwardEx);
    state.generate(params); // get proofs
    Map<String, List<Proof>> backwardProofs = state.proofs;
    if (opts.verbose >= 1) {
      LogInfo.logs("Transformer.transform: Number of backward proofs=%s", state.proofCount);
      LogInfo.end_track();
    }

    if (opts.verbose >= 1)
      LogInfo.begin_track("Completing proofs");
    completeProofs(example, params, forwardProofs, backwardProofs);
    example.setEvaluation(params);
    if (opts.verbose >= 1)
      LogInfo.end_track();
    LogInfo.end_track();
  }

  private void completeProofs(ParaphraseExample ex, Params params, Map<String, List<Proof>> forwardProofs, Map<String, List<Proof>> backwardProofs) {

    List<Proof> predictedProofs = new ArrayList<>();
    for (String forwardProofDesc : forwardProofs.keySet()) {
      for (Proof forwardProof : forwardProofs.get(forwardProofDesc)) {
        for (Proof backwardProof : MapUtils.get(backwardProofs, forwardProofDesc, new ArrayList<Proof>())) {
          Proof newProof = forwardProof.copy();
          if (newProof.completeProof(backwardProof, params))
            predictedProofs.add(newProof);
          if (opts.verbose >= 1)
            LogInfo.logs("Transformer: completeProof=%s", newProof);
        }
      }
    }
    // BEGIN_HIDE
    ex.addPredictedProofs(predictedProofs);
    // END_HIDE
  }

  public class TransformerState {
    private List<Proof> beam = new ArrayList<Proof>();
    private Map<String, List<Proof>> proofs = new HashMap<String, List<Proof>>();
    private int proofCount = 0;

    public TransformerState(ParaphraseExample ex) {
      ex.ensureAnnotated();
      Proof initialProof;
      initialProof = new Proof(ex.sourceInfo, ex.targetInfo);
      beam.add(initialProof); // add to beam
      MapUtils.addToList(proofs, initialProof.currConsequent().lemmaPhrase(0, initialProof.currConsequent().numTokens()), initialProof);
      proofCount++;
    }

    public void generate(Params params) {
      if (opts.verbose >= 1)
        LogInfo.begin_track_printAll("Transformer.generate");
      int steps = 0;
      // bound the depth of a proof
      while (steps < opts.maxNumOfSteps && !beam.isEmpty()) {

        if (opts.verbose >= 1)
          LogInfo.begin_track_printAll("Step %s", steps);
        List<Proof> newBeam = new ArrayList<>();
        // go over all things on the beam
        for (Proof currProof : beam) {
          // go over rules
          for (RuleApplier candidateRule : rulebase.getRules()) {

            if (opts.verbose >= 3) {
              LogInfo.logs("Matching rule: %s", candidateRule);
            }
            // expand proofs
            List<Proof> newProofs = currProof.expandProof(candidateRule, params);
            if (opts.verbose >= 3) {
              if (newProofs.isEmpty())
                LogInfo.log("No new proofs");
            }
            for (Proof newProof : newProofs) {
              MapUtils.addToList(proofs,
                  newProof.currConsequent().numTokens() == 0 ? "" : newProof.currConsequent().lemmaPhrase(0, newProof.currConsequent().numTokens()),
                      newProof);
              proofCount++;
              if (!newProof.isCompleted()) {
                newBeam.add(newProof);
                if (opts.verbose >= 3)
                  LogInfo.logs("Transformer.genrate: incomplete proof=%s", newProof);
              } else if (opts.verbose >= 3)
                LogInfo.logs("Transformer.genrate: completed proof=%s", newProof);
            }
          }
        }
        if (opts.verbose >= 1)
          LogInfo.logs("Transformer.generate: proofs on beam=%s", newBeam.size());
        sortAndTruncateBeam(newBeam); // re-set the beam

        beam.clear();
        beam = newBeam;
        steps++;
        if (opts.verbose >= 1)
          LogInfo.end_track();
      }
      if (opts.verbose >= 1)
        LogInfo.end_track();
    }

    private void sortAndTruncateBeam(List<Proof> newBeam) {
      Collections.sort(newBeam, new Proof.ProofComparator());
      if (newBeam.size() > opts.beamSize)
        newBeam.subList(opts.beamSize, newBeam.size()).clear(); // remove items out of the beam
    }
  }
}
