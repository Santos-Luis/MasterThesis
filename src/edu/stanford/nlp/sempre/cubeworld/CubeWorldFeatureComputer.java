package edu.stanford.nlp.sempre.cubeworld;

import fig.basic.*;
import java.util.*;
import java.util.regex.Pattern;

import com.beust.jcommander.internal.Lists;

import edu.stanford.nlp.sempre.cubeworld.CubeWorld;
import edu.stanford.nlp.sempre.*;

/**
 * Sida Wang
 */
public class CubeWorldFeatureComputer implements FeatureComputer {
  private static final String PREFIX = "edu\\.stanford\\.nlp\\.sempre\\.cubeworld\\.CubeWorld\\.";
  public static class Options {
    @Option(gloss = "Verbosity")
    public int verbose = 0;
    @Option(gloss = "the N in N-gram")
    public int ngramN = 2;
    @Option(gloss = "the rule gram number")
    public int rulegramN = 2;
  }
  public static Options opts = new Options();

  @Override public void extractLocal(Example ex, Derivation deriv) {
    addNgramRuleFeatures(ex, deriv);
    addHeadsFeatures(ex, deriv);
  }

  private String unverbosify(String s) {
    return s.replaceAll(PREFIX, "").replaceAll("context:", "");
  }
  private String convertDeriv(Derivation deriv) {
    return unverbosify(deriv.getFormula().toString());
  }

  // like lambda-call-number-call
  private List<String> getTopLevelTypes(Derivation deriv) {
    List<String> heads = new ArrayList<>();
    Formula formula = deriv.getFormula();
    LispTree tree = formula.toLispTree();
    List<LispTree> allchildren = tree.children;
    for (LispTree t : allchildren) {
      if (!t.isLeaf()) {
        LispTree head = t.head();
        String headstring = head.toString();
        heads.add(headstring);
      }
    }
    return heads;
  }

  // getColor setUnion Number
  private List<String> getTopLevelFuncs(Derivation deriv) {
    List<String> heads = new ArrayList<>();
    Formula formula = deriv.getFormula();
    LispTree tree = formula.toLispTree();
    List<LispTree> allchildren = tree.children;
    for (LispTree t : allchildren) {
      if (!t.isLeaf()) {
        LispTree head = t.head();
        String headstring = head.toString();
        if (headstring.equals("call"))
          heads.add("_" + unverbosify(t.child(1).toString()));
        else if (headstring.equals("number"))
          heads.add(unverbosify(t.toString()));
      }
    }
    return heads;
  }

  private static final Pattern replaceCallLambda = Pattern.compile("\\(|call|lambda|\\)");

  //getColor setUnion Number
  private List<String> getOpSequence(Derivation deriv) {
    String treestr = unverbosify(deriv.getFormula().toString());
    return Lists.newArrayList(replaceCallLambda.split(treestr));
  }

 
  private void addNgramRuleFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("ngram")) return;
    //if (!deriv.cat.equals(Rule.rootCat)) return;
    if (deriv.rule != Rule.nullRule) {
      //deriv.addFeature("utter", convertDeriv(deriv) + " <===> " + ex.utterance);
      for (String token : ex.languageInfo.tokens)
        deriv.addFeature("token", convertDeriv(deriv)  + " <==> " + token);
    }
  }

  private List<String> getAllNgrams(List<String> tokens, int n) {
    List<String> ngrams = new ArrayList<>();
    List<String> paddedTokens = new ArrayList<>();
    List<String> suffixEnd = new ArrayList<>();
    for (int i=0; i<n-1; i++) {
      suffixEnd.add(">");
      paddedTokens.add("<");
    }
    paddedTokens.addAll(tokens);
    paddedTokens.addAll(suffixEnd);
    for (int i=0; i<tokens.size()+n-1; i++) {
      ngrams.add( paddedTokens.subList(i, i+n).toString() );
    }
    return ngrams;
  }
  private List<String> getAllSkipGrams(List<String> tokens) {
    List<String> ngrams = new ArrayList<>();
    List<String> paddedTokens = new ArrayList<>();
    
    paddedTokens.add("<");
    paddedTokens.addAll(tokens);
    paddedTokens.add(">");
    for (int i=0; i<tokens.size(); i++) {
      ngrams.add( "[" + paddedTokens.get(i).toString() + ", " + paddedTokens.get(i+2) +"]");
    }
    return ngrams;
  }

  private void addHeadsFeaturesHelper(Derivation deriv, String ngram, List<String> treeseq, String subname, int N) {
    for (int n=1; n<N+1; n++) {
      for (String head : getAllNgrams(treeseq, n)) {
        deriv.addFeature(subname, head  + "-" + ngram);
      }
    }
  }
  private void addHeadsFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("heads")) return;
    //if (!deriv.cat.equals(Rule.rootCat)) return;
    if (deriv.rule != Rule.nullRule) {
      //deriv.addFeature("utter", convertDeriv(deriv) + " <===> " + ex.utterance);
      for (int n=1; n<opts.ngramN+1; n++) {
        for (String ngram : getAllNgrams(ex.languageInfo.tokens, n)) {
          addHeadsFeaturesHelper(deriv, ngram, getTopLevelTypes(deriv), "ng.type", 2);
          addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "ng.func", 2);
          //addHeadsFeaturesHelper(deriv, ngram, getOpSequence(deriv), "seq", 2);
        }
      }
      for (String ngram : getAllSkipGrams(ex.languageInfo.tokens)) {
        addHeadsFeaturesHelper(deriv, ngram, getTopLevelTypes(deriv), "sg.type", 2);
        addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "sg.func", 2);
        //addHeadsFeaturesHelper(deriv, ngram, getOpSequence(deriv), "seq", 2);
      }
    }
  }

  private void addContextFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("context")) return;
    List<List<NumberValue>> currentWall = CubeWorld.getWallFromContext(ex.context);
  }
}
