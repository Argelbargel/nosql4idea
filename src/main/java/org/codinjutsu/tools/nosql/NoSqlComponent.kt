/*
 * Copyright (c) 2015 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.nosql

import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.project.Project

class NoSqlComponent(project: Project) : AbstractProjectComponent(project) {

    override fun getComponentName() = COMPONENT_NAME

    override fun projectOpened() {
        NoSqlWindowManager.getInstance(myProject)
    }

    override fun projectClosed() {
        NoSqlWindowManager.getInstance(myProject).unregisterMyself()
    }

    companion object {
        private val COMPONENT_NAME = "NoSql"
    }
}
