package org.codinjutsu.tools.nosql.solr.model.explorer

import org.codinjutsu.tools.nosql.commons.model.Database
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer
import org.codinjutsu.tools.nosql.commons.model.explorer.CommonDatabaseFolder
import org.codinjutsu.tools.nosql.commons.model.explorer.DatabaseServerFolder
import org.codinjutsu.tools.nosql.solr.view.editor.SolrObjectFile

internal class SolrDatabaseServerFolder(databaseServer: DatabaseServer) :
        DatabaseServerFolder<Database>(databaseServer) {
    override fun createDatabaseFolder(database: Database) =
            CommonDatabaseFolder(database, this, true) {
                SolrObjectFile(it, databaseServer.configuration, database)
            }

    override fun canShowConsoleApplication() = true
}