package ch.ethz.inf.vs.quizio;

import java.util.Arrays;
import java.util.List;

/**
 * Created by peter on 01.12.17.
 */

public class Question {

    public String TheQuestion;

    public String Answer1;
    public String Answer2;
    public String Answer3;
    public String Answer4;

    public Integer CorrectAnswer;


    public Question(String questionSet){
        List<String> questionAsList = Arrays.asList(questionSet.split("\\s*,\\s*"));
        this.TheQuestion = questionAsList.get(1);
        this.Answer1 = questionAsList.get(2);
        this.Answer2 = questionAsList.get(3);
        this.Answer3 = questionAsList.get(4);
        this.Answer4 = questionAsList.get(5);
        this.CorrectAnswer = Integer.parseInt(questionAsList.get(6));
    }

    public Question(String theQuestion,String answer1,String answer2,String answer3,String answer4, Integer correctAnswer) {
        this.TheQuestion = theQuestion;
        this.Answer1 = answer1;
        this.Answer2 = answer2;
        this.Answer3 = answer3;
        this.Answer4 = answer4;
        this.CorrectAnswer = correctAnswer;
    }
    public String asString(){
        return TheQuestion + Answer1 + Answer2 + Answer3 + Answer4 + Integer.toString(CorrectAnswer);
    }

    public boolean result(Integer answer){
        return answer == CorrectAnswer;
    }


}
