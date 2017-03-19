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
//        speech.setText("Hello 2017 nw hack! Glad to have you here today.");
        speech.setText("Hello NW hack 2017! Are you ready to battle?");

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

        if ("SetPlayerName".equals(intentName)){
            return nwHelper.SetPlayerNameIntent(intent);
        }
//        else if ("SetPoints".equals(intentName)){
//            return nwHelper.SetPointsToPlayTo(intent);
//        }
        else if ("Instructions".equals(intentName)){
            return nwHelper.InstructionsIntent();
        }
        else if ("BuildJourney".equals(intentName)){
            return nwHelper.BuildJourney();
        }
        else if ("StartQuiz".equals(intentName)){
            return nwHelper.StartQuizIntent();
        }
//        else if ("Answer".equals(intentName)){
//            return nwHelper.PlayerAnswer(intent);
//
//        }
        else if ("AnswerBattle".equals(intentName)){
            return nwHelper.AnswerBattle(intent);
        }
        else {
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Lets quiz!");

            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newAskResponse(speech, reprompt);
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

    }
}
