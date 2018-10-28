package edu.stanford.nlp.sempre.logicpuzzles;

import java.util.*;

import edu.stanford.nlp.sempre.*;

/**
 * Manages the type system for PuzzleKnowledgeGraph.
 *
 * @author Robin Jia
 */
public abstract class PuzzleTypeSystem {
  public static final String ENTITY_PREFIX = "fb:entity.";
  public static final String UNARY_PREFIX = "fb:unary.";
  public static final String BINARY_PREFIX = "fb:binary.";
  public static final String RECORD_PREFIX = "fb:record.";
  public static final String ORDER_PREFIX = "fb:order.";
  public static final String ENTITY_TYPE_PREFIX = "fb:type.entity_";
  public static final String RECORD_TYPE_PREFIX = "fb:type.record_";

  public static boolean isEntity(NameValue value) {
    return value.id.startsWith(ENTITY_PREFIX);
  }

  public static boolean isUnary(NameValue value) {
    return value.id.startsWith(UNARY_PREFIX);
  }

  public static boolean isBinary(NameValue value) {
    return value.id.startsWith(BINARY_PREFIX);
  }

  public static boolean isRecord(NameValue value) {
    return value.id.startsWith(RECORD_PREFIX);
  }

  public static boolean isOrderRelation(NameValue value) {
    return value.id.startsWith(ORDER_PREFIX);
  }

  public static String createEntityId(String type, String entity) {
    return ENTITY_PREFIX + type.toLowerCase() + "_" + entity.toLowerCase();
  }

  public static String createUnaryId(String type) {
    return UNARY_PREFIX + type.toLowerCase();
  }

  public static String createUnaryId(String type, String name) {
    return UNARY_PREFIX + type.toLowerCase() + "_" + name.toLowerCase();
  }

  public static String createBinaryId(String relation, String type) {
    return BINARY_PREFIX + relation.toLowerCase() + "." + type.toLowerCase();
  }

  public static String createRecordId(String relation, List<String> entities) {
    return RECORD_PREFIX + createRecordDescription(relation, entities);
  }

  public static String createRecordDescription(String relation, List<String> entities) {
    String answer = relation.toLowerCase();
    for (String e: entities) {
      answer = answer + "_" + e.toLowerCase();
    }
    return answer;
  }

  public static String createOrderRelationId(String name, String type) {
    return ORDER_PREFIX + name.toLowerCase() + "." + type.toLowerCase();
  }

  public static NameValue createEntity(String type, String entity) {
    String id = createEntityId(type, entity);
    return new NameValue(id, entity);
  }

  public static NameValue createUnary(String type) {
    String id = createUnaryId(type);
    return new NameValue(id, type);
  }

  public static NameValue createUnary(String type, String name) {
    String id = createUnaryId(type, name);
    return new NameValue(id, name);
  }

  public static NameValue createBinary(String relation, String type) {
    String id = createBinaryId(relation, type);
    return new NameValue(id, relation);
  }

  public static NameValue createRecord(String relation, List<String> entities) {
    String id = createRecordId(relation, entities);
    String description = createRecordDescription(relation, entities);
    return new NameValue(id, description);
  }

  public static NameValue createOrderRelation(String name, String type) {
    String id = createOrderRelationId(name, type);
    return new NameValue(id, name);
  }

  public static SemType getEntityTypeFromId(String entity) {
    if (entity.startsWith(ENTITY_PREFIX)) {
      String type = entity.substring(ENTITY_PREFIX.length()).split("_")[0];
      return SemType.newAtomicSemType(ENTITY_TYPE_PREFIX + type);
    } else if (entity.startsWith(UNARY_PREFIX)) {
      String type = entity.substring(UNARY_PREFIX.length()).split("_")[0];
      return SemType.newAtomicSemType(ENTITY_TYPE_PREFIX + type);
    } else if (entity.startsWith(RECORD_PREFIX)) {
      String type = entity.substring(UNARY_PREFIX.length()).split("_")[0];
      return SemType.newAtomicSemType(RECORD_TYPE_PREFIX + type);
    }
    return null;
  }

  public static SemType getPropertyTypeFromId(String property) {
    if (property.startsWith(BINARY_PREFIX)) {
      String argType = ENTITY_TYPE_PREFIX + property.substring(BINARY_PREFIX.length()).split("\\.")[1];
      String retType = RECORD_TYPE_PREFIX + property.substring(BINARY_PREFIX.length()).split("\\.")[0];
      return SemType.newFuncSemType(argType, retType);
    } else if (property.startsWith(ORDER_PREFIX)) {
      String type = ENTITY_TYPE_PREFIX + property.substring(ORDER_PREFIX.length()).split("\\.")[1];
      return SemType.newFuncSemType(type, type);
    }
    return null;
  }
}
