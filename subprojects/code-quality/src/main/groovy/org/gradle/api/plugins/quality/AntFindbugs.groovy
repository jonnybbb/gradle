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

package org.gradle.api.plugins.quality

import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Ant findbugs execute task.
 */
public class AntFindbugs {


    private static final Logger LOGGER = LoggerFactory.getLogger(AntFindbugs.class);

    public void execute(AntBuilder ant, FileTree source, File destinationDir, FileCollection classpath, File configFile, File reportFile, Map<String, ?> properties, boolean ignoreFailures) {
        String propertyName = "org.gradle.findbugs.violations"

        ant.project.addTaskDefinition('findbugs', GradleAntFindBugsTask)
        ant.findbugs(classpath: classpath.asPath,
                output: "xml:withMessages",
                outputFile: reportFile, errorProperty: propertyName) {
            sourcePath(path: source.asPath)
            "class"(location: destinationDir.getPath())

                if (!ignoreFailures && ant.properties[propertyName]) {
                    throw new GradleException("Findbugs check violations were found in $source. See the report at $resultFile.")
                }

            }


        }
    
}