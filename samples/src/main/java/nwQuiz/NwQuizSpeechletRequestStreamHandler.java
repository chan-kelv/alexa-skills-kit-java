package nwQuiz;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kelvinchan on 2017-03-18.
 */
public class NwQuizSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add("amzn1.ask.skill.75f273ef-0c96-4dbc-b1e7-cc40ca950973");
    }

    public NwQuizSpeechletRequestStreamHandler() {
        super(new NwQuizSpeechlet(), supportedApplicationIds);
    }
}
