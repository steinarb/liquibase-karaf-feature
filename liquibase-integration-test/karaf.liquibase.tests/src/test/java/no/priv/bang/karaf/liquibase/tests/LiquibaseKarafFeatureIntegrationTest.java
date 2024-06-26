/*
 * Copyright 2022-2024 Steinar Bang
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
package no.priv.bang.karaf.liquibase.tests;

import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import no.priv.bang.karaf.liquibase.sample.services.Account;
import no.priv.bang.karaf.liquibase.sample.services.SampleLiquibaseDatasourceReceiverService;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

import java.util.stream.Stream;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LiquibaseKarafFeatureIntegrationTest extends KarafTestSupport {

    @Configuration
    public Option[] config() {
        final var sampleappFeatureRepo = maven()
            .groupId("no.priv.bang.karaf")
            .artifactId("karaf.liquibase.sample.datasource.receiver")
            .version("LATEST")
            .type("xml")
            .classifier("features");
        var options = new Option[] {
            features(sampleappFeatureRepo)
        };
        return Stream.of(super.config(), options).flatMap(Stream::of).toArray(Option[]::new);
    }

    @Test
    public void testLoadFeature() throws Exception {
        installAndAssertFeature("karaf-liquibase-sample-datasource-receiver");
        var service = getOsgiService(SampleLiquibaseDatasourceReceiverService.class);
        var initialAccounts = service.accounts();
        assertEquals(1, initialAccounts.size());
        var initialAccount = initialAccounts.get(0);
        assertEquals("jod", initialAccount.username());
        var newAccount = Account.with().username("jad").build();
        var accountsAfterAdd = service.addAccount(newAccount);
        assertEquals(2, accountsAfterAdd.size());
        var addedAccount = accountsAfterAdd.get(0);
        assertEquals(initialAccount.id() + 1, addedAccount.id());
        assertEquals("jad", addedAccount.username());
    }

}
