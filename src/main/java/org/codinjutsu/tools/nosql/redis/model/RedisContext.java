package org.codinjutsu.tools.nosql.redis.model;

import org.codinjutsu.tools.nosql.commons.configuration.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.model.DatabaseContext;
import org.codinjutsu.tools.nosql.redis.logic.RedisClient;
import org.codinjutsu.tools.nosql.redis.model.RedisDatabase;
import org.jetbrains.annotations.NotNull;

public class RedisContext extends DatabaseContext<RedisClient> {

    private final RedisDatabase database;

    public RedisContext(RedisClient redisClient, @NotNull ServerConfiguration serverConfiguration, RedisDatabase database) {
        super(redisClient, serverConfiguration);
        this.database = database;
    }

    public RedisDatabase getDatabase() {
        return database;
    }
}