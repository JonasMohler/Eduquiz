package ch.ethz.inf.vs.quizio;

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
    }

    private Lock lock;
    private boolean kill;
    private WifiManager wifiManager;
    private Listener listener;
    private int points;
    private InetAddress host;
    private int port;
    private List<Question> questions;
    private int NrQuestions;
    private Socket socket;
    private int rank;
    private boolean joined = false;
    private int currentQuestion = 0;
    private JSONObject JsonQuiz;


    QuizClient(WifiManager wifiManager, Listener listener) {
        //TODO: let client accept host and port instead of wifimanager
        this.wifiManager = wifiManager;
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

        for(int i = 0;i<NrQuestions;i++){
            //while server doesnt give us the go for next question sleep
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
            listener.onStartQuestion(questions.get(i));

        }

        //TODO: What to do? Go back to mainActivity?






    }

    void joinQuiz(InetAddress host, int port,String name,String code) {

        this.host = host;
        this.port = port;

        //setup a connection to the server
        try{


            URL url = new URL("http://"+host+":"+port+"/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("join");
            connection.setRequestProperty("name",name);
            connection.setRequestProperty("code",code);
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //read answer from server
            StringBuilder sBuilder = new StringBuilder();
            String line = "";
            while((line = in.readLine()) != null){
                sBuilder.append(line+"\n");
            }
            String response = sBuilder.toString();


            String status;
            String message;
            StringTokenizer st = new StringTokenizer(response,"<>");
            while(!st.nextToken().equals("Body")){}
            //Unsuccessful
            if ((status = st.nextToken()).equals("JoinFailed")){
                //join was unsuccessful, handle the problem
                if((message = st.nextToken()).equals("wrongCode")){
                    //prompt user to input right code
                    listener.onJoinResult(false,message);
                }else{
                    //prompt user to chose another name
                    listener.onJoinResult(false,message);
                }
            //Success
            }else if(status.equals("JoinSuccessful")) {
                //extract JsonObject to get number of questions and actual questions
                JsonQuiz = (JSONObject) new JSONTokener(st.nextToken()).nextValue();
                NrQuestions = JsonQuiz.getInt("QuestionCount");
                JSONObject JsonQuestions = JsonQuiz.getJSONObject("Questions");
                JSONObject question;
                for(int i = 0;i<NrQuestions;i++){
                    question = JsonQuestions.getJSONObject("q"+i);

                    questions.add(i,new Question(question.getString("theQuestion"),question.getString("answer1"),question.getString("answer2"),question.getString("answer3"),question.getString("answer4"),question.getInt("CorrectAnswer")));
                }

                //go forward in game
                joined = true;
                listener.onJoinResult(true,null);
            //Unexpected response
            }else{
                Log.d(TAG,"Unknown response on trying to Join");
            }


        }
        catch (Exception e){
            Log.d(TAG,":on join quiz communication");
            e.printStackTrace();
        }



    }

    void submitAnswer(QuestionFragment.Answer ans,int CorrectAns, int oldPoints) {



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
            URL url = new URL("http://"+host+":"+port+"/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            connection.setRequestMethod("submitAnswer");

            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = in.readLine())!= null){
                sb.append(line);
            }
            String response = sb.toString();

            StringTokenizer st = new StringTokenizer(response,"<>");
            while(!st.nextToken().equals("Body")){}
            //expected response
            if(st.nextToken().equals("AnswerReceived")){

                //read out values
                JSONObject JsonRank = (JSONObject) new JSONTokener(st.nextToken()).nextValue();
                points = JsonRank.getInt("points");
                rank = JsonRank.getInt("rank");

                //forward to game activity
                listener.onResult(right,points,rank);

            //unexpected response
            }else{
                Log.d(TAG,"unexpected response on submitting answer");
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



    private boolean pollNextQuestion(){

        try {
            URL url = new URL("http://"+host+":"+port+"/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connection.setRequestMethod("isQuestionStarted");
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine())!= null){
                sb.append(line+"\n");
            }
            String response = sb.toString();
            StringTokenizer st = new StringTokenizer(response,"<>");
            while(!st.nextToken().equals("Body")){}


            if(st.nextToken().equals("true")){
                //go for next question
                return true;
            }else{
                //wait
                return false;
            }


        }catch (Exception e){
            Log.d(TAG,"in pollNextQuestion");
            e.printStackTrace();
        }

        return false;
    }

    public void tryReconnect(InetAddress host,int port){
        this.host = host;
        this.port = port;

        try {
            URL url = new URL("http://"+host+":"+port+"/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            connection.setRequestMethod("resume");
            //TODO:
            // if quiz doesnt need to be sent do this in a poll like style until server responds with
            // <ResumeSucceeded><CurrentQuestion>
            //
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine())!= null){
                sb.append(line+"\n");
            }
            String response = sb.toString();
            StringTokenizer st = new StringTokenizer(response,"<>");
            while(!st.nextToken().equals("Body")){}
            if(st.nextToken().equals("ResumeSucceeded")){
                //server accepted quiz
                startFromIthQuestion(Integer.valueOf(st.nextToken()));
            }else{
                Log.d(TAG,"reconnect failed");
            }





        }catch (Exception e){
            Log.d(TAG,"error on socket creation in reconnect");
            e.printStackTrace();
        }


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
            listener.onStartQuestion(questions.get(i));
        }

        //at this point we should be through all questions
        //we now ask the server for the complete scoreboard
        //TODO: What to do? Go back to mainActivity?



    }

}
