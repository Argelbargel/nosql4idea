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

package org.codinjutsu.tools.nosql.commons.view.nodedescriptor.result

import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.ColoredTreeCellRenderer

class StandardResultDescriptor @JvmOverloads constructor(collectionName: String = "") : ResultDescriptor<Any> {

    private val formattedText = "results of '$collectionName'"

    override fun renderValue(cellRenderer: ColoredTableCellRenderer, isNodeExpanded: Boolean) {}

    override fun renderNode(cellRenderer: ColoredTreeCellRenderer) {
    }

    override fun getFormattedKey() = formattedText

    override fun getFormattedValue() = ""

    override fun getValue() = null

    override fun setValue(value: Any) {
    }
}
