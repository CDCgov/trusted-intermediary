# 19. Link message IDs

Date: 2024-04-22

## Decision

We will add a `message_link` database table to store order and result IDs in order to link them.

## Status

Accepted

## Context

As part of the requirement to link an order with its corresponding result(s), we need to match fields in the `metadata` table to find the messages to link. Considering that the matching fields may change in the future, we want to store the linked IDs to preserve the relationship even if the matching fields change.

We decided to create a new table for this purpose, but we also discussed the option to instead add a new field to the `metadata` table with an array of link IDs. Even though this option would reduce the complexity in our database and code, we decided to create the new table because we'll soon need to pull data for each ID in order to populate the response from the `metadata` endpoint, which is a use-case that the separate table will fit better. If we find that the tradeoff is worth it, we may decide in the future to refactor the code and use the array field instead of the table.

## Impact

### Positive

- **Data Integrity:** Decouples the link storage from the `metadata` table, ensuring links remain intact even if metadata fields change. 


- **Flexibility:** The `message_link` table can adapt to future requirements without altering the `metadata` schema. 


- **Efficient Querying:** Simplifies the process of retrieving linked data by isolating relationships in a dedicated table. 


- **Scalability:** A separate table is better suited for handling large-scale relationships, particularly as the volume of linked data grows.



### Negative

- **Increased Complexity:** Adds another table to the database, which introduces additional queries and joins in the codebase. 


- **Data Synchronization Overhead:** Requires careful management to ensure consistency between the `metadata` table and the `message_link` table. 


- **Initial Implementation Effort:** More time and resources needed for implementation compared to the simpler array field approach.

### Risks

- **Data Consistency:** Potential risks of mismatches between `metadata` and `message_link` entries due to bugs or incomplete synchronization. 

- **Rollback Complexity:** Adding a new table increases the complexity of rollbacks during migrations, especially if records are linked inconsistently or if dependent data is removed from the `message_link` table prematurely.

- **Performance Concerns:** Queries involving multiple joins with the `message_link` table might impact database performance if not optimized. 


- **Schema Evolution:** Future changes to linking logic or requirements may necessitate updates to the `message_link` table structure.


### Related Issues

#621
