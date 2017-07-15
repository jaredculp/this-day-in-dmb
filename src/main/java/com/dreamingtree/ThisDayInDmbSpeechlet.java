package com.dreamingtree;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * {@link Speechlet} which handles the lifecycle of the Alexa application.
 */
final class ThisDayInDmbSpeechlet implements Speechlet {

    private static final Logger LOG = LoggerFactory.getLogger(ThisDayInDmbSpeechlet.class);
    private static final String BASE_URL = "http://dmbalmanac.com";

    private static final Almanac ALMANAC = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(Almanac.class);

    private static final ShowSupplier SHOW_SUPPLIER = new ShowSupplier(ALMANAC);

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        LOG.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        LOG.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        final PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Welcome to this day in DMB history! Say setlist to hear a historical setlist");

        final Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        final Intent intent = request.getIntent();
        final String intentName = (intent != null) ? intent.getName() : "AMAZON.StopIntent";

        final PlainTextOutputSpeech speech;
        switch (intentName) {
            case "SetlistIntent":
                final Show show;
                try {
                    show = SHOW_SUPPLIER.get();
                } catch (IOException e) {
                    throw new SpeechletException("Could not retrieve setlist");
                }

                final List<String> setlist = show.getSetlist();

                speech = new PlainTextOutputSpeech();
                speech.setText("On this day Dave Matthews Band played in... " + show.getVenue() + " " +
                        "The set list was... " +
                        setlist.stream().collect(joining(", ")));

                final SimpleCard simpleCard = new SimpleCard();
                simpleCard.setTitle(LocalDate.now().toString() + show.getVenue());
                final String content = BASE_URL + "/" + show.getUrl() + "\n\n\n" + setlist.stream().collect(joining("\n"));
                simpleCard.setContent(content);

                return SpeechletResponse.newTellResponse(speech, simpleCard);
            case "AMAZON.HelpIntent":
            case "AMAZON.StopIntent":
                speech = new PlainTextOutputSpeech();
                speech.setText("Eat drink and be merry!");

                return SpeechletResponse.newTellResponse(speech);
            default:
                throw new SpeechletException("Invalid intent");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }
}
