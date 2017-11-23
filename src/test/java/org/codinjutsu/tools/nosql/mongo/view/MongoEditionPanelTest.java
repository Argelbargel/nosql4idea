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

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.codinjutsu.tools.nosql.commons.view.ActionCallback;
import org.codinjutsu.tools.nosql.commons.view.EditionPanel;
import org.codinjutsu.tools.nosql.commons.view.NoSQLResultPanelDocumentOperations;
import org.codinjutsu.tools.nosql.commons.view.nodedescriptor.NodeDescriptor;
import org.codinjutsu.tools.nosql.mongo.view.columninfo.MongoWriteableColumnInfoDecider;
import org.codinjutsu.tools.nosql.mongo.view.model.MongoTreeModelFactory;
import org.fest.swing.data.TableCell;
import org.fest.swing.driver.BasicJTableCellReader;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.Containers;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTableFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MongoEditionPanelTest {

    private EditionPanel<DBObject> mongoEditionPanel;

    private FrameFixture frameFixture;
    private MongoTreeModelFactory nodeDescriptorFactory = new MongoTreeModelFactory();
    private NoSQLResultPanelDocumentOperations<DBObject> mockMongoOperations = mock(NoSQLResultPanelDocumentOperations.class);
    private ActionCallback mockActionCallback = mock(ActionCallback.class);

    @AfterEach
    void tearDown() {
        frameFixture.cleanUp();
    }

    @BeforeEach
    void setUp() throws Exception {

        mongoEditionPanel = GuiActionRunner.execute(new GuiQuery<EditionPanel<DBObject>>() {
            protected EditionPanel<DBObject> executeInEDT() {
                EditionPanel<DBObject> panel = new EditionPanel<DBObject>(nodeDescriptorFactory, new MongoWriteableColumnInfoDecider()) {
                    @Override
                    protected void buildPopupMenu() {
                    }
                };
                panel.init(mockMongoOperations, mockActionCallback);
                return panel;
            }
        });

        mongoEditionPanel.updateEditionTree(buildDocument("simpleDocument.json"));

        frameFixture = Containers.showInFrame(mongoEditionPanel);
    }

    @Test
    void displayMongoDocumentInTheTreeTable() {
        frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader())
                .requireColumnCount(2)
                .requireContents(new String[][]{
                        {"_id", "50b8d63414f85401b9268b99"},
                        {"label", "toto"},
                        {"visible", "false"},
                        {"image", "null"}
                });
    }

    @Test
    void editKeyWithStringValue() {
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());

//        edit 'label' key
        editionTreeTable.enterValue(TableCell.row(1).column(1), "Hello");

        frameFixture.button("saveButton").click();

        ArgumentCaptor<DBObject> argument = ArgumentCaptor.forClass(DBObject.class);
        verify(mockMongoOperations).updateDocument(argument.capture());

        assertEquals("{ \"_id\" : { \"$oid\" : \"50b8d63414f85401b9268b99\"} , \"label\" : \"Hello\" , \"visible\" : false , \"image\" :  null }",
                argument.getValue().toString());

        verify(mockActionCallback, times(1)).onOperationSuccess(any(String.class));
    }

    @Test
    void cancelEdition() {
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());

//        edit 'label' key
        editionTreeTable.enterValue(TableCell.row(1).column(1), "Hello");

        frameFixture.button("cancelButton").click();
        verify(mockMongoOperations, times(0)).updateDocument(any(DBObject.class));

        verify(mockActionCallback, times(1)).onOperationCancelled(any(String.class));
    }

    @Test
    void addKeyWithSomeValue() {
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());


        editionTreeTable.selectCell(TableCell.row(1).column(1));
        mongoEditionPanel.addKey("stringKey", "pouet");

        editionTreeTable.selectCell(TableCell.row(1).column(1));
        mongoEditionPanel.addKey("numberKey", "1.1");

        editionTreeTable.requireContents(new String[][]{
                {"_id", "50b8d63414f85401b9268b99"},
                {"label", "toto"},
                {"visible", "false"},
                {"image", "null"},
                {"stringKey", "pouet"},
                {"numberKey", "1.1"},
        });
    }

    @Test
    void addValueInAList() throws Exception {

        mongoEditionPanel.updateEditionTree(buildDocument("simpleDocumentWithSubList.json"));
        JTableFixture editionTreeTable = frameFixture.table("editionTreeTable").cellReader(new JsonTableCellReader());

        editionTreeTable.requireContents(new String[][]{
                {"_id", "50b8d63414f85401b9268b99"},
                {"title", "XP by example"},
                {"tags", "[ \"pair programming\" , \"tdd\" , \"agile\"]"},
                {"[0]", "pair programming"},
                {"[1]", "tdd"},
                {"[2]", "agile"},
                {"innerList", "[ [ 1 , 2 , 3 , 4] , [ false , true] , [ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]]"},
                {"[0]", "[ 1 , 2 , 3 , 4]"},
                {"[1]", "[ false , true]"},
                {"[2]", "[ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]"}});

        editionTreeTable.selectCell(TableCell.row(3).column(1));
        mongoEditionPanel.addValue("refactor");

        editionTreeTable.requireContents(new String[][]{
                {"_id", "50b8d63414f85401b9268b99"},
                {"title", "XP by example"},
                {"tags", "[ \"pair programming\" , \"tdd\" , \"agile\"]"},
                {"[0]", "pair programming"},
                {"[1]", "tdd"},
                {"[2]", "agile"},
                {"[3]", "refactor"},
                {"innerList", "[ [ 1 , 2 , 3 , 4] , [ false , true] , [ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]]"},
                {"[0]", "[ 1 , 2 , 3 , 4]"},
                {"[1]", "[ false , true]"},
                {"[2]", "[ { \"tagName\" : \"pouet\"} , { \"tagName\" : \"paf\"}]"}});

    }

    private static class JsonTableCellReader extends BasicJTableCellReader {

        @Override
        public String valueAt(JTable table, int row, int column) {
            Object value = table.getValueAt(row, column);
            if (value instanceof NodeDescriptor) {
                NodeDescriptor nodeDescriptor = (NodeDescriptor) value;
                return nodeDescriptor.getFormattedKey();
            }
            return value == null ? "null" : value.toString();
        }

    }

    private DBObject buildDocument(String jsonFile) throws IOException {
        DBObject mongoDocument = (DBObject) JSON.parse(IOUtils.toString(getClass().getResourceAsStream(jsonFile), Charset.defaultCharset()));
        mongoDocument.put("_id", new ObjectId(String.valueOf(mongoDocument.get("_id"))));
        return mongoDocument;
    }
}
