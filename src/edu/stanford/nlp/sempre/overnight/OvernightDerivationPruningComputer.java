package edu.stanford.nlp.sempre.overnight;

import java.util.*;
import java.util.regex.Pattern;

import edu.stanford.nlp.sempre.*;
import fig.basic.Option;
import fig.basic.MapUtils;

/**
 * Hard-coded hacks for pruning derivations in floating parser for overnight domains.
 *
 */

public class OvernightDerivationPruningComputer extends DerivationPruningComputer {
  public static class Options {
    @Option (gloss = "Whether filter derivations using hard constraints")
    public boolean applyHardConstraints = false;
    // BEGIN_HIDE
    @Option (gloss = "Use predicate dictionary")
    public boolean usePredicateDict = false;
    @Option (gloss = "Path to predicate dictionary")
    public String predicateDictPath;
    // END_HIDE
  }
  public static Options opts = new Options();

  // BEGIN_HIDE
  Map<String, Set<String>> predicateDict;
  // END_HIDE
  
  public OvernightDerivationPruningComputer(DerivationPruner pruner) {
    super(pruner);
  }

  @Override
  public boolean isPruned(Derivation deriv) {
    // BEGIN_HIDE
    if (opts.usePredicateDict && !predicateMatch(deriv)) return true;
    // END_HIDE
    if (opts.applyHardConstraints && violateHardConstraints(deriv)) return true;
    return false;
  }

  // Check a few hard constraints on each derivation
  private static boolean violateHardConstraints(Derivation deriv) {
    if (deriv.value != null) {
      if (deriv.value instanceof ErrorValue) return true;
      if (deriv.value instanceof StringValue) { //empty denotation
        if (((StringValue) deriv.value).value.equals("[]")) return true;
      }
      if (deriv.value instanceof ListValue) {
        List<Value> values = ((ListValue) deriv.value).values;
        // empty lists
        if (values.size() == 0) return true;
        // NaN
        if (values.size() == 1 && values.get(0) instanceof NumberValue) {
          if (Double.isNaN(((NumberValue) values.get(0)).value)) return true;
        }
        // If we are supposed to get a number but we get a string (some sparql weirdness)
        if (deriv.type.equals(SemType.numberType) &&
                values.size() == 1 &&
                !(values.get(0) instanceof NumberValue)) return true;
      }
    }
    return false;
  }

  // BEGIN_HIDE
  // Hack for making things faster for geo880
  private boolean predicateMatch(Derivation newDeriv) {

    // For certain categories, we use a predicate dictionary to filter
    if (newDeriv.cat.equals("$VP") || newDeriv.cat.equals("$VP/NP") || newDeriv.cat.equals("$RelNP")) {
      if (predicateDict == null) {
        predicateDict = new HashMap<>();
        for (String line : edu.stanford.nlp.io.IOUtils.readLines(opts.predicateDictPath)) {
          String[] tokens = line.split("\t");
          MapUtils.addToSet(predicateDict, tokens[0], LanguageInfo.LanguageUtils.stem(tokens[1]));
          MapUtils.addToSet(predicateDict, tokens[0], LanguageInfo.LanguageUtils.stem(tokens[0]));
        }
      }
      Set<String> targetPredicates = predicateDict.keySet();
      String[] derivTokens = newDeriv.canonicalUtterance.split("\\s+");
      for (String derivToken: derivTokens) {

        // if we generate one of the predicates
        if (targetPredicates.contains(derivToken)) {
          Set<String> predicateParaphrases = predicateDict.get(derivToken);
          boolean contains = false;
          for (int i = 0; i < pruner.ex.numTokens(); ++i) {
            String inputToken = pruner.ex.token(i);
            if (predicateParaphrases.contains(LanguageInfo.LanguageUtils.stem(inputToken))) {
              contains = true;
              break;
            }
          }
          if (!contains)
            return false;
        }
      }
    }
    return true;
  }
  // END_HIDE
}
