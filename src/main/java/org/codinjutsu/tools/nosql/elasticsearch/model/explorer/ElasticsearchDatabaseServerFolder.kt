package org.codinjutsu.tools.nosql.elasticsearch.model.explorer

import com.intellij.openapi.project.Project
import org.codinjutsu.tools.nosql.DatabaseVendorClientManager
import org.codinjutsu.tools.nosql.commons.model.Database
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer
import org.codinjutsu.tools.nosql.commons.model.explorer.FolderDatabaseServerFolder
import org.codinjutsu.tools.nosql.commons.model.explorer.Folder
import org.codinjutsu.tools.nosql.commons.view.editor.NoSqlDatabaseObjectFile
import org.codinjutsu.tools.nosql.elasticsearch.model.ElasticsearchType
import org.codinjutsu.tools.nosql.elasticsearch.model.ElasticsearchDatabase
import org.codinjutsu.tools.nosql.elasticsearch.model.ElasticsearchServerConfiguration

internal class ElasticsearchDatabaseServerFolder(databaseserver: DatabaseServer<ElasticsearchServerConfiguration>, databaseVendorClientManager: DatabaseVendorClientManager) :
        FolderDatabaseServerFolder<ElasticsearchServerConfiguration, ElasticsearchType>(databaseserver, databaseVendorClientManager) {
    override fun createDatabaseFolder(database: Database) =
            ElasticsearchIndexFolder(database as ElasticsearchDatabase, this)

    override fun createNoSqlObjectFile(project: Project): NoSqlDatabaseObjectFile<*>? = null

    override fun deleteChild(child: Folder<*>) {
        databaseClient.dropDatabase(configuration, child.data as ElasticsearchDatabase)
    }
}