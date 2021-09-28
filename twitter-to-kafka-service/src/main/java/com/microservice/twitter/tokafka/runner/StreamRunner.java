package com.microservice.twitter.tokafka.runner;

import twitter4j.TwitterException;

public interface StreamRunner {

    public void start() throws TwitterException;
}
