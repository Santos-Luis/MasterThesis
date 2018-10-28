package edu.stanford.nlp.sempre;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import fig.basic.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;

/**
 * A Master manages multiple sessions. Currently, they all share the same model,
 * but they need not in the future.
 */
public class Master {
  public static class Options {
    @Option(gloss = "Execute these commands before starting")
    public List<String> scriptPaths = Lists.newArrayList();
    @Option(gloss = "Execute these commands before starting (after scriptPaths)")
    public List<String> commands = Lists.newArrayList();
    @Option(gloss = "Write a log of this session to this path")
    public String logPath;

    @Option(gloss = "Print help on startup")
    public boolean printHelp = true;

    @Option(gloss = "Number of exchanges to keep in the context")
    public int contextMaxExchanges = 0;
    
    @Option(gloss = "Do pragmatic inference")
    public boolean bePragmatic = false;

    @Option(gloss = "make sessions independent")
    public boolean independentSessions = false;
   
    @Option(gloss = "Online update weights on new examples.")
    public boolean onlineLearnExamples = true;
    @Option(gloss = "Write out new examples to this directory")
    public String newExamplesPath;
    @Option(gloss = "Write out new parameters to this directory")
    public String newParamsPath;

    @Option(gloss = "Write out new grammar rules")
    public String newGrammarPath;

    //(L.S.) options created for the explicit and active learning processes
    @Option(gloss = "Mode where the user can provide explicit feedback")
    public boolean explicitMode = false;
    @Option(gloss = "Type of explicit feedback provided by the user")
    public int explicitModeType = 0;
    @Option(gloss = "At the end of each iteration we call the active learning")
    public boolean activeLearningNow = false;
    @Option(gloss = "Game with/without active learning")
    public boolean activeLearningGame = false;
  }

  public static Options opts = new Options();

  public class Response {
    // Example that was parsed, if any.
    Example ex;

    // Which derivation we're selecting to show
    int candidateIndex = -1;

    // Detailed information
    List<String> lines = new ArrayList<>();
    public Value commandResponse = null;
    
    public String getFormulaAnswer() {
      if (ex.getPredDerivations().size() == 0)
        return "(no answer)";
      else {
        Derivation deriv = getDerivation();
        return deriv.getFormula() + " => " + deriv.getValue();
      }
    }
    public String getAnswer() {
      if (ex.getPredDerivations().size() == 0)
        return "(no answer)";
      else {
        Derivation deriv = getDerivation();
        deriv.ensureExecuted(builder.executor, ex.context);
        return deriv.getValue().toString();
      }
    }
    public List<String> getLines() { return lines; }
    public Example getExample() { return ex; }
    public int getCandidateIndex() { return candidateIndex; }

    public Derivation getDerivation() {
      return ex.getPredDerivations().get(candidateIndex);
    }
  }
  private ParserState actualStates; // To print the actual states (L.S.)
  private String initialState; // The state presented to the user as initial (L.S.)
  private static HashMap<String, Integer> utterancePermutations = new HashMap<>(); // To store the combinations of utters. introduced (L.S.)
  private static HashMap<String, Integer> unigramsAL = new HashMap<>(); // To store the unigrams of utters. introduced (L.S.)
  private static ArrayList<String> alreadyAL = new ArrayList<String>(); // To store utters. asked by the system and ignored, during AL (L.S.)
  public static String activeLearningUtterance; // Utterance with more uncertainty, to be displayed in the AL (L.S.)
  public static Boolean al_on = false; // differentiate from AL or non-AL in the logs files (L.S.)

  private Builder builder;
  private Learner learner;
  private HashMap<String, Session> sessions = new LinkedHashMap<>();

  public Master(Builder builder) {
    this.builder = builder;
    if (!opts.independentSessions) {
      this.learner = new Learner(builder.parser, builder.params, new Dataset());
    }
  }

  public Params getParams() { return builder.params; }

  // Return the unique session identified by session id |id|.
  // Create a new session if one doesn't exist.
  public Session getSession(String id) {
    Session session = sessions.get(id);
    if (session == null) {
      session = new Session(id);
      
      if (opts.independentSessions) {
        session.useIndependentLearner(builder);
      }
      
      for (String path : opts.scriptPaths)
        processScript(session, path);
      for (String command : opts.commands)
        processQuery(session, command);
      if (id != null)
        sessions.put(id, session);
    }
    if (opts.independentSessions)
      builder.params = session.params;
    return session;
  }

  void printHelp() {
    LogInfo.log("Enter an utterance to parse or one of the following commands:");
    LogInfo.log("  (explicit): enter in a mode where the user can help the system with explicit feedback");
    LogInfo.log("  (help): show this help message");
    LogInfo.log("  (status): prints out status of the system");
    LogInfo.log("  (get |option|): get a command-line option (e.g., (get Parser.verbose))");
    LogInfo.log("  (set |option| |value|): set a command-line option (e.g., (set Parser.verbose 5))");
    LogInfo.log("  (reload): reload the grammar/parameters");
    LogInfo.log("  (grammar): prints out the grammar");
    LogInfo.log("  (params [|file|]): dumps all the model parameters");
    LogInfo.log("  (select |candidate index|): show information about the |index|-th candidate of the last utterance.");
    LogInfo.log("  (accept |candidate index|): record the |index|-th candidate as the correct answer for the last utterance.");
    LogInfo.log("  (answer |answer|): record |answer| as the correct answer for the last utterance (e.g., (answer (list (number 3)))).");
    LogInfo.log("  (rule |lhs| (|rhs_1| ... |rhs_k|) |sem|): adds a rule to the grammar (e.g., (rule $Number ($TOKEN) (NumberFn)))");
    LogInfo.log("  (type |logical form|): perform type inference (e.g., (type (number 3)))");
    LogInfo.log("  (execute |logical form|): execute the logical form (e.g., (execute (call + (number 3) (number 4))))");
    LogInfo.log("  (def |key| |value|): define a macro to replace |key| with |value| in all commands (e.g., (def type fb:type.object type)))");
    LogInfo.log("  (context [(user |user|) (date |date|) (exchange |exchange|) (graph |graph|)]): prints out or set the context");
    LogInfo.log("Press Ctrl-D to exit.");
  }

  void printHelpExplicit() {
    LogInfo.log("Select the type of explicit feedback to provide:");
    LogInfo.log("  (help): show this help message");
    LogInfo.log("  (normal): change to the normal mode");
    LogInfo.log("  (color): select a color and provide the corresponding utterance");
    LogInfo.log("  (position): select a position (left | right) and provide the corresponding utterance");
    LogInfo.log("  (color-pos): select a color OR a position (left | right) and provide the corresponding utterance");
  }

  public void runInteractivePrompt() {
    Session session = getSession("stdin");

    if (opts.printHelp)
      printHelp();

    while (true) {
      LogInfo.stdout.print("> ");
      LogInfo.stdout.flush();
      String line;
      try {
        line = LogInfo.stdin.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (line == null) break;

      int indent = LogInfo.getIndLevel();
      try {
        processQuery(session, line);
      } catch (Throwable t) {
        while (LogInfo.getIndLevel() > indent)
          LogInfo.end_track();
        t.printStackTrace();
      }
    }
  }

  // Read LispTrees from |scriptPath| and process each of them.
  public void processScript(Session session, String scriptPath) {
    Iterator<LispTree> it = LispTree.proto.parseFromFile(scriptPath);
    while (it.hasNext()) {
      LispTree tree = it.next();
      processQuery(session, tree.toString());
    }
  }

  // Process user's input |line|
  // Currently, synchronize a very crude level.
  // In the future, refine this.
  // Currently need the synchronization because of writing to stdout.
  public synchronized Response processQuery(Session session, String line) {
    line = line.trim();
    Response response = new Response();

    Boolean endIteration = false;         // To know if the current query is referent to the
    if (line.contains("%end%")) {         // end of an iteration (L.S.)
      line = line.replace("%end%", "");
      endIteration = true;
    }

    if (line.contains("execute")) {
      if (line.contains("activeLearning"))
        al_on = true;
      else
        al_on = false;
    }

    // Capture log output and put it into response.
    // Hack: modifying a static variable to capture the logging.
    // Make sure we're synchronized!
    StringWriter stringOut = new StringWriter();
    LogInfo.setFileOut(new PrintWriter(stringOut));
    
    if (!opts.explicitMode) {
      // ################## (L.S.) ###################
      // ## Facilitating the initialization and     ##
      // ## interaction with the non-interface menu ##
      // ##                                         ##

      if (line.startsWith("{")) {
        if (line.substring(1,6).equals("Level")) {
          String lvl = "";
          switch(line.substring(7,8)) {
            case "1":
              lvl = "remove";
              break;
            case "2":
              lvl = "babystep";
              break;
            case "3":
              lvl = "pattern";
              break;
            case "4":
              lvl = "babystack";
              break;
            case "5":
              lvl = "littlehouse";
              break;
            case "6":
              lvl = "triangle";
              break;
            case "7":
              lvl = "babynot";
              break;
            default:
              throw new RuntimeException("Invalid level");
          }

          LogInfo.log("");
          line = new String("(execute (call edu.stanford.nlp.sempre.cubeworld.StacksWorld.getLevel (string " + lvl + ")))");
          handleCommand(session, line, response);
          LogInfo.log("");
          line = new String("(context (graph NaiveKnowledgeGraph ((string " + initialState + ") (name b) (name c))))");
          handleCommand(session, line, response);
          LogInfo.log("Context updated to: " + initialState);
          LogInfo.log("");
        }
      
        else if (line.substring(1,8).equals("Choices")){
          printChoices();
        }
      }
      // ##                                         ##
      // #############################################
      else if (line.startsWith("(")) 
        handleCommand(session, line, response);
      else
        handleUtterance(session, line, response);
    } 
    else {                              // Explicit menu (L.S.)
      if (line.equals("(normal)")) {
        opts.explicitMode = false;
        printHelp();
      } else if (line.equals("(help)")) {
        printHelpExplicit();
      } else {
        handleExplicit(session, line, response);
      }
    }    
    
    // Clean up
    for (String outLine : stringOut.toString().split("\n"))
      response.lines.add(outLine);
    LogInfo.setFileOut(null);

    // Log interaction to disk
    if (!Strings.isNullOrEmpty(opts.logPath)) {
      PrintWriter out;
      if (opts.independentSessions) {
        out = IOUtils.openOutAppendHard(
            Paths.get(opts.logPath, session.id + ".log").toString());
      } else out = IOUtils.openOutAppendHard(opts.logPath);
      
      out.println(
          Joiner.on("\t").join(
              Lists.newArrayList(
                  "date=" + new Date().toString(),
                  "sessionId=" + session.id,
                  "remote=" + session.remoteHost,
                  "format=" + session.format,
                  "query=" + line,
                  "response=" + summaryString(response))));
      out.close();
    }


    /* ########################### (L.S.) ############################
     * ## We want to call the active learning cicle only after the  ##
     * ## user give feedback (so we do not interrupt any iteration) ##
     * ##                                                           ##
     */
    
    if(opts.activeLearningGame && opts.activeLearningNow && endIteration) {
      if(line.startsWith("(accept")) {
        unigramsAL = sortHashMapByValues(unigramsAL);
        ArrayList<String> unigramsHigherTwo = new ArrayList<String>();        // List of unigrams with count higher than 2
        for (String utter : unigramsAL.keySet()) {
          Integer count  = unigramsAL.get(utter);
          if (count > 2)
            unigramsHigherTwo.add(utter);
          else
            break;
        }

        if (!unigramsHigherTwo.isEmpty()) {
          HashMap<String, Double> entropies = new HashMap<String, Double>();
          for (String utter : unigramsHigherTwo) {
            builder.parser.opts.printAllDerivations = false;
            handleUtterance(session, utter, null);
            builder.parser.opts.printAllDerivations = true;
            List<Derivation> derivations = actualStates.predDerivations;
            List<Double> derivationsValues = new ArrayList<Double>();

            for(Derivation d : derivations) {
              derivationsValues.add(d.prob);
            }
            Double entropy = entropy(derivationsValues);
            entropies.put(utter, entropy);
          }
          // The 2 unigrams with max entropy
          String uniMax1 = Collections.max(entropies.entrySet(), Map.Entry.comparingByValue()).getKey();
          entropies.remove(uniMax1);
          String uniMax2 = (!entropies.isEmpty()) ? Collections.max(entropies.entrySet(), Map.Entry.comparingByValue()).getKey() : "";

          Boolean bothUnigrams = false;
          ArrayList<String> utterances = new ArrayList<String>(utterancePermutations.keySet());
          for (String utter : alreadyAL) {
            utterances.remove(utter);
          }
          for(String utter : utterances) {
            if (utter.contains(uniMax1) && utter.contains(uniMax2))
              bothUnigrams = true;
          }
          ArrayList<String> utterances_aux = new ArrayList<String>(utterances); // So we can iterate in the list while delete elements
          for(String utter : utterances_aux) {
            if (bothUnigrams) {
              if(!(utter.contains(uniMax1) && utter.contains(uniMax2))) {
                utterances.remove(utter);
              }
            }
            else {
              if(!(utter.contains(uniMax1) || utter.contains(uniMax2)))
                utterances.remove(utter);
            }
          }

          if (!utterances.isEmpty()) {
            entropies = new HashMap<String, Double>();
            for (String utter : utterances) {
              builder.parser.opts.printAllDerivations = false;
              handleUtterance(session, utter, null);
              builder.parser.opts.printAllDerivations = true;
              List<Derivation> derivations = actualStates.predDerivations;
              List<Double> derivationsValues = new ArrayList<Double>();

              for(Derivation d : derivations) {
                derivationsValues.add(d.prob);
              }
              Double entropy = entropy(derivationsValues);
              entropies.put(utter, entropy);
            }
            activeLearningUtterance = Collections.max(entropies.entrySet(), Map.Entry.comparingByValue()).getKey();
          }
          else {
            opts.activeLearningNow = false;
          }
        }
        else {
          opts.activeLearningNow = false;
        }
      }
    
      else if(line.startsWith("(execute")) {
        if (activeLearningUtterance != null) {
          defineActiveState(session, activeLearningUtterance, response);
          
          if(!alreadyAL.contains(activeLearningUtterance))
            alreadyAL.add(activeLearningUtterance);
        }
        else
          opts.activeLearningNow = false;
      }
    }
    else {
      opts.activeLearningNow = false;
    }

    /* ##                                                           ##
     * ###############################################################
     */
    
    return response;
  }

  String summaryString(Response response) {
    if (response.getExample() != null)
      return response.getFormulaAnswer();
    if (response.getLines().size() > 0)
      return response.getLines().get(0);
    return null;
  }

  private void handleUtterance(Session session, String query, Response response) {
    session.updateContext();

    // Create example
    Example.Builder b = new Example.Builder();
    b.setId("session:" + session.id);
    b.setUtterance(query);
    b.setContext(session.context);
    Example ex = b.createExample();

    ex.preprocess();

    // Parse!
    if (!opts.independentSessions)
      builder.parser.parse(builder.params, ex, false);
    else {
      actualStates = builder.parser.parse(session.params, ex, false);
    }
    
    if (!opts.activeLearningNow) { // So we dont print derivations and update context in AL (L.S.)
      response.ex = ex;
      ex.log();
      
      if (ex.predDerivations.size() > 0) {
        response.candidateIndex = 0;
        printDerivation(response.getDerivation());
      }
      session.updateContext(ex, opts.contextMaxExchanges);
    }


  }
  
  private void printDerivation(Derivation deriv) {
    // Print features
    HashMap<String, Double> featureVector = new HashMap<>();
    deriv.incrementAllFeatureVector(1, featureVector);
    FeatureVector.logFeatureWeights("Pred", featureVector, builder.params); 

    // Print choices
    Map<String, Integer> choices = new LinkedHashMap<>();
    deriv.incrementAllChoices(1, choices);
    FeatureVector.logChoices("Pred", choices);

    // Print denotation
    LogInfo.begin_track("Top formula");
    
    LogInfo.logs("%s", deriv.formula);
    LogInfo.end_track();
    if (deriv.value != null) {
      LogInfo.begin_track("Top value");
      deriv.value.log();
      LogInfo.end_track();
    }
  }


  /* ########################### (L.S.) ############################
   * ##             Funtions needed to handle the new             ##
   * ##      functionalites of active and explicit learning       ##
   * ##                                                           ##*/
   
  // Receives an utterance in the arr[] and makes the possible combinations of size N (size of data[])
  public static void combinationUtil(String arr[], String data[], int start, int end, int index, int r) {
      // Current combination is ready to be printed, print it
      if (index == r)
      {
          String s = "";
          for (int j = 0; j < r; j++) {
              s += data[j];
              if (j + 1 < r)
                  s += " ";
          }
          if(r == 1)
            unigramsAL.merge(s, 1, Integer::sum);
          else
            utterancePermutations.merge(s, 1, Integer::sum);
          
          return;
      }

      // replace index with all possible elements. The condition
      // "end-i+1 >= r-index" makes sure that including one element
      // at index will make a combination with remaining elements
      // at remaining positions
      for (int i=start; i<=end && end-i+1 >= r-index; i++)
      {
          data[index] = arr[i];
          combinationUtil(arr, data, i+1, end, index+1, r);
      }
  }

  private void printChoices() {
    List<Derivation> derivations = actualStates.predDerivations;
    for (int i = 0; i <= derivations.size()/2; i++) {
      String choice = derivations.get(i).getValue().toString();
      System.out.println("" + i + " - " + choice.substring(8, choice.length()-1));
    }
    System.out.println();
  }

  public Double entropy(List<Double> values) {
      Double entropy = 0.0;
      for (Double d : values) {
        entropy -= d*java.lang.Math.log(d);
      }
      return entropy;
  }

  // Method that prits the diffs. explicit menus and handle the receiving feedback (L.S.)
  private void handleExplicit(Session session, String line, Response response) {
    if (opts.explicitModeType == 0) {         // When the user is selecting the type of feedback
      if (line.startsWith("(color-pos)")) {
        String comm;
        if (!builder.online) {
          comm = new String("(execute (call edu.stanford.nlp.sempre.cubeworld.StacksWorld.getLevel (string explicit_2)))");
          handleCommand(session, comm, response);
        } else {
          initialState = line.substring(line.indexOf('['));
        }
        comm = new String("(context (graph NaiveKnowledgeGraph ((string " + initialState + ") (name b) (name c))))");
        handleCommand(session, comm, response);

        opts.explicitModeType = 3;
        LogInfo.log(initialState);
        LogInfo.log("Select the index correspondent to the cube with the desired color OR position, following by the utterance");
        LogInfo.log("Examples: 0 bleu  |  3 left");
        LogInfo.log("");
      }
      else {
        String comm = new String("(execute (call edu.stanford.nlp.sempre.cubeworld.StacksWorld.getLevel (string explicit_1)))");
        handleCommand(session, comm, response);
        comm = new String("(context (graph NaiveKnowledgeGraph ((string " + initialState + ") (name b) (name c))))");
        handleCommand(session, comm, response);

        if (line.equals("(color)")) {
          opts.explicitModeType = 1;
          LogInfo.log("[[0],[1],[2],[3]]");
          LogInfo.log("Select the index correspondent to the cube with the desired color, following by the utterance");
          LogInfo.log("Example: 0 bleu");
          LogInfo.log("");
        } else if(line.equals("(position)")) {
          opts.explicitModeType = 2;
          LogInfo.log("0-left  1-right");
          LogInfo.log("Select the index correspondent to the position desired (left | right), following by the utterance");
          LogInfo.log("Example: 1 droite");
          LogInfo.log("");
        }
      }
    }
    else {      // When the user is entering the feedback
      handleUtterance(session, line.substring(2), response);
      handleCommand(session, "(expl_acc " + line.substring(0,1) + ")", response);
      opts.explicitModeType = 0;
      printHelpExplicit();
    }
  }

  // Method to print the active learning menu and to handle the receiving feedback (L.S.)
  private void handleActiveLearning(Session session, String utter, Response response) {
    defineActiveState(session, utter, response);

    String lvl = new String("(context (graph NaiveKnowledgeGraph ((string " + initialState + ") (name b) (name c))))");
    LogInfo.log("");
    LogInfo.log("Welcome to active learning menu.");
    LogInfo.log("Considering the following first state, enter the correct option to the utterance: \"" + utter + "\"");
    LogInfo.log(initialState);
    handleCommand(session, lvl, response);
    LogInfo.log("");

    opts.activeLearningNow = false;

    processQuery(session, utter);
  }

  // Method to define the initial state displayed in the active learning
  private void defineActiveState(Session session, String utter, Response response) {
    String stateWithMaxDiff = "";
    Double maxDiff = 0.0;
    int[] randActionsIndex = new int[10];
    
    //Generates 10 Random Numbers in the range 0-117 (possible actions)
    for(int i = 0; i < randActionsIndex.length; i++) {
      randActionsIndex[i] = (int)(Math.random()*118);
    }
    //Generates 6 levels (from levels 0 and 1)
    int[] randStates = new int[6];
    for(int i = 0; i < randStates.length; i++) {
      if (i < 3)
        randStates[i] = 0;
      else
        randStates[i] = 1;
    }

    for(int i = 0; i < randStates.length ; i++) {
      String lvl = randStates[i] == 0 ? "remove" : "babystep";
      lvl = new String("(execute (call edu.stanford.nlp.sempre.cubeworld.StacksWorld.getLevel (string " + lvl + ")))");
      handleCommand(session, lvl, response);
      lvl = new String("(context (graph NaiveKnowledgeGraph ((string " + initialState + ") (name b) (name c))))");
      handleCommand(session, lvl, response);

      builder.parser.opts.printAllDerivations = false;
      handleUtterance(session, utter, response);
      builder.parser.opts.printAllDerivations = true;

      double avgDiff = 0.0;
      ArrayList<String> initialStateArray = statesToArray(initialState);
      for(int j : randActionsIndex) {
        String newState = actualStates.predDerivations.get(j).value.toLispTree().tail().head().toString();
        ArrayList<String> newStateArray = statesToArray(newState);
        avgDiff += diffBetweenStates(initialStateArray, newStateArray);
      }

      for (int j = 0; j < 4; j++) { // So we take in consideration the number of different colors
        for (String s : initialStateArray) {
          if (s.contains(""+j)) {
            avgDiff++;
            break;
          }
        }
      }

      avgDiff = avgDiff / (randActionsIndex.length + 1); // +1 to take into account the nr of diffs. colors aswell
      if (avgDiff > maxDiff) {
        maxDiff = avgDiff;
        stateWithMaxDiff = initialState;
      }
    }
    initialState = stateWithMaxDiff;
  }

  // Receives a string of states and return an ArrayList where each position is a column
  public ArrayList<String> statesToArray(String states) {
    ArrayList<String> statesArray = new ArrayList<String>();

    String s = states;
    s = s.substring(s.indexOf("[")+1, s.lastIndexOf("]"));

    while (s.contains("[")) {
      statesArray.add(s.substring(s.indexOf("[")+1, s.indexOf("]")));
      s = s.substring(s.indexOf("]")+1);
    }
    return statesArray;
  }

  // Returns the number of differences between the two states received
  public int diffBetweenStates(ArrayList<String> s1, ArrayList<String> s2) {
    if (s1.size() != s2.size())
      throw new RuntimeException("States comparation: states with different sizes.");

    int diffSize = 0;

    for (int i = 0; i < s1.size(); i++) {
      if (s1.get(i).contains(",")) {
        String[] parts1 = s1.get(i).split(",");
        if (s2.get(i).contains(",")) {
          String[] parts2 = s2.get(i).split(",");
          diffSize += Math.abs(parts1.length - parts2.length);
        } else {
          diffSize += Math.abs(parts1.length - s2.get(i).length());
        }
      } 
      else if (s2.get(i).contains(",")) {
        String[] parts2 = s2.get(i).split(",");
        diffSize += Math.abs(s1.get(i).length() - parts2.length); 
      }
      else {
        diffSize += Math.abs(s1.get(i).length() - s2.get(i).length());
      }
    }

    return diffSize;
  }

  public LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
    LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

    Object[] a = passedMap.entrySet().toArray();
    Arrays.sort(a, new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((Map.Entry<String, Integer>) o2).getValue()
                       .compareTo(((Map.Entry<String, Integer>) o1).getValue());
        }
    });
    for (Object e : a) {
        sortedMap.put(((Map.Entry<String, Integer>) e).getKey(), ((Map.Entry<String, Integer>) e).getValue());
    }
    return sortedMap;
  }

  /* ##                                                           ##
   * ###############################################################
   */



  private void handleCommand(Session session, String line, Response response) {
	  LispTree tree = LispTree.proto.parseFromString(line);
    tree = builder.grammar.applyMacros(tree);

    String command = tree.child(0).value;

    /* #################### (L.S.) ######################
     * ##        Code needed to handle the new         ##
     * ##   commands of active and explicit learning   ##
     * ##                                              ##
     */
    if (command.equals("explicit")) { 
      opts.explicitMode = true;
      printHelpExplicit();
    } else if (command.equals("activeLearning")) { 
      String utter = line.substring(line.indexOf(" ")+1, line.length()-1);
      handleActiveLearning(session, utter, response);
    } else if (command.equals("expl_acc")) { 
      Example ex = session.getLastExample();
      
      if(opts.explicitModeType == 1) {
        int color = Integer.parseInt(tree.child(1).value);
        ex.setColor(color);
      } else if (opts.explicitModeType == 2) {
        String position = tree.child(1).value.equals("0") ? "leftMost1" : "rightMost1";
        ex.setPosition(position);
      } else if (opts.explicitModeType == 3) {
        List<Integer> initialStateVec = new ArrayList<Integer>();
        String[] parts = initialState.split("]");
        for (int i = 0; i < parts.length; i++) {
          initialStateVec.add(Integer.parseInt(parts[i].substring(parts[i].length()-1)));
        }
        int color = initialStateVec.get(Integer.parseInt(tree.child(1).value));
        String position = "";
        if (Integer.parseInt(tree.child(1).value) == 0)
          position = "leftMost1";
        else if(Integer.parseInt(tree.child(1).value) == 3)
          position = "rightMost1";
        ex.setColor(color);
        ex.setPosition(position);
      }
      response.ex = ex;
      addNewExample(ex, session);
    } 
    /* ##                                              ##
     * ##################################################
     */


    else if (command == null || command.equals("help")) {
      printHelp();
    } else if (command.equals("status")) {
      LogInfo.begin_track("%d sessions", sessions.size());
      for (Session otherSession : sessions.values())
        LogInfo.log(otherSession + (session == otherSession ? " *" : ""));
      LogInfo.end_track();
      StopWatchSet.logStats();
    } else if (command.equals("reload")) {
      builder.build();
    } else if (command.equals("grammar")) {
      for (Rule rule : builder.grammar.rules)
        LogInfo.logs("%s", rule.toLispTree());
    } else if (command.equals("params")) {
      if (tree.children.size() == 1) {
        builder.params.write(LogInfo.stdout);
        if (LogInfo.getFileOut() != null)
          builder.params.write(LogInfo.getFileOut());
      } else {
        builder.params.write(tree.child(1).value);
      }
    } else if (command.equals("get")) {
      if (tree.children.size() != 2) {
        LogInfo.log("Invalid usage: (get |option|)");
        return;
      }
      String option = tree.child(1).value;
      LogInfo.logs("%s", getOptionsParser().getValue(option));
    } else if (command.equals("set")) {
      if (tree.children.size() != 3) {
        LogInfo.log("Invalid usage: (set |option| |value|)");
        return;
      }
      String option = tree.child(1).value;
      String value = tree.child(2).value;
      if (!getOptionsParser().parse(new String[] {"-" + option, value}))
        LogInfo.log("Unknown option: " + option);
    } else if (command.equals("select") || command.equals("accept") ||
               command.equals("s") || command.equals("a")) {
      // Select an answer
      if (tree.children.size() != 2) {
        LogInfo.logs("Invalid usage: (%s |candidate index|)", command);
        return;
      }
      Example ex = session.getLastExample();
      if (ex == null) {
        LogInfo.log("No examples - please enter a query first.");
        return;
      }
      int index = Integer.parseInt(tree.child(1).value);
      if (index < 0 || index >= ex.predDerivations.size()) {
        LogInfo.log("Candidate index out of range: " + index);
        return;
      }

      response.ex = ex;
      response.candidateIndex = index;
      session.updateContextWithNewAnswer(ex, response.getDerivation());
      printDerivation(response.getDerivation());

      // Add a training example.  While the user selects a particular derivation, there are three ways to interpret this signal:
      // 1. This is the correct derivation (Derivation).
      // 2. This is the correct logical form (Formula).
      // 3. This is the correct denotation (Value).
      // Currently:
      // - Parameters based on the denotation.
      // - Grammar rules are induced based on the denotation.
      // We always save the logical form and the denotation (but not the entire
      // derivation) in the example.
      if (command.equals("accept") || command.equals("a")) {
        ex.setTargetFormula(response.getDerivation().getFormula());
        ex.setTargetValue(response.getDerivation().getValue());
        ex.setContext(session.getContextExcludingLast());
        ex.setNBestInd(index);
        addNewExample(ex, session);
        opts.activeLearningNow = true; //(L.S.)
        /* 
         * Update the n-gram vector
         * (L.S.)
         */
        String arr[] = ex.utterance.split(" ");
        int r = 3; //tri-grams
        int n = arr.length;
        String data[]=new String[r];
        combinationUtil(arr, data, 0, n-1, 0, r);
        r = 2; //bi-grams
        data = new String[r];
        combinationUtil(arr, data, 0, n-1, 0, r);
        r = 1; //uni-grams
        data = new String[r];
        combinationUtil(arr, data, 0, n-1, 0, r);
      }
    } else if (command.equals("answer")) {
      if (tree.children.size() != 2) {
        LogInfo.log("Missing answer.");
      }

      // Set the target value.
      Example ex = session.getLastExample();
      if (ex == null) {
        LogInfo.log("Please enter a query first.");
        return;
      }
      ex.setTargetValue(Values.fromLispTree(tree.child(1)));
      addNewExample(ex, session);
    } else if (command.equals("rule")) {
      int n = builder.grammar.rules.size();
      builder.grammar.addStatement(tree.toString());
      for (int i = n; i < builder.grammar.rules.size(); i++)
        LogInfo.logs("Added %s", builder.grammar.rules.get(i));
      // Need to update the parser given that the grammar has changed.
      builder.parser = null;
      builder.buildUnspecified();
    } else if (command.equals("type")) {
      LogInfo.logs("%s", TypeInference.inferType(Formulas.fromLispTree(tree.child(1))));
    } else if (command.equals("execute")) {
      if (line.contains("activeLearning")) { //(L.S.)
        line = line.replace("activeLearning", "activeLearning" + initialState);
        tree = LispTree.proto.parseFromString(line);
        tree = builder.grammar.applyMacros(tree);
      }
      Example ex = session.getLastExample();
      ContextValue context = (ex != null ? ex.context : session.context);
      Executor.Response execResponse = builder.executor.execute(Formulas.fromLispTree(tree.child(1)), context);
      response.commandResponse = execResponse.value;
      if (!opts.explicitMode) // So we do not print the logs in explicit mode (L.S.)
        LogInfo.logs("%s", execResponse.value);
      // Hack to have the initial state in the format that we need (L.S.)
      initialState = "" + execResponse.value;
      initialState = initialState.substring(initialState.indexOf('['), initialState.indexOf('|'));
    } else if (command.equals("def")) {
      builder.grammar.interpretMacroDef(tree);
    } else if (command.equals("context")) {
      if (tree.children.size() == 1) {
        LogInfo.logs("%s", session.context);
      } else {
        session.context = new ContextValue(tree);
      }
    } else {
      LogInfo.log("Invalid command: " + tree);
    }
  }

  void addNewExample(Example origEx, Session session) {
    // Create the new example, but only add relevant information.
    Example ex = new Example.Builder()
        .setId(origEx.id)
        .setUtterance(origEx.utterance)
        .setTargetFormula(origEx.targetFormula)
        .setTargetValue(origEx.targetValue)
        .setNBestInd(origEx.NBestInd)
        .createExample();

    // When I tried to add this property to the Builder (initialization above)
    // it did not work. dunno why? So we add separately (L.S.)
    ex.learning_mode = opts.explicitMode ? "Explicit" : al_on ? "Active" : "Implicit";
    
    if (!Strings.isNullOrEmpty(opts.newExamplesPath) && !opts.independentSessions) {
      LogInfo.begin_track("Adding new example");
      Dataset.appendExampleToFile(opts.newExamplesPath, ex);
      LogInfo.end_track();
    }

    if (opts.onlineLearnExamples) {
      LogInfo.begin_track("Updating parameters");
      learner.onlineLearnExample(origEx);
      if (!Strings.isNullOrEmpty(opts.newParamsPath))
        builder.params.write(opts.newParamsPath);
      LogInfo.end_track();
    }
    
    if (opts.independentSessions) {
      if (opts.onlineLearnExamples) {
        LogInfo.warning("both independentSessions and onlineLearnExamples");
      }
      if (!Strings.isNullOrEmpty(opts.newExamplesPath)) {
        LogInfo.begin_track("Adding new example, independently");
        Dataset.appendExampleToFile( Paths.get(opts.newExamplesPath, session.id + ".lisp").toString(), ex);
        LogInfo.end_track();
      }
      LogInfo.begin_track("Updating parameters (independent)");
      if (opts.explicitMode) //(L.S.)
        session.learner.onlineExplicitLearnExample(origEx, opts.explicitModeType);
      else
        session.learner.onlineLearnExample(origEx);

      LogInfo.end_track();
    }

    int count = 0;
    for (String key : getParams().getWeights().keySet()) {
      Double value = getParams().getWeights().get(key);  
      System.out.println(key + " " + value);
      if (count == 20)
        break;
      else
        count++;
    }
  }

  public static OptionsParser getOptionsParser() {
    OptionsParser parser = new OptionsParser();
    // Dynamically figure out which options we need to load
    // To specify this:
    //   java -Dmodules=core,freebase
    List<String> modules = Arrays.asList(System.getProperty("modules", "core").split(","));

    // All options are assumed to be of the form <class>opts.
    // Read the module-classes.txt file, which specifies which classes are
    // associated with each module.
    List<Object> args = new ArrayList<Object>();
    for (String line : IOUtils.readLinesHard("module-classes.txt")) {

      // Example: core edu.stanford.nlp.sempre.Grammar
      String[] tokens = line.split(" ");
      if (tokens.length != 2) throw new RuntimeException("Invalid: " + line);
      String module = tokens[0];
      String className = tokens[1];
      if (!modules.contains(tokens[0])) continue;

      // Group (e.g., Grammar)
      String[] classNameTokens = className.split("\\.");
      String group = classNameTokens[classNameTokens.length - 1];

      // Object (e.g., Grammar.opts)
      Object opts = null;
      try {
        for (Field field : Class.forName(className).getDeclaredFields()) {
          if (!"opts".equals(field.getName())) continue;
          opts = field.get(null);
        }
      } catch (Throwable t) {
        System.out.println("Problem processing: " + line);
        throw new RuntimeException(t);
      }

      if (opts != null) {
        args.add(group);
        args.add(opts);
      }
    }

    parser.registerAll(args.toArray(new Object[0]));
    return parser;
  }

}
