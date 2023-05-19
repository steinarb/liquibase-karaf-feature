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
package no.priv.bang.karaf.liquibase.sample.datasource.receiver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;

import no.priv.bang.karaf.liquibase.sample.services.Account;
import no.priv.bang.karaf.liquibase.sample.services.SampleLiquibaseDatasourceReceiverService;

@Component(immediate=true)
public class SampleLiquibaseDatasourceReceiver implements SampleLiquibaseDatasourceReceiverService {

    private Logger logger;
    private DataSource datasource;

    @Reference
    public void setLogService(LogService logservice) {
        this.logger = logservice.getLogger(SampleLiquibaseDatasourceReceiver.class);
    }

    @Reference
    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    @Activate
    public void activate() {
        try (var connection = datasource.getConnection()) {
            logger.info("Liquibase sample data source receiver activated");
        } catch (SQLException e) {
            logger.info("Datasource errored when getting connection", e);
        }
    }

    @Override
    public List<Account> accounts() throws SQLException {
        try (var connection = datasource.getConnection()) {
            var accounts = new ArrayList<Account>();
            var sql = "select * from sampleapp_accounts";
            try(var statement = connection.createStatement()) {
                try(var results = statement.executeQuery(sql)) {
                    while(results.next()) {
                        var account = Account.with()
                            .id(results.getInt("account_id"))
                            .username(results.getString("username"))
                            .build();
                        accounts.add(account);
                    }
                }
            }
            return accounts;
        }
    }

    @Override
    public List<Account> addAccount(Account account) throws SQLException {
        try (var connection = datasource.getConnection()) {
            var sql = "insert into sampleapp_accounts (username) values (?)";
            try(var statement = connection.prepareStatement(sql)) {
                statement.setString(1, account.getUsername());
                statement.execute();
            }
        }

        return accounts();
    }

}
