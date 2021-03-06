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

package org.codinjutsu.tools.nosql.mongo.logic;

class MongoClientTest {
/* todo

    private MongoClient mongoClient;
    private final QueryOptions queryOptions = new QueryOptionsImpl();
    private MongoContext context;
    private DBCollection dbCollection;

    @Nested
    class LoadCollectionsTest {

        private DBCursor cursor;
        private int count = 0;

        @BeforeEach
        void setUp() {
            cursor = mock(DBCursor.class);
            when(cursor.hasNext()).thenAnswer((Answer<Boolean>) invocation -> count++ == 0);
            when(cursor.next()).thenAnswer(invocation -> new BasicDBObject());
        }

        @Test
        void loadCollectionsWithEmptyFilter() {
            when(dbCollection.find(any())).thenReturn(cursor);
            MongoQueryOptions mongoQueryOptions = new MongoQueryOptions(queryOptions);
            mongoQueryOptions.setResultLimit(3);

            mongoClient.loadCollectionValues(context, mongoQueryOptions);

            verify(dbCollection, times(1)).find(any());
            verify(dbCollection, never()).find(any(), (DBObject) any());
            verify(cursor, never()).sort(any());
            verify(cursor, times(2)).hasNext();
            verify(cursor, times(1)).next();
            verify(cursor, times(1)).close();
        }

        @Test
        void loadCollectionsWithFilterAndProjection() {
            MongoQueryOptions mongoQueryOptions = new MongoQueryOptions(queryOptions);
            mongoQueryOptions.setFilter("{\"label\":\"tata\"}");
            mongoQueryOptions.setProjection("{\"label\":1, \"_id\": 0}");
            mongoQueryOptions.setResultLimit(3);
            when(dbCollection.find(mongoQueryOptions.getFilter(), mongoQueryOptions.getProjection())).thenReturn(cursor);

            mongoClient.loadCollectionValues(context, mongoQueryOptions);

            verify(dbCollection, times(1)).find(mongoQueryOptions.getFilter(), mongoQueryOptions.getProjection());
            verify(dbCollection, never()).find(any());
            verify(cursor, never()).sort(any());
            verify(cursor, times(2)).hasNext();
            verify(cursor, times(1)).next();
            verify(cursor, times(1)).close();
        }

        @Test
        void loadCollectionsWithFilterAndProjectionAndSortByPrice() {
            MongoQueryOptions mongoQueryOptions = new MongoQueryOptions(queryOptions);
            mongoQueryOptions.setFilter("{\"label\":\"tata\"}");
            mongoQueryOptions.setProjection("{\"label\": 1, \"_id\": 0, \"price\": 1}");
            mongoQueryOptions.setSort("{\"price\": 1}");
            mongoQueryOptions.setResultLimit(3);
            when(dbCollection.find(mongoQueryOptions.getFilter(), mongoQueryOptions.getProjection())).thenReturn(cursor);
            when(cursor.sort(any())).thenReturn(cursor);

            mongoClient.loadCollectionValues(context, mongoQueryOptions);

            verify(dbCollection, times(1)).find(mongoQueryOptions.getFilter(), mongoQueryOptions.getProjection());
            verify(dbCollection, never()).find(any());
            verify(cursor, times(1)).sort(any());
            verify(cursor, times(2)).hasNext();
            verify(cursor, times(1)).next();
            verify(cursor, times(1)).close();
        }
    }

    @Test
    void updateMongoDocument() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions(queryOptions);
        mongoQueryOptions.setFilter("{'label': 'tete'}");
        DBObject dbObject = new BasicDBObject();
        mongoClient.update(context, dbObject);
        verify(dbCollection, times(1)).save(dbObject);
    }

    @Test
    void deleteMongoDocument() {
        mongoClient.delete(context, new Object());
        verify(dbCollection, times(1)).remove(any());
    }

    @Test
    void loadCollectionsWithAggregateOperators() {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions(queryOptions);
        mongoQueryOptions.setOperations("[{'$match': {'price': 15}}, {'$project': {'label': 1, 'price': 1}}, {'$group': {'_id': '$label', 'total': {'$sum': '$price'}}}]");
        AggregationOutput aggregationOutput = mock(AggregationOutput.class);
        when(dbCollection.aggregate(any())).thenReturn(aggregationOutput);
        BasicDBObject basicDBObject1 = new BasicDBObject();
        BasicDBObject basicDBObject2 = new BasicDBObject();
        when(aggregationOutput.results()).thenReturn(Arrays.asList(basicDBObject1, basicDBObject2));

        SearchResult mongoSearchResult = mongoClient.loadCollectionValues(context, mongoQueryOptions);
        assertNotNull(mongoSearchResult);

        verify(dbCollection, times(1)).aggregate(any());
        verify(dbCollection, never()).find(any());
        verify(dbCollection, never()).find(any(), (DBObject) any());

        List<ObjectWrapper> mongoObjects = mongoSearchResult.getRecords();

        assertEquals(2, mongoObjects.size());
        assertEquals(basicDBObject1, mongoObjects.get(0));
        assertEquals(basicDBObject2, mongoObjects.get(1));
    }

    @BeforeEach
    void setUp() {
        dbCollection = mock(DBCollection.class);
        DB db = mock(DB.class);
        when(db.getCollection(any())).thenReturn(dbCollection);
        com.mongodb.MongoClient mongo = mock(com.mongodb.MongoClient.class);
        when(mongo.getDB(any())).thenReturn(db);
        mongoClient = new MongoClientStub(mongo);
        MongoServerConfiguration serverConfiguration = mongoClient.defaultConfiguration();
        serverConfiguration.setServerUrl("localhost:27017");
        context = new MongoContext(mongoClient, serverConfiguration, new MongoCollection("dummyCollection", "test"));
    }

*/
}

