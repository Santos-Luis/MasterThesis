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
public class StacksWorldFeatureComputer implements FeatureComputer {
  private static final String PREFIX = "edu\\.stanford\\.nlp\\.sempre\\.cubeworld\\.StacksWorld\\.";
  public static class Options {
    @Option(gloss = "Verbosity")
    public int verbose = 0;
    @Option(gloss = "the N in N-gram")
    public int ngramN = 2;
    @Option(gloss = "the rule gram number")
    public int rulegramN = 2;
    @Option(gloss = "the number of wildcards to pad")
    public int numWild = 3;
    @Option(gloss = "decides if we pad ngrams")
    public boolean padNgram = false;
    @Option(gloss = "only add projective features")
    public boolean projective = false;
  }
  public static Options opts = new Options();
  public static int kk = 0;

  @Override public void extractLocal(Example ex, Derivation deriv) {
    addNgramRuleFeatures(ex, deriv);
    addHeadsFeatures(ex, deriv);
    addTreeFeatures(ex, deriv);
    //addEntireFeatures(ex, deriv);
    //addNgramToLogicalFormFeatures(ex, deriv);
    addTreeMatchingGrams(ex,deriv);
    addSubtreeFeatures(ex,deriv);
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
      } else {
        heads.add(unverbosify(t.value));
      }
    }
    return heads;
  }

  private List<String> getTopLevelFuncs(Derivation deriv) {
    Formula formula = deriv.getFormula();
    LispTree tree = formula.toLispTree();
    return getTopLevelFuncs(tree);
  }
  // getColor setUnion Number
  private List<String> getTopLevelFuncs(LispTree tree) {
    List<String> heads = new ArrayList<>();
    if (!tree.isLeaf()) {
      String headstring = tree.head().value;
      if (headstring.equals("call")) {
        for (LispTree t : tree.tail().children) {
          if (!t.isLeaf())
            heads.addAll(getTopLevelFuncs(t));
          else
            heads.add("_"+unverbosify(t.value));
        }
      }
      else if (headstring.equals("number"))
        heads.add(unverbosify(tree.toString()));
      else if (headstring.equals("lambda")) {
        for (LispTree t : tree.tail().children) {
          if (!t.isLeaf())
            heads.addAll(getTopLevelFuncs(t));
          else
            heads.add("l_"+ unverbosify(t.value));
        }
      }
      else
        heads.add(headstring + "_" + unverbosify(tree.toString()));
    }
    return heads;
  }

  private String lambdaVar(LambdaFormula lambdaFormula) {
    return lambdaFormula.var.substring(0, 1);
  }
  private String getSubtreeFeature(Formula f, int depth) {
    return getSubtreeFeature(f, depth, false);
  }
  private String getSubtreeFeature(Formula f, int depth, boolean anchored) {
    if (depth == 0) return "*";
    if (f instanceof CallFormula) {
      CallFormula callFormula = (CallFormula) f;
      String func = "(" + unverbosify(callFormula.func.toString());
      for (int i = 0; i < callFormula.args.size(); i++) {
        func += " " + getSubtreeFeature(callFormula.args.get(i), depth - 1, anchored) ;
      }
      return func + ")";
    } else if (f instanceof LambdaFormula) {
      LambdaFormula lambdaFormula = (LambdaFormula) f;
      return "lambda_"+ lambdaVar(lambdaFormula) + "." + getSubtreeFeature(lambdaFormula.body, depth - 1, anchored);
    } else if (f instanceof ValueFormula<?>) {
      ValueFormula<?> valueFormula = (ValueFormula<?>) f;
      if (valueFormula.value instanceof NumberValue) {
        NumberValue numberValue = (NumberValue) valueFormula.value;
        if (anchored)
          return numberValue.unit;
        else
          return numberValue.value+"";
      }
      return valueFormula.toString();
    } else if (f instanceof VariableFormula) {
      VariableFormula variableFormula = (VariableFormula) f;
      return variableFormula.name;
    } else throw new RuntimeException("Unhandled formula type: " + f.getClass().toString());
  }
 //getColor setUnion Number
 private List<String> getTreeFeatures(LispTree tree, String prefix, int depth) {
   List<String> bstr = new ArrayList<>();
   if (depth==0) return bstr;
   
   if (!tree.isLeaf()) {
     String headstring = tree.head().value;
     if (headstring.equals("call"))  {
       int argnum = 1;
       String funcstr = unverbosify(tree.children.get(1).value);
       if (tree.children.size() == 2) { // if call takes no argument
         bstr.add(prefix + funcstr + ".0");
       } else {
         for (LispTree t : tree.tail().tail().children) {
           String currentprefix = prefix + funcstr + "." + argnum +"-";
           if (!t.isLeaf())
             bstr.addAll(getTreeFeatures(t, currentprefix, depth-1));
           else
             bstr.add(currentprefix + unverbosify(t.value));
           argnum++;
         }
       }
     }
     else if (headstring.equals("lambda"))
       bstr.addAll(getTreeFeatures(tree.tail().child(1), prefix, depth-1));
     else if (headstring.equals("number"))
       if (prefix.length()>0)
         bstr.add(prefix + tree.children.get(2));
       else
         bstr.add(prefix + unverbosify(tree.toString()));
     else
       bstr.add(prefix + ".X-" + unverbosify(tree.toString()));
   } else throw new RuntimeException("reached leaf at  " + tree.toString());
   return bstr;
 }

  //private static final Pattern replaceCallLambda = Pattern.compile("\\(|call|lambda|\\)");

  //getColor setUnion Number
  private List<String> getOpSequence(Derivation deriv) {
    String treestr = unverbosify(deriv.getFormula().toLispTree().toString());
    List<String> opseq = Lists.newArrayList(treestr.replace(')', ' ').replace('(', ' ').replaceAll("call", "")
        .replaceAll("lambda", "").split(" ",-1));
    opseq.removeAll(Arrays.asList(null,""));
    return opseq;
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
    if (opts.padNgram) {
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
    } else {
      List<String> paddedTokens = new ArrayList<>();
      //paddedTokens.add("*"); // have have weird consequnces
      paddedTokens.addAll(tokens);
      for (int i=0; i<paddedTokens.size()-n+1; i++) {
        List<String> current = new ArrayList<>(paddedTokens.subList(i, i+n));
        while (current.size() < opts.numWild)
          current.add("*"); // a bit weird, it does not support "* on cyan"
        ngrams.add( current.toString() );
      }
    }
    return ngrams;
  }
  private List<String> getAllSkipGrams(List<String> tokens) {
    List<String> ngrams = new ArrayList<>();
    List<String> paddedTokens = new ArrayList<>();
    if (tokens.size() < 3)
      return ngrams;
    paddedTokens.add("*");
    paddedTokens.addAll(tokens);
    paddedTokens.add("*");
    for (int i=0; i<tokens.size()-2; i++) {
      ngrams.add( "[" + tokens.get(i).toString() + ", *, " + tokens.get(i+2) +"]");
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
          //addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "ng.func", 3);
          addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "ng.func", 2);
          addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "ng.func", 1);
         // addHeadsFeaturesHelper(deriv, ngram, getOpSequence(deriv), "seq", 3);
        }
      }
      for (String ngram : getAllSkipGrams(ex.languageInfo.tokens)) {
        //addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "sg.func", 3);
        addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "sg.func", 2);
        addHeadsFeaturesHelper(deriv, ngram, getTopLevelFuncs(deriv), "sg.func", 1);
        //addHeadsFeaturesHelper(deriv, ngram, getOpSequence(deriv), "seq", 2);
      }
    }
  }
    
  private void addSubtreeFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("subtree")) return;
    if (deriv.rule != Rule.nullRule) {
      //List<String> anchorAbstractTokens = parametrizeAnchors(ex.languageInfo.tokens, deriv);
      List<String> anchorAbstractTokens = ex.languageInfo.tokens;
      for (int n=1; n<=opts.ngramN; n++) {
        for (String ngram : getAllNgrams(anchorAbstractTokens, n)) {
          deriv.addFeature("subtree", getSubtreeFeature(deriv.formula, 1, false) + " :: " + ngram);
          deriv.addFeature("subtree", getSubtreeFeature(deriv.formula, 2, false) + " :: " + ngram);
          deriv.addFeature("subtree", getSubtreeFeature(deriv.formula, 3, false) + " :: " + ngram);
        }
      }
      for (String ngram : getAllSkipGrams(anchorAbstractTokens)) {
        deriv.addFeature("subtree", getSubtreeFeature(deriv.formula, 1, false) + " :: " + ngram);
	      deriv.addFeature("subtree", getSubtreeFeature(deriv.formula, 2, false) + " :: " + ngram);
        deriv.addFeature("subtree", getSubtreeFeature(deriv.formula, 3, false) + " :: " + ngram);
      }
    }
  }

  private void addTreeFeaturesHelper(Derivation deriv, String ngram, List<String> treeseq, String subname) {
    for (String head : treeseq) {
      deriv.addFeature(subname, head  + "-" + ngram);
    }
  }

  private void addTreeFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("tree")) return;
    //if (!deriv.cat.equals(Rule.rootCat)) return;
    if (deriv.rule != Rule.nullRule) {
      //deriv.addFeature("utter", convertDeriv(deriv) + " <===> " + ex.utterance);
      for (int n=1; n<opts.ngramN+1; n++) {
        for (String ngram : getAllNgrams(ex.languageInfo.tokens, n)) {
          addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 1), "tree");
          addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 2), "tree");
          addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "tree");
          //addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 4), "tree"); //remove this for the std model
        }
      }
      for (String ngram : getAllSkipGrams(ex.languageInfo.tokens)) {
         // addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 1), "tree");
         // addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 2), "tree");
         // addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "tree"); //remove this for the std model
      }
    }
  }
  
  private void addTreeMatchingGrams(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("mtree")) return;
    //if (!deriv.cat.equals(Rule.rootCat)) return;
    if (deriv.rule != Rule.nullRule) {
      //deriv.addFeature("utter", convertDeriv(deriv) + " <===> " + ex.utterance);
      for (String ngram : getAllNgrams(ex.languageInfo.tokens, 1)) {
        addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 1), "mtree");
        addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 2), "mtree");
      }
      for (String ngram : getAllNgrams(ex.languageInfo.tokens, 2)) {
        addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 2), "mtree");
        addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "mtree");
      }
      //for (String ngram : getAllNgrams(ex.languageInfo.tokens, 2))
      //  addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "mtree");
      //addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "mtree");
      //addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 4), "tree"); //remove this for the std model

    }
    for (String ngram : getAllSkipGrams(ex.languageInfo.tokens)) {
      // addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "mtree");
      // addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "mtree");
      // addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 3), "tree"); //remove this for the std model
    }
  }

  
  private void addEntireFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("mem")) return;
    //if (!deriv.cat.equals(Rule.rootCat)) return;
    if (deriv.rule != Rule.nullRule) {
      //deriv.addFeature("utter", convertDeriv(deriv) + " <===> " + ex.utterance);
      deriv.addFeature("mem", convertDeriv(deriv) + " <===> " + ex.utterance);
    }
  }
  // same as the tree features, except now just add stuff
  private void addNgramToLogicalFormFeatures(Example ex, Derivation deriv) {
    if (!FeatureExtractor.containsDomain("logf") && !FeatureExtractor.containsDomain("logftree")) return;
    //if (!deriv.cat.equals(Rule.rootCat)) return;
    if (deriv.rule != Rule.nullRule) {
      //deriv.addFeature("utter", convertDeriv(deriv) + " <===> " + ex.utterance);
      for (int n=1; n<opts.ngramN+1; n++) {
        for (String ngram : getAllNgrams(ex.languageInfo.tokens, n)) {
          if (FeatureExtractor.containsDomain("logf"))
            deriv.addFeature("logf", convertDeriv(deriv)  + "<==>" + ngram);
          else
            addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 1), "logftree");
        }
      }
      for (String ngram : getAllSkipGrams(ex.languageInfo.tokens)) {
        if (FeatureExtractor.containsDomain("logf"))
          deriv.addFeature("logf", convertDeriv(deriv)  + "<==>" + ngram);
        else
          addTreeFeaturesHelper(deriv, ngram, getTreeFeatures(deriv.formula.toLispTree(), "", 1), "logftree");
      }
    }
  }
}
