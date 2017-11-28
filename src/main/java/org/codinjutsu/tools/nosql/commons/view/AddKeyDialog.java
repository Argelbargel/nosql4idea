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

package org.codinjutsu.tools.nosql.commons.view;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AddKeyDialog<DOCUMENT> extends AbstractAddDialog<DOCUMENT> {

    private JTextField nameTextfield;
    private ComboBox typeCombobox;
    private JPanel valuePanel;
    private JPanel mainPanel;

    private AddKeyDialog(EditionPanel<DOCUMENT> editionPanel) {
        super(editionPanel);
        mainPanel.setPreferredSize(GuiUtils.enlargeWidth(mainPanel.getPreferredSize(), 1.5d));
        valuePanel.setLayout(new BorderLayout());
        nameTextfield.setName("keyName");
        typeCombobox.setName("valueType");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    public static <DOCUMENT> AddKeyDialog<DOCUMENT> createDialog(EditionPanel<DOCUMENT> parentPanel) {
        AddKeyDialog<DOCUMENT> dialog = new AddKeyDialog<DOCUMENT>(parentPanel);
        dialog.init();
        dialog.setTitle("Add A Key");
        return dialog;
    }

    @Override
    protected void init() {
        super.init();
        initCombo(typeCombobox, valuePanel);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String keyName = getKey();
        if (StringUtils.isBlank(keyName)) {
            return new ValidationInfo("Key name is not set");
        }

        if (editionPanel.containsKey(keyName)) {
            return new ValidationInfo(String.format("Key '%s' is already used", keyName));
        }

        try {
            currentEditor.validate();
        } catch (Exception ex) {
            return new ValidationInfo(ex.getMessage());
        }

        return null;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return nameTextfield;
    }

    public String getKey() {
        return nameTextfield.getText();
    }

    @Override
    public Object getValue() {
        return currentEditor.getValue();
    }
}