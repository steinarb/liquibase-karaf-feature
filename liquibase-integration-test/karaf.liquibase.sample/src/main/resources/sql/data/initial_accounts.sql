--liquibase formatted sql
--changeset sb:initial_accounts
insert into sampleapp_accounts (username) values ('jod');
--rollback delete from sampleapp_accounts; alter table sampleapp_accounts alter account_id restart with 1;
