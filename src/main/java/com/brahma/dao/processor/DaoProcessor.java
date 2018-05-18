package com.brahma.dao.processor;

import com.brahma.dao.annotations.GenerateDao;
import com.brahma.dao.utils.AnnotatedClassUtils;
import com.brahma.dao.utils.CreateDaoUtils;
import com.brahma.dao.utils.GenerateModuleUtils;
import com.squareup.javapoet.*;
import io.dropwizard.hibernate.AbstractDAO;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DaoProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;
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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generateDao(roundEnv);
        return false;
    }


    private void generateJavaFile(TypeSpec generatedDaoClass, String packageName) {
        JavaFile javaFile = JavaFile.builder(packageName, generatedDaoClass).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {

        }
    }


    private void generateDao(RoundEnvironment roundEnv) {


        CreateDaoUtils.checkEntity(roundEnv);
        Set<Element> daoClasses = new HashSet<>(roundEnv.getElementsAnnotatedWith(GenerateDao.class));
        generateDaoClasses(daoClasses);
        generateDaoModule(daoClasses);

    }

    private void generateDaoClasses(Set<Element> daoClasses) {
        for (Element annotatedElement : daoClasses) {
            TypeElement typeElement = (TypeElement) annotatedElement;

            ClassName entityClass = ClassName.get(AnnotatedClassUtils.getQualifiedClassName(typeElement,
                    elementUtils), AnnotatedClassUtils
                    .getSimpleName(typeElement));


            MethodSpec createOrUpdate = CreateDaoUtils.createPersistMethod(typeElement, entityClass,
                    annotatedElement, messager);

            MethodSpec constructor = CreateDaoUtils.createConstructor();
            MethodSpec createSearchQueryMethod = CreateDaoUtils.createGetSearchQueryMethod(
                    entityClass);

            MethodSpec addToPredicateListMethod = CreateDaoUtils.addToPredicateListMethod(annotatedElement,
                    messager, entityClass);
            MethodSpec createSearchQueryMethodWithParams = CreateDaoUtils.createGetSearchQueryMethodWithParams
                    (entityClass);
            MethodSpec searchMethod = CreateDaoUtils.createSearchMethod(entityClass);
            MethodSpec searchMethodWithParams = CreateDaoUtils.createSearchMethodWithParams(
                    entityClass);
            MethodSpec uniqueResult = CreateDaoUtils.createUniqueResultMethod(entityClass);
            MethodSpec createGetById = CreateDaoUtils.createGetByIdMethod(entityClass, annotatedElement,
                    typeElement);
            MethodSpec searchMethodWithPredicateList = CreateDaoUtils.createSearchMethodWithPredicateList
                    (entityClass);

            MethodSpec createSearchQueryWithPredicateList = CreateDaoUtils
                    .createGetSearchQueryMethodWithPredicateList(entityClass);

            MethodSpec searchMethodWithParamsAndPredicateList = CreateDaoUtils
                    .createSearchMethodWithParamsAndPredicateList(entityClass);
            MethodSpec createSearchQueryWithParamsAndPredicateList = CreateDaoUtils
                    .createGetSearchQueryWithParamsAndPredicateList(entityClass);
            TypeSpec generatedDaoClass = TypeSpec.classBuilder("Zefo_" + AnnotatedClassUtils.getSimpleName
                    (typeElement) + "Dao")
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ParameterizedTypeName.get(ClassName.get(AbstractDAO.class), entityClass))
                    .addMethod(constructor)
                    .addMethod(createOrUpdate)
                    .addMethod(createGetById)
                    .addMethod(addToPredicateListMethod)
                    .addMethod(createSearchQueryMethod)
                    .addMethod(createSearchQueryMethodWithParams)
                    .addMethod(createSearchQueryWithPredicateList)
                    .addMethod(createSearchQueryWithParamsAndPredicateList)
                    .addMethod(searchMethod)
                    .addMethod(searchMethodWithParams)
                    .addMethod(searchMethodWithPredicateList)
                    .addMethod(searchMethodWithParamsAndPredicateList)
                    .addMethod(uniqueResult)
                    .build();
            generateJavaFile(generatedDaoClass, AnnotatedClassUtils.getQualifiedClassName(typeElement,
                    elementUtils));

        }

    }

    private void generateDaoModule(Set<Element> daoClasses) {
        if (!daoClasses.isEmpty()) {
            TypeSpec daoModule = GenerateModuleUtils.generateDaoModule(daoClasses, elementUtils);
            generateJavaFile(daoModule, "com.zefo.dagger");
        }
    }
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

}
