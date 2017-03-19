package nwQuiz;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

/**
 * Created by kelvinchan on 2017-03-19.
 */
public class NwHelper {
    boolean arePlayersReady;

    public NwHelper(){
        arePlayersReady = false;
    }

    public SpeechletResponse PrepPlayersIntent() {
        if (!arePlayersReady){
            SimpleCard card = new SimpleCard();
            card.setTitle("Players get ready!");
            card.setContent("Ready?");

            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Players are you ready to quiz?");

            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText("Players get ready to quiz");
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);
        }
        else{
            return StartQuizIntent();
        }
    }

    public SpeechletResponse StartQuizIntent(){
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Quiz starts in 5,4,3,2,1,GO!");
        return SpeechletResponse.newTellResponse(speech);
    }

    public SpeechletResponse ImReadyIntent(){
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("I'm ready too, lets go!");
        return SpeechletResponse.newTellResponse(speech);
    }

    public SpeechletResponse PlayersReadyIntent(){
        arePlayersReady = true;
        return ImReadyIntent();
    }
}
