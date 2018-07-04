/*
 * Copyright (c) 2018 gozefo.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *SOFTWARE
 */
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
import javax.ws.rs.ProcessingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates methods of dao class to be generated for the elements annotated with {@linkplain GenerateDao}.
 * <p>
 * {@linkplain com.brahma.dao.processor.DaoProcessor #generateDaoClasses()} {@linkplain
 * com.brahma.dao.processor.DaoProcessor #generateAllSearchVariationsMethods(TypeSpec.Builder, ClassName)}
 */
public final class CreateDaoUtils {
    /**
     * setting default set of max results.
     */
    private static Integer defaultMaxResults = 1000;

    /**
     *
     */
    private CreateDaoUtils() {

    }

    /**
     * Created method that persists a object in db.
     *
     * @param typeElement      Represents a class or interface annotated element
     * @param entityClass      Entity class annotated with {@linkplain GenerateDao} annotation.
     * @param annotatedElement Element of the entity class
     * @param messager         Used to report errors, warnings, and other notices
     * @return Returns persist method
     * @see "/testfiles/generatedao_output1.txt #createOrUpdate"
     */
    public static MethodSpec createPersistMethod(final TypeElement typeElement, final ClassName entityClass, final
    Element annotatedElement, final Messager messager) {
        MethodSpec.Builder createOrUpdate = MethodSpec.methodBuilder("createOrUpdate");

        annotatedElement.getEnclosedElements().stream().filter(field -> field.getKind().isField()).forEach(field -> {


            if (ClassName.get(field.asType()).isPrimitive()) {
                MessagingUtils.error(messager, field, "Primitive types are not supported while using @GenerateDao");
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

    /**
     * @return Constructor of the generated dao class
     */
    public static MethodSpec createConstructor() {

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(SessionFactory.class), "sessionFactory")
                .addStatement("super($N)", "sessionFactory")
                .build();

    }

    /**
     * @param annotatedElement Element of the entity class annotated with {@linkplain GenerateDao}
     * @param messager         Used to report errors, warnings, and other notices
     * @param entityClass      Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Returns method which add the attribute value to the Predicate {@linkplain Predicate} list
     * @see "/testfiles/generatedao_output1.txt #getPredicateList"
     */
    public static MethodSpec addToPredicateListMethod(final Element annotatedElement,
                                                      final Messager messager, final ClassName entityClass) {

        MethodSpec.Builder addToPredicateList = MethodSpec.methodBuilder("getPredicateList")
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)))
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery").build())
                .addParameter(ParameterSpec.builder(CriteriaBuilder.class, "criteriaBuilder").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass),
                        "from").build())
                .addStatement("$T<$T> searchRestrictions = new $T<>()", ClassName.get(List.class), ClassName.get
                                (Predicate.class),
                        ClassName.get(ArrayList.class));
        annotatedElement.getEnclosedElements().stream().filter(field -> field.getKind().isField()).forEach(field -> {


            if (ClassName.get(field.asType()).isPrimitive()) {
                MessagingUtils.error(messager, field, "Primitive types are not supported while using @GenerateDao");
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

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates search method with user defined Predicate {@linkplain Predicate} list and adds attribute to
     * that list only. The method returns {@linkplain Query}. The method "getSearchQueryWithPredicateList" is only
     * generated when {@linkplain GenerateDao#supportSearchVariations()} is true.
     * @see "/testfiles/generatedao_output1.txt #getSearchQueryWithPredicateList"
     */
    public static MethodSpec createGetSearchQueryMethodWithPredicateList(final ClassName entityClass) {
        return MethodSpec.methodBuilder("getSearchQueryWithPredicateList")
                .returns(ParameterizedTypeName.get(ClassName.get(Query.class), entityClass))
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass),
                        "from").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(CriteriaQuery.class),
                        entityClass), "query").build())
                .addStatement("$T session = this.currentSession()", Session.class)
                .addStatement("$T<$T> select = $N.select($N)", CriteriaQuery.class, entityClass, "query", "from")
                .addStatement("select = $N.where($N.toArray(new $T[$N.size()]))", "select", "searchRestrictions",
                        Predicate.class, "searchRestrictions")
                .addStatement("return $N.createQuery($N)", "session", "select")
                .build();


    }

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates simple search method.The generated search method returns {@linkplain Query}.
     * @see "/testfiles/generatedao_output1.txt #getSearchQuery"
     */
    public static MethodSpec createGetSearchQueryMethod(final ClassName entityClass) {

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

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates search method with SearchMeta {@linkplain SearchMeta}.The generated search method returns
     * {@linkplain Query}.The method "getSearchQueryWithParams" is only generated when {@linkplain
     * GenerateDao#supportSearchVariations()} is true.
     * @see "/testfiles/generatedao_output1.txt #getSearchQueryWithParams"
     */
    public static MethodSpec createGetSearchQueryMethodWithParams(final ClassName entityClass) {

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

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates search method with SearchMeta {@linkplain SearchMeta} and user defined Predicate {@linkplain
     * Predicate} list and returns Query {@linkplain Query}. The method "getSearchQueryWithParamsAndPredicateList" is
     * only generated when {@linkplain GenerateDao#supportSearchVariations()} is true.
     * @see "/testfiles/generatedao_output1.txt #getSearchQueryWithParamsAndPredicateList"
     */
    public static MethodSpec createGetSearchQueryWithParamsAndPredicateList(final ClassName entityClass) {
        return MethodSpec.methodBuilder("getSearchQueryWithParamsAndPredicateList")
                .addModifiers(Modifier.PROTECTED)
                .returns(ParameterizedTypeName.get(ClassName.get(Query.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterSpec.builder(SearchMeta.class, "searchParams").build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass),
                        "from").build())
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

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates a overloaded search method for the query generated in {@linkplain
     * #createGetSearchQueryMethod(ClassName)} The generated method returns the list of entity objects.
     * @see "/testfiles/generatedao_output1.txt #search(ClassName})"
     */
    public static MethodSpec createSearchMethod(final ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addStatement("return $N($N).list()", "getSearchQuery", "searchQuery").build();


    }

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates a overloaded search method for the query generated in {@linkplain
     * #createGetSearchQueryMethodWithParams(ClassName)}. The generated method returns the list of entity objects.The
     * method "searchQuery" is only generated when {@linkplain GenerateDao#supportSearchVariations()} is true.
     * @see "/testfiles/generatedao_output1.txt #search"
     */
    public static MethodSpec createSearchMethodWithParams(final ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .addException(ProcessingException.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterSpec.builder(SearchMeta.class, "searchParams").build())
                .beginControlFlow("if (searchParams != null)")
                .addStatement("$T querySize = $N.getMaxResults() != null ? $N.getMaxResults() : $L", Integer.class,
                        "searchParams", "searchParams", defaultMaxResults)
                .addStatement("$T queryIndex = $N.getFirstResult() != null ? $N.getFirstResult() : $L", Integer
                        .class, "searchParams", "searchParams", 0)
                .addStatement("return $N($N,$N).setMaxResults($N).setFirstResult($N).list()",
                        "getSearchQueryWithParams", "searchQuery", "searchParams", "querySize", "queryIndex")
                .endControlFlow()
                .addStatement("throw new $T($S)", ProcessingException.class, "searchParams object found null ")
                .build();

    }

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates a overloaded search method for the query generated in #createSearchMethodWithPredicateList
     * (ClassName). The generated method returns the list of entity objects.The method "searchQuery" is only generated
     * when {@linkplain GenerateDao#supportSearchVariations()} is true.
     * @see "/testfiles/generatedao_output1.txt #search(ClassName, Predicate, Root, CriteriaQuery)"
     */
    public static MethodSpec createSearchMethodWithPredicateList(final ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass),
                        "from").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(CriteriaQuery.class),
                        entityClass), "query").build())
                .addStatement("return $N($N, $N, $N, $N).list()", "getSearchQueryWithPredicateList", "searchQuery",
                        "searchRestrictions", "from", "query").build();

    }

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates a overloaded search method for the query generated in {@linkplain
     * #createSearchMethodWithPredicateList(ClassName)}. The generated method returns the list of entity objects.The
     * method "searchQuery" is only generated when {@linkplain GenerateDao#supportSearchVariations()} is true.
     * @see "/testfiles/generatedao_output1.txt #search(ClassName, SearchMeta, Predicate, Root, CriteriaQuery)"
     */
    public static MethodSpec createSearchMethodWithParamsAndPredicateList(final ClassName entityClass) {
        return MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PUBLIC)
                .addException(ProcessingException.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addParameter(ParameterSpec.builder(SearchMeta.class, "searchParams").build())
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Predicate.class)),
                        "searchRestrictions")
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Root.class), entityClass),
                        "from").build())
                .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(CriteriaQuery.class),
                        entityClass), "query").build())

                .beginControlFlow("if (searchParams != null)")
                .addStatement("$T querySize = $N.getMaxResults() != null ? $N.getMaxResults() : $L", Integer.class,
                        "searchParams", "searchParams", defaultMaxResults)
                .addStatement("$T queryIndex = $N.getFirstResult() != null ? $N.getFirstResult() : $L", Integer
                        .class, "searchParams", "searchParams", 0)
                .addStatement("return $N($N,$N,$N,$N,$N).setMaxResults($N).setFirstResult($N).list()",
                        "getSearchQueryWithParamsAndPredicateList", "searchQuery", "searchParams",
                        "searchRestrictions", "from", "query", "querySize", "queryIndex")
                .endControlFlow()
                .addStatement("throw new $T($S)", ProcessingException.class, "searchParams object found null ")
                .build();

    }

    /**
     * @param entityClass      Entity class annotated with {@linkplain GenerateDao} annotation
     * @param annotatedElement Element of the entity class
     * @param typeElement      Represents a class or interface annotated element
     * @param messager         Messager for logging
     * @return Generates method which returns a row of the entity table with value of the field annotated with @Id
     * {@linkplain Id}
     * @see "/testfiles/generatedao_output1.txt #getById()"
     */
    public static MethodSpec createGetByIdMethod(final ClassName entityClass, final Element annotatedElement,
                                                 final TypeElement typeElement, final Messager messager) {

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

        MessagingUtils.error(messager, annotatedElement, "No field found with @Id annotation in class %s",
                AnnotatedClassUtils.getSimpleName(typeElement));
        throw new AssertionError("No field found with @Id annotation in class " + AnnotatedClassUtils.getSimpleName(typeElement));

    }

    /**
     * @param entityClass Entity class annotated with {@linkplain GenerateDao} annotation
     * @return Generates search method which returns unique result from the retrieved list of objects.
     * @see "/testfiles/generatedao_output1.txt #searchUniqueResult"
     */
    public static MethodSpec createUniqueResultMethod(final ClassName entityClass) {

        return MethodSpec.methodBuilder("searchUniqueResult")
                .addModifiers(Modifier.PUBLIC)
                .returns(entityClass)
                .addParameter(ParameterSpec.builder(entityClass, "searchQuery")
                        .build())
                .addStatement("return $N($N).uniqueResult()", "getSearchQuery", "searchQuery").build();

    }

    /**
     * Validation for the class annotated with {@linkplain GenerateDao}.
     *
     * @param roundEnv contains all the classes/elements of the project
     * @param messager Messager for logging
     */
    public static void entityValidation(final RoundEnvironment roundEnv, final Messager messager) {
        roundEnv.getElementsAnnotatedWith(GenerateDao.class).stream().filter(annotatedElement -> annotatedElement
                .getAnnotation(Entity.class) == null).forEach(annotatedElement -> {
            MessagingUtils.error(messager, annotatedElement, "@GenerateDao can only be used for Entity Classes");
            throw new AssertionError("@GenerateDao can only be used for Entity Classes " + AnnotatedClassUtils
                    .getSimpleName((TypeElement) annotatedElement));
        });

    }


}
