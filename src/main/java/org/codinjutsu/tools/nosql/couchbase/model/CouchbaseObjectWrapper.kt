package org.codinjutsu.tools.nosql.couchbase.model

import com.couchbase.client.java.document.json.JsonObject
import org.codinjutsu.tools.nosql.commons.view.wrapper.ObjectWrapper

class CouchbaseObjectWrapper(private val jsonObject: JsonObject) : ObjectWrapper {

    override val names: Collection<String>
        get() = jsonObject.names

    override fun get(name: String): Any? = jsonObject.get(name)
}
