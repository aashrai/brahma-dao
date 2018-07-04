package com.brahma.processor;

import com.brahma.dao.processor.DaoProcessor;
import com.brahma.utils.TestUtils;
import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Assert;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.Collections;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class DaoProcessorTest {
    @Test
    public void generateDaoTestWithDefault() {

        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.example.BrahmaEntity",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_input1.txt"))

                )
        );

        final JavaFileObject output = JavaFileObjects.forSourceString(
                "com.example.Brahma_BrahmaEntityDao",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_output1.txt"))

                )
        );

        assertAbout(javaSource())
                .that(input)
                .processedWith(new DaoProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(output);
    }

    @Test
    public void generateDaoGetByIdTest() {

        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.example.BrahmaEntity",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_input2.txt"))

                )
        );

        final JavaFileObject output = JavaFileObjects.forSourceString(
                "com.example.Brahma_BrahmaEntityDao",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_output2.txt"))

                )
        );

        assertAbout(javaSource())
                .that(input)
                .processedWith(new DaoProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(output);
    }

    @Test
    public void generateDaoCheckPrimitiveFieldTest() {

        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.example.BrahmaEntity",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_input3.txt"))

                )
        );

        assertAbout(javaSource())
                .that(input)
                .processedWith(new DaoProcessor())
                .failsToCompile()
                .withErrorContaining("Primitive types are not supported while using @GenerateDao");


    }

    @Test
    public void generateDaoIdAnnotationTest() {

        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.example.BrahmaEntity",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_input4.txt"))

                )
        );
        try {
            assertAbout(javaSource())
                    .that(input)
                    .processedWith(new DaoProcessor())
                    .failsToCompile();
        } catch (RuntimeException e) {
            Assert.assertEquals("java.lang.AssertionError: No field found with @Id annotation in class " +
                    "BrahmaEntity", e.getMessage());
        }


    }

    @Test
    public void generateDaoEntityAnnotationTest() {

        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.example.BrahmaEntity",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_input5.txt"))

                )
        );
        try {
            assertAbout(javaSource())
                    .that(input)
                    .processedWith(new DaoProcessor())
                    .failsToCompile();
        } catch (RuntimeException e) {
            Assert.assertEquals("java.lang.AssertionError: @GenerateDao can only be used for Entity Classes " +
                    "BrahmaEntity", e.getMessage());
        }
    }

    @Test
    public void generateDaoSupportSearchVariationsTest() {

        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.example.BrahmaEntity",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_input6.txt"))

                )
        );
        final JavaFileObject output = JavaFileObjects.forSourceString(
                "com.example.Brahma_BrahmaEntityDao",
                Joiner.on('\n').join(Collections.singleton(TestUtils.readLineByLineJava8
                        ("testfiles/generatedao_output6.txt"))

                )
        );

        assertAbout(javaSource())
                .that(input)
                .processedWith(new DaoProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(output);

    }


}
