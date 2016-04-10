package mx.com.gunix.framework.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;


@Configuration
@EnableMongoRepositories(basePackages = "mx.com.gunix.domain.persistence.mongo")
@ComponentScan("mx.com.gunix.domain.persistence.mongo")
public class MongoDBConfig {

    public @Bean
    MongoDbFactory mongoDbFactory() throws Exception {

        MongoClient mongoClient = null;

        String mongoServer = (System.getenv("MONGO_SERVER")!=null && !System.getenv("MONGO_SERVER").isEmpty()? System.getenv("MONGO_SERVER") : "localhost");
        int mongoPort = (System.getenv("MONGO_PORT")!=null && !System.getenv("MONGO_PORT").isEmpty() ? Integer.parseInt(System.getenv("MONGO_PORT")) : 27017);
        String mongoDB = System.getenv("MONGO_DB_NAME");
        String mongoUser = System.getenv("MONGO_USER");
        String mongoPassword = System.getenv("MONGO_PASSWORD");

        if(mongoUser!=null && !mongoUser.isEmpty() && mongoPassword!=null && !mongoPassword.isEmpty()){

            MongoCredential credential = MongoCredential.createCredential(mongoUser, mongoDB, mongoPassword.toCharArray());
            mongoClient = new MongoClient(new ServerAddress(mongoServer,mongoPort), Arrays.asList(credential));

        }else {
            mongoClient = new MongoClient(mongoServer,mongoPort);
        }


        return new SimpleMongoDbFactory(mongoClient, mongoDB);
    }

    public @Bean
    MongoTemplate mongoTemplate() throws Exception {

        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());

        return mongoTemplate;

    }

}
