package org.example.dynamotest.config;

import jakarta.annotation.PostConstruct;
import org.example.dynamotest.repository.ItemRepository;
import org.springframework.stereotype.Component;

@Component
public class DynamoTableInitializer {

    private final ItemRepository repository;

    public DynamoTableInitializer(ItemRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        repository.ensureTableExists();
    }
}
