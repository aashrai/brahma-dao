package com.brahma.dao.utils;

import com.squareup.javapoet.*;
import dagger.Module;
import dagger.Provides;
import org.hibernate.SessionFactory;

import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Set;

public class GenerateModuleUtils {
    public static TypeSpec generateDaoModule(Set<Element> daoClasses, Elements elementUtils) {
        TypeSpec.Builder daoModule = TypeSpec.classBuilder("Zefo_DaoModule")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Module.class)
                .addField(FieldSpec.builder(SessionFactory.class, "sessionFactory", Modifier.PRIVATE).build())
                .addMethod(createModuleConstructor());
        for (Element daoClass : daoClasses) {

            ClassName generatedDaoClass = ClassName.get(AnnotatedClassUtils.getPackageOf((TypeElement) daoClass,
                    elementUtils), createGeneratedDaoClass(AnnotatedClassUtils.getSimpleName((TypeElement) daoClass)));
            daoModule.addMethod(createDaoModuleMethod(generatedDaoClass, createGeneratedDaoClass(AnnotatedClassUtils
                    .getSimpleName((TypeElement) daoClass))));
        }
        return daoModule.build();
    }

    private static MethodSpec createModuleConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(SessionFactory.class, "sessionFactory").build())
                .addStatement("this.sessionFactory = sessionFactory")
                .build();
    }

    public static String createGeneratedDaoClass(String key) {
        return "Zefo_" + key + "Dao";
    }

    private static MethodSpec createDaoModuleMethod(ClassName genratedDaoClass, String generatedDaoName) {
        return MethodSpec.methodBuilder("get" + generatedDaoName)
                .returns(genratedDaoClass)
                .addAnnotation(Singleton.class)
                .addAnnotation(Provides.class)
                .addStatement("return new $T($N)", genratedDaoClass, "sessionFactory").build();
    }



}
