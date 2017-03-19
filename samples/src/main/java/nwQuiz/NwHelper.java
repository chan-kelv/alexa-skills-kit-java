package nwQuiz;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kelvinchan on 2017-03-19.
 */
public class NwHelper {
    private static final Logger log = LoggerFactory.getLogger(NwHelper.class);
    boolean arePlayersReady;
    String playerName;
    String URL_QUESTION = "http://52.168.90.9:8081/random";

    public NwHelper(){
        arePlayersReady = false;
        playerName = "not set";
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
        String message = "could not get questions";
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(URL_QUESTION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            ObjectMapper mapper = new ObjectMapper();
            Question q = mapper.readValue(result.toString(), Question.class);
            message = q.term;
        }
        catch (Exception e){
            log.error("NWHelper", "read server error:" + e.getMessage());
        }

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(message);
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

    public SpeechletResponse SayPlayerNameIntent(){
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Welcome" + playerName);
        return SpeechletResponse.newTellResponse(speech);
    }

    public SpeechletResponse SetPlayerNameIntent(Intent intent){
        playerName = intent.getSlot("PlayerName").getValue();
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Welcome " + playerName);

        // Create reprompt
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        String repromptText;
        if (playerName == null){
            repromptText = "Could not set name, please restart";
        }
        else{
            repromptText = "Start game?";
        }
        repromptSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }
}
