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

package org.codinjutsu.tools.nosql;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.PanelWithButtons;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.commons.configuration.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.configuration.WriteableServerConfiguration;
import org.codinjutsu.tools.nosql.commons.view.ServerConfigurationPanelFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang.StringUtils.isBlank;

public class NoSqlConfigurable extends BaseConfigurable {

    private final Project project;

    private final NoSqlConfiguration configuration;

    private final List<ServerConfiguration> configurations;
    private final ServerConfigurationPanelFactory serverConfigurationPanelFactory;

    private JPanel mainPanel;
    private JBTable table;
    private final NoSqlServerTableModel tableModel;
    private ShellPathPanel mongoShellPanel;
    private ShellPathPanel redisShellPanel;
    private ShellPathPanel solrShellPanel;

    public NoSqlConfigurable(Project project) {
        this.project = project;
        this.configuration = NoSqlConfiguration.getInstance(project);
        this.serverConfigurationPanelFactory = new ServerConfigurationPanelFactory(
                project,
                DatabaseVendorUIManager.getInstance(project)
        );
        configurations = new LinkedList<>(this.configuration.getServerConfigurations());
        tableModel = new NoSqlServerTableModel(configurations);
        mainPanel = new JPanel(new BorderLayout());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "NoSql Servers";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preferences.noSqlOptions";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel databaseVendorShellOptionsPanel = new JPanel();
        databaseVendorShellOptionsPanel.setLayout(new BoxLayout(databaseVendorShellOptionsPanel, BoxLayout.Y_AXIS));
        mongoShellPanel = new ShellPathPanel(DatabaseVendor.MONGO, "--version");
        databaseVendorShellOptionsPanel.add(mongoShellPanel);
        redisShellPanel = new ShellPathPanel(DatabaseVendor.REDIS, "--version");
        databaseVendorShellOptionsPanel.add(redisShellPanel);
        solrShellPanel = new ShellPathPanel(DatabaseVendor.SOLR, "-V");
        databaseVendorShellOptionsPanel.add(solrShellPanel);

        mainPanel.add(databaseVendorShellOptionsPanel, BorderLayout.NORTH);

        mainPanel.add(new TablePanel(), BorderLayout.CENTER);

        return mainPanel;
    }

    public boolean isModified() {
        return areConfigurationsModified() || isRedisShellPathModified();
    }

    @Override
    public void apply() {
        stopEditing();
        if (areConfigurationsModified()) {
            configuration.setServerConfigurations(configurations);
        }

        if (isMongoShellPathModified()) {
            configuration.setShellPath(DatabaseVendor.MONGO, mongoShellPanel.getShellPath());
        }

        if (isRedisShellPathModified()) {
            configuration.setShellPath(DatabaseVendor.REDIS, redisShellPanel.getShellPath());
        }

        if (isSolrShellPathModified()) {
            configuration.setShellPath(DatabaseVendor.SOLR, solrShellPanel.getShellPath());
        }

        NoSqlWindowManager.getInstance(project).apply();
    }

    private boolean isMongoShellPathModified() {
        return mongoShellPanel.isShellPathModified(NoSqlConfiguration.getInstance(project).getShellPath(DatabaseVendor.MONGO));
    }

    private boolean isRedisShellPathModified() {
        return redisShellPanel.isShellPathModified(NoSqlConfiguration.getInstance(project).getShellPath(DatabaseVendor.REDIS));
    }

    private boolean isSolrShellPathModified() {
        return solrShellPanel.isShellPathModified(NoSqlConfiguration.getInstance(project).getShellPath(DatabaseVendor.SOLR));
    }

    private boolean areConfigurationsModified() {
        List<ServerConfiguration> existingConfigurations = NoSqlConfiguration.getInstance(project).getServerConfigurations();
        return configurations.size() != existingConfigurations.size() || existingConfigurations.stream().anyMatch(existingConfiguration -> !configurations.contains(existingConfiguration));
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        tableModel.removeTableModelListener(table);
        mongoShellPanel.dispose();
        redisShellPanel.dispose();
        solrShellPanel.dispose();
        table = null;
    }

    private void stopEditing() {
        if (table.isEditing()) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }

    private void onAddAction() {
        stopEditing();

        SelectDatabaseVendorDialog databaseVendorDialog = new SelectDatabaseVendorDialog(mainPanel);
        databaseVendorDialog.setTitle("Add a NoSql Server");
        databaseVendorDialog.show();
        if (!databaseVendorDialog.isOK()) {
            return;
        }

        DatabaseVendor selectedDatabaseVendor = databaseVendorDialog.getSelectedDatabaseVendor();
        WriteableServerConfiguration serverConfiguration = (WriteableServerConfiguration) selectedDatabaseVendor.getClient(project).defaultConfiguration();

        ConfigurationDialog dialog = new ConfigurationDialog(
                mainPanel,
                serverConfigurationPanelFactory,
                serverConfiguration
        );
        dialog.setTitle("Add a NoSql Server");
        dialog.show();
        if (dialog.isOK()) {
            configurations.add(serverConfiguration);
            int index = configurations.size() - 1;
            tableModel.fireTableRowsInserted(index, index);
            table.getSelectionModel().setSelectionInterval(index, index);
            table.scrollRectToVisible(table.getCellRect(index, 0, true));
        }
    }

    private void onEditAction() {
        stopEditing();

        int selectedIndex = table.getSelectedRow();
        if (selectedIndex < 0 || selectedIndex >= tableModel.getRowCount()) {
            return;
        }
        ServerConfiguration sourceConfiguration = configurations.get(selectedIndex);
        WriteableServerConfiguration copiedConfiguration = (WriteableServerConfiguration) sourceConfiguration.copy();

        ConfigurationDialog dialog = new ConfigurationDialog(
                mainPanel,
                serverConfigurationPanelFactory,
                copiedConfiguration
        );
        dialog.setTitle("Edit a NoSql Server");
        dialog.show();
        if (dialog.isOK()) {
            configurations.set(selectedIndex, copiedConfiguration);
            tableModel.fireTableRowsUpdated(selectedIndex, selectedIndex);
            table.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
        }
    }

    private void onRemoveAction() {
        stopEditing();
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0 && selectedIndex < tableModel.getRowCount()) {
            TableUtil.removeSelectedItems(table);
        }
    }

    private class ShellPathPanel extends JPanel implements Disposable {

        private static final int TIMEOUT_MS = 60 * 1000;
        private final String testParameter;

        private LabeledComponent<TextFieldWithBrowseButton> shellPathField;

        private ShellPathPanel(DatabaseVendor databaseVendor, String testParameter) {
            this.testParameter = testParameter;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(createLabel(databaseVendor.name));
            shellPathField = createShellPathField(databaseVendor);
            add(shellPathField);
            add(createTestButton(databaseVendor));
        }

        private JLabel createLabel(String databaseVendorName) {
            return new JLabel(String.format("Path to %s CLI:\t\t", databaseVendorName));
        }

        private JButton createTestButton(final DatabaseVendor databaseVendorName) {
            JButton testButton = new JButton("Test");
            testButton.addActionListener(actionEvent -> testPath(databaseVendorName));
            return testButton;
        }

        private void testPath(final DatabaseVendor databaseVendor) {
            ProcessOutput processOutput;
            try {
                processOutput = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> checkShellPath(databaseVendor, getShellPath()), "Testing " + databaseVendor.name + " CLI Executable...", true, NoSqlConfigurable.this.project);
            } catch (ProcessCanceledException pce) {
                return;
            } catch (Exception e) {
                Messages.showErrorDialog(mainPanel, e.getMessage(), "Something wrong happened");
                return;
            }
            if (processOutput != null && processOutput.getExitCode() == 0) {
                Messages.showInfoMessage(mainPanel, processOutput.getStdout(), databaseVendor.name + " CLI Path Checked");
            }
        }

        ProcessOutput checkShellPath(DatabaseVendor databaseVendor, String shellPath) throws ExecutionException, TimeoutException {
            if (isBlank(shellPath)) {
                return null;
            }

            GeneralCommandLine commandLine = new GeneralCommandLine();
            commandLine.setExePath(shellPath);
            if (testParameter != null) {
                commandLine.addParameter(testParameter);
            }
            CapturingProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), CharsetToolkit.getDefaultSystemCharset(), "");
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            ProcessOutput result = indicator == null ?
                    handler.runProcess(TIMEOUT_MS) :
                    handler.runProcessWithProgressIndicator(indicator);
            if (result.isTimeout()) {
                throw new TimeoutException(String.format("Couldn't check %s CLI executable - stopped by timeout.", databaseVendor.name));
            }
            if (result.isCancelled()) {
                throw new ProcessCanceledException();
            }
            if (result.getExitCode() != 0 || !result.getStderr().isEmpty()) {
                throw new ExecutionException(String.format("Errors while executing %s. exitCode=%s errors: %s",
                        commandLine.toString(),
                        result.getExitCode(),
                        result.getStderr()));
            }
            return result;
        }

        String getShellPath() {
            String shellPath = shellPathField.getComponent().getText();
            if (StringUtils.isNotBlank(shellPath)) {
                return shellPath;
            }

            return null;
        }

        boolean isShellPathModified(String shellPath) {
            return !StringUtils.equals(shellPath, getShellPath());
        }

        private LabeledComponent<TextFieldWithBrowseButton> createShellPathField(DatabaseVendor databaseVendor) {
            LabeledComponent<TextFieldWithBrowseButton> shellPathField = new LabeledComponent<>();
            TextFieldWithBrowseButton component = new TextFieldWithBrowseButton();
            component.getChildComponent().setName("shellPathField");
            shellPathField.setComponent(component);
            shellPathField.getComponent().addBrowseFolderListener(String.format("%s CLI configuration", databaseVendor.name), "", null,
                    new FileChooserDescriptor(true, false, false, false, false, false));

            shellPathField.getComponent().setText(configuration.getShellPath(databaseVendor));

            return shellPathField;
        }

        @Override
        public void dispose() {
            shellPathField = null;
        }

    }

    private class TablePanel extends PanelWithButtons {

        private TablePanel() {
            initPanel();
        }

        @NotNull
        @Override
        protected String getLabelText() {
            return "Servers";
        }

        @Override
        protected JButton[] createButtons() {
            return new JButton[]{};
        }

        @Override
        protected JComponent createMainComponent() {
            table = new JBTable(tableModel);
            table.getEmptyText().setText("No server configuration set");
            table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setDefaultRenderer(DatabaseVendor.class, new ColoredTableCellRenderer() {
                @Override
                protected void customizeCellRenderer(JTable jTable, Object value, boolean b, boolean b1, int i, int i1) {
                    DatabaseVendor databaseVendor = (DatabaseVendor) value;
                    this.setIcon(databaseVendor.icon);
                    this.append(databaseVendor.name);
                }
            });

            TableColumn autoConnectColumn = table.getColumnModel().getColumn(3);
            int autoConnectColumnWidth = table.getFontMetrics(table.getFont()).stringWidth(table.getColumnName(3)) + 10;
            autoConnectColumn.setPreferredWidth(autoConnectColumnWidth);
            autoConnectColumn.setMaxWidth(autoConnectColumnWidth);
            autoConnectColumn.setMinWidth(autoConnectColumnWidth);

            return ToolbarDecorator.createDecorator(table)
                    .setAddAction(button -> onAddAction())
                    .setAddActionName("addServer")
                    .setEditAction(button -> onEditAction())
                    .setEditActionName("editServer")
                    .setRemoveAction(button -> onRemoveAction())
                    .setRemoveActionName("removeServer")
                    .disableUpDownActions()
                    .createPanel();
        }
    }
}
