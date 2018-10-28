package edu.stanford.nlp.sempre.cubeworld;

import com.google.common.collect.Lists;
import fig.basic.*;
import edu.stanford.nlp.sempre.*;
import java.util.*;
import java.util.stream.IntStream;
/**
 *
 * @author Sida Wang
 */
public final class CubeWorld {
  public static class Options {
    @Option(gloss = "Verbosity")
    public int verbose = 0;
    @Option(gloss = "When performing a join with getProperty, do we want to deduplicate?")
    public boolean joinDedup = true;
    @Option(gloss = "operating entirely in lowercase")
    public boolean ignoreCase = true;
  }
  public static Options opts = new Options();

  public static final String COLOR = "COLOR";

  private static final Random random = new Random(1);

  public enum Color {
    Cyan(0), Brown(1), Red (2), Orange(3), Yellow(4), None(5);
    private final int value;
    private static final int MAXCOLOR = 4;
    private Color(int value) { this.value = value; }
    public int intValue() { return this.value; }
    public static NumberValue fromInt(int i) { return new NumberValue(i, COLOR); }
    public static NumberValue randomColor() { 
      int i = random.nextInt(5);
      return new NumberValue(i, COLOR); 
    }
    public static NumberValue randomColor(int max) { 
      int i = random.nextInt(max);
      return new NumberValue(i, COLOR); 
    }
    public static NumberValue randomColor(int[] colors) { 
      int i = colors[random.nextInt(colors.length)];
      return new NumberValue(i, COLOR); 
    }
    public static List<NumberValue> allColors() {
      List<NumberValue> allColors = new ArrayList<>();
      for (int i=0; i<MAXCOLOR; i++)
        allColors.add(Color.fromInt(i));
      return allColors;
    }
    
    public NumberValue toNumVal() { return new NumberValue(this.value, COLOR); }
  }

  public static int[] randColors(int limColor, int numColor) {
    int[] allcolors = IntStream.range(0,limColor).toArray();
    int[] somecolors = new int[numColor];
    for (int i=0; i<numColor; i++) {
      int next = i+random.nextInt(limColor-i);
      int tmp = allcolors[i];
      allcolors[i] = allcolors[next];
      allcolors[next] = tmp;
      somecolors[i] = allcolors[i];
    }
    return somecolors;
  }
  
  public static String getLevel(String level) {
    List<List<NumberValue>> wall = null;
    List<List<NumberValue>> target = null;
    if (false) {}
    else if (level.startsWith("0.0")) // remove
    {
      int[] rcolors = randColors(4,3);
      wall = randomWall(new int[]{2,3,4,5,6}, new int[]{1,2,3,4}, rcolors);
      ContextValue context = getContextFromWall(wall);
      if (random.nextBoolean()) {
        target = keepSet(getColor(Color.randomColor(rcolors), context), context);
      } else {
        if (random.nextBoolean())
          target = keepSet(topOfSet(getAll(context)), context);
        else
          target = keepSet(rightOfSet(getAll(context)), context);
      }
    }
    else if (level.startsWith("0.1")) // keep a color, or top or right
    {
      int[] rcolors = randColors(4,3);
      wall = randomWall(new int[]{2,3,4,5,6}, new int[]{1,2,3,4}, rcolors);
      ContextValue context = getContextFromWall(wall);
      if (random.nextBoolean()) {
        target = removeSet(getColor(Color.randomColor(rcolors), context), context);
      } else {
        if (random.nextBoolean())
          target = removeSet(topOfSet(getAll(context)), context);
        else
          target = removeSet(rightOfSet(getAll(context)), context);
      }
    }
    else if (level.startsWith("0.2")) { // change color
      int[] rcolors = randColors(4,3);
      wall = randomWall(new int[]{3,4,5}, new int[]{3,4,5}, rcolors);
      ContextValue context = getContextFromWall(wall);
      if (random.nextBoolean()) {
        target = changeSetColor(getColor(Color.fromInt(rcolors[0]), context),
            Color.fromInt(rcolors[1]), context);
      } else {
        if (random.nextBoolean())
          target = changeSetColor(topOfSet(getAll(context)), Color.fromInt(rcolors[1]), context);
        else
          target = changeSetColor(rightOfSet(getAll(context)), Color.fromInt(rcolors[1]), context);
      }
    }
    else if (level.startsWith("0.3")) { // stack
      int[] rcolors = randColors(4,3);
      wall = randomWall(new int[]{3,4,5,6}, new int[]{3,4,5,6}, rcolors);
      ContextValue context = getContextFromWall(wall);
      if (random.nextBoolean()) {
        target = stackOnTop(getColor(Color.fromInt(rcolors[0]), context),
            Color.fromInt(rcolors[1]), context);
      } else {
          target = stackOnTop(topOfSet(getAll(context)), Color.fromInt(rcolors[1]), context);
      }
    }
    else if (level.startsWith("0.4")) { // stack on top and some operations
      int[] rcolors = randColors(4,3);
      wall = randomWall(new int[]{5,6,8}, new int[]{4,5,6,7}, rcolors);
      ContextValue context = getContextFromWall(wall);
        target = stackOnTop(setIntersection(topOfSet(getAll(context)), getColor(Color.randomColor(rcolors), context)),
            Color.randomColor(rcolors), context);
    }
    
    else if (level.startsWith("1.0")) { // temp variable
      int[] color3 = randColors(4,3);
      int[] rcolors = new int[]{color3[0], color3[1]};
      wall = randomWall(new int[]{4,5,6,7,8}, new int[]{1,2,3,5}, rcolors);
      
      ContextValue context = getContextFromWall(wall);
      target = changeSetColor(getColor(Color.fromInt(color3[1]), context),
          Color.fromInt(color3[2]), context);
      context = getContextFromWall(target);
      target = changeSetColor(getColor(Color.fromInt(color3[0]), context),
          Color.fromInt(color3[1]), context);
      context = getContextFromWall(target);
      target = changeSetColor(getColor(Color.fromInt(color3[2]), context),
          Color.fromInt(color3[0]), context);

    }
    else if (level.startsWith("1.1")) { //great wall
      int base = random.nextInt(4);
      int next = (base + 1 + random.nextInt(3)) % 4;
      int[] rcolors = new int[]{base, next};
      wall = randomWall(new int[]{5,7,9}, new int[]{2}, rcolors);
      for (int s=0; s<wall.size(); s++)
      {
        wall.get(s).set(0, Color.fromInt(rcolors[s%2]));
        wall.get(s).set(1, Color.fromInt(rcolors[s%2]));
      }
      ContextValue context = getContextFromWall(wall);
      target = stackOnTop(setIntersection(
          topOfSet(getAll(context)),
          getColor(Color.fromInt(rcolors[0]), context)),
          Color.fromInt(rcolors[1]), context);

    }
    else if (level.startsWith("1.2")) { //checkerboard
      int[] color3 = randColors(4,3);
      wall = randomWall(new int[]{4}, new int[]{3}, color3);
      for (int s=0; s<wall.size(); s++)
      {
        for (int j=0; j<wall.get(0).size(); j++)
          wall.get(s).set(j, Color.fromInt(color3[(s+j)%2]));
      }
      ContextValue context = getContextFromWall(wall);
      target = stackOnTop(setIntersection(
          topOfSet(getAll(context)),
          getColor(Color.fromInt(color3[0]), context)),
          Color.fromInt(color3[2]), context);
      context = getContextFromWall(target);
      target = stackOnTop(setIntersection(
          topOfSet(getAll(context)),
          getColor(Color.fromInt(color3[1]), context)),
          Color.fromInt(color3[0]), context);
      context = getContextFromWall(target);
      target = changeSetColor(getColor(Color.fromInt(color3[2]), context),
          Color.fromInt(color3[1]), context);
    } 
    else if (level.startsWith("1.3")) { 
      char newChar = Character.forDigit(random.nextInt(4),10);
      wall = stringToWall("[[c],[c],[c],[c]]".replace('c', newChar));
      target = stringToWall("[[c,c,c,c],[c,c,c],[c,c],[c]]".replace('c', newChar));
      //cheating a bit here but oh well, java too verbose.
    } else {
      throw new RuntimeException("wall type not specified");
    }
    return wallToString(wall) + "|" + wallToString(target);
  }

  public static List<List<NumberValue>> randomWall(int[] stacks, int[] heights, int[] allowedColors) {
    List<List<NumberValue>> wall = new ArrayList<>();
    int numStack = stacks[random.nextInt(stacks.length)];
    for (int s = 0; s < numStack; s++)
    {
      int height = heights[random.nextInt(heights.length)];
      List<NumberValue> stack = new ArrayList<>();
      for (int h = 0; h < height; h++)
        stack.add(Color.randomColor(allowedColors));
      wall.add(stack);
    }
    return wall;
  }

  private CubeWorld() { }

  public static boolean colorsEqual(NumberValue c1, NumberValue c2)
  {
    if (!c1.unit.equals(COLOR) || !c2.unit.equals(COLOR)) return false;
    int c1v = (int) c1.value; int c2v = (int) c2.value;
    if (c1v != c2v) return false;
    return true;
  }

  // wallString consists of [[1,2,3],[2,1,2],[2,3,4]]
  public static List<List<NumberValue>> getWallFromContext(ContextValue context) {
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph)context.graph;
    String wallString = ((StringValue)graph.triples.get(0).e1).value;
    return stringToWall(wallString);
  }
  //wallString consists of [[1,2,3],[2,1,2],[2,3,4]]
  public static ContextValue getContextFromWall(List<List<NumberValue>> wall) {
    String wallString = wallToString(wall);
    LispTree tree = LispTree.proto.parseFromString(
        "(context (graph NaiveKnowledgeGraph ((string " + wallString + ") (name b) (name c))))");
    return new ContextValue(tree);
  }

  public static List<List<NumberValue>> stringToWall(String wallString) {
    List<List<NumberValue>> wall = new ArrayList<>();
    List<List<Integer>> intwall = Json.readValueHard(wallString, List.class);
    //throw new RuntimeException(a.toString()+a.get(1).toString());

    for (List<Integer> intstack : intwall) {
      List<NumberValue> stack = new ArrayList<>();
      for (Integer intcube : intstack) {
        stack.add(Color.fromInt(intcube.intValue()));
      }
      wall.add(stack);
    }
    return wall;
  }

  public static String wallToString(List<List<NumberValue>> wall) {
    List<List<Integer>> intwall = new ArrayList<>();
    for (List<NumberValue> stack : wall) {
      List<Integer> intstack = new ArrayList<>();
      for (NumberValue cube : stack) {
        intstack.add((int)cube.value);
      }
      intwall.add(intstack);
    }
    return Json.writeValueAsStringHard(intwall);
  }

  // well, here are all final actions that one can take, which returns a wall
  // considering returning a List< List<List<NumberValue>> > here, of the original and the new
  public static List<List<NumberValue>> removeSet(List<List<BooleanValue>> cubeset, ContextValue context) {
    return changeSetColor(getWallFromContext(context), cubeset, Color.None.toNumVal());
  }

  public static List<List<NumberValue>> keepSet(List<List<BooleanValue>> cubeset, ContextValue context) {
    return changeSetColor(getWallFromContext(context), setOperation(cubeset,cubeset,"complement"), Color.None.toNumVal());
  }

  public static List<List<NumberValue>> changeSetColor(List<List<BooleanValue>> cubeset, NumberValue color, ContextValue context) {
    return changeSetColor(getWallFromContext(context), cubeset, color);
  }
  public static List<List<NumberValue>> stackOnTop(List<List<BooleanValue>> cubeset, NumberValue color, ContextValue context) {
    return stackOnTop(getWallFromContext(context), cubeset, color);
  }

  private static List<List<NumberValue>> changeSetColor(List<List<NumberValue>> wall, List<List<BooleanValue>> cubeset, NumberValue color) {
    List<List<NumberValue>> newwall = new ArrayList<>();
    for (int s = 0; s < cubeset.size(); s++) {
      List<BooleanValue> stackset = cubeset.get(s);
      List<NumberValue> newstack = new ArrayList<>();
      for (int r = 0; r < stackset.size(); r++) {
        if (cubeset.get(s).get(r).value == true) {
          if ( !colorsEqual(color, Color.None.toNumVal()) )
            newstack.add(color);
        } else {
          newstack.add(wall.get(s).get(r));
        }
      }
      newwall.add(newstack);
    }
    return newwall;
  }

  private static List<List<NumberValue>> stackOnTop(List<List<NumberValue>> wall, List<List<BooleanValue>> cubeset, NumberValue color) {
    List<List<NumberValue>> newwall = new ArrayList<>();
    for (int s = 0; s < cubeset.size(); s++) {
      List<BooleanValue> stackset = cubeset.get(s);
      List<NumberValue> newstack = new ArrayList<>();
      for (int r = 0; r < stackset.size(); r++) {
        newstack.add(wall.get(s).get(r));
        if (cubeset.get(s).get(r).value == true) {
          newstack.add(color);
        }
      }
      newwall.add(newstack);
    }
    return newwall;
  }


  public static List<List<BooleanValue>> getColor(NumberValue color, ContextValue context) {
    List<List<BooleanValue>> cubeset = new ArrayList<>();
    for (List<NumberValue> stack : getWallFromContext(context)) {
      List<BooleanValue> newstack = new ArrayList<>();
      for (NumberValue cube : stack) {
        if (colorsEqual(cube, color) ) {
          newstack.add(new BooleanValue(true));
        } else {
          newstack.add(new BooleanValue(false));
        }
      }
      cubeset.add(newstack);
    }
    return cubeset;
  }
  public static List<List<BooleanValue>> getAll(ContextValue context) {
    List<List<BooleanValue>> retset = new ArrayList<>();
    List<List<NumberValue>>  wall = getWallFromContext(context);
    for (int s = 0; s < wall.size(); s++) {
      List<NumberValue> stackset = wall.get(s);
      List<BooleanValue> retstack = new ArrayList<>();
      for (int r = 0; r < stackset.size(); r++) {
        retstack.add(new BooleanValue(true));
      }
      retset.add(retstack);
    }
    return retset;
  }

  // Begin UnarySetOp
  public static List<List<BooleanValue>> complementOfSet(List<List<BooleanValue>> set) {
    return setOperation(set, set, "complement");
  }
  public static List<List<BooleanValue>> topOfSet(List<List<BooleanValue>> set) {
    return reverseSetStack(bottomOfSet(reverseSetStack(set)));
  }
  public static List<List<BooleanValue>> bottomOfSet(List<List<BooleanValue>> set) {
    List<List<BooleanValue>> retset = new ArrayList<>();
    for (int s = 0; s < set.size(); s++) {
      List<BooleanValue> stackset = set.get(s);
      List<BooleanValue> retstack = new ArrayList<>();
      boolean gotIt = false;
      for (int r = 0; r < stackset.size(); r++) {
        BooleanValue currentval = stackset.get(r);
        if (!gotIt && currentval.value)
        {
          retstack.add(new BooleanValue(true));
          gotIt = true;
        } else {
          retstack.add(new BooleanValue(false));
        }
      }
      retset.add(retstack);
    }
    return retset;
  }
  
  public static List<List<BooleanValue>> rightOfSet(List<List<BooleanValue>> set) {
    return reverseStacksLeft(leftOfSet(reverseStacksLeft(set)));
  }
  public static List<List<BooleanValue>> leftOfSet(List<List<BooleanValue>> set) {
    List<List<BooleanValue>> retset = new ArrayList<>(set);
    for (int r = 0; ;r++) {
      boolean gotLeftOfRowR = false;
      boolean exceededMaxRow = true;
      for (int s = 0; s < set.size(); s++) {
        List<BooleanValue> retstack = retset.get(s);
        if (r >= retstack.size()) continue;
        exceededMaxRow = false;
        if (!gotLeftOfRowR && retstack.get(r).value) {
          retstack.set(r, new BooleanValue(true));
          gotLeftOfRowR = true;
        }
        else
          retstack.set(r, new BooleanValue(false));
      }
      if (exceededMaxRow) break;
    }
    return retset;
  }
  private static List<List<BooleanValue>> reverseStacksLeft(List<List<BooleanValue>> set) {
    List<List<BooleanValue>> retset = new ArrayList<>(set);
    Collections.reverse(retset);
    return retset;
  }
  
  private static List<List<BooleanValue>> reverseSetStack(List<List<BooleanValue>> set) {
    List<List<BooleanValue>> retset = new ArrayList<>();
    for (int s = 0; s < set.size(); s++) {
      List<BooleanValue> stackset = set.get(s);
      List<BooleanValue> retstack = new ArrayList<>();
      for (int r = 0; r < stackset.size(); r++) {
        int revind = stackset.size()-1-r;
        retstack.add(new BooleanValue(stackset.get(revind).value));
      }
      retset.add(retstack);
    }
    return retset;
  }
  // End UnarySetOp

  // Begin BinarySetOp
  public static List<List<BooleanValue>> setUnion(List<List<BooleanValue>> set1, List<List<BooleanValue>> set2) {
    return setOperation(set1, set2, "union");
  }
  public static List<List<BooleanValue>> setIntersection(List<List<BooleanValue>> set1, List<List<BooleanValue>> set2) {
    return setOperation(set1, set2, "intersection");
  }
  public static List<List<BooleanValue>> setDifference(List<List<BooleanValue>> set1, List<List<BooleanValue>> set2) {
    return setOperation(set1, set2, "difference");
  }
  //End BinarySetOp
  public static List<List<BooleanValue>> setOperation(List<List<BooleanValue>> set1, List<List<BooleanValue>> set2, String op) {
    List<List<BooleanValue>> retset = new ArrayList<>();

    if (set1.size() != set2.size()) {
      throw new RuntimeException("Sets have different sizes while aggregating"); 
    }
    for (int s = 0; s < set1.size(); s++) {
      List<BooleanValue> stackset1 = set1.get(s);
      List<BooleanValue> stackset2 = set2.get(s);
      List<BooleanValue> retstack = new ArrayList<>();
      for (int r = 0; r < stackset1.size(); r++) {
        boolean val1 = stackset1.get(r).value;
        boolean val2 = stackset2.get(r).value;

        boolean opval;
        if ( op == "union" )
          opval = val1 || val2;
        else if (op == "intersection")
          opval = val1 && val2;
        else if (op == "difference")
          opval = val1 && !val2;
        else if (op == "complement")
          opval = !val1;
        else
          opval = val1;

        retstack.add(new BooleanValue(opval));
      }
      retset.add(retstack);
    }

    return retset;
  }


}
