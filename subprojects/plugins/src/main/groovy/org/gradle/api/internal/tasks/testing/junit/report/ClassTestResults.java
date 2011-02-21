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
package org.gradle.api.internal.tasks.testing.junit.report;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Test results for a given class.
 */
public class ClassTestResults extends CompositeTestResults {
    private final String name;
    private final PackageTestResults packageResults;
    private final Set<TestResult> results = new TreeSet<TestResult>();
    private final StringBuilder standardOutput = new StringBuilder();
    private final StringBuilder standardError = new StringBuilder();

    public ClassTestResults(String name, PackageTestResults packageResults) {
        super(packageResults);
        this.name = name;
        this.packageResults = packageResults;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return StringUtils.substringAfterLast(name, ".");
    }

    public PackageTestResults getPackageResults() {
        return packageResults;
    }

    public Collection<TestResult> getTestResults() {
        return results;
    }

    public CharSequence getStandardError() {
        return standardError;
    }

    public CharSequence getStandardOutput() {
        return standardOutput;
    }

    public TestResult addTest(String testName, long duration) {
        TestResult test = new TestResult(testName, duration, this);
        results.add(test);
        return addTest(test);
    }

    public void addStandardOutput(String textContent) {
        standardOutput.append(textContent);
    }

    public void addStandardError(String textContent) {
        standardError.append(textContent);
    }
}