package com.svydovets.entity;

public record EntityKey<T> (Class<T> clazz, Object id) {}
