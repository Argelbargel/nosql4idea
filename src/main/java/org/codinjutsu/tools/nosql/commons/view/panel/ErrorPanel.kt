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

package org.codinjutsu.tools.nosql.commons.view.panel

import com.intellij.openapi.ui.Messages
import com.intellij.ui.HoverHyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel

import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import java.awt.*

class ErrorPanel(ex: Exception) : JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        val comp = JBLabel("Error during query execution:")
        comp.foreground = JBColor.RED
        add(comp)
        val hoverHyperlinkLabel = HoverHyperlinkLabel("more detail...")
        hoverHyperlinkLabel.addHyperlinkListener { hyperlinkEvent ->
            if (hyperlinkEvent.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                Messages.showErrorDialog(ex.message, "Error During Query Execution")
            }
        }
        add(Box.createRigidArea(Dimension(10, 10)))
        add(hoverHyperlinkLabel)
    }
}
