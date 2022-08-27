package com.svydovets;

import com.svydovets.entity.Product;
import org.postgresql.ds.PGSimpleDataSource;

import java.math.BigDecimal;
import javax.sql.DataSource;

public class DemoApp {

  public static void main(String[] args) {
    var ds = initDataSource();
    var sf = new SessionFactory(ds);
    var session = sf.createSession();

    final Product product = session.find(Product.class, 99);
    session.remove(product);

    final Product newProduct2 = new Product();
    newProduct2.setId(5);
    newProduct2.setName("test");
    newProduct2.setPrice(BigDecimal.valueOf(99.99));

    session.persist(newProduct2);

    session.close();
  }

  private static DataSource initDataSource() {
    var ds = new PGSimpleDataSource();
    ds.setUrl("******");
    ds.setUser("***");
    ds.setPassword("***");
    ds.setCurrentSchema("***");
    return ds;
  }

}
