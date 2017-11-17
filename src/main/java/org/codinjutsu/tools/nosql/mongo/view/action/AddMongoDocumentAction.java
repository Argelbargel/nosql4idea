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

package org.codinjutsu.tools.nosql.mongo.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.nosql.commons.view.panel.AbstractNoSQLResultPanel;

import java.awt.event.KeyEvent;

public class AddMongoDocumentAction extends AnAction implements DumbAware {

    private final AbstractNoSQLResultPanel resultPanel;

    public AddMongoDocumentAction(AbstractNoSQLResultPanel resultPanel) {
        super("Add", "Add mongo document", AllIcons.General.Add);
        registerCustomShortcutSet(KeyEvent.VK_INSERT, KeyEvent.ALT_MASK, resultPanel);
        this.resultPanel = resultPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        resultPanel.addDocument();
    }
}
