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
    String URL_QUESTION = "http://52.168.90.9:8081/random"; //Azure
    int winPoints = 2;
    Question currentQuestion;
    String currentPlayerName;
    int player1Point = 0;
    int player2Point = 0;

    public NwHelper(){
        arePlayersReady = false;
        currentPlayerName = "not set";
    }

    public SpeechletResponse InstructionsIntent(){
        String speechText = "I will ask you a trivia question. Respond with your name, followed by the answer. " +
                "First person to " + winPoints + " points win!";
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(speechText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    public SpeechletResponse BuildJourney(){
        String speechText = "I get my questions from Quizlet API. They are saved on Microsoft Azure " +
                "using Cockroach DB using Go lang. The app runs using Amazon lambda functions";
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("Lets start the battle!");
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    public SpeechletResponse SetPlayerNameIntent(Intent intent){
        playerName1 = intent.getSlot("PlayerNameOne").getValue();
        playerName2 = intent.getSlot("PlayerNameTwo").getValue();

        String speechText = "Welcome " +playerName1 +" and " + playerName2;
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
        if (playerName1 == null || playerName2 == null) {
            String speechText = "Player names have not been set yet. Please set them first using Set playername one and two";
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(speechText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt);
        }
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
        speech.setText(message);

        SimpleCard card = new SimpleCard();
        card.setTitle(currentQuestion.term);
        card.setContent(currentQuestion.definition);

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(message);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    public SpeechletResponse AnswerBattle(Intent intent){
        String playerName = intent.getSlot("PlayerName").getValue();
        String playerAnswer = intent.getSlot("PlayerAnswer").getValue();

        SimpleCard card = new SimpleCard();
        card.setTitle(currentPlayerName + "'s Answer");
        card.setContent(playerAnswer);

        String foundPlayer;
        if(playerName.equals(playerName1)){
            foundPlayer = playerName1;
        }
        else if (playerName.equals(playerName2)){
            foundPlayer = playerName2;
        }
        else{
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Sorry I didn't catch that, please try again," + currentQuestion.term);
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(currentQuestion.term);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);
            return SpeechletResponse.newAskResponse(speech, reprompt);
        }

        String speechText;
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        if (currentQuestion.definition.equals(playerAnswer)){
            speechText = foundPlayer +" is correct!";
            if(foundPlayer.equals(playerName1)){
                player1Point++;
                if(player1Point == winPoints){
                    speech.setText("Congratulations " + playerName1 + "You have won!");
                    return SpeechletResponse.newTellResponse(speech, card);
                }
            }
            else{
                player2Point++;
                if(player1Point == winPoints){
                    speech.setText("Congratulations " + playerName2 + "You have won!");
                    return SpeechletResponse.newTellResponse(speech, card);
                }
            }
        }
        else{
            speechText = foundPlayer + "said" + playerAnswer+"The correct answer is ," + currentQuestion.definition;
        }
        speech.setText(speechText
                        + playerName1 +"has " + player1Point + "points,"
                        + playerName2 +"has "+ player2Point +"points,"
                        + "are you ready for the next round?");

        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("Players are you ready?");
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);
        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
