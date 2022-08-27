package com.svydovets;

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
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

@RequiredArgsConstructor
public class Session {

  private static final String FIND_BY_ID = "SELECT * FROM %s WHERE id= ?";

  private final DataSource dataSource;

  private Map<EntityKey<?>, Object> cache = new HashMap<>();

  @SneakyThrows
  public <T> T find(Class<T> entityType, Object id) {
    var entityKey = new EntityKey<>(entityType, id);
    var entity = cache.computeIfAbsent(entityKey, this::loadFromDb);
    return entityType.cast(entity);
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
    cache.clear();
  }
}
