package com.microservice.twitter.tokafka.runner.impl;

 import com.microservice.demo.config.TwitterToKafkaConfig;
 import com.microservice.twitter.tokafka.listener.TwitterKafkaStatusListener;
import com.microservice.twitter.tokafka.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets", havingValue = "false",matchIfMissing = true)
public class TwitterkafkaStreamRunner implements StreamRunner {

    private static final Logger log = LoggerFactory.getLogger(TwitterkafkaStreamRunner.class);
    private TwitterToKafkaConfig twitterToKafkaConfig;
    private TwitterKafkaStatusListener twitterKafkaStatusListener;
    private TwitterStream twitterStream;

    public TwitterkafkaStreamRunner(TwitterToKafkaConfig kafkaConfig, TwitterKafkaStatusListener listener) {
        this.twitterToKafkaConfig = kafkaConfig;
        this.twitterKafkaStatusListener = listener;
    }

    @Override
    public void start() throws TwitterException {
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(twitterKafkaStatusListener);
        addFilter();
    }

    private void addFilter() {
        String[] keywords = twitterToKafkaConfig.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);
        log.info("Keywords {}", Arrays.toString(keywords));
    }

    @PreDestroy
    public void shutdownStream(){

        if(twitterStream!= null)
            twitterStream.shutdown();
    }
}
