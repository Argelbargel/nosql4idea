package org.codinjutsu.tools.nosql.elasticsearch.logic.commands

import org.codinjutsu.tools.nosql.commons.logic.gson.AbstractGetCommand
import org.codinjutsu.tools.nosql.elasticsearch.view.ElasticsearchContext

internal class FetchDocument(private val context: ElasticsearchContext, private val id: String) : AbstractGetCommand() {

    override fun buildURL() = urlWithIndexAndType(context).addNameToPath(id)
}