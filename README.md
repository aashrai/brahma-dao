[![CircleCI](https://circleci.com/gh/gozefo/brahma-dao/tree/master.svg?style=svg)](https://circleci.com/gh/gozefo/brahma-dao/tree/master)
[![codecov](https://codecov.io/gh/gozefo/brahma-dao/branch/master/graph/badge.svg)](https://codecov.io/gh/gozefo/brahma-dao)
# Bramha-Dao
An annotation processor which auto generates dao classes for specified entity classes with ```@GenerateDao``` annotation.
## About 
Simplify writing dao classes for specified entities by generating dao with basic funcionalities. ```@GenerateDao``` can only be used with [```@Entity```](https://docs.oracle.com/javaee/6/api/javax/persistence/Entity.html) annotated classes.

## Example
Here's a entity class for which a dao needs to be generated.
```java
package com.example;
import com.brahma.dao.annotations.GenerateDao;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@GenerateDao
@Entity
public class BrahmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
   
    private String orderId;
    public Integer getId() {
    	return this.id;
    }

    public String getOrderId() {
    	return this.orderId;
    }
    
}
```

### Generated Code
```java
package com.example.dao;

import com.brahma.testclass.TestEnum;
import io.dropwizard.hibernate.AbstractDAO;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

public class Brahma_BrahmaEntityDao extends AbstractDAO<BrahmaEntity> {

  public Brahma_BrahmaEntityDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public BrahmaEntity createOrUpdate(BrahmaEntity brahmaEntity) {

    return persist(brahmaEntity);
  }

  public BrahmaEntity getById(Integer id) {
    return get(id);
  }

  protected List<Predicate> getPredicateList(BrahmaEntity searchQuery,
      CriteriaBuilder criteriaBuilder, Root<BrahmaEntity> from) {
    List<Predicate> searchRestrictions = new ArrayList<>();
    if (searchQuery.getId() != null) {
      searchRestrictions.add(criteriaBuilder.equal(from.get("id"), searchQuery.getId()));
    }
    if (searchQuery.getOrderId() != null) {
      searchRestrictions.add(criteriaBuilder.equal(from.get("orderId"), searchQuery.getOrderId()));
    }
    return searchRestrictions;
  }
  protected Query<BrahmaEntity> getSearchQuery(BrahmaEntity searchQuery) {
    Session session = this.currentSession();
    CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
    CriteriaQuery<BrahmaEntity> query = criteriaBuilder.createQuery(BrahmaEntity.class);
    Root<BrahmaEntity> from = query.from(BrahmaEntity.class);
    CriteriaQuery<BrahmaEntity> select = query.select(from);
    List<Predicate> searchRestrictions = getPredicateList(searchQuery,criteriaBuilder,from);
    select = select.where(searchRestrictions.toArray(new Predicate[searchRestrictions.size()]));
    return session.createQuery(select);
  }

  public List<BrahmaEntity> search(BrahmaEntity searchQuery) {
      return getSearchQuery(searchQuery).list();
  }

  public BrahmaEntity searchUniqueResult(BrahmaEntity searchQuery) {
      return getSearchQuery(searchQuery).uniqueResult();
    }
}
```

## Project Brahma
This repo is a part of project brahma, a suite of annotation processors built with :hearts: by folks at [Gozefo]( https://www.gozefo.com/) enginnering to remove boilerplate in our java projects.

## Download


## License
MIT License

Copyright (c) 2018 gozefo

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
