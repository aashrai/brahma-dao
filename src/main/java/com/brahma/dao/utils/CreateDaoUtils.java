package com.brahma.dao.utils;

import com.brahma.dao.annotations.Default;
import com.brahma.dao.annotations.GenerateDao;
import com.brahma.dao.meta.SearchMeta;
import com.brahma.dao.meta.SortType;
import com.squareup.javapoet.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.tools.Diagnostic;
import javax.ws.rs.ProcessingException;
import java.util.ArrayList;
import java.util.List;

public class CreateDaoUtils {


    public static MethodSpec createPersistMethod(TypeElement typeElement, ClassName entityClass, Element
            annotatedElement, Messager messager) {
        MethodSpec.Builder createOrUpdate = MethodSpec.methodBuilder("createOrUpdate");

        annotatedElement.getEnclosedElements().stream().filter(field -> field.getKind().isField()).forEach(field -> {


            if (ClassName.get(field.asType()).isPrimitive()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Primitive type cannot be added to predicate list.Please" +
                        " check the type declared in the class with @GenerateDao");
            }

            if ((field.getAnnotation(Default.class) != null)) {

                String parameterName = AnnotatedClassUtils.getParameterName(typeElement);
                String getMethod = AnnotatedFieldUtils.getMethod(field);
                String setMethod = AnnotatedFieldUtils.setMethod(field);
                String value = AnnotatedFieldUtils.getDefaultValue(field);

                if (AnnotatedFieldUtils.getClassType(field).equals("java.lang.Enum")) {

                    createOrUpdate.beginControlFlow("if($N.$N == null)", parameterName,
                            getMethod)
                            .addStatement("$N.$N($T.$N)", parameterName, setMethod, AnnotatedFieldUtils.getCastType
                                    (field), value)
                            .endControlFlow();
                } else {
                    createOrUpdate.beginControlFlow("if($N.$N == null)", parameterName,
                            getMethod)
                            .addStatement("$N.$N($T.valueOf($S))", parameterName, setMethod, AnnotatedFieldUtils
                                    .getCastType(field), value)
                            .endControlFlow();

                }

            }
        });
        return createOrUpdate.addModifiers(Modifier.PUBLIC)
                .returns(entityClass)
                .addParameter(ParameterSpec.builder(entityClass, AnnotatedClassUtils.getParameterName(typeElement))
                        .build())
                .addStatement("return persist($N)", AnnotatedClassUtils.getParameterName(typeElement))
                .build();
    }

    public static MethodSpec createConstructor() {

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(SessionFactory.class), "sessionFactory")
                .addStatement("super($N)", "sessionFactory")
                .build();

    }

    public static MethodSpec addToPredicateListMethod(Element annotatedElement,
                                                      Messager messager, ClassName entityClass) {

        MethodSpec.Builder addToPredicateList = MethodSpec.methodBuilder("getPredicateList")
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)))
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery").build())
                .addParameter(ParameterSpec.builder(CriteriaBuilder.class, "criteriaBuilder").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass)
                        , "from").build())
                .addStatement("$T<$T> searchRestrictions = new $T<>()", ClassName.get(List.class), ClassName.get
                                (Predicate.class),
                        ClassName.get(ArrayList.class));
        annotatedElement.getEnclosedElements().stream().filter(field -> field.getKind().isField()).forEach(field -> {


            if (ClassName.get(field.asType()).isPrimitive()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Primitive type cannot be added to predicate list \n " +
                        "please check the type declared in the class with @GenerateDao");
            }

            if ((field.getAnnotation(Transient.class) == null) && (field.getAnnotation
                    (OneToMany.class) == null) && (field.getAnnotation(ManyToOne.class) == null)) {

                String parameterName = "searchQuery";
                String getMethod = AnnotatedFieldUtils.getMethod(field);

                addToPredicateList.beginControlFlow("if ($N.$N != null)", parameterName,
                        getMethod)
                        .addStatement("$N.add($N.equal($N.get($S), $N.$N))", "searchRestrictions",
                                "criteriaBuilder", "from", field.getSimpleName(), parameterName, getMethod)
                        .endControlFlow();
            }
        });
        return addToPredicateList.addStatement("return searchRestrictions").build();


    }

    public static MethodSpec createGetSearchQueryMethodWithPredicateList(ClassName entityClass) {
        return MethodSpec.methodBuilder("getSearchQueryWithPredicateList")
                .returns(ParameterizedTypeName.get(ClassName.get(Query.class), entityClass))
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass)
                        , "from").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(CriteriaQuery.class),
                        entityClass), "query").build())
                .addStatement("$T session = this.currentSession()", Session.class)
                .addStatement("$T<$T> select = $N.select($N)", CriteriaQuery.class, entityClass, "query", "from")
                .addStatement("select = $N.where($N.toArray(new $T[$N.size()]))", "select", "searchRestrictions",
                        Predicate.class, "searchRestrictions")
                .addStatement("return $N.createQuery($N)", "session", "select")
                .build();


    }


    public static MethodSpec createGetSearchQueryMethod(ClassName entityClass) {

        return MethodSpec.methodBuilder("getSearchQuery")
                .addModifiers(Modifier.PROTECTED)
                .returns(ParameterizedTypeName.get(ClassName.get(Query.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addStatement("$T session = this.currentSession()", Session.class)
                .addStatement("$T criteriaBuilder = $N.getCriteriaBuilder()", CriteriaBuilder.class, "session")
                .addStatement("$T<$T> query = $N.createQuery($T.class)", CriteriaQuery.class, entityClass,
                        "criteriaBuilder", entityClass)
                .addStatement("$T<$T> from = $N.from($T.class)", Root.class, entityClass, "query", entityClass)
                .addStatement("$T<$T> select = $N.select($N)", CriteriaQuery.class, entityClass, "query", "from")
                .addStatement("$T<$T> searchRestrictions = getPredicateList($N,$N,$N)", List.class, Predicate.class,
                        "searchQuery",
                        "criteriaBuilder", "from")
                .addStatement("select = $N.where($N.toArray(new $T[$N.size()]))", "select", "searchRestrictions",
                        Predicate.class, "searchRestrictions")
                .addStatement("return $N.createQuery($N)", "session", "select")
                .build();

    }

    public static MethodSpec createGetSearchQueryMethodWithParams(ClassName entityClass) {

        return MethodSpec.methodBuilder("getSearchQueryWithParams")
                .addModifiers(Modifier.PROTECTED)
                .returns(ParameterizedTypeName.get(ClassName.get(Query.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterSpec.builder(SearchMeta.class, "searchParams").build())
                .addStatement("$T session = this.currentSession()", Session.class)
                .addStatement("$T criteriaBuilder = $N.getCriteriaBuilder()", CriteriaBuilder.class, "session")
                .addStatement("$T<$T> query = $N.createQuery($T.class)", CriteriaQuery.class, entityClass,
                        "criteriaBuilder", entityClass)
                .addStatement("$T<$T> from = $N.from($T.class)", Root.class, entityClass, "query", entityClass)
                .addStatement("$T<$T> select = $N.select($N)", CriteriaQuery.class, entityClass, "query", "from")
                .beginControlFlow("if ($N.getSortBy() != null)", "searchParams")
                .addStatement("$T $N = $N.getSortBy()", String.class, "sortByValue", "searchParams")
                .beginControlFlow("if ($N.getSortType() == ($T.DESC))", "searchParams", SortType.class)
                .addStatement("$N.orderBy($N.desc(from.get($N)))", "select", "criteriaBuilder", "sortByValue")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$N.orderBy($N.asc(from.get($N)))", "select", "criteriaBuilder", "sortByValue")
                .endControlFlow()
                .endControlFlow()
                .addStatement("$T<$T> searchRestrictions = getPredicateList($N,$N,$N)", List.class, Predicate.class,
                        "searchQuery",
                        "criteriaBuilder", "from")
                .addStatement("select = $N.where($N.toArray(new $T[$N.size()]))", "select", "searchRestrictions",
                        Predicate.class, "searchRestrictions")
                .addStatement("return $N.createQuery($N)", "session", "select")
                .build();

    }

    public static MethodSpec createGetSearchQueryWithParamsAndPredicateList(ClassName entityClass) {
        return MethodSpec.methodBuilder("getSearchQueryWithParamsAndPredicateList")
                .addModifiers(Modifier.PROTECTED)
                .returns(ParameterizedTypeName.get(ClassName.get(Query.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterSpec.builder(SearchMeta.class, "searchParams").build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass)
                        , "from").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(CriteriaQuery.class),
                        entityClass), "query").build())
                .addStatement("$T session = this.currentSession()", Session.class)
                .addStatement("$T criteriaBuilder = $N.getCriteriaBuilder()", CriteriaBuilder.class, "session")
                .addStatement("$T<$T> select = $N.select($N)", CriteriaQuery.class, entityClass, "query", "from")
                .beginControlFlow("if ($N.getSortBy() != null)", "searchParams")
                .addStatement("$T $N = $N.getSortBy()", String.class, "sortByValue", "searchParams")
                .beginControlFlow("if ($N.getSortType() == ($T.DESC))", "searchParams", SortType.class)
                .addStatement("$N.orderBy($N.desc(from.get($N)))", "select", "criteriaBuilder", "sortByValue")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("$N.orderBy($N.asc(from.get($N)))", "select", "criteriaBuilder", "sortByValue")
                .endControlFlow()
                .endControlFlow()
                .addStatement("select = $N.where($N.toArray(new $T[$N.size()]))", "select", "searchRestrictions",
                        Predicate.class, "searchRestrictions")
                .addStatement("return $N.createQuery($N)", "session", "select")
                .build();

    }


    public static MethodSpec createSearchMethod(ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addStatement("return $N($N).list()", "getSearchQuery", "searchQuery").build();


    }

    public static MethodSpec createSearchMethodWithParams(ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .addException(ProcessingException.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterSpec.builder(SearchMeta.class, "searchParams").build())
                .beginControlFlow("if (searchParams != null)")
                .addStatement("$T querySize = $N.getMaxResults() != null ? $N.getMaxResults() : $L", Integer.class,
                        "searchParams", "searchParams", 1000)
                .addStatement("$T queryIndex = $N.getFirstResult() != null ? $N.getFirstResult() : $L", Integer
                        .class, "searchParams", "searchParams", 0)
                .addStatement("return $N($N,$N).setMaxResults($N).setFirstResult($N).list()",
                        "getSearchQueryWithParams", "searchQuery", "searchParams", "querySize", "queryIndex")
                .endControlFlow()
                .addStatement("throw new $T($S)", ProcessingException.class, "searchParams object found null ")
                .build();

    }

    public static MethodSpec createSearchMethodWithPredicateList(ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass)
                        , "from").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(CriteriaQuery.class),
                        entityClass), "query").build())
                .addStatement("return $N($N, $N, $N, $N).list()", "getSearchQueryWithPredicateList", "searchQuery",
                        "searchRestrictions", "from", "query").build();

    }

    public static MethodSpec createSearchMethodWithParamsAndPredicateList(ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .addException(ProcessingException.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterSpec.builder(SearchMeta.class, "searchParams").build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass)
                        , "from").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(CriteriaQuery.class),
                        entityClass), "query").build())

                .beginControlFlow("if (searchParams != null)")
                .addStatement("$T querySize = $N.getMaxResults() != null ? $N.getMaxResults() : $L", Integer.class,
                        "searchParams", "searchParams", 1000)
                .addStatement("$T queryIndex = $N.getFirstResult() != null ? $N.getFirstResult() : $L", Integer
                        .class, "searchParams", "searchParams", 0)
                .addStatement("return $N($N,$N,$N,$N,$N).setMaxResults($N).setFirstResult($N).list()",
                        "getSearchQueryWithParamsAndPredicateList", "searchQuery", "searchParams",
                        "searchRestrictions", "from", "query", "querySize", "queryIndex")
                .endControlFlow()
                .addStatement("throw new $T($S)", ProcessingException.class, "searchParams object found null ")
                .build();

    }

    public static MethodSpec createGetByIdMethod(ClassName entityClass, Element annotatedElement,
                                                 TypeElement typeElement) {

        for (Element variableElement : annotatedElement.getEnclosedElements()) {
            if (variableElement instanceof VariableElement && variableElement.getAnnotation(Id.class) != null) {
                TypeName className = ClassName.get(variableElement.asType());
                return MethodSpec.methodBuilder("getById")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(entityClass)
                        .addParameter(ParameterSpec.builder(className, "id").build())
                        .addStatement("return get($N)", "id")
                        .build();

            }
        }
        throw new AssertionError("No field found with @Id annotation in class " +
                AnnotatedClassUtils.getSimpleName(typeElement));

    }

    public static MethodSpec createUniqueResultMethod(ClassName entityClass) {

        return MethodSpec.methodBuilder("searchUniqueResult")
                .addModifiers(Modifier.PUBLIC)
                .returns(entityClass)
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addStatement("return $N($N).uniqueResult()", "getSearchQuery", "searchQuery").build();

    }

    public static void checkEntity(RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(GenerateDao.class).stream().filter(annotatedElement -> annotatedElement
                .getAnnotation(Entity.class) == null).forEach(annotatedElement -> {
            throw new AssertionError("@GenerateDao can only be used for Entity Classes " + AnnotatedClassUtils
                    .getSimpleName((TypeElement) annotatedElement));
        });

    }


}