package nwQuiz;

/**
 * Created by kelvinchan on 2017-03-19.
 */
public class Question {
    String term;
    String definition;
    int rank;

    public Question(){

    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
