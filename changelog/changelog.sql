--liquibase formatted sql

--changeset jeff.crichlake:1 labels:create-type context:example-context
--comment: create status type
CREATE TYPE message_status AS ENUM ('PENDING', 'DELIVERED', 'FAILED');
--rollback DROP TYPE message_status;

--changeset jeff.crichlake:2 labels:create-metadata-table context:metadata
--comment: create partner metadata table
CREATE TABLE IF NOT EXISTS metadata (received_message_id varchar(40) PRIMARY KEY, sent_message_id varchar(40), sender varchar(30), receiver varchar(30), hash_of_order varchar(1000), time_received timestamptz, time_delivered timestamptz, delivery_status message_status, failure_reason varchar(1000));
--rollback DROP TABLE metadata


-- --changeset jeff.crichlake:3 labels:permissions context:permissions
-- --comment: update permissions as part of deploy
-- GRANT ALL ON metadata TO azure_pg_admin;
--
-- --changeset jeff.crichlake:4 labels:permissions context:permissions
-- --comment change table owner
-- ALTER TABLE metadata OWNER TO azure_pg_admin;
--
-- --changeset jeff.crichlake:5 labels:permissions context:permissions
-- ALTER TYPE message_status OWNER TO azure_pg_admin
-- --rollback DROP TYPE message_status
