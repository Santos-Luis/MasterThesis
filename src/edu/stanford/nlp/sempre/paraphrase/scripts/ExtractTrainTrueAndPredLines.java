package edu.stanford.nlp.sempre.paraphrase.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.FbFormulasInfo;
import edu.stanford.nlp.sempre.Formula;
import edu.stanford.nlp.sempre.JoinFormula;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;


/**
 * We have a file with true and part entities and relation for the entire train set
 * but unfortunately not for the train set when splitting with a dev set.
 * This script is intended to get just the lines that are in the train set
 * @author jonathanberant
 *
 */
public final class ExtractTrainTrueAndPredLines {
  private ExtractTrainTrueAndPredLines() { }

  public static void main(String[] args) throws IOException {
    // first get all train set (no dev) examples from some log file (440.exec)
    Set<String> trainExamples = new HashSet<>();
    boolean readExample = false;
    for (String line:IOUtils.readLines(args[0])) {
      if (line.contains("parsing_iter=0.train:")) {
        LogInfo.log("train example");
        readExample = true;
      }
      if (line.contains("parsing_iter=0.dev:")) {
        LogInfo.log("dev example");
        readExample = false;
      }
      if (readExample && line.contains("Example: ")) {
        trainExamples.add(line.trim());
      }
    }
    LogInfo.logs("Number of train examples=%s", trainExamples.size());
    // now go over log file and get all relations, types, and domains that are relevant
    Set<Formula> relations = new HashSet<>();
    boolean getRelations = false;
    for (String line:IOUtils.readLines(args[1])) { // 25.exec

      if (line.contains("Example: ")) {
        if (trainExamples.contains(line.trim())) {
          LogInfo.log("train example");
          getRelations = true;
        } else {
          LogInfo.log("dev example");
          getRelations = false;
        }
      }

      if (getRelations && (line.contains("True@") || line.contains("Part@"))) { // a formula that leads to the correct answer
        int fIndex = line.indexOf("(formula");
        int vIndex = line.indexOf("(value");
        if (fIndex == -1 || vIndex == -1)
          throw new RuntimeException("This true line does not have a formula and a value: " + line);
        String formulaDesc = line.substring(fIndex + 9, vIndex - 1);
        if (!(formulaDesc.charAt(0) == '(') || !(formulaDesc.charAt(formulaDesc.length() - 1) == ')'))
          throw new RuntimeException("Formula brackets are wrong " + formulaDesc);
        Formula formula = Formula.fromString(formulaDesc);
        if (!(formula instanceof JoinFormula))
          throw new RuntimeException("Not join formula: " + formula);
        JoinFormula jFormula = (JoinFormula) formula;
        if (jFormula.toString().contains("fb:common")) {
          LogInfo.logs("fb:common line: %s", line);
          continue;
        }
        if (jFormula.toString().contains("fb:type.object.type")) {
          LogInfo.logs("fb:type.object.type line: %s", line);
          continue;
        }
        if (jFormula.toString().contains("fb:base.")) {
          LogInfo.logs("fb:base line: %s", line);
          continue;
        }
        if (jFormula.toString().contains("fb:user.")) {
          LogInfo.logs("fb:user line: %s", line);
          continue;
        }
        relations.add(jFormula.relation);
      }
    }
    LogInfo.logs("Number of relations: %s", relations.size());

    LogInfo.log("Get rid of equivalent formulas...");
    Set<Formula> newRelations = new HashSet<>();
    FbFormulasInfo fbFormulasInfo = FbFormulasInfo.getSingleton();
    for (Formula relation : relations) {
      if (fbFormulasInfo.hasOpposite(relation)) {
        Formula equivalentFormula = fbFormulasInfo.equivalentFormula(relation);
        if (relations.contains(equivalentFormula)) {
          if (!relation.toString().contains("!"))
            newRelations.add(relation);
        } else {
          newRelations.add(relation);
        }
      } else {
        newRelations.add(relation);
      }
    }
    relations = newRelations;
    LogInfo.logs("Number of relations: %s", relations.size());
    // now we want to get the type and domain of all relations
    PrintWriter writer = IOUtils.getPrintWriter(args[2]);
    for (Formula relation : relations) {
      writer.println(relation);
    }
  }
}
