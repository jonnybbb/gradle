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
package org.gradle.launcher.daemon.context;

import org.gradle.util.Jvm;
import org.gradle.api.internal.Factory;

import java.io.File;

/**
 * Builds a daemon context, reflecting the current environment.
 * <p>
 * The builder itself has properties for different context values, that allow you to override
 * what would be set based on the environment. This is primarily to aid in testing.
 */
public class DaemonContextBuilder implements Factory<DaemonContext> {

    public static final String FAKE_JAVA_HOME_OVERRIDE_PROPERTY = "org.gradle.testing.override.daemon.context.javahome";
    
    private final Jvm jvm = Jvm.current();

    private File javaHome;

    public DaemonContextBuilder() { 
        // This nonsense is just so we can test that we spin a new daemon at the integration test level
        // if the JDK versions are different. This is the best I could think of.
        // See DaemonLifecycleSpec fo where this is used - LD.
        String overriddenJavaHome = System.getProperty(FAKE_JAVA_HOME_OVERRIDE_PROPERTY);
        javaHome = overriddenJavaHome == null ? jvm.getJavaHome() : new File(overriddenJavaHome);
    }

    public File getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(File javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * Creates a new daemon context, based on the current state of this builder.
     */
    public DaemonContext create() {
        return new DefaultDaemonContext(javaHome);
    }
}