/*
 * Copyright 2023 Steinar Bang
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
package no.priv.bang.karaf.liquibase.sample.services;

public class Account {
    private int id;
    private String username;

    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }

    public static Builder with() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private String username;

        public Account build() {
            var account = new Account();
            account.id = this.id;
            account.username = this.username;
            return account;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

    }
}
