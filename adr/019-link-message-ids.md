# 19. Link message IDs

Date: 2024-04-22

## Decision

We will add a `message_link` database table to store order and result IDs in order to link them.

## Status

Accepted

## Context

As part of the requirement to link an order with its corresponding result(s), we need to match fields in the `metadata` table to find the messages to link. Considering that the matching fields may change in the future, we want to store the linked IDs to preserve the relationship even if the matching fields change.

We decided to create a new table for this purpose, but we also discussed the option to instead add a new field to the `metadata` table with an array of link IDs. Even though this option would reduce the complexity in our database and code, we decided to create the new table because we'll soon need to pull data for each ID in order to populate the response from the `metadata` endpoint, which is a usecase that the separate table will fit better. If we find that the tradeoff is worth it, we may decide in the future to refactor the code and use the array field instead of the table.

### Related Issues

#621
