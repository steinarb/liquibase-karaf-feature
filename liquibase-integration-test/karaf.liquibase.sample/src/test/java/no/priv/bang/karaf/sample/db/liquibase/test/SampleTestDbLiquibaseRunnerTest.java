/*
 * Copyright 2022 Steinar Bang
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

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.ops4j.pax.jdbc.derby.impl.DerbyDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

import no.priv.bang.osgi.service.mocks.logservice.MockLogService;

class SampleDbLiquibaseRunnerTest {

    @Test
    void testCreateAndVerifySomeDataInSomeTables() throws Exception {
        DataSourceFactory dataSourceFactory = new DerbyDataSourceFactory();
        Properties properties = new Properties();
        properties.setProperty(DataSourceFactory.JDBC_URL, "jdbc:derby:memory:sample;create=true");
        DataSource datasource = dataSourceFactory.createDataSource(properties);

        MockLogService logservice = new MockLogService();
        SampleDbLiquibaseRunner runner = new SampleDbLiquibaseRunner();
        runner.setLogService(logservice);
        runner.activate();
        runner.prepare(datasource);
        assertAccounts(datasource, 0);
        addAccounts(datasource);
        assertAccounts(datasource, 1);
        addCounterIncrementSteps(datasource);
        assertCounterIncrementSteps(datasource);
        addCounters(datasource);
        assertCounters(datasource);
    }

    private void addAccounts(DataSource datasource) throws Exception {
        try(Connection connection = datasource.getConnection()) {
            addAccount(connection, "admin");
        }
    }

    private void assertAccounts(DataSource datasource, int expectedNumberOfAccounts) throws Exception {
        try(Connection connection = datasource.getConnection()) {
            String sql = "select count(*) from sampleapp_accounts";
            try(PreparedStatement statement = connection.prepareStatement(sql)) {
                try(ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        int count = results.getInt(1);
                        assertEquals(expectedNumberOfAccounts, count);
                    }
                }
            }
        }
    }

    private void addCounterIncrementSteps(DataSource datasource) throws Exception {
        try(Connection connection = datasource.getConnection()) {
            addCounterIncrementStep(connection, findAccountId(connection, "admin"), 10);
        }
    }

    private void assertCounterIncrementSteps(DataSource datasource) throws Exception {
        try(Connection connection = datasource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                try(ResultSet results = statement.executeQuery("select * from counter_increment_steps")) {
                    assertTrue(results.next());
                    assertEquals(findAccountId(connection, "admin"), results.getInt(2));
                    assertEquals(10, results.getInt(3));
                }
            }
        }
    }

    private void addCounters(DataSource datasource) throws Exception {
        try(Connection connection = datasource.getConnection()) {
            addCounter(connection, findAccountId(connection, "admin"), 3);
        }
    }

    private void assertCounters(DataSource datasource) throws Exception {
        try(Connection connection = datasource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                try(ResultSet results = statement.executeQuery("select * from counters")) {
                    assertTrue(results.next());
                    assertEquals(findAccountId(connection, "admin"), results.getInt(2));
                    assertEquals(3, results.getInt(3));
                }
            }
        }
    }

    private int addAccount(Connection connection, String username) throws Exception {
        String sql = "insert into sampleapp_accounts (username) values (?)";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }

        return findAccountId(connection, username);
    }

    private void addCounterIncrementStep(Connection connection, int accountid, int counterIncrementStep) throws Exception {
        String sql = "insert into counter_increment_steps (account_id, counter_increment_step) values (?, ?)";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountid);
            statement.setInt(2, counterIncrementStep);
            statement.executeUpdate();
        }
    }

    private void addCounter(Connection connection, int accountid, int count) throws Exception {
        String sql = "insert into counters (account_id, counter) values (?, ?)";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountid);
            statement.setInt(2, count);
            statement.executeUpdate();
        }
    }

    private int findAccountId(Connection connection, String username) throws Exception {
        String sql = "select account_id from sampleapp_accounts where username=?";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try(ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        }

        return -1;
    }

}
