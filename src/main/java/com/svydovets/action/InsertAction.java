package com.svydovets.action;

import com.svydovets.annotation.Column;
import com.svydovets.annotation.Table;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class InsertAction implements Action {

  private static final String INSERT_SQL = "INSERT INTO %s VALUES";
  private static final int PRIORITY = 0;

  private final Object entity;

  @Override
  @SneakyThrows
  public String configureRequest() {
    var tableName= entity.getClass().getDeclaredAnnotation(Table.class).name();
    var sb = new StringBuilder().append("(");
    for (Field field : entity.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      var value = field.get(entity);
      if (value != null) {
        sb.append("'").append(value).append("'").append(",");
      }
    }
    sb.deleteCharAt(sb.length() - 1).append(");");
    final String resultSql = String.format(INSERT_SQL, tableName).concat(sb.toString());
    return resultSql;
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }
}
