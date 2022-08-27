package com.svydovets;

import com.svydovets.action.Action;
import com.svydovets.action.DeleteAction;
import com.svydovets.action.InsertAction;
import com.svydovets.annotation.Column;
import com.svydovets.annotation.Table;
import com.svydovets.entity.EntityKey;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.sql.DataSource;

@RequiredArgsConstructor
public class Session {

  private static final String FIND_BY_ID = "SELECT * FROM %s WHERE id= ?";

  private final DataSource dataSource;

  private Map<EntityKey<?>, Object> cache = new HashMap<>();

  private Queue<Action> actionQueue = new LinkedList<>();

  @SneakyThrows
  public <T> T find(Class<T> entityType, Object id) {
    var entityKey = new EntityKey<>(entityType, id);
    var entity = cache.computeIfAbsent(entityKey, this::loadFromDb);
    return entityType.cast(entity);
  }

  public void persist(Object entity) {
    var insertAction = new InsertAction(entity);
    actionQueue.add(insertAction);
  }

  public void remove(Object entity) {
    var deleteAction = new DeleteAction(entity);
    actionQueue.add(deleteAction);
  }

  public void flush() {
    actionQueue.stream()
        .sorted(Comparator.comparing(Action::getPriority))
        .forEach(this::fire);
  }

  private void fire(Action action) {
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        var request = action.configureRequest();
        System.out.println("REQUEST: " + request);
        statement.execute(request);
        actionQueue.remove(action);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  private <T> T loadFromDb(EntityKey<T> entityKey) {
    var entityType = entityKey.clazz();
    final Object id = entityKey.id();
    try (Connection connection = dataSource.getConnection()) {
      try (PreparedStatement ps = connection.prepareStatement(createSelectQuery(entityType))) {
        ps.setObject(1, id);
        System.out.println("Request: " + ps);
        var rs = ps.executeQuery();
        return extractEntity(rs, entityType);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  private <T> String createSelectQuery(Class<T> entityType) {
    var tableName = entityType.getDeclaredAnnotation(Table.class).name();
    return String.format(FIND_BY_ID, tableName);
  }

  @SneakyThrows
  private <T> T extractEntity(ResultSet rs, Class<T> type) {
    rs.next();
    var entity = type.getConstructor().newInstance();
    for (Field field : type.getDeclaredFields()) {
      var columnName = field.getDeclaredAnnotation(Column.class).name();
      field.setAccessible(true);
      var fieldValue = rs.getObject(columnName);
      field.set(entity, fieldValue);
    }
    return entity;
  }

  public void close() {
    flush();
    cache.clear();
  }
}
