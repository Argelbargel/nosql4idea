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

package org.codinjutsu.tools.nosql.mongo.model;

import com.mongodb.DBObject;
import org.codinjutsu.tools.nosql.commons.model.SearchResult;
import org.codinjutsu.tools.nosql.commons.view.wrapper.ObjectWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MongoResult implements SearchResult {

    private final String collectionName;
    private final List<DBObject> mongoObjects = new LinkedList<DBObject>();

    public MongoResult(String collectionName) {
        this.collectionName = collectionName;
    }

    public void add(DBObject dbObject) {
        mongoObjects.add(dbObject);
    }

    public List<DBObject> getMongoObjects() {
        return mongoObjects;
    }

    @NotNull
    @Override
    public String getName() {
        return collectionName;
    }

    @NotNull
    @Override
    public List<ObjectWrapper> getRecords() {
        return Collections.emptyList(); // todo
    }

    @Override
    public int getCount() {
        return getRecords().size();
    }
}
