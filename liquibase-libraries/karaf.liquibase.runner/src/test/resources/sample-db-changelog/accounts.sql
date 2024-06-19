--liquibase formatted sql
--changeset sb:example_accounts
insert into accounts (username) values ('jod');
