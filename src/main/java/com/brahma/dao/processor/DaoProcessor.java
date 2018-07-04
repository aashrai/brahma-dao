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

package com.brahma.dao.processor;

import com.brahma.dao.annotations.GenerateDao;
import com.brahma.dao.utils.AnnotatedClassUtils;
import com.brahma.dao.utils.CreateDaoUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.dropwizard.hibernate.AbstractDAO;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * /** An {@linkplain javax.annotation.processing.Processor annotation processor} to generate dao {@linkplain
 * io.dropwizard.hibernate.AbstractDAO} classes with basic functionalities for specified entity classes.
 *
 * @author sarthak
 * @version 1.0
 */
@AutoService(Processor.class)
public final class DaoProcessor extends AbstractProcessor {
    /**
     * A utils class to fetch details about the annotated element.
     *
     * @see "{@link com.brahma.dao.utils.AnnotatedClassUtils}"
     */
    private Elements elementUtils;
    /**
     * Writes the generated java class.
     */
    private Filer filer;
    /**
     * Used to report errors, warnings, and other notices.
     */
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(GenerateDao.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        generateDao(roundEnv);
        return false;
    }

    /**
     * Generates java files with dao classes. {@linkplain #generateDaoClasses(Set)} (RoundEnvironment)}
     *
     * @param generatedDaoClass Java class to be generated
     * @param packageName       package of the entity class annotated with @GenerateDao and dao class is generated in
     *                          the same package
     */
    private void generateJavaFile(final TypeSpec generatedDaoClass, final String packageName) {
        JavaFile javaFile = JavaFile.builder(packageName, generatedDaoClass).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {

        }
    }

    /**
     * Stores all the elements annotated with @GenerateDao {@linkplain GenerateDao} annotation in a Set {@linkplain
     * java.util.Set}.
     *
     * @param roundEnv contains all the classes/elements of the project
     */

    private void generateDao(final RoundEnvironment roundEnv) {


        CreateDaoUtils.entityValidation(roundEnv, messager);
        Set<Element> daoClasses = new HashSet<>(roundEnv.getElementsAnnotatedWith(GenerateDao.class));
        generateDaoClasses(daoClasses);

    }

    /**
     * Generates dao classes with basic functionalities for the @GenerateDao {@linkplain GenerateDao} annotated entity
     * {@linkplain javax.persistence.Entity} classes.
     *
     * @param daoClasses set of all entity classes annotated with @GenerateDao {@linkplain GenerateDao} annotation
     * @see "/testfiles/generatedao_output6.txt"
     */
    private void generateDaoClasses(final Set<Element> daoClasses) {
        for (Element annotatedElement : daoClasses) {
            TypeElement typeElement = (TypeElement) annotatedElement;
            ClassName entityClass = ClassName.get(AnnotatedClassUtils.getQualifiedClassName(typeElement,
                    elementUtils), AnnotatedClassUtils
                    .getSimpleName(typeElement));
            TypeSpec.Builder generatedDaoClass = TypeSpec.classBuilder("Brahma_" + AnnotatedClassUtils.getSimpleName
                    (typeElement) + "Dao")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ParameterizedTypeName.get(ClassName.get(AbstractDAO.class), entityClass));
            MethodSpec createOrUpdate = CreateDaoUtils.createPersistMethod(typeElement, entityClass,
                    annotatedElement, messager);
            MethodSpec constructor = CreateDaoUtils.createConstructor();
            MethodSpec createSearchQueryMethod = CreateDaoUtils.createGetSearchQueryMethod(
                    entityClass);
            MethodSpec uniqueResult = CreateDaoUtils.createUniqueResultMethod(entityClass);
            MethodSpec createGetById = CreateDaoUtils.createGetByIdMethod(entityClass, annotatedElement,
                    typeElement, messager);
            MethodSpec searchMethod = CreateDaoUtils.createSearchMethod(entityClass);
            MethodSpec addToPredicateListMethod = CreateDaoUtils.addToPredicateListMethod(annotatedElement,
                    messager, entityClass);
            generatedDaoClass.addMethod(constructor)
                    .addMethod(createOrUpdate)
                    .addMethod(createGetById)
                    .addMethod(addToPredicateListMethod)
                    .addMethod(createSearchQueryMethod)
                    .addMethod(searchMethod)
                    .addMethod(uniqueResult);

            if (annotatedElement.getAnnotation(GenerateDao.class).supportSearchVariations()) {
                generateAllSearchVariationsMethods(generatedDaoClass, entityClass);
            }
            generateJavaFile(generatedDaoClass.build(), AnnotatedClassUtils.getQualifiedClassName(typeElement,
                    elementUtils));

        }

    }

    /**
     * Generates search methods with variations. These methods are only generated when {@linkplain
     * GenerateDao#supportSearchVariations()} is true.
     * <pre><code>
     * {@literal @}Entity
     * {@literal @}GenerateDao(supportSearchVariations = true)
     * public class EntityClass {}
     * </code></pre>
     *
     * @param generatedDaoClass Java class needs to be generated
     * @param entityClass       Entity class annotated with {@linkplain GenerateDao}
     * @see "/testfiles/generatedao_input1.txt"
     * @see "/testfiles/generatedao_output1.txt"
     */
    private void generateAllSearchVariationsMethods(final TypeSpec.Builder generatedDaoClass, final ClassName
            entityClass) {
        MethodSpec createSearchQueryMethodWithParams = CreateDaoUtils.createGetSearchQueryMethodWithParams
                (entityClass);
        MethodSpec searchMethodWithParams = CreateDaoUtils.createSearchMethodWithParams(
                entityClass);
        MethodSpec searchMethodWithPredicateList = CreateDaoUtils.createSearchMethodWithPredicateList
                (entityClass);

        MethodSpec createSearchQueryWithPredicateList = CreateDaoUtils
                .createGetSearchQueryMethodWithPredicateList(entityClass);

        MethodSpec searchMethodWithParamsAndPredicateList = CreateDaoUtils
                .createSearchMethodWithParamsAndPredicateList(entityClass);
        MethodSpec createSearchQueryWithParamsAndPredicateList = CreateDaoUtils
                .createGetSearchQueryWithParamsAndPredicateList(entityClass);
        generatedDaoClass.addMethod(createSearchQueryMethodWithParams)
                .addMethod(createSearchQueryWithPredicateList)
                .addMethod(createSearchQueryWithParamsAndPredicateList)
                .addMethod(searchMethodWithParams)
                .addMethod(searchMethodWithPredicateList)
                .addMethod(searchMethodWithParamsAndPredicateList).build();
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

}

