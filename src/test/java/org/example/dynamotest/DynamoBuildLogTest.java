package org.example.dynamotest;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("test")
class DynamoBuildLogTest {

    private static final Logger log = LoggerFactory.getLogger(DynamoBuildLogTest.class);

    private final DynamoDbClient dynamoDbClient;

    @Value("${app.dynamodb.tableName}")
    private String tableName;

    @Value("${app.dynamodb.region}")
    private String region;

    DynamoBuildLogTest(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Test
    void logDynamoStateDuringBuild() {
        // This test is intended to emit clear logs during `mvn test` / build.
        // It should never fail the pipeline even if Dynamo is unreachable.
        try {
            var desc = dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build()).table();
            String msg = String.format("[BUILD_LOG][DynamoDB] Table info -> name='%s' status=%s itemCount=%d sizeBytes=%d region=%s",
                    desc.tableName(), desc.tableStatusAsString(), desc.itemCount(), desc.tableSizeBytes(), region);
            System.out.println(msg);
            log.info(msg);

            var scan = dynamoDbClient.scan(ScanRequest.builder().tableName(tableName).limit(10).build());
            List<Map<String, Object>> items = scan.items().stream()
                    .map(this::toSimpleMap)
                    .collect(Collectors.toList());
            String scanMsg = String.format("[BUILD_LOG][DynamoDB] Scan -> count=%d scannedCount=%d sampleItems=%s",
                    scan.count(), scan.scannedCount(), items);
            System.out.println(scanMsg);
            log.info(scanMsg);
        } catch (Exception e) {
            String warn = "[BUILD_LOG][DynamoDB] Could not log DynamoDB state during build: " + e.getMessage();
            System.out.println(warn);
            log.warn(warn);
            // Do not rethrow; keep build green.
        }
    }

    private Map<String, Object> toSimpleMap(Map<String, AttributeValue> item) {
        return item.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> simplify(e.getValue())));
    }

    private Object simplify(AttributeValue v) {
        if (v == null) return null;
        if (v.s() != null) return v.s();
        if (v.n() != null) return v.n();
        if (v.bool() != null) return v.bool();
        if (v.ss() != null && !v.ss().isEmpty()) return v.ss();
        if (v.ns() != null && !v.ns().isEmpty()) return v.ns();
        if (v.bs() != null && !v.bs().isEmpty()) return v.bs();
        if (v.m() != null && !v.m().isEmpty()) {
            return v.m().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> simplify(e.getValue())));
        }
        if (v.l() != null && !v.l().isEmpty()) {
            return v.l().stream().map(this::simplify).collect(Collectors.toList());
        }
        if (v.nul() != null && v.nul()) return null;
        return v.toString();
    }
}
