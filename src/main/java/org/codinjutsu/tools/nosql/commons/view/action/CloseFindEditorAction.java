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

package org.codinjutsu.tools.nosql.commons.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.nosql.commons.view.NoSqlResultView;

import java.awt.event.KeyEvent;

public class CloseFindEditorAction extends CloseTabToolbarAction {
    private final NoSqlResultView noSqlResultView;

    public CloseFindEditorAction(NoSqlResultView noSqlResultView) {
        getTemplatePresentation().setIcon(AllIcons.Actions.Close);
        registerCustomShortcutSet(KeyEvent.VK_ESCAPE, 0, noSqlResultView);
        this.noSqlResultView = noSqlResultView;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        noSqlResultView.closeFindEditor();
    }

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(false);
    }
}