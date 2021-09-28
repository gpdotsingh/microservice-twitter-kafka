package com.microservice.twitter.tokafka;

import com.microservice.demo.config.TwitterToKafkaConfig;
import com.microservice.twitter.tokafka.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan("com.microservice")
public class TwitterToKafka implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TwitterToKafka.class);
    private TwitterToKafkaConfig twitterToKafkaConfig;
    private StreamRunner streamRunner;
    // Spring is setting config data after initialising  TwitterToKafkaConfig
    public TwitterToKafka(TwitterToKafkaConfig configData, StreamRunner streamRunner) {
        this.twitterToKafkaConfig = configData;
        this.streamRunner = streamRunner;
    }

    public static void main(String a[]) {
        SpringApplication.run(TwitterToKafka.class, a);
    }

    @Override
    public void run(String... args) throws Exception {
        log.error(Arrays.toString(twitterToKafkaConfig.getTwitterKeywords().toArray()));
        log.info(twitterToKafkaConfig.getWelcomeMessage());
        streamRunner.start();
    }
}
