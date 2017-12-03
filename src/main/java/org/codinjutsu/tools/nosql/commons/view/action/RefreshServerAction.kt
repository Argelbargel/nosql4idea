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

package org.codinjutsu.tools.nosql.commons.view.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import org.codinjutsu.tools.nosql.NoSqlExplorerPanel
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils

class RefreshServerAction(private val noSqlExplorerPanel: NoSqlExplorerPanel) : AnAction(REFRESH_TEXT), DumbAware {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        noSqlExplorerPanel.refreshSelectedServer()
    }

    override fun update(event: AnActionEvent) {
        val selected = noSqlExplorerPanel.isDatabaseServerSelected
        event.presentation.isVisible = selected
        if (selected) {
            val isConnected = noSqlExplorerPanel.hasDatabaseServerChildren()
            event.presentation.icon = if (isConnected) REFRESH_ICON else CONNECT_ICON
            event.presentation.text = if (isConnected) REFRESH_TEXT else CONNECT_TEXT
        }
    }

    companion object {
        private val CONNECT_ICON = GuiUtils.loadIcon("connector.png", "connector_dark.png")
        private val REFRESH_ICON = AllIcons.Actions.Refresh
        private val REFRESH_TEXT = "Refresh this server"
        private val CONNECT_TEXT = "Connect to this server"
    }
}
