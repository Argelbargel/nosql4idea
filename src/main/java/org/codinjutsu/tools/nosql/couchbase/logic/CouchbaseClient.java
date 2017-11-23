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

package org.codinjutsu.tools.nosql.couchbase.logic;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.nosql.DatabaseVendor;
import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.logic.ConfigurationException;
import org.codinjutsu.tools.nosql.commons.logic.LoadableDatabaseClient;
import org.codinjutsu.tools.nosql.commons.model.AuthenticationSettings;
import org.codinjutsu.tools.nosql.commons.model.Database;
import org.codinjutsu.tools.nosql.commons.model.DatabaseServer;
import org.codinjutsu.tools.nosql.commons.view.panel.query.QueryOptions;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseDatabase;
import org.codinjutsu.tools.nosql.couchbase.model.CouchbaseResult;
import org.codinjutsu.tools.nosql.couchbase.view.CouchbaseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static java.util.Collections.singletonList;

public class CouchbaseClient implements LoadableDatabaseClient<CouchbaseContext, CouchbaseResult, JsonObject> {

    public static CouchbaseClient getInstance(Project project) {
        return ServiceManager.getService(project, CouchbaseClient.class);
    }

    @Override
    public void connect(ServerConfiguration serverConfiguration) {
        CouchbaseCluster cluster = createCluster(serverConfiguration);
        String userDatabase = serverConfiguration.getUserDatabase();
        Bucket bucket = null;
        try {
            if (StringUtils.isEmpty(userDatabase)) {
                bucket = cluster.openBucket();
            } else {
                bucket = cluster.openBucket(userDatabase);
            }
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        } finally {
            if (bucket != null) {
                bucket.close();
            }
            cluster.disconnect();
        }
    }

    @NotNull
    CouchbaseCluster createCluster(ServerConfiguration serverConfiguration) {
        return CouchbaseCluster.create(serverConfiguration.getServerUrl());
    }

    @Override
    public void loadServer(DatabaseServer databaseServer) {
        Cluster cluster = createCluster(databaseServer.getConfiguration());
        databaseServer.setDatabases(collectCouchDatabases(databaseServer, cluster));
        cluster.disconnect();
    }

    @NotNull
    private List<Database> collectCouchDatabases(DatabaseServer databaseServer, Cluster cluster) {
        AuthenticationSettings authenticationSettings = databaseServer.getConfiguration().getAuthenticationSettings();
        ClusterManager clusterManager = cluster.clusterManager(authenticationSettings.getUsername(), authenticationSettings.getPassword());

        String userBucket = databaseServer.getConfiguration().getUserDatabase();
        if (StringUtils.isNotBlank(userBucket)) {
            return singletonList(new CouchbaseDatabase(clusterManager.getBucket(userBucket).name()));
        }

        return clusterManager.getBuckets()
                .stream()
                .map(bucketSettings -> new CouchbaseDatabase(bucketSettings.name()))
                .collect(Collectors.toList());
    }

    @Override
    public void cleanUpServers() {

    }

    @Override
    public void registerServer(DatabaseServer databaseServer) {

    }

    @Override
    public ServerConfiguration defaultConfiguration() {
        return ServerConfiguration.Companion.create(DatabaseVendor.COUCHBASE, "localhost");
    }

    @Override
    public CouchbaseResult loadRecords(CouchbaseContext context, QueryOptions queryOptions) {
        ServerConfiguration configuration = context.getServerConfiguration();
        CouchbaseDatabase database = context.getDatabase();
        Cluster cluster = CouchbaseCluster.create(DefaultCouchbaseEnvironment
                        .builder()
                        .queryEnabled(true)
                        .build(),
                configuration.getServerUrl());
//        AuthenticationSettings authenticationSettings = configuration.getAuthenticationSettings();
//        ClusterManager clusterManager = cluster.clusterManager(authenticationSettings.getUsername(), authenticationSettings.getPassword());

        Bucket bucket = cluster.openBucket(database.getName(), 10, TimeUnit.SECONDS);
        N1qlQueryResult queryResult = bucket.query(N1qlQuery.simple(select("*").from(i(database.getName())).limit(queryOptions.getResultLimit())));

//TODO dirty zone :(
        CouchbaseResult result = new CouchbaseResult(database.getName());
        List<JsonObject> errors = queryResult.errors();
        if (!errors.isEmpty()) {
            cluster.disconnect();
            result.addErrors(errors);
            return result;
        }

        for (N1qlQueryRow row : queryResult.allRows()) {
            result.add(row.value());
        }
        cluster.disconnect();
        return result;
    }

    @Nullable
    @Override
    public JsonObject findDocument(CouchbaseContext context, @NotNull Object _id) {
        return null;
    }

    @Override
    public void update(@NotNull CouchbaseContext context, @NotNull JsonObject jsonObject) {
    }

    @Override
    public void delete(@NotNull CouchbaseContext context, @NotNull Object _id) {
    }
}
