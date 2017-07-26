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
import com.amazon.speech.ui.SsmlOutputSpeech;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import java.io.IOException;
import java.time.LocalDate;

/**
 * {@link Speechlet} which handles the lifecycle of the Alexa application.
 */
final class ThisDayInDmbSpeechlet implements Speechlet {

    private static final Logger LOG = LoggerFactory.getLogger(ThisDayInDmbSpeechlet.class);

    private static final PlainTextOutputSpeech WELCOME_SPEECH = new PlainTextOutputSpeech();
    private static final PlainTextOutputSpeech HELP_SPEECH = new PlainTextOutputSpeech();
    private static final PlainTextOutputSpeech HELP_REPROMPT_SPEECH = new PlainTextOutputSpeech();
    private static final Reprompt HELP_REPROMPT = new Reprompt();
    private static final PlainTextOutputSpeech STOP_SPEECH = new PlainTextOutputSpeech();
    private static final PlainTextOutputSpeech UNKNOWN_SONG_SPEECH = new PlainTextOutputSpeech();

    static {
        WELCOME_SPEECH.setText("Welcome to this day in d. m. b. history! Say setlist to hear a historical setlist!");
        HELP_SPEECH.setText("I can tell you historical d. m. b. set lists");
        HELP_REPROMPT_SPEECH.setText("Say setlist to hear a historical setlist or stop to exit");
        HELP_REPROMPT.setOutputSpeech(HELP_REPROMPT_SPEECH);
        STOP_SPEECH.setText("O. K. stopping. Eat, drink, and be merry!");
        UNKNOWN_SONG_SPEECH.setText("Sorry, I don't know that song");
    }

    private static final String BASE_URL = "http://dmbalmanac.com";
    private static final Almanac ALMANAC = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(Almanac.class);

    private static final ShowSupplier SHOW_SUPPLIER = new ShowSupplier(ALMANAC);
    private static final SongSupplier SONG_SUPPLIER = new SongSupplier(ALMANAC);

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        LOG.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        LOG.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        final Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(WELCOME_SPEECH);

        return SpeechletResponse.newAskResponse(WELCOME_SPEECH, reprompt);
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        final Intent intent = request.getIntent();
        final String intentName = (intent != null) ? intent.getName() : "AMAZON.StopIntent";

        switch (intentName) {
            case "SetlistIntent":
                final Show show;
                try {
                    show = SHOW_SUPPLIER.get();
                } catch (IOException e) {
                    throw new SpeechletException("Could not retrieve setlist");
                }

                final String speechText = withPauses(1, "On this day Dave Matthews Band played in",
                        show.getVenue(),
                        "where the set list was",
                        String.join(", ", show.getSetlist()));

                final String cardTitle = LocalDate.now() + " " + show.getVenue();
                final String cardContent = BASE_URL + "/" + show.getUrl() + "\n\n\n" + String.join("\n", show.getSetlist());

                return tellResponse(speechText, cardTitle, cardContent);
            case "SongIntent":
                final String songName = intent.getSlot("SongName").getValue();
                final String lastPlayed;
                try {
                    lastPlayed = SONG_SUPPLIER.get(songName);
                } catch (IOException e) {
                    throw new SpeechletException("Could not find last played date for " + songName);
                }

                final PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
                speech.setText(songName + " was last played on " + lastPlayed);
                return SpeechletResponse.newTellResponse(speech);
            case "AMAZON.HelpIntent":
                return SpeechletResponse.newAskResponse(HELP_SPEECH, HELP_REPROMPT);
            case "AMAZON.StopIntent":
            case "AMAZON.CancelIntent":
                return SpeechletResponse.newTellResponse(STOP_SPEECH);
            default:
                throw new SpeechletException("Invalid intent");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    private static String withPauses(int pauseSeconds, String... parts) {
        return "<speak>" + String.join(" <break time=\"" + pauseSeconds + "s\"/> ", parts) + "</speak>";
    }

    private static SpeechletResponse tellResponse(String sayText, String cardTitle, String cardText) {
        final SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml(sayText);

        final SimpleCard simpleCard = new SimpleCard();
        simpleCard.setTitle(cardTitle);
        simpleCard.setContent(cardText);

        return SpeechletResponse.newTellResponse(speech, simpleCard);
    }
}
