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
    String playerName1;
    String playerName2;
    String URL_QUESTION = "http://52.168.90.9:8081/random";
    Question currentQuestion;
    String currentPlayerName;
    int player1Point = 0;
    int player2Point = 0;
    int totalPlayers = 0;

    public NwHelper(){
        arePlayersReady = false;
        currentPlayerName = "not set";
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
        speech.setText("Welcome" + currentPlayerName);
        return SpeechletResponse.newTellResponse(speech);
    }

    public SpeechletResponse SetPlayerNameIntent(Intent intent){
        playerName1 = intent.getSlot("PlayerNameOne").getValue();
        if (playerName1 != null){
            totalPlayers++;
        }
        playerName2 = intent.getSlot("PlayerNameTwo").getValue();
        if (playerName2 != null){
            totalPlayers++;
        }

        String speechText = totalPlayers == 1? "Welcome" + playerName1 : "Welcome " +playerName1 +" and " + playerName2;
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText + ". Are you ready to quiz?");

        currentPlayerName = playerName1;

        // Create reprompt
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        String repromptText;
        if (currentPlayerName == null){
            repromptText = "Could not set names, please restart";
        }
        else{
            repromptText = "Start game?";
        }
        repromptSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
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
            currentQuestion = mapper.readValue(result.toString(), Question.class);
            message = currentQuestion.term;
        }
        catch (Exception e){
            log.error("NWHelper", "read server error:" + e.getMessage());
        }

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(currentPlayerName + "," + message);

        SimpleCard card = new SimpleCard();
        card.setTitle(currentQuestion.term);
        card.setContent(currentQuestion.definition);

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(message);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    public SpeechletResponse PlayerAnswer(Intent intent){
        String playerAnswer = intent.getSlot("PlayerAnswer").getValue();

        SimpleCard card = new SimpleCard();
        card.setTitle(currentPlayerName + "'s Answer");
        card.setContent(playerAnswer);

        String speechText;
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        if (currentQuestion.definition.equals(playerAnswer)){
            speechText = currentPlayerName +" is correct!";
            if(currentPlayerName.equals(playerName1)){
                player1Point++;
                if(player1Point == 3){
                    speech.setText("Congratulations " + playerName1 + "You have won!");
                    return SpeechletResponse.newTellResponse(speech, card);
                }
            }
            else{
                player2Point++;
                if(player1Point == 3){
                    speech.setText("Congratulations " + playerName1 + "You have won!");
                    return SpeechletResponse.newTellResponse(speech, card);
                }
            }
        }
        else{
            speechText = currentPlayerName +" is incorrect! The correct answer is " + currentQuestion.definition;
        }

        //switch players
        currentPlayerName = currentPlayerName.equals(playerName1)? playerName2 : playerName1;
        int currentPoints = currentPlayerName.equals(playerName1)? player1Point : player2Point;

        speech.setText(speechText +". You have " + currentPoints +" points. " + currentPlayerName + "are you ready?" );

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(currentPlayerName + "are you ready?");
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
