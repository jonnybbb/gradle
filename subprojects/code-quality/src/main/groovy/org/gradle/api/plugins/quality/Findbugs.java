/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.plugins.quality;

import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Findbugs code quality check.
 */
public class Findbugs extends SourceTask implements VerificationTask {
    private AntFindbugs antFindbugs = new AntFindbugs();

    private File reportFile;
    private File configFile;
    private boolean ignoreFailures;
    private FileCollection classpath;
      private Map<String, Object> properties = new HashMap<String, Object>();

    @TaskAction
    public void check() {
        getLogging().captureStandardOutput(LogLevel.INFO);
        antFindbugs.execute(getAnt(), getSource(), getDestinationDir(), getClasspath(), getConfigFile(), getReportFile(), getProperties(), isIgnoreFailures());
    }

    private File getDestinationDir() {
        return getProject().getBuildDir();
    }


 /**
     * Returns the file to generate the XML results to.
     *
     * @return The result XML file.
     */
    @OutputFile
    public File getReportFile() {
        return reportFile;
    }


      /**
     * Specifies the file to generate the XML results to.
     *
     * @param reportFile The result XML file. Must not be null.
     */
    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
    }



     /**
     * Returns the Findbugs configuration file to use.
     *
     * @return The configuration file.
     */
    @InputFile
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Specifies the Findbugs configuration file to use.
     *
     * @param configFile The configuration file. Must not be null.
     */
    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }


    /**
     * Returns the classpath containing the compiled classes for the source files to be inspected.
     *
     * @return The classpath.
     */
    @InputFiles
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Specified the classpath containing the compiled classes for the source file to be inspected.
     *
     * @param classpath The classpath. Must not be null.
     */
    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    /**
     * Returns the properties available for use in the configuration file. These are substituted into the configuration
     * file.
     *
     * @return The properties available in the configuration file. Returns an empty map when there are no such
     *         properties.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }



    /**
     * {@inheritDoc}
     */
    public boolean isIgnoreFailures() {
        return ignoreFailures;
    }

    /**
     * {@inheritDoc}
     */
    public Findbugs setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
        return this;
    }


}
