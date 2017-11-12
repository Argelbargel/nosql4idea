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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoIterable;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.commons.logic.DatabaseClient;
import org.codinjutsu.tools.nosql.commons.logic.FolderDatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.model.Database;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.mongo.model.MongoCollection;
import org.codinjutsu.tools.nosql.mongo.model.MongoDatabase;
import org.codinjutsu.tools.nosql.mongo.model.MongoQueryOptions;
import org.codinjutsu.tools.nosql.mongo.model.MongoResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MongoClient implements DatabaseClient, FolderDatabaseClient<MongoDatabase, MongoCollection> {

    private static final Logger LOG = Logger.getLogger(MongoClient.class);
    private final List<DatabaseServer> databaseServers = new LinkedList<>();

    public static MongoClient getInstance(Project project) {
        return ServiceManager.getService(project, MongoClient.class);
    }

    public void connect(ServerConfiguration configuration) {
        com.mongodb.MongoClient mongo = null;
        try {
            String userDatabase = configuration.getUserDatabase();
            mongo = createMongoClient(configuration);

            MongoIterable<String> collectionNames;
            if (StringUtils.isNotEmpty(userDatabase)) {
                collectionNames = mongo.getDatabase(userDatabase).listCollectionNames();
            } else {
                collectionNames = mongo.getDatabase("test").listCollectionNames();
            }
            collectionNames.first();

        } catch (IOException ex) {
            throw new MongoConnectionException(ex);
        } catch (MongoException ex) {
            LOG.error("Error when accessing Mongo server", ex);
            throw new MongoConnectionException(ex.getMessage());
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void cleanUpServers() {
        databaseServers.clear();
    }

    public void registerServer(DatabaseServer databaseServer) {
        databaseServers.add(databaseServer);
    }

    @Override
    public ServerConfiguration defaultConfiguration() {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDatabaseVendor(DatabaseVendor.MONGO);
        serverConfiguration.setServerUrl(DatabaseVendor.MONGO.defaultUrl);
        return serverConfiguration;
    }

    public List<DatabaseServer> getServers() {
        return databaseServers;
    }

    public void loadServer(DatabaseServer databaseServer) {
        databaseServer.setStatus(DatabaseServer.Status.LOADING);
        List<Database> mongoDatabases = loadDatabaseCollections(databaseServer.getConfiguration());
        databaseServer.setDatabases(mongoDatabases);
        databaseServer.setStatus(DatabaseServer.Status.OK);
    }

    List<Database> loadDatabaseCollections(ServerConfiguration configuration) {
        com.mongodb.MongoClient mongo = null;
        List<Database> mongoDatabases = new LinkedList<>();
        try {
            String userDatabase = configuration.getUserDatabase();

            mongo = createMongoClient(configuration);

            if (StringUtils.isNotEmpty(userDatabase)) {
                DB database = mongo.getDB(userDatabase);
                mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
            } else {
                List<String> databaseNames = mongo.getDatabaseNames();
                Collections.sort(databaseNames);
                for (String databaseName : databaseNames) {
                    DB database = mongo.getDB(databaseName);
                    mongoDatabases.add(createMongoDatabaseAndItsCollections(database));
                }
            }

            return mongoDatabases;
        } catch (MongoException | UnknownHostException mongoEx) {
            throw new ConfigurationException(mongoEx);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private MongoDatabase createMongoDatabaseAndItsCollections(DB database) {
        MongoDatabase mongoDatabase = new MongoDatabase(database.getName());


        Set<String> collectionNames = database.getCollectionNames();
        for (String collectionName : collectionNames) {
            mongoDatabase.addCollection(new MongoCollection(collectionName, database.getName()));
        }
        return mongoDatabase;
    }

    public void update(ServerConfiguration configuration, MongoCollection mongoCollection, DBObject mongoDocument) {
        com.mongodb.MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.save(mongoDocument);
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public void delete(ServerConfiguration configuration, MongoCollection mongoCollection, Object _id) {
        com.mongodb.MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.remove(new BasicDBObject("_id", _id));
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    @Override
    public void dropFolder(@NotNull ServerConfiguration configuration, @NotNull MongoCollection mongoCollection) {
        com.mongodb.MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            collection.drop();
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    @Override
    public void dropDatabase(@NotNull ServerConfiguration configuration, @NotNull MongoDatabase database) {
        com.mongodb.MongoClient mongo = null;
        try {
            mongo = createMongoClient(configuration);
            mongo.dropDatabase(database.getName());
        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public MongoResult loadCollectionValues(ServerConfiguration configuration, MongoCollection mongoCollection, MongoQueryOptions mongoQueryOptions) {
        com.mongodb.MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());

            MongoResult mongoResult = new MongoResult(mongoCollection.getName());
            if (mongoQueryOptions.isAggregate()) {
                return aggregate(mongoQueryOptions, mongoResult, collection);
            }

            return find(mongoQueryOptions, mongoResult, collection);

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    public DBObject findMongoDocument(ServerConfiguration configuration, MongoCollection mongoCollection, Object _id) {
        com.mongodb.MongoClient mongo = null;
        try {
            String databaseName = mongoCollection.getDatabaseName();
            mongo = createMongoClient(configuration);

            DB database = mongo.getDB(databaseName);
            DBCollection collection = database.getCollection(mongoCollection.getName());
            return collection.findOne(new BasicDBObject("_id", _id));

        } catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (mongo != null) {
                mongo.close();
            }
        }
    }

    private MongoResult aggregate(MongoQueryOptions mongoQueryOptions, MongoResult mongoResult, DBCollection collection) {
        AggregationOutput aggregate = collection.aggregate(mongoQueryOptions.getOperations());
        int index = 0;
        Iterator<DBObject> iterator = aggregate.results().iterator();
        while (iterator.hasNext() && index < mongoQueryOptions.getResultLimit()) {
            mongoResult.add(iterator.next());
        }
        return mongoResult;
    }

    private MongoResult find(MongoQueryOptions mongoQueryOptions, MongoResult mongoResult, DBCollection collection) {
        DBObject filter = mongoQueryOptions.getFilter();
        DBObject projection = mongoQueryOptions.getProjection();
        DBObject sort = mongoQueryOptions.getSort();

        DBCursor cursor;
        if (projection == null) {
            cursor = collection.find(filter);
        } else {
            cursor = collection.find(filter, projection);
        }

        if (sort != null) {
            cursor = cursor.sort(sort);
        }

        try {
            int index = 0;
            while (cursor.hasNext() && index < mongoQueryOptions.getResultLimit()) {
                mongoResult.add(cursor.next());
                index++;
            }
        } finally {
            cursor.close();
        }
        return mongoResult;
    }

    private com.mongodb.MongoClient createMongoClient(ServerConfiguration configuration) throws UnknownHostException {
        String serverUrl = configuration.getServerUrl();
        if (StringUtils.isEmpty(serverUrl)) {
            throw new ConfigurationException("server host is not set");
        }

        MongoClientURIBuilder uriBuilder = MongoClientURIBuilder.builder();
        uriBuilder.setServerAddresses(serverUrl);
        AuthenticationSettings authenticationSettings = configuration.getAuthenticationSettings();
        MongoExtraSettings mongoExtraSettings = new MongoExtraSettings(authenticationSettings.getExtras());
        if (StringUtils.isNotEmpty(authenticationSettings.getUsername())) {
            uriBuilder.setCredential(authenticationSettings.getUsername(), authenticationSettings.getPassword(), mongoExtraSettings.getAuthenticationDatabase());
        }


        if (mongoExtraSettings.getAuthenticationMechanism() != null) {
            uriBuilder.setAuthenticationMecanism(mongoExtraSettings.getAuthenticationMechanism());
        }

        if (mongoExtraSettings.isSsl()) {
            uriBuilder.sslEnabled();
        }

        return new com.mongodb.MongoClient(new MongoClientURI(uriBuilder.build()));
    }

}
