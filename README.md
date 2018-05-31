# Bramha-Dao
Annotation processor to generate [Hibernate](http://hibernate.org/orm/) DAO.
## About
Auto generate DAO for ```@Entity``` classes with support for annotation driven validations, defaults, sorting, filtering and much more. Changes to the entity classes are automatically reflected in the generated DAO class, have cleaner codebases and simpler PR's.
> Focus on codebase not on **Predicates**.

## Example
To generate a DAO annotate your ```@Entity``` class with ```@GenerateDao```.
```java
package com.example;

@GenerateDao //Annotation to generate DAO class
@Entity
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String orderId;
    private String customerName;
}
```

### Generated Code
Brahma-Dao create a DAO with CRUD methods already generated and basic search API. We provide a more in depth search api, filtering, sorting etc, please refer the [Wiki](https://github.com/gozefo/brahma-dao/wiki) to know more. 
You can always extend this class to add more custom functionality.
```java
package com.example.dao;

public class Brahma_DeliveryDao extends AbstractDAO<Delivery> {

  public Brahma_DeliveryDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Delivery createOrUpdate(Delivery delivery) {
    return persist(delivery);
  }

  public Delivery getById(Integer id) {
    return get(id);
  }

  //Any attribute/column changes in the  entity class are automatically reflected here
  protected List<Predicate> getPredicateList(Delivery searchQuery,
      CriteriaBuilder criteriaBuilder, Root<Delivery> from) {
    List<Predicate> searchRestrictions = new ArrayList<>();
    if (searchQuery.getId() != null) {
      searchRestrictions.add(criteriaBuilder.equal(from.get("id"), searchQuery.getId()));
    }
    if (searchQuery.getOrderId() != null) {
      searchRestrictions.add(criteriaBuilder.equal(from.get("orderId"), searchQuery.getOrderId()));
    }
    if (searchQuery.getCustomerName() != null) {
      searchRestrictions.add(criteriaBuilder.equal(from.get("customerName"), searchQuery.getCustomerName()));
    }
    return searchRestrictions;
  }

  public List<Delivery> search(Delivery searchQuery) {
      return getSearchQuery(searchQuery).list();
  }

  public Delivery searchUniqueResult(Delivery searchQuery) {
      return getSearchQuery(searchQuery).uniqueResult();
    }
}
```
### Go Beyond
Create more powerful DAO's with support for validations, defaults, sorting, filtering and much more. Refer the [Wiki](https://github.com/gozefo/brahma-dao/wiki) to know more.

## Project Brahma
This repo is a part of project brahma, a suite of annotation processors built with :hearts: by folks at [Gozefo]( https://www.gozefo.com/) engineering to remove boilerplate in our java projects.

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
