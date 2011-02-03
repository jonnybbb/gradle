/*
 * Copyright 2010 the original author or authors.
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


package org.gradle.api.plugins.quality

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.GroovyBasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.api.tasks.SourceSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A {@link Plugin} which measures and enforces code quality for Java and Groovy projects.
 */
public class CodeQualityPlugin implements Plugin<Project> {
    public static final String CHECKSTYLE_MAIN_TASK = "checkstyleMain";
    public static final String CHECKSTYLE_TEST_TASK = "checkstyleTest";
    public static final String CODE_NARC_MAIN_TASK = "codenarcMain";
    public static final String CODE_NARC_TEST_TASK = "codenarcTest";
    public static final String FINDBUGS_MAIN_TASK = "findbugsMain";
    public static final String FINDBUGS_TEST_TASK = "findbugsMain";
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeQualityPlugin.class);
    private static final String FINDBUGS = "findbugs"


    public void apply(final Project project) {
        project.plugins.apply(ReportingBasePlugin.class);

        JavaCodeQualityPluginConvention javaPluginConvention = new JavaCodeQualityPluginConvention(project)
        project.convention.plugins.javaCodeQuality = javaPluginConvention;

        GroovyCodeQualityPluginConvention groovyPluginConvention = new GroovyCodeQualityPluginConvention(project)
        project.convention.plugins.groovyCodeQuality = groovyPluginConvention;

        configureCheckstyleDefaults(project, javaPluginConvention)
        configureFindbugsDefaults(project, javaPluginConvention)
        configureCodeNarcDefaults(project, groovyPluginConvention)

        project.plugins.withType(JavaBasePlugin.class) {
            configureForJavaPlugin(project, javaPluginConvention);
        }
        project.plugins.withType(GroovyBasePlugin.class) {
            configureForGroovyPlugin(project, groovyPluginConvention);
        }
    }

    private void configureCheckstyleDefaults(Project project, JavaCodeQualityPluginConvention pluginConvention) {
        project.tasks.withType(Checkstyle.class) {Checkstyle checkstyle ->
            checkstyle.conventionMapping.configFile = { pluginConvention.checkstyleConfigFile }
            checkstyle.conventionMapping.map('properties') { pluginConvention.checkstyleProperties }
        }
    }

    private void configureFindbugsDefaults(Project project, JavaCodeQualityPluginConvention pluginConvention) {
        project.tasks.withType(Findbugs.class) {Findbugs findbugs ->
            findbugs.conventionMapping.configFile = { pluginConvention.findbugsConfigFile }
            findbugs.conventionMapping.map('properties') { pluginConvention.findbugsProperties }
        }
    }

    private void configureCodeNarcDefaults(Project project, GroovyCodeQualityPluginConvention pluginConvention) {
        project.tasks.withType(CodeNarc.class) {CodeNarc codenarc ->
            codenarc.conventionMapping.configFile = { pluginConvention.codeNarcConfigFile }
        }
    }

    private void configureCheckTask(Project project) {
        Task task = project.tasks[JavaBasePlugin.CHECK_TASK_NAME]
        task.setDescription("Executes all quality checks");
        task.dependsOn project.tasks.withType(Checkstyle.class)
        task.dependsOn project.tasks.withType(CodeNarc.class)
        task.dependsOn project.tasks.withType(Findbugs.class)
    }

    private void configureForJavaPlugin(Project project, JavaCodeQualityPluginConvention pluginConvention) {
        configureCheckTask(project);
        project.configurations.add(FINDBUGS)

        project.convention.getPlugin(JavaPluginConvention.class).sourceSets.all {SourceSet set ->
            Checkstyle checkstyle = project.tasks.add(set.getTaskName("checkstyle", null), Checkstyle.class);
            checkstyle.description = "Runs Checkstyle against the $set.name Java source code."
            checkstyle.conventionMapping.defaultSource = { set.allJava; }
            checkstyle.conventionMapping.configFile = { pluginConvention.checkstyleConfigFile }
            checkstyle.conventionMapping.resultFile = { new File(pluginConvention.checkstyleResultsDir, "${set.name}.xml") }
            checkstyle.conventionMapping.classpath = { set.compileClasspath; }


            String findbugsTaskName = set.getTaskName(FINDBUGS, null)
            Findbugs findbugs = project.tasks.add(findbugsTaskName, Findbugs.class);
            String compileJavaTaskName = set.getCompileJavaTaskName()
            project.getTasks().getByName(findbugsTaskName).dependsOn(compileJavaTaskName);
            findbugs.description = "Runs Findbugs against the $set.name Java source code."
            findbugs.conventionMapping.defaultSource = { set.allJava; }
            findbugs.conventionMapping.configFile = { pluginConvention.findbugsConfigFile }
            findbugs.conventionMapping.reportFile = { new File(pluginConvention.findbugsResultsDir, "${set.name}.xml") }
            findbugs.conventionMapping.classpath = {project.configurations.getByName(FINDBUGS)}

        }
    }

    private void configureForGroovyPlugin(Project project, GroovyCodeQualityPluginConvention pluginConvention) {
        project.convention.getPlugin(JavaPluginConvention.class).sourceSets.all {SourceSet set ->
            GroovySourceSet groovySourceSet = set.convention.getPlugin(GroovySourceSet.class)
            CodeNarc codeNarc = project.tasks.add(set.getTaskName("codenarc", null), CodeNarc.class);
            codeNarc.setDescription("Runs CodeNarc against the $set.name Groovy source code.");
            codeNarc.conventionMapping.defaultSource = { groovySourceSet.allGroovy; }
            codeNarc.conventionMapping.configFile = { pluginConvention.codeNarcConfigFile; }
            codeNarc.conventionMapping.reportFile = { new File(pluginConvention.codeNarcReportsDir, "${set.name}.html"); }
        }
    }
}
