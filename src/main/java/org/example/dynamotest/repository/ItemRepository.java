package org.example.dynamotest.repository;

import org.example.dynamotest.model.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public ItemRepository(DynamoDbEnhancedClient enhancedClient,
                          @Value("${app.dynamodb.tableName:Items}") String tableName) {
        this.enhancedClient = enhancedClient;
        this.tableName = tableName;
    }

    private DynamoDbTable<Item> table() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Item.class));
    }

    public Item save(Item item) {
        table().putItem(item);
        return item;
    }

    public Optional<Item> findById(String id) {
        Item key = new Item();
        key.setId(id);
        Item found = table().getItem(key);
        return Optional.ofNullable(found);
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    public void deleteById(String id) {
        Item key = new Item();
        key.setId(id);
        table().deleteItem(key);
    }

    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        PageIterable<Item> pages = table().scan();
        pages.items().forEach(items::add);
        return items;
    }

    public void ensureTableExists() {
        try {
            table().describeTable();
        } catch (ResourceNotFoundException ex) {
            // Create table with provisioned throughput to satisfy AWS requirements when billing mode is not set
            table().createTable(r -> r.provisionedThroughput(b -> b.readCapacityUnits(5L).writeCapacityUnits(5L)));
        }
    }
}
