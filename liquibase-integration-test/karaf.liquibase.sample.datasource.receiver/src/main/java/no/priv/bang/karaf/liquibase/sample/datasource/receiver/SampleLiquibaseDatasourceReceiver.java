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
package no.priv.bang.karaf.liquibase.sample.datasource.receiver;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;

@Component(immediate=true)
public class SampleLiquibaseDatasourceReceiver {

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
        try (Connection connection = datasource.getConnection()) {
        	logger.info("Liquibase sample data source receiver activated");
        } catch (SQLException e) {
        	logger.info("Datasource errored when getting connection", e);
		}
    }

}
