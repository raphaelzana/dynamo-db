package org.example.dynamotest.service;

import org.example.dynamotest.model.Item;
import org.example.dynamotest.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public Item create(Item item) {
        if (item.getId() == null || item.getId().isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        return repository.save(item);
    }

    public Item get(String id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Item not found: " + id));
    }

    public Item update(String id, Item item) {
        item.setId(id);
        return repository.save(item);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<Item> list() {
        return repository.findAll();
    }
}
