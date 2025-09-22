package org.example.dynamotest.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dynamo")
public class DynamoInfoController {

    private static final Logger log = LoggerFactory.getLogger(DynamoInfoController.class);

    private final DynamoDbClient dynamoDbClient;

    @Value("${app.dynamodb.tableName}")
    private String tableName;

    @Value("${app.dynamodb.region}")
    private String region;

    public DynamoInfoController(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        DescribeTableRequest req = DescribeTableRequest.builder().tableName(tableName).build();
        TableDescription td = dynamoDbClient.describeTable(req).table();

        Map<String, Object> body = new HashMap<>();
        body.put("tableName", td.tableName());
        body.put("tableStatus", td.tableStatusAsString());
        body.put("itemCount", td.itemCount());
        body.put("tableSizeBytes", td.tableSizeBytes());
        body.put("region", region);
        body.put("message", "Connected to DynamoDB and reading live table description.");

        // Log the info so you can confirm in the logs
        log.info("[DynamoDB] Table info: name='{}' status={} itemCount={} sizeBytes={} region={}",
                td.tableName(), td.tableStatusAsString(), td.itemCount(), td.tableSizeBytes(), region);

        return ResponseEntity.ok(body);
    }
}
