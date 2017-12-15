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

package org.codinjutsu.tools.nosql.couchbase

import com.intellij.openapi.project.Project
import org.codinjutsu.tools.nosql.commons.DatabaseUI
import org.codinjutsu.tools.nosql.commons.view.NoSqlResultView
import org.codinjutsu.tools.nosql.commons.view.editor.NoSqlDatabaseObjectFile
import org.codinjutsu.tools.nosql.couchbase.logic.CouchbaseClient
import org.codinjutsu.tools.nosql.couchbase.view.CouchbaseAuthenticationPanel
import org.codinjutsu.tools.nosql.couchbase.view.CouchbaseContext
import org.codinjutsu.tools.nosql.couchbase.view.CouchbasePanel
import org.codinjutsu.tools.nosql.couchbase.view.editor.CouchbaseObjectFile

class CouchbaseUI : DatabaseUI {
    override fun createAythenticationView() = CouchbaseAuthenticationPanel()

    override fun createResultPanel(project: Project, objectFile: NoSqlDatabaseObjectFile): NoSqlResultView {
        val couchbaseObjectFile = objectFile as CouchbaseObjectFile
        return CouchbasePanel(project, CouchbaseContext(CouchbaseClient.getInstance(project), couchbaseObjectFile.configuration, couchbaseObjectFile.database))
    }
}
