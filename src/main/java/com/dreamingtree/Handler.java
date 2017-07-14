package com.dreamingtree;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Handler is a {@link SpeechletRequestStreamHandler} that serves as the entry point to this Alexa skill.
 */
public final class Handler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    /*
     * Prevent other unauthorized skills from accessing this handler.
     */
    static {
        supportedApplicationIds = new HashSet<>();
        supportedApplicationIds.add("amzn1.ask.skill.7c4278c5-3848-4bd1-a047-774d6591de0d");
    }

    public Handler() {
        super(new ThisDayInDmbSpeechlet(), supportedApplicationIds);
    }
}
