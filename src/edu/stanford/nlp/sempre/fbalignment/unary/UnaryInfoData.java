package edu.stanford.nlp.sempre.fbalignment.unary;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.sempre.Formula;
import edu.stanford.nlp.sempre.JoinFormula;
import edu.stanford.nlp.util.Pair;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public final class UnaryInfoData {
  private UnaryInfoData() { }

  private Map<String, UnaryInfoEntry> midToUnaryEntriesMap;

  private UnaryInfoData(Map<String, UnaryInfoEntry> midToUnaryEntriesMap) {
    this.midToUnaryEntriesMap = midToUnaryEntriesMap;
  }

  public static UnaryInfoData fromUnaryInfoFile(String unaryInfoFile) throws IOException {

    Map<String, UnaryInfoEntry> res = new TreeMap<String, UnaryInfoData.UnaryInfoEntry>();

    for (String line : IOUtils.readLines(unaryInfoFile)) {

      String[] tokens = line.split("\t");
      String mid = tokens[0];
      JoinFormula formula = (JoinFormula) Formula.fromString(tokens[1]);
      double popularity = Double.parseDouble(tokens[2]);
      String description = tokens[3];

      UnaryInfoEntry unaryInfoEntry = res.get(mid);
      if (unaryInfoEntry == null) {
        unaryInfoEntry = new UnaryInfoEntry(mid, formula, popularity, description);
        res.put(mid, unaryInfoEntry);
      } else {
        unaryInfoEntry.addDescription(description);
      }
    }
    LogInfo.log("Uploaded unary entries: " + res.size());
    return new UnaryInfoData(res);
  }

  public void saveToUnaryInfoFile(String file) throws IOException {

    PrintWriter writer = IOUtils.getPrintWriter(file);
    for (String mid : midToUnaryEntriesMap.keySet()) {

      UnaryInfoEntry entry = midToUnaryEntriesMap.get(mid);
      for (String description : entry.getDescriptions()) {
        writer.println(mid + "\t" + entry.unaryFormula + "\t" + entry.popularity + "\t" + description);
      }
    }
    writer.close();
  }

  public void fixBadIds(Map<String, String> mid2IdMap) {

    List<String> entriesToRemove = new LinkedList<String>();
    List<Pair<String, UnaryInfoEntry>> entriesToAdd = new LinkedList<Pair<String, UnaryInfoEntry>>();

    for (String mid : midToUnaryEntriesMap.keySet()) {

      UnaryInfoEntry entry = midToUnaryEntriesMap.get(mid);
      Formula correctChild = Formula.fromString(mid2IdMap.get(mid));
      Formula currentChild = entry.getUnaryFormulaChild();
      JoinFormula correctFormula = new JoinFormula(entry.unaryFormula.relation, correctChild);

      if (!correctChild.toString().equals(currentChild.toString())) {
        UnaryInfoEntry newEntry = new UnaryInfoEntry(entry.mid, correctFormula, entry.popularity, entry.descriptions);
        LogInfo.log("Old entry: " + entry + ", new entry: " + newEntry);
        entriesToRemove.add(mid);
        entriesToAdd.add(new Pair<String, UnaryInfoEntry>(mid, newEntry));
      }
    }
    LogInfo.log("Number of entries replaced: " + entriesToRemove.size());
    for (String mid : entriesToRemove) {
      midToUnaryEntriesMap.remove(mid);
    }
    for (Pair<String, UnaryInfoEntry> pair : entriesToAdd) {
      midToUnaryEntriesMap.put(pair.first(), pair.second());
    }
  }

  public void addAliases(String entityInfoFile) {

    int i = 0;
    for (String line : IOUtils.readLines(entityInfoFile)) {

      String[] tokens = line.split("\t");
      String mid = tokens[0];
      String id = tokens[1];
      String alias = tokens[3];
      if (midToUnaryEntriesMap.containsKey(mid)) {

        UnaryInfoEntry entry = midToUnaryEntriesMap.get(mid);
        if (entry.addDescription(alias))
          LogInfo.log("Added alias: " + alias + " to id: " + id);
      }
      i++;
      if (i % 1000000 == 0)
        LogInfo.log("Number of lines: " + i);
    }

  }

  public static class UnaryInfoEntry {

    public String mid;
    public JoinFormula unaryFormula;
    public double popularity;
    private Set<String> descriptions;

    public UnaryInfoEntry(String mid, JoinFormula formula, double popularity,
                          Set<String> descriptions) {
      this.mid = mid;
      this.unaryFormula = formula;
      this.popularity = popularity;
      this.descriptions = descriptions;
    }

    public UnaryInfoEntry(String mid, JoinFormula formula, double popularity,
                          String description) {
      this.mid = mid;
      this.unaryFormula = formula;
      this.popularity = popularity;
      this.descriptions = new HashSet<String>();
      descriptions.add(description);
    }

    public Set<String> getDescriptions() {
      return descriptions;
    }

    public void setDescriptions(Set<String> descriptions) {
      this.descriptions = descriptions;
    }

    public boolean addDescription(String description) {
      return this.descriptions.add(description);
    }
    /**
     * We assume the unary formula is basically an edge and a node:
     * <i>fb:type.object.type fb:en.lawyer</i>
     */
    public Formula getUnaryFormulaChild() {
      return unaryFormula.child;
    }

    public String toString() {
      return mid + "\t" + unaryFormula + "\t" + popularity + "\t" + descriptions;
    }
  }
}
