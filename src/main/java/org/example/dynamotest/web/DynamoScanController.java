package org.example.dynamotest.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dynamo")
public class DynamoScanController {

    private static final Logger log = LoggerFactory.getLogger(DynamoScanController.class);

    private final DynamoDbClient dynamoDbClient;

    @Value("${app.dynamodb.tableName}")
    private String tableName;

    public DynamoScanController(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @GetMapping("/scan")
    public ResponseEntity<Map<String, Object>> scan() {
        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .limit(100) // avoid huge payloads; adjust if needed
                .build();

        ScanResponse response = dynamoDbClient.scan(request);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            items.add(convertItem(item));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("tableName", tableName);
        body.put("count", response.count());
        body.put("scannedCount", response.scannedCount());
        body.put("lastEvaluatedKey", response.hasLastEvaluatedKey() && !response.lastEvaluatedKey().isEmpty() ? convertItem(response.lastEvaluatedKey()) : null);
        body.put("items", items);
        body.put("message", "Scan executed against DynamoDB table and returning live data.");

        // Log the contents so you can see it in the application logs
        try {
            log.info("[DynamoDB] Scan on table='{}' count={} scannedCount={} items={}", tableName, response.count(), response.scannedCount(), items);
        } catch (Exception e) {
            log.warn("[DynamoDB] Failed to log scan items due to: {}", e.toString());
        }

        return ResponseEntity.ok(body);
    }

    private Map<String, Object> convertItem(Map<String, AttributeValue> item) {
        Map<String, Object> map = new HashMap<>();
        item.forEach((k, v) -> map.put(k, attributeToSimple(v)));
        return map;
    }

    private Object attributeToSimple(AttributeValue v) {
        if (v.s() != null) return v.s();
        if (v.n() != null) return v.n();
        if (v.bool() != null) return v.bool();
        if (v.ss() != null && !v.ss().isEmpty()) return v.ss();
        if (v.ns() != null && !v.ns().isEmpty()) return v.ns();
        if (v.bs() != null && !v.bs().isEmpty()) return v.bs();
        if (v.m() != null && !v.m().isEmpty()) {
            Map<String, Object> m = new HashMap<>();
            v.m().forEach((k, av) -> m.put(k, attributeToSimple(av)));
            return m;
        }
        if (v.l() != null && !v.l().isEmpty()) {
            List<Object> l = new ArrayList<>();
            v.l().forEach(av -> l.add(attributeToSimple(av)));
            return l;
        }
        if (v.nul() != null && v.nul()) return null;
        // Fallback to string representation if nothing matched
        return v.toString();
    }
}
