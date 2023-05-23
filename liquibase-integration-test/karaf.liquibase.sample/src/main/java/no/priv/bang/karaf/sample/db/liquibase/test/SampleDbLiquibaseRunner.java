/*
 * Copyright 2022-2023 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.karaf.sample.db.liquibase.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;

import liquibase.Scope;
import liquibase.Scope.ScopedRunner;
import liquibase.ThreadLocalScopeManager;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.OSGiResourceAccessor;

@Component(immediate=true, property = "name=sampledb")
public class SampleDbLiquibaseRunner implements PreHook {

    private Logger logger;
    private Bundle bundle;

    @Reference
    public void setLogService(LogService logservice) {
        this.logger = logservice.getLogger(SampleDbLiquibaseRunner.class);
    }

    @Activate
    public void activate(BundleContext bundlecontext) {
        Scope.setScopeManager(new ThreadLocalScopeManager());
        this.bundle = bundlecontext.getBundle();
    }

    @Override
    public void prepare(DataSource datasource) throws SQLException {
        try (Connection connection = datasource.getConnection()) {
            applyLiquibaseChangelist(connection, "sample-db-changelog/db-changelog-1.0.0.xml");
        } catch (Exception e) {
            logger.error("Error creating sampleapp test database schema", e);
        }

        try (Connection connection = datasource.getConnection()) {
            insertMockData(connection);
        } catch (Exception e) {
            logger.error("Error creating sampleapp test database mock data", e);
        }
    }

    public void insertMockData(Connection connect) throws Exception {
        var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connect));
        Map<String, Object> scopeObjects = Map.of(
            Scope.Attr.database.name(), database,
            Scope.Attr.resourceAccessor.name(), new OSGiResourceAccessor(bundle));

        Scope.child(scopeObjects, (ScopedRunner<?>) () -> new CommandScope("update")
                    .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                    .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "sql/data/db-changelog.xml")
                    .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, new ChangeLogParameters(database))
                    .execute());
    }

    private void applyLiquibaseChangelist(Connection connection, String changelistClasspathResource) throws Exception {
        var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Map<String, Object> scopeObjects = Map.of(
            Scope.Attr.database.name(), database,
            Scope.Attr.resourceAccessor.name(), new OSGiResourceAccessor(bundle));

        Scope.child(scopeObjects, (ScopedRunner<?>) () -> new CommandScope("update")
                    .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                    .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelistClasspathResource)
                    .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, new ChangeLogParameters(database))
                    .execute());
    }

}
