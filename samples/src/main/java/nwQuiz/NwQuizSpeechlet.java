package nwQuiz;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kelvinchan on 2017-03-18.
 */
public class NwQuizSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(NwQuizSpeechlet.class);
    private NwHelper nwHelper;

    @Override
    public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", sessionStartedRequest.getRequestId(),
                session.getSessionId());

        nwHelper = new NwHelper();
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

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("Did you need a repeat?");

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
        if (nwHelper == null) {
            nwHelper = new NwHelper();
        }
        Intent intent = intentRequest.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("NwQuiz".equals(intentName)){
            return nwHelper.PrepPlayersIntent();
        }
        else if ("GetReady".equals(intentName)){
            return nwHelper.ImReadyIntent();
        }
        else if ("ImReady".equals(intentName)){
            return nwHelper.PlayersReadyIntent();
        }
//        else if ("startQuiz".equals(intentName)){
//            return nwHelper.StartQuizIntent();
//        }
        else {
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Lets quiz!");

            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newTellResponse(speech);
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

    }
}
