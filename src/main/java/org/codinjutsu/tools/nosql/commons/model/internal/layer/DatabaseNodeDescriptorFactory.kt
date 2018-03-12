package org.codinjutsu.tools.nosql.commons.model.internal.layer

import org.codinjutsu.tools.nosql.commons.style.StyleAttributesProvider
import org.codinjutsu.tools.nosql.commons.utils.getSimpleTextAttributes
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptorFactory
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.internal.DatabaseIndexedValueDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.internal.DatabaseResultDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.keyvalue.BooleanKeyValueDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.keyvalue.DateKeyValueDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.keyvalue.DefaultKeyValueDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.keyvalue.KeyValueDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.keyvalue.NumberKeyValueDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.keyvalue.StringKeyValueDescriptor
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.keyvalue.TypedKeyValueDescriptor

internal class DatabaseNodeDescriptorFactory : NodeDescriptorFactory {

    override fun createResultDescriptor(name: String) = DatabaseResultDescriptor(name)

    override fun createKeyValueDescriptor(key: String, value: DatabaseElement): KeyValueDescriptor<*> =
            when (value) {
                is DatabasePrimitive -> when {
                    value.isBoolean() -> BooleanKeyValueDescriptor(key, value.asBoolean())
                    value.isNumber() -> NumberKeyValueDescriptor(key, value.asNumber())
                    value.isDate() -> DateKeyValueDescriptor(key, value.asDate())
                    value.isString() -> StringKeyValueDescriptor(key, value.asString())
                    else -> DefaultKeyValueDescriptor(key, value, getSimpleTextAttributes(value))
                }
            // todo is String -> StringKeyValueDescriptor(key, value)
                else -> TypedKeyValueDescriptor(key, value, StyleAttributesProvider.getObjectAttribute(), findIcon(value))
            // todo else -> DefaultKeyValueDescriptor(key, value, getSimpleTextAttributes(value))
            }

    override fun createIndexValueDescriptor(index: Int, value: DatabaseElement) =
            DatabaseIndexedValueDescriptor.createDescriptor(index, value)
}