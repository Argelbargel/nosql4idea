package org.codinjutsu.tools.nosql.commons.view.nodedescriptor

import com.couchbase.client.java.document.json.JsonArray
import org.codinjutsu.tools.nosql.commons.model.SearchResult
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode
import org.codinjutsu.tools.nosql.commons.view.wrapper.ObjectWrapper

internal fun buildTree(searchResult: SearchResult, nodeDescriptorFactory: NodeDescriptorFactory): NoSqlTreeNode {
    val resultTreeNode = NoSqlTreeNode(nodeDescriptorFactory.createResultDescriptor(searchResult))
    searchResult.records.forEach { processRecord(resultTreeNode, it, nodeDescriptorFactory) }
    return resultTreeNode
}

private fun processRecord(parentNode: NoSqlTreeNode, record: ObjectWrapper, nodeDescriptorFactory: NodeDescriptorFactory) {
    record.names.forEach {
        val value = record.getData(it)
        val currentNode = NoSqlTreeNode(nodeDescriptorFactory.createKeyValueDescriptor(it, value))
        process(value, currentNode, nodeDescriptorFactory)
        parentNode.add(currentNode)
    }
}

private fun processRecordListValues(parentNode: NoSqlTreeNode, values: JsonArray, nodeDescriptorFactory: NodeDescriptorFactory) {
    for ((index, value) in values.withIndex()) {
        val currentValueNode = NoSqlTreeNode(nodeDescriptorFactory.createValueDescriptor(index, value))
        process(value, currentValueNode, nodeDescriptorFactory)
        parentNode.add(currentValueNode)
    }
}

private fun process(value: Any?, currentValueNode: NoSqlTreeNode, nodeDescriptorFactory: NodeDescriptorFactory) {
    if (value is JsonArray) {
        processRecordListValues(currentValueNode, value, nodeDescriptorFactory)
    } else if (value is ObjectWrapper) {
        processRecord(currentValueNode, value, nodeDescriptorFactory)
    }
}
