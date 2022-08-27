package com.svydovets;

import com.svydovets.entity.Product;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class DemoApp {

  public static void main(String[] args) {
    var ds = initDataSource();
    var sf = new SessionFactory(ds);
    var session = sf.createSession();

    final Product product = session.find(Product.class, 1);
    System.out.println(product);

    final Product anotherProduct = session.find(Product.class, 1);
    System.out.println(anotherProduct);

    System.out.println(product == anotherProduct);

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
