package com.svydovets.entity;

import com.svydovets.annotation.Column;
import com.svydovets.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Table(name = "products")
@Getter
@Setter
@ToString
public class Product {

  @Column(name = "id")
  private Integer id;

  @Column(name = "name")
  private String name;

  @Column(name = "price")
  private BigDecimal price;

}
