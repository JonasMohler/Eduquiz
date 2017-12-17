package ch.ethz.inf.vs.quizio;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by peter on 01.12.17.
 */

public class Quiz {

    String QuizName;
    ArrayList<Player> playerList = new ArrayList<Player>();
    ArrayList<Question> questionList = new ArrayList<Question>();
    public Integer currentQuestion = 0;
    public Integer gameCode;
    Set<String> questionStringSet = new HashSet<String>();

    public Quiz() {
        this.gameCode = generateCode();
    }

    public int generateCode() {
        //generate a 4 digit integer 1000 <10000
        return (int)(Math.random()*9000)+1000;
    }

    public int getNumPlayers () {
        return this.playerList.size();
    }


    public Question getQuestion (Integer questionNumber)  {
        try{
            Question result = questionList.get(questionNumber);
            return result;
        }catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<String> getQuestionList() {
        ArrayList<String> questions = new ArrayList<String>();
        for (int n = 0; n<questionList.size();n++) {
            questions.add(questionList.get(n).TheQuestion);
        }
        return questions;
    }


    public void resumeQuiz(int currentQuestion, int gameCode,ArrayList<String> questions){

        for (String questr : questions) {
            Question question = new Question(questr);
            addQuestion(question);
        }
        this.gameCode = gameCode;
        this.currentQuestion = currentQuestion;
    }


    public void playerRejoins(String name,ArrayList<Integer> answers){
        Player player = new Player(name);
        for (int i = 1;i<answers.size();i++){
            PlayerAnswers(player,answers.get(i),answers.get(i));
        }
    }

    synchronized public boolean addQuestion(Question question) {
        if (question != null){
            questionList.add(question);
            return true;
        } else return false;
    }

    synchronized public boolean editQuestion(Integer questionNumber,Question editedQuestion) {
        if (editedQuestion != null){
            questionList.set(questionNumber, editedQuestion);
            return true;
        } else return false;
    }

    synchronized public boolean deleteQuestion(Integer questionNumber) {
        if(questionList.get(questionNumber) != null) {
            questionList.remove(questionNumber);
            return true;
        }else return false;
    }

    synchronized public boolean PlayerJoins (Player player) {
        if (player != null){
            playerList.add(player);
            return true;
        } else return false;

    }

    synchronized public void PlayerAnswers(Player player, Integer answer) {
        player.answerd(answer == questionList.get(currentQuestion).CorrectAnswer);
    }
    synchronized public void PlayerAnswers(Player player, Integer answer,int question) {
        player.answerd(answer == questionList.get(question).CorrectAnswer);
    }




    public Player getPlayerOnRank(Integer rank) {
        sortPlayers();
        if (rank>=playerList.size()) rank = playerList.size()-1;
        return playerList.get(rank);
    }

    synchronized private void sortPlayers() {
        Comparator<Player> comp = new PlayerComparator();
        Collections.sort(playerList,comp);
    }

    public ArrayList<String> getAllPlayerNames () {
        ArrayList<String> names = new ArrayList<String>();
        for (Player player : playerList) {
            names.add(player.name);
        }
        return names;
    }

    public String getAllPlayerNamesString () {
        String result = "";
        Integer n = playerList.size();
        for(Integer i = 0;i<n;i++){
            result = result + playerList.get(i).name
                    +  System.lineSeparator();
        }
        return result;
    }

    public void createQuestionSet() {
        for (int i = 1; i < questionList.size(); i++) {
            questionStringSet.add(questionList.get(i).asString());
        }
    }

    public Player getRankForPlayer(Player player) {
        int rank = 0;
        this.sortPlayers();
        for(Integer i = 0;i<playerList.size();i++){
            if (player == playerList.get(i)) {
                player.rank = i + 1;
            }
        }
        return player;
    }

    public String getFinalScoreboard() {
        this.sortPlayers();
        String result = "";
        Integer n = playerList.size();
        for(Integer i = 0;i<n;i++){
            Integer place = i + 1;
            result = result + place.toString() + ". "  + playerList.get(i).name
                    +  " with " + playerList.get(i).getScore() + " Points" +  System.lineSeparator();
        }
        /*
        1. Name with XX Points
        2. Name with XX Points
        ...
         */

        return result;
    }
}