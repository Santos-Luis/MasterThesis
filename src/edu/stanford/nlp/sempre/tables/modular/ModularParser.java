package edu.stanford.nlp.sempre.tables.modular;

import java.io.PrintWriter;
import java.util.*;

import edu.stanford.nlp.sempre.*;
import fig.basic.*;
import fig.exec.Execution;

/**
 * A parser that trains one "module" at a time.
 *
 * For each example during training:
 * * allDerivs = {}
 *   - For each ModularSemanticFn in that template:
 *     - Set the mode of r to HIGH PRECISION
 *     - Set the mode of other ModularSemanticFn to HIGH RECALL
 *     - Parse with floating parser
 *     - Add the derivations to allDerivs
 * * Update parameters
 *
 * During test time, all modules are set to HIGH RECALL.
 *
 * @author ppasupat
 */
public class ModularParser extends FloatingParser implements MutatingParser {
  public static class Options {
    @Option
    public int verbosity = 0;
    @Option(gloss = "Random object for shuffling the derivation list")
    public Random shuffleRandom = new Random(1);
    @Option(gloss = "Number of training iterations to use modular training")
    public int numModularIterations = Integer.MAX_VALUE;
  }
  public static Options opts = new Options();

  public int currentIter = 0;
  public String currentGroup = null;
  public final ModularPrecisionRecallTuner tuner;
  public final PrintWriter modularEventsOut;

  public ModularParser(Spec spec) {
    super(spec);
    tuner = new ModularPrecisionRecallTuner(new FloatingParser(spec));
    modularEventsOut = IOUtils.openOutAppendEasy(Execution.getFile("modular.events"));
  }

  @Override
  public void mutate(int iter, int numIters, String group) {
    currentIter = iter;
    currentGroup = group;
  }

  @Override
  public ParserState newParserState(Params params, Example ex, boolean computeExpectedCounts) {
   return new ModularParserState(this, params, ex, computeExpectedCounts);
  }

}

class ModularParserState extends ParserState {

  final ModularPrecisionRecallTuner tuner;

  public ModularParserState(Parser parser, Params params, Example ex, boolean computeExpectedCounts) {
    super(parser, params, ex, computeExpectedCounts);
    tuner = ((ModularParser) parser).tuner;
  }

  @Override
  public void infer() {
    LogInfo.begin_track("ModularParser.infer()");
    ModularParser p = (ModularParser) parser;
    if (computeExpectedCounts && p.currentIter < ModularParser.opts.numModularIterations) {
      for (ModularSemanticFn f : tuner) {
        tuner.setSingleRecall(f);
        ParserState ps = tuner.getParserState(params, ex, computeExpectedCounts);
        logEvent(ps);
        logDerivations(ps);
        predDerivations.addAll(ps.predDerivations);
      }
    } else {
      tuner.setAllRecall();
      ParserState ps = tuner.getParserState(params, ex, computeExpectedCounts);
      logEvent(ps);
      // logDerivations(ps);
      predDerivations.addAll(ps.predDerivations);
    }
    Collections.shuffle(predDerivations, ModularParser.opts.shuffleRandom);
    ensureExecuted();
    expectedCounts = new HashMap<>();
    ParserState.computeExpectedCounts(predDerivations, expectedCounts);
    LogInfo.end_track();
  }

  void logEvent(ParserState ps) {
    ModularParser p = (ModularParser) parser;
    List<String> fields = new ArrayList<>();
    fields.add("iter=" + p.currentIter);
    fields.add("group=" + p.currentGroup);
    fields.add("utterance=" + ex.utterance);
    fields.add("targetValue=" + ex.targetValue);
    fields.add("preciseProbs=" + tuner.getPreciseProbsString());
    fields.add("numDerivs=" + ps.predDerivations.size());
    if (ps.predDerivations.size() > 0) {
      Derivation deriv = ps.predDerivations.get(0);
      fields.add("predValue=" + deriv.value);
      fields.add("predFormula=" + deriv.formula);
    }
    p.modularEventsOut.println(String.join("\t", fields));
    p.modularEventsOut.flush();
  }

  void logDerivations(ParserState ps) {
    if (ModularParser.opts.verbosity >= 2) {
      LogInfo.begin_track("%s: %d derivations", Fmt.D(tuner.getAllPreciseProbs()), ps.predDerivations.size());
      int limit = Math.min(Parser.opts.maxPrintedPredictions, ps.predDerivations.size());
      for (int i = 0; i < limit; i++) {
        LogInfo.logs("%s", ps.predDerivations.get(i));
      }
      LogInfo.end_track();
    }
  }
}
