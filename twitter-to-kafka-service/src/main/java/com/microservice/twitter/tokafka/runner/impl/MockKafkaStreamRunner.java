package com.microservice.twitter.tokafka.runner.impl;

import com.microservice.demo.config.TwitterToKafkaConfig;
import com.microservice.twitter.tokafka.exception.TwitterToKafkaServiceException;
import com.microservice.twitter.tokafka.listener.TwitterKafkaStatusListener;
import com.microservice.twitter.tokafka.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets", havingValue = "true")
public class MockKafkaStreamRunner implements StreamRunner {

    private static final Logger log = LoggerFactory.getLogger(MockKafkaStreamRunner.class);
    private TwitterToKafkaConfig twitterToKafkaConfig;
    private TwitterKafkaStatusListener twitterKafkaStatusListener;
    private static final Random random = new Random();
    private static final String[] words = {
            "A",
            "B",
            "C",
            "D"
    };
   

    public MockKafkaStreamRunner(TwitterToKafkaConfig toKafkaConfig, TwitterKafkaStatusListener kafkaStatusListener) {
        this.twitterToKafkaConfig = toKafkaConfig;
        this.twitterKafkaStatusListener = kafkaStatusListener;
    }
    private static final String tweetAsRawJson = "{" +
            "\"created_at\":\"{0}\"," +
            "\"id\":\"{1}\"," +
            "\"text\":\"{2}\"," +
            "\"user\":{\"id\":\"{3}\"}" +
            "}";
    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    @Override
    public void start() throws TwitterException {
        final String[] keywords = twitterToKafkaConfig.getTwitterKeywords().toArray(new String[0]);
        final int minTweetLength = twitterToKafkaConfig.getMockMinLength();
        final int maxTweetLength = twitterToKafkaConfig.getMockMaxLength();
        long sleepTimeMs = twitterToKafkaConfig.getMockSleepMs();
        log.info("Starting mock filtering twitter streams for keywords {}", Arrays.toString(keywords));
        simulateTwitterStream(keywords, minTweetLength, maxTweetLength, sleepTimeMs);
    }

    private void simulateTwitterStream(String[] keywords, int minTweetLength, int maxTweetLength, long sleepTimeMs) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    String formattedTweetAsRawJson = getFormattedTweet(keywords, minTweetLength, maxTweetLength);
                    Status status = TwitterObjectFactory.createStatus(formattedTweetAsRawJson);
                    twitterKafkaStatusListener.onStatus(status);
                    sleep(sleepTimeMs);
                }
            } catch (TwitterException e) {
                log.error("Error creating twitter status!", e);
            }
        });
    }
    private String formatTweetAsJsonWithParams(String[] params) {
        String tweet = tweetAsRawJson;

        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }
        return tweet;
    }

    private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {
        StringBuilder tweet = new StringBuilder();
        int tweetLength = random.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength;
        return constructRandomTweet(keywords, tweet, tweetLength);
    }

    private String getFormattedTweet(String[] keywords, int minTweetLength, int maxTweetLength) {
        String[] params = new String[]{
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };
        return formatTweetAsJsonWithParams(params);
    }
    private void sleep(long sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {
            throw new TwitterToKafkaServiceException("Error while sleeping for waiting new status to create!!");
        }
    }

    private String constructRandomTweet(String[] keywords, StringBuilder tweet, int tweetLength) {
        for (int i = 0; i < tweetLength; i++) {
            tweet.append(words[random.nextInt(words.length)]).append(" ");
            if (i == tweetLength / 2) {
                tweet.append(keywords[random.nextInt(keywords.length)]).append(" ");
            }
        }
        return tweet.toString().trim();
    }
}
