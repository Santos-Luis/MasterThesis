package edu.stanford.nlp.sempre.paraphrase.scripts;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.freebase.FbFormulasInfo;
import edu.stanford.nlp.sempre.freebase.FbFormulasInfo.BinaryFormulaInfo;
import edu.stanford.nlp.sempre.Formula;
import edu.stanford.nlp.sempre.JoinFormula;
import fig.basic.LogInfo;
import fig.basic.MapUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This take a log file from a run where we learn the contexts of an entire training set
 * (train and dev) and outputs the set of relations and entities that occurred in it -
 * this is for an oracle experiment where we try to paraphrase dev queries into
 * questions generated from Freebase.
 * By creating this dataset we don't have to handle all of Freebase and we know that
 * we have all of the relevant relations and entities (for questions for which we were
 * able to get a correct formula while doing the learning of context).
 *
 * TODO(joberant): can we delete this class?
 *
 * Currently the log file is 25.exec
 * @author jonathanberant
 *
 */
public final class GenerateOracleRelationsAndEntities {
  private GenerateOracleRelationsAndEntities() { }

  /**
   *
   * @param args
   * 0 - log file
   * 1 - relation file
   * 2 - entity file
   * 3 - relation + entity file
   * 4 - entity info file
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    Set<Formula> relations = new HashSet<>();
    Set<String> entities = new HashSet<>();
    // pick relations and entities
    LogInfo.log("Getting relations and entities from log file...");
    for (String line : IOUtils.readLines(args[0])) {
      if (line.contains("True@") || line.contains("Part@")) { // a formula that leads to the correct answer
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
        entities.add(jFormula.child.toString());
      }
    }
    LogInfo.logs("Number of relations: %s", relations.size());
    LogInfo.logs("Number of entites: %s", entities.size());

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
    LogInfo.logs("Number of entites: %s", entities.size());

    LogInfo.log("Get entity info");
    Map<String, Set<String>> entityToTypes = new HashMap<>();
    Map<String, String> entityToName = new HashMap<>();
    Map<String, Double> entityToPopularity = new HashMap<>();
    for (String line : IOUtils.readLines(args[4])) {
      String[] tokens = line.split("\t");
      String entity = tokens[1];

      if (entities.contains(entity)) {
        String name = tokens[3];
        double popularity = Double.parseDouble(tokens[2]);
        entityToName.put(entity, name);
        entityToPopularity.put(entity, popularity);
        String[] types = tokens[4].split(",");
        for (String type : types) {
          MapUtils.addToSet(entityToTypes, entity, type);
        }
      }
    }
    LogInfo.log("Writing oracle formulas");
    PrintWriter fWriter = IOUtils.getPrintWriter(args[3]);
    for (Formula relation : relations) {
      BinaryFormulaInfo bInfo = fbFormulasInfo.getBinaryInfo(relation);
      if (bInfo == null)
        throw new RuntimeException("Can not find info for relation " + relation);
      for (String entity : entities) {
        if (entityToTypes.get(entity).contains(bInfo.expectedType2)) {
          fWriter.println(relation + "\t" + entity + "\t" + entityToName.get(entity) + "\t" + entityToPopularity.get(entity));
        }
      }
    }
    fWriter.close();

    LogInfo.log("Writing relations");
    PrintWriter rWriter = IOUtils.getPrintWriter(args[1]);
    for (Formula relation : relations) {
      rWriter.println(relation);
    }
    rWriter.close();

    LogInfo.log("Writing entities");
    PrintWriter eWriter = IOUtils.getPrintWriter(args[2]);
    for (String entity : entities) {
      eWriter.println(entity);
    }
    eWriter.close();
  }

}
