package org.codinjutsu.tools.nosql.elasticsearch.model

import org.codinjutsu.tools.nosql.commons.model.Folder
import org.codinjutsu.tools.nosql.commons.model.Database

internal class ElasticsearchDatabase(name: String, private val types: MutableCollection<ElasticsearchCollection> = mutableListOf()) : Database(name), Folder<ElasticsearchCollection> {
    override fun getChildFolders() = types

    override fun addCollection(collection: ElasticsearchCollection) {
        types.add(collection)
    }
}