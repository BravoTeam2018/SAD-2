package com.cit.models;

import com.cit.model.Distance;
import org.junit.Test;
import pl.pojo.tester.api.assertion.Method;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

public class DistanceTest {

    @Test
    public void Should_Pass_All_Pojo_Tests_Using_All_Testers() {
        // given
        final Class<?> classUnderTest = Distance.class;


        // then
        assertPojoMethodsFor(classUnderTest).quickly()
                .areWellImplemented();

        // then
        assertPojoMethodsFor(classUnderTest).testing(Method.GETTER, Method.SETTER, Method.TO_STRING)
                .testing(Method.EQUALS)
                .testing(Method.HASH_CODE)
                .testing(Method.CONSTRUCTOR)
                .areWellImplemented();

    }


}