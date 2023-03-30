package com.berrie.gamerental.config;

import com.berrie.gamerental.model.Game;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    private static final String MONGODB_URI = "mongodb://user:pass@localhost:27018/game-rental?authSource=admin";
    private static final String DATABASE_NAME = "game-rental";


    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(MONGODB_URI);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, DATABASE_NAME);
    }

    @Bean
    public MongoCollection<Game> gameCollection(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Game> mongoCollection = database.getCollection("games", Game.class);
        mongoCollection.createIndex(Indexes.text("title"));
        return mongoCollection;
    }
}
