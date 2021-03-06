/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.testing

import org.gradle.integtests.fixtures.TestResources
import org.gradle.integtests.fixtures.internal.AbstractIntegrationSpec
import org.junit.Rule
import org.junit.Test
import spock.lang.Issue

public class TestOutputListenerIntegrationTest extends AbstractIntegrationSpec {
    @Rule public final TestResources resources = new TestResources()

    @Test
    @Issue("GRADLE-1009")
    def "standard output is shown when tests are executed"() {
        given:
        def test = file("src/test/java/SomeTest.java")
        test << """
import org.junit.*;

public class SomeTest {
    @Test
    public void showsOutputWhenPassing() {
        System.out.println("out passing");
        System.err.println("err passing");
        Assert.assertTrue(true);
    }

    @Test
    public void showsOutputWhenFailing() {
        System.out.println("out failing");
        System.err.println("err failing");
        Assert.assertTrue(false);
    }
}
"""
        def buildFile = file('build.gradle')
        buildFile << """
apply plugin: 'java'

repositories {
    mavenCentral()
}

dependencies {
    testCompile "junit:junit:4.8.2"
}

test.addTestOutputListener(new VerboseOutputListener(logger: project.logger))

class VerboseOutputListener implements TestOutputListener {

    def logger

    public void onOutput(TestDescriptor descriptor, TestOutputEvent event) {
        logger.lifecycle(descriptor.name + " " + event.destination + " " + event.message);
    }
}
"""

        when:
        def failure = executer.withTasks('test').runWithFailure()

        then:
        failure.output.contains('SomeTest StdOut out passing')
        failure.output.contains('SomeTest StdOut out failing')
        failure.output.contains('SomeTest StdErr err passing')
        failure.output.contains('SomeTest StdErr err failing')
    }

    @Test
    def "can register output listener at gradle level"() {
        given:
        def test = file("src/test/java/SomeTest.java")
        test << """
import org.junit.*;

public class SomeTest {
    @Test
    public void foo() {
        System.out.println("message from foo");
    }
}
"""
        def buildFile = file('build.gradle')
        buildFile << """
apply plugin: 'java'

repositories {
    mavenCentral()
}

dependencies {
    testCompile "junit:junit:4.8.2"
}

test.onOutput { descriptor, event ->
    println "first: " + event.message
}

gradle.addListener(new VerboseOutputListener(logger: project.logger))

class VerboseOutputListener implements TestOutputListener {

    def logger

    public void onOutput(TestDescriptor descriptor, TestOutputEvent event) {
        logger.lifecycle("second: " + event.message);
    }
}
"""

        when:
        def result = executer.withTasks('test').run()

        then:
        result.output.contains('first: message from foo')
        result.output.contains('second: message from foo')
    }

    @Test
    def "shows standard stream also for test ng"() {
        given:
        def test = file("src/test/java/SomeTest.java")
        test << """
import org.testng.*;
import org.testng.annotations.*;

public class SomeTest {
    @Test
    public void foo() {
        System.out.println("output from foo");
        System.err.println("error from foo");
    }
}
"""

        def buildFile = file('build.gradle')
        buildFile << """
apply plugin: 'java'

repositories {
    mavenCentral()
}

dependencies {
    testCompile 'org.testng:testng:5.14'
}

test {
    useTestNG()
    testLogging.showStandardStreams = true
}
"""
        when: "run without '-i'"
        def result = executer.withTasks('test').run()
        then:
        !result.output.contains('output from foo')

        when: "run with '-i'"
        result = executer.withTasks('cleanTest', 'test').withArguments('-i').run()

        then:
        result.output.contains('output from foo')
        result.error.contains('error from foo')
    }
}
