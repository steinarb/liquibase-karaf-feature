package no.priv.bang.karaf.liquibase.runner;
/*
 * Copyright 2024 Steinar Bang
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

import static java.util.Optional.ofNullable;
import static liquibase.Scope.Attr.resourceAccessor;
import static liquibase.command.core.UpdateCommandStep.CHANGELOG_FILE_ARG;
import static liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep.DATABASE_ARG;

import java.sql.Connection;
import java.util.Map;

import liquibase.Scope;
import liquibase.changelog.visitor.StandardValidatingVisitorGenerator;
import liquibase.changelog.visitor.ValidatingVisitorGeneratorFactory;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Base class for classes that load liquibase change lists from class path resources and
 * apply the files to a JDBC database.
 */
public class LiquibaseClassPathChangeLogRunner {

    /**
     * Apply a liquibase change list fetched from the classpath to a database connected to via
     * a JDBC {@link Connection}
     *
     * @param connect the database to apply the changelist in, closing this connection is the responsibility of the caller
     * @param changelistClasspathResource the class path of the file containing the liquibase change list
     * @param classLoader a {@link ClassLoader} from the OSGi bundle containing the liquibase file to be loaded
     * @return a {@code CommandResults} object describing the result from applying the change list
     * @throws LiquibaseException on failure to apply the change list
     */
    public CommandResults applyLiquibaseChangelist(Connection connect, String changelistClasspathResource, ClassLoader classLoader) throws LiquibaseException {
        try (var database = findCorrectDatabaseImplementation(connect)) {
            createAndRegisterValidatingVisitorGeneratorIfItIsMissing();
            return Scope.child(scopeObjectsWithClassPathResourceAccessor(classLoader), () -> new CommandScope("update")
                .addArgumentValue(DATABASE_ARG, database)
                .addArgumentValue(CHANGELOG_FILE_ARG, changelistClasspathResource)
                .execute());
        } catch (LiquibaseException e) {
            throw e;
        } catch (Exception e) {
            // AutoClosable.close() may throw Exception
            throw new LiquibaseException(e);
        }
    }

    private void createAndRegisterValidatingVisitorGeneratorIfItIsMissing() {
        var factory = Scope.getCurrentScope().getSingleton(ValidatingVisitorGeneratorFactory.class);
        ofNullable(factory.getValidatingVisitorGenerator()).ifPresentOrElse(v -> {}, () -> factory.register(new StandardValidatingVisitorGenerator()));
    }

    private Map<String, Object> scopeObjectsWithClassPathResourceAccessor(ClassLoader classLoader) {
        return Map.of(resourceAccessor.name(), new ClassLoaderResourceAccessor(classLoader));
    }

    private Database findCorrectDatabaseImplementation(Connection connection) throws DatabaseException {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

}
