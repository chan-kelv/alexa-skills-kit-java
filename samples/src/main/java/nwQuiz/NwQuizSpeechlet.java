package nwQuiz;

import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kelvinchan on 2017-03-18.
 */
public class NwQuizSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(NwQuizSpeechlet.class);

    @Override
    public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", sessionStartedRequest.getRequestId(),
                session.getSessionId());

    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", launchRequest.getRequestId(),
                session.getSessionId());

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Hello 2017 nw hack! Glad to have you here today.");

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("NW Quiz start");
        card.setContent("Start the quiz!");

        return SpeechletResponse.newTellResponse(speech, card);
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
        return null;
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

    }
}
