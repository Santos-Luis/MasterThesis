package edu.stanford.nlp.sempre.logicpuzzles;

import edu.stanford.nlp.sempre.*;

/**
 * Look up typtes for entities and properties in PuzzleKnowledgeGraph.
 * (Delegate all decisions to PuzzleTypeSystem.)
 *
 * Based heavily on tables.TableTypeLookup
 *
 * @author Robin Jia
 */
public class PuzzleTypeLookup implements TypeLookup {
  @Override public SemType getEntityType(String entity) {
    return PuzzleTypeSystem.getEntityTypeFromId(entity);
  }

  @Override public SemType getPropertyType(String property) {
    return PuzzleTypeSystem.getPropertyTypeFromId(property);
  }
}
