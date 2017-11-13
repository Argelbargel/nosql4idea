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

package org.codinjutsu.tools.nosql.mongo.view;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.commons.utils.GuiUtils;
import org.codinjutsu.tools.nosql.commons.view.ActionCallback;
import org.codinjutsu.tools.nosql.commons.view.NoSqlTreeNode;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.codinjutsu.tools.nosql.mongo.view.action.CopyResultAction;
import org.codinjutsu.tools.nosql.mongo.view.action.EditMongoDocumentAction;
import org.codinjutsu.tools.nosql.mongo.view.model.JsonTreeModel;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoKeyValueDescriptor;
import org.codinjutsu.tools.nosql.mongo.view.nodedescriptor.MongoResultDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

public class MongoResultPanel extends JPanel implements Disposable {

    private final MongoPanel.MongoDocumentOperations mongoDocumentOperations;
    private JPanel mainPanel;
    private JPanel containerPanel;
    private final Splitter splitter;
    private final JPanel resultTreePanel;
    private final MongoEditionPanel mongoEditionPanel;

    JsonTreeTableView resultTableView;


    public MongoResultPanel(Project project, MongoPanel.MongoDocumentOperations mongoDocumentOperations) {
        this.mongoDocumentOperations = mongoDocumentOperations;
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        splitter = new Splitter(true, 0.6f);

        resultTreePanel = new JPanel(new BorderLayout());

        splitter.setFirstComponent(resultTreePanel);

        mongoEditionPanel = createMongoEditionPanel();

        containerPanel.setLayout(new BorderLayout());
        containerPanel.add(splitter);

        Disposer.register(project, this);
    }

    private MongoEditionPanel createMongoEditionPanel() {
        return new MongoEditionPanel().init(mongoDocumentOperations, new ActionCallback() {
            public void onOperationSuccess(@NotNull String message) {
                hideEditionPanel();
                GuiUtils.showNotification(resultTreePanel, MessageType.INFO, message, Balloon.Position.above);
            }

            @Override
            public void onOperationFailure(@NotNull Exception exception) {
                GuiUtils.showNotification(resultTreePanel, MessageType.ERROR, exception.getMessage(), Balloon.Position.above);
            }

            @Override
            public void onOperationCancelled(@NotNull String message) {
                hideEditionPanel();
            }
        });
    }

    public void updateResultTableTree(MongoResult mongoResult) {
        resultTableView = new JsonTreeTableView(JsonTreeModel.buildJsonTree(mongoResult), JsonTreeTableView.COLUMNS_FOR_READING);
        resultTableView.setName("resultTreeTable");

        resultTableView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && MongoResultPanel.this.isSelectedNodeId()) {
                    MongoResultPanel.this.editSelectedMongoDocument();
                }
            }
        });

        buildPopupMenu();

        resultTreePanel.invalidate();
        resultTreePanel.removeAll();
        resultTreePanel.add(new JBScrollPane(resultTableView));
        resultTreePanel.validate();
    }

    void buildPopupMenu() {
        DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoResultPopupGroup", true);
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(new EditMongoDocumentAction(this));
            actionPopupGroup.add(new CopyResultAction(this));
        }

        PopupHandler.installPopupHandler(resultTableView, actionPopupGroup, "POPUP", ActionManager.getInstance());
    }


    public void editSelectedMongoDocument() {

        DBObject mongoDocument = getSelectedMongoDocument();

        if (mongoDocument == null) {
            return;
        }

        mongoEditionPanel.updateEditionTree(mongoDocument);

        splitter.setSecondComponent(mongoEditionPanel);
    }


    public void addMongoDocument() {
        mongoEditionPanel.updateEditionTree(null);
        splitter.setSecondComponent(mongoEditionPanel);
    }

    private DBObject getSelectedMongoDocument() {
        TreeTableTree tree = resultTableView.getTree();
        NoSqlTreeNode treeNode = (NoSqlTreeNode) tree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return null;
        }

        NodeDescriptor descriptor = treeNode.getDescriptor();
        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            if (StringUtils.equals(keyValueDescriptor.getKey(), "_id")) {
                return mongoDocumentOperations.getMongoDocument(keyValueDescriptor.getValue());
            }
        }

        return null;
    }


    public boolean isSelectedNodeId() {
        if (resultTableView == null) {
            return false;
        }
        TreeTableTree tree = resultTableView.getTree();
        NoSqlTreeNode treeNode = (NoSqlTreeNode) tree.getLastSelectedPathComponent();
        if (treeNode == null) {
            return false;
        }

        NodeDescriptor descriptor = treeNode.getDescriptor();
        if (descriptor instanceof MongoKeyValueDescriptor) {
            MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
            return StringUtils.equals(keyValueDescriptor.getKey(), "_id");
        }

        return false;
    }


    void expandAll() {
        TreeUtil.expandAll(resultTableView.getTree());
    }

    void collapseAll() {
        TreeTableTree tree = resultTableView.getTree();
        TreeUtil.collapseAll(tree, 1);
    }

    public String getSelectedNodeStringifiedValue() {
        NoSqlTreeNode lastSelectedResultNode = (NoSqlTreeNode) resultTableView.getTree().getLastSelectedPathComponent();
        if (lastSelectedResultNode == null) {
            lastSelectedResultNode = (NoSqlTreeNode) resultTableView.getTree().getModel().getRoot();
        }
        NodeDescriptor userObject = lastSelectedResultNode.getDescriptor();
        if (userObject instanceof MongoResultDescriptor) {
            return stringifyResult(lastSelectedResultNode);
        }

        return userObject.toString();
    }

    private void hideEditionPanel() {
        splitter.setSecondComponent(null);
    }

    private String stringifyResult(DefaultMutableTreeNode selectedResultNode) {
        List<Object> stringifiedObjects = new LinkedList<Object>();
        for (int i = 0; i < selectedResultNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedResultNode.getChildAt(i);
            stringifiedObjects.add(childNode.getUserObject());
        }

        return String.format("[ %s ]", StringUtils.join(stringifiedObjects, " , "));
    }

    @Override
    public void dispose() {
        resultTableView = null;
        mongoEditionPanel.dispose();
    }
}
