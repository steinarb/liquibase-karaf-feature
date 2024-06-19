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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.ops4j.pax.jdbc.derby.impl.DerbyDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

import liquibase.exception.LiquibaseException;
import liquibase.report.UpdateReportParameters;

class LiquibaseClassPathChangeLogRunnerTest {
    DataSourceFactory derbyDataSourceFactory = new DerbyDataSourceFactory();

    @Test
    void testCreateSchema() throws Exception {
        var datasource = createDataSource("runner1");
        var liquibase = new LiquibaseClassPathChangeLogRunner();
        var connect = datasource.getConnection();
        var changelistClasspathResource = "sample-db-changelog/db-changelog.xml";
        var classLoader = getClass().getClassLoader();
        var commandResult = liquibase.applyLiquibaseChangelist(connect, changelistClasspathResource, classLoader);
        var updateReport = (UpdateReportParameters)commandResult.getResults().get("updateReport");
        assertTrue(updateReport.getSuccess());
    }

    @Test
    void testFailToInsertDataInDatabaseWithNoSchema() throws Exception {
        var datasource = createDataSource("runner2");
        var liquibase = new LiquibaseClassPathChangeLogRunner();
        var connect = datasource.getConnection();
        var changelistClasspathResource = "sample-db-changelog/accounts.sql";
        var classLoader = getClass().getClassLoader();
        var e = assertThrows(LiquibaseException.class, () -> liquibase.applyLiquibaseChangelist(connect, changelistClasspathResource, classLoader));
        assertThat(e.getCause()).isInstanceOf(LiquibaseException.class);
    }

    @Test
    void testCreateSchemaWithNonDatabaseException() throws Exception {
        var liquibase = new LiquibaseClassPathChangeLogRunner();
        var connect = mock(Connection.class);
        when(connect.getMetaData()).thenThrow(RuntimeException.class);
        var changelistClasspathResource = "sample-db-changelog/db-changelog.xml";
        var classLoader = getClass().getClassLoader();
        var e = assertThrows(LiquibaseException.class, () -> liquibase.applyLiquibaseChangelist(connect, changelistClasspathResource, classLoader));
        assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
    }

    private DataSource createDataSource(String dbname) throws Exception {
        var properties = new Properties();
        properties.setProperty(DataSourceFactory.JDBC_URL, "jdbc:derby:memory:" + dbname + ";create=true");
        return derbyDataSourceFactory.createDataSource(properties);
    }

}
