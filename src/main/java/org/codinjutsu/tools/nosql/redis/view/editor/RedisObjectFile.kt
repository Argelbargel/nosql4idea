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

package org.codinjutsu.tools.nosql.redis.view.editor

import com.intellij.openapi.project.Project
import org.codinjutsu.tools.nosql.commons.configuration.ServerConfiguration
import org.codinjutsu.tools.nosql.commons.model.Database
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils
import org.codinjutsu.tools.nosql.commons.view.editor.NoSQLFakeFileType
import org.codinjutsu.tools.nosql.commons.view.editor.NoSqlDatabaseObjectFile
import javax.swing.Icon

class RedisObjectFile(project: Project, configuration: ServerConfiguration, val database: Database) : NoSqlDatabaseObjectFile(project, configuration, String.format("%s/%s", configuration.label, database.name)) {

    override fun getFileType() = FILE_TYPE

    companion object {
        val icon: Icon = GuiUtils.loadIcon("redis.png")
        private val FILE_TYPE = NoSQLFakeFileType("REDIS", icon)
    }
}
