package ch.ethz.inf.vs.quizio;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QuizClient extends Thread {

    private static final String TAG = "QuizClient";



    public interface Listener {
        void onJoinResult(boolean success, String message);
        void onStartQuestion(Question question);
        void onResult(boolean correct, int points, int rank);
        void discover();
        void goHome();
    }

    private Lock lock;
    private boolean kill;
    private Listener listener;
    private int points;
    private InetAddress host;
    private int port;
    private int NrQuestions;
    private int rank;
    private boolean joined = false;
    private int currentQuestion = 0;
    private Quiz quiz;
    private Player player;


    QuizClient(Listener listener) {
        this.listener = listener;
        lock = new ReentrantLock();
    }

    @Override public void run() {

        Log.d(TAG, "Start listening for quizzes");

        //Wait until join is successful
        while (!joined) {
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                Log.d(TAG,"interrupted on waiting to join quiz");
                e.printStackTrace();
            }
        }
        // Did we kill the thread?
        lock.lock();
        if (kill) {
            lock.unlock();
            return;
        }
        lock.unlock();

        Log.d(TAG, "Start waiting loop");

        // After joining, we should now have the quiz
        // start the game loop



        NrQuestions = quiz.questionList.size();

        for(int i = 0;i<NrQuestions;i++){

            while(!pollNextQuestion()){
                try {
                    sleep(2000);
                }catch (InterruptedException e){
                    Log.d(TAG,"interrupted while polling server for go on "+i+" th question");
                    e.printStackTrace();
                }
            }
            //as soon as we have the go, inform the listener to start the next question
            currentQuestion++;
            listener.onStartQuestion(quiz.questionList.get(i));

        }

        try{
            sleep(10000);
        }catch (InterruptedException e){
            Log.d(TAG, "End of Game: Interrupted on waiting to go to main activity");
        }
        listener.goHome();






    }

    void joinQuiz(InetAddress host, int port,String name,String code) {
        //TODO auf 8080 gesetzt
        this.host = host;
        this.port = 8080;

        //setup a connection to the server


        new Thread(new Runnable() {
            @Override
            public void run() {
                try{


                    URL url = new URL("http:/"+host+":"+8080+"/?join" );
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");

                    PrintWriter pw = new PrintWriter(connection.getOutputStream());
                    JSONObject data = new JSONObject();
                    data.put("name",name);
                    data.put("code",code);
                    pw.print(data.toString());


                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));


                    //read answer from server
                    int status = connection.getResponseCode();


                    if(status==200){

                        StringBuilder sBuilder = new StringBuilder();
                        String line = "";
                        while((line = in.readLine()) != null){
                            sBuilder.append(line+"\n");
                        }
                        String response = sBuilder.toString();


                        String message;
                        String error;

                        StringTokenizer st = new StringTokenizer(response, "/");

                        //Unsuccessful
                        if (st.hasMoreElements()) {
                            if ((message = st.nextToken()).equals("JoinFailed")) {
                                //join was unsuccessful, handle the problem
                                if ((error = st.nextToken()).equals("wrongCode")) {
                                    //prompt user to input right code
                                    listener.onJoinResult(false, error);
                                } else {
                                    //prompt user to chose another name
                                    listener.onJoinResult(false, error);
                                }
                                //Success
                            } else if (message.equals("JoinSuccessful")) {

                                //go forward in game
                                player = new Player(name);
                                joined = true;
                                getQuiz();
                                listener.onJoinResult(true, null);
                                //Unexpected response
                            } else {
                                Log.d(TAG, "Unknown response on trying to Join");
                            }
                        }


                    }else{
                        listener.onJoinResult(false,"Bad server response");
                    }
                }
                catch (Exception e){
                    Log.d(TAG,":on join quiz communication");
                    e.printStackTrace();
                }
            }
        }).start();




    }

    void submitAnswer(QuestionFragment.Answer ans,int CorrectAns, int timeRemaining) {



        Log.d(TAG, String.format("Submitting answer: %s", ans.toString().toLowerCase()));

        QuestionFragment.Answer corrAns;
        switch (CorrectAns){
            case 1:
                //red
                corrAns = QuestionFragment.Answer.RED;
            case 2:
                //yellow
                corrAns = QuestionFragment.Answer.YELLOW;
            case 3:
                //green
                corrAns = QuestionFragment.Answer.GREEN;
            case 4:
                //blue
                corrAns = QuestionFragment.Answer.BLUE;
            default:
                corrAns = QuestionFragment.Answer.NONE;

        }
        boolean right = (corrAns.equals(ans));

        //post message with boolean to server, expects points and rank back

        try {
            URL url = new URL("http://"+host+":"+port+"/submitAnswer");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter pw = new PrintWriter(connection.getOutputStream(),true);
            connection.setRequestMethod("POST");

            if(right){
                points = points + 10 * timeRemaining;
                player.setScore(points);
            }
            String Player = new Gson().toJson(player, ch.ethz.inf.vs.quizio.Player.class);
            pw.print(Player);


            int status = connection.getResponseCode();

            if(status == 200) {

                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                String response = sb.toString();

                StringTokenizer st = new StringTokenizer(response,"/");
                //expected response
                if(st.nextToken().equals("AnswerReceived")) {


                    player = new Gson().fromJson(st.nextToken(), Player.class);
                    points = player.getScore();
                    rank = player.getRank();

                    //forward to game activity
                    listener.onResult(right, points, rank);
                }else{
                    Log.d(TAG,"server did not accept response");
                }
            }else{
                Log.d(TAG,"Bad server response");
            }

        }catch (Exception e){
            Log.d(TAG," on submitting answer");
            e.printStackTrace();
        }

    }

    void killClient() {

        Log.d(TAG, "killClient()");

        lock.lock();
        kill = true;
        lock.unlock();
        interrupt();
    }

    private void getQuiz(){
        try {
            URL url = new URL("http://" + host + ":" + port + "/getQuiz");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");


            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            int status = connection.getResponseCode();
            if(status == 200) {

                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line + "\n");
                }
                String response = sb.toString();

                quiz = new Gson().fromJson(response,Quiz.class);

            }else{
                Log.d(TAG,"bad server response");
            }

        }catch (Exception e){
            Log.d(TAG,"error on getQuiz communication");
            e.printStackTrace();
        }
    }

    private boolean pollNextQuestion(){

        try {
            URL url = new URL("http://"+host+":"+port+"/isQuestionStarted");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            int status = connection.getResponseCode();
            if(status == 200) {

                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line + "\n");
                }
                String response = sb.toString();




                if (response.equals("true")) {
                    //go for next question
                    return true;
                } else {
                    //wait
                    return false;
                }
            }else{
                Log.d(TAG,"bad server response");
            }

        }catch (Exception e){
            Log.d(TAG,"in pollNextQuestion");
            e.printStackTrace();
        }

        return false;
    }

    public boolean tryReconnect(InetAddress host,int port){
        this.host = host;
        this.port = port;

        try {
            URL url = new URL("http://"+host+":"+port+"/resume");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter pw = new PrintWriter(connection.getOutputStream(),true);

            String Quiz = new Gson().toJson(quiz, ch.ethz.inf.vs.quizio.Quiz.class);
            pw.print(Quiz);
            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = in.readLine())!= null){
                sb.append(line+"\n");
            }
            String response = sb.toString();
            StringTokenizer st = new StringTokenizer(response,"/");
            if(response.equals("ResumeSucceeded")){
                //server accepted quiz
                startFromIthQuestion(Integer.valueOf(st.nextToken()));
                return true;
            }else{
                Log.d(TAG,"reconnect failed");
                return false;
            }





        }catch (Exception e){
            Log.d(TAG,"error communicating in reconnect");
            e.printStackTrace();
        }

        return false;
    }


    private void startFromIthQuestion(int i){
        currentQuestion = i;
        while(currentQuestion<NrQuestions){
            while(!pollNextQuestion()){
                try {
                    sleep(2000);
                }catch (InterruptedException e){
                    Log.d(TAG,"interrupted while polling server for go on "+i+" th question");
                    e.printStackTrace();
                }
            }
            //as soon as we have the go, inform the listener to start the next question
            currentQuestion++;
            listener.onStartQuestion(quiz.questionList.get(i));
        }
        try{
            sleep(10000);
        }catch (InterruptedException e){
            Log.d(TAG, "End of Game: Interrupted on waiting to go to main activity");
        }
        listener.goHome();

    }

}
