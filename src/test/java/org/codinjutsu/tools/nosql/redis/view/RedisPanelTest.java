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

package org.codinjutsu.tools.nosql.redis.view;

import com.intellij.ide.CommonActionsManager;
import com.intellij.mock.MockApplicationEx;
import com.intellij.mock.MockEditorFactory;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.impl.DefaultColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.SettingsImpl;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformLiteFixture;
import org.codinjutsu.tools.nosql.ServerConfigurationImpl;
import org.codinjutsu.tools.nosql.commons.view.TableCellReader;
import org.codinjutsu.tools.nosql.commons.view.panel.query.QueryOptions;
import org.codinjutsu.tools.nosql.redis.RedisContext;
import org.codinjutsu.tools.nosql.redis.logic.RedisClient;
import org.codinjutsu.tools.nosql.redis.model.RedisDatabase;
import org.codinjutsu.tools.nosql.redis.model.RedisResult;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTableFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Tuple;

import javax.swing.JPanel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled("Doesn't work at the moment.")
class RedisPanelTest extends PlatformLiteFixture {

    private RedisPanel redisPanelWrapper;

    private FrameFixture frameFixture;

    private final Project dummyProject = DummyProject.getInstance();

    private final RedisClient redisClientMock = mock(RedisClient.class);

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        frameFixture.cleanUp();
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initApplication();
        MockApplicationEx application = PlatformLiteFixture.getApplication();

        EditorEx editor = mock(EditorEx.class);
        when(editor.getSettings()).thenReturn(new SettingsImpl());
        when(editor.getColorsScheme()).thenReturn(new DefaultColorsScheme());
        when(editor.getComponent()).thenReturn(new JPanel());

        EditorFactory editorFactory = new MockEditorFactory() {
            @Override
            public Editor createEditor(@NotNull Document document, Project project) {
                return editor;
            }
        };

        ActionManager actionManager = mock(ActionManager.class);

        ActionToolbar actionToolbar = mock(ActionToolbar.class);
        when(actionToolbar.getComponent()).thenReturn(new JPanel());

        when(actionManager.createActionToolbar(any(), any(), anyBoolean())).thenReturn(actionToolbar);

        CommonActionsManager commonActionsManager = mock(CommonActionsManager.class);

        application.addComponent(EditorFactory.class, editorFactory);
        application.addComponent(ActionManager.class, actionManager);
        application.registerService(CommonActionsManager.class, commonActionsManager);

        when(redisClientMock.loadRecords(any(RedisContext.class), any(QueryOptions.class))).thenReturn(new RedisResult());

        redisPanelWrapper = GuiActionRunner.execute(new GuiQuery<RedisPanel>() {
            protected RedisPanel executeInEDT() {
                return new RedisPanel(dummyProject, new RedisContext(redisClientMock, new ServerConfigurationImpl(), new RedisDatabase("0"))) {
                    @Override
                    protected void addActions(DefaultActionGroup actionResultGroup, AnAction expandAllAction, AnAction collapseAllAction) {
                    }
                };
            }
        });

        frameFixture = Containers.showInFrame(redisPanelWrapper);
    }

    @Test
    void displayTreeWithEachSupportedKeyType() {

        redisPanelWrapper.updateResultTableTree(createRedisResults(), false, "");

        frameFixture.table("resultTreeTable").cellReader(new TableCellReader())
                    .requireColumnCount(2)
                    .requireContents(new String[][]{
                            {"foo:bar", "john"},
                            {"stuff:bar", "[drink, some, beer]"},
                            {"[0]", "drink"},
                            {"[1]", "some"},
                            {"[2]", "beer"},
                            {"stuff:countries", "{France, Canada, Japan}"},
                            {"-", "France"},
                            {"-", "Canada"},
                            {"-", "Japan"},
                            {"stuff:aliases", "{david=dada, mickael=mike, bruno=nono}"},
                            {"david", "dada"},
                            {"mickael", "mike"},
                            {"bruno", "nono"},
                            {"stuff:games:critics", "{(unreal, 8.0), (quake, 9.0), (half-life, 10.0)}"},
                            {"-", "(unreal, 8.0)"},
                            {"-", "(quake, 9.0)"},
                            {"-", "(half-life, 10.0)"},
                    });
    }

    @Test
    void testDisplayTreeWithFragmentedKey() {
        redisPanelWrapper.updateResultTableTree(createRedisResults(), true, ":");
        redisPanelWrapper.expandAll();

        JTableFixture resultTreeTable = frameFixture.table("resultTreeTable");
        resultTreeTable.cellReader(new TableCellReader())
                       .requireColumnCount(2)
                       .requireContents(new String[][]{
                               {"foo", ""},
                               {"bar", "john"},
                               {"stuff", ""},
                               {"bar", "[drink, some, beer]"},
                               {"[0]", "drink"},
                               {"[1]", "some"},
                               {"[2]", "beer"},
                               {"countries", "{France, Canada, Japan}"},
                               {"-", "France"},
                               {"-", "Canada"},
                               {"-", "Japan"},
                               {"aliases", "{david=dada, mickael=mike, bruno=nono}"},
                               {"david", "dada"},
                               {"mickael", "mike"},
                               {"bruno", "nono"},
                               {"games", ""},
                               {"critics", "{(unreal, 8.0), (quake, 9.0), (half-life, 10.0)}"},
                               {"-", "(unreal, 8.0)"},
                               {"-", "(quake, 9.0)"},
                               {"-", "(half-life, 10.0)"},
                       });
    }

    private RedisResult createRedisResults() {
        RedisResult redisResult = new RedisResult();
        redisResult.addString("foo:bar", "john");
        redisResult.addList("stuff:bar", Arrays.asList("drink", "some", "beer"));
        Set<String> countries = new HashSet<>();
        countries.add("France");
        countries.add("Japan");
        countries.add("Canada");
        redisResult.addSet("stuff:countries", countries);

        Map<String, String> aliasByPeopleName = new HashMap<>();
        aliasByPeopleName.put("david", "dada");
        aliasByPeopleName.put("mickael", "mike");
        aliasByPeopleName.put("bruno", "nono");
        redisResult.addHash("stuff:aliases", aliasByPeopleName);

        Set<Tuple> scoreByGameTitle = new TreeSet<>();
        scoreByGameTitle.add(new Tuple("quake", 9d));
        scoreByGameTitle.add(new Tuple("half-life", 10d));
        scoreByGameTitle.add(new Tuple("unreal", 8d));

        redisResult.addSortedSet("stuff:games:critics", scoreByGameTitle);
        return redisResult;
    }
}