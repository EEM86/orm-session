package com.svydovets.action;

import com.svydovets.annotation.Table;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Arrays;

@RequiredArgsConstructor
public class DeleteAction implements Action {

  private static final String DELETE_SQL = "DELETE FROM %s WHERE id = ";
  private static final int PRIORITY = 1;

  private final Object entity;

  @Override
  @SneakyThrows
  public String configureRequest() {
    var tableName= entity.getClass().getDeclaredAnnotation(Table.class).name();
    var idField = Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(field -> field.getName().equals("id"))
        .findAny().orElseThrow();
    idField.setAccessible(true);
    final Object id = idField.get(entity);
    final String resultSql = String.format(DELETE_SQL, tableName).concat(String.valueOf(id));
    return resultSql;
  }

  @Override
  public int getPriority() {
    return PRIORITY;
  }
}
