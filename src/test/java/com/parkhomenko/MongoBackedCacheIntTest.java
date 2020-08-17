package com.parkhomenko;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class MongoBackedCacheIntTest {

    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    @BeforeAll
    static void setUp() {
        mongoDBContainer.start();
        final String host = mongoDBContainer.getHost();
        final List<Integer> exposedPorts = mongoDBContainer.getExposedPorts();
        System.out.println(host);
        System.out.println(exposedPorts);

        final String replicaSetUrl = mongoDBContainer.getReplicaSetUrl();
        mongoClient = com.mongodb.client.MongoClients.create(replicaSetUrl);

        database = mongoClient.getDatabase("mydb");
    }

    @AfterAll
    static void close() {
        mongoClient.close();
        mongoDBContainer.close();
    }

    @Test
    void testSimpleInsertOperation() {
        MongoCollection<Document> collection = database.getCollection("test");
        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("info", new Document("x", 203).append("y", 102));

        collection.insertOne(doc);
        assertEquals(1, collection.countDocuments());

        // check if id was generated
        final Document first = collection.find()
                .first();
        assert first != null;
        assertTrue(first.toJson().contains("\"_id\": {\"$oid\":"));
    }
}