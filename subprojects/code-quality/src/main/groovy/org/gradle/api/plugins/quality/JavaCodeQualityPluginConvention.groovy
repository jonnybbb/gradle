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

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal

class JavaCodeQualityPluginConvention {

    /**
     * The name of the Checkstyle configuration file, relative to the project directory.
     */
    String checkstyleConfigFileName

    /**
     * The name of the directory to write Checkstyle results to, relative to the build directory.
     */
    String checkstyleResultsDirName

    /**
     * The set of properties to substitute into the Checkstyle configuration file.
     */
    Map<String, Object> checkstyleProperties = [:]


    /**
     * The name of the Findbugs configuration file, relative to the project directory.
     */
    String findbugsConfigFileName

     /**
     * The name of the directory to write Findbugs results to, relative to the build directory.
     */
    String findbugsResultsDirName

     /**
     * The set of properties to substitute into the Findbugs configuration file.
     */
    Map<String, Object> findbugsProperties = [:]

    private ProjectInternal project

    def JavaCodeQualityPluginConvention(Project project) {
        this.project = project
        checkstyleConfigFileName = 'config/checkstyle/checkstyle.xml'
        checkstyleResultsDirName = 'checkstyle'
        findbugsConfigFileName = 'config/findbugs/findbugs.xml'
        findbugsResultsDirName = 'findbugs'
    }

    /**
     * The Checkstyle configuration file.
     */
    File getCheckstyleConfigFile() {
        project.file(checkstyleConfigFileName)
    }

    /**
     * The directory to write the Checkstyle results into.
     */
    File getCheckstyleResultsDir() {
        project.fileResolver.withBaseDir(project.buildDir).resolve(checkstyleResultsDirName)
    }

   File getFindbugsConfigFile() {
        project.file(findbugsConfigFileName)
}

    File getFindbugsResultsDir() {
        project.fileResolver.withBaseDir(project.buildDir).resolve(findbugsResultsDirName)
    }
}
