package org.codinjutsu.tools.nosql.commons.logic;

import org.codinjutsu.tools.nosql.ServerConfiguration;
import org.codinjutsu.tools.nosql.commons.model.SearchResult;
import org.codinjutsu.tools.nosql.commons.view.DatabaseContext;
import org.codinjutsu.tools.nosql.commons.view.panel.query.QueryOptions;

public interface LoadableDatabaseClient<CONTEXT extends DatabaseContext, RESULT extends SearchResult, DOCUMENT, SERVERCONFIGURATION extends ServerConfiguration> extends
        DatabaseClient<CONTEXT, DOCUMENT, SERVERCONFIGURATION> {
    RESULT loadRecords(CONTEXT context, QueryOptions query);
}
