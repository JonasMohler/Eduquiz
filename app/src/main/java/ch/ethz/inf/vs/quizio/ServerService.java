package ch.ethz.inf.vs.quizio;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Binder;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.content.Context;
import android.view.Display;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Enumeration;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.net.InetAddress;


import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import fi.iki.elonen.NanoHTTPD;

public class ServerService extends Service {

    QuizServer quizServer;
    Quiz quiz;
    ServerSocket mServerSocket;
    int mLocalPort = 0;
    RegistrationListener mRegistrationListener;
    String mServiceName;
    NsdManager mNsdManager;



    public ServerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        try {
            quizServer = new QuizServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeServerSocket();
        initializeRegistrationListener();
        registerService(mLocalPort);

        return super.onStartCommand(intent, flags, startId);
    }

    public void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName("NsdChat");
        serviceInfo.setServiceType("_nsdchat._tcp");
        serviceInfo.setPort(port);


        //mNsdManager = Context.getSystemService(Context.NSD_SERVICE);
        mNsdManager = (NsdManager) getSystemService(NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Store the chosen port.
        mLocalPort = mServerSocket.getLocalPort();
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
            }
        };
    }

    public class QuizServer extends NanoHTTPD {
        //boolean quizResumeDataAvailable = true;
        int numAnswersSubmitted = 0;
        int questionNumber = 0;
        int numRejoinedPlayers = 0;
        int numQuestions = 0;
        int numPlayers = 0;
        Quiz quiz;
        boolean hasQuestionStarted = true;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();


        public QuizServer() throws IOException {
            super(8080);
            //only do this if Quizserver is started for the first time
            if(!CreateQuizActivity.getQuizResume()) {
                String json = mPrefs.getString("quiz", "");
                quiz = gson.fromJson(json, Quiz.class);
                numQuestions = quiz.questionList.size();
                numPlayers = quiz.getNumPlayers();

            }
            start();
            System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        }

        public void startNextQuestion() {

            int timeForThisQuestion = 10; //in seconds

            hasQuestionStarted = true;
            Intent intent = new Intent(getApplicationContext(), ModeratorQuestionActivity.class);
            startActivity(intent);


            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    numAnswersSubmitted = 0;
                    hasQuestionStarted = false;
                    questionNumber += 1;
                    quiz.currentQuestion += 1;

                    if (questionNumber == numQuestions) {
                        Intent intent = new Intent(getApplicationContext(), ModeratorScoreboardActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(getApplicationContext(), ModeratorResultActivity.class);
                        startActivity(intent);
                    }
                    prefsEditor.putInt("currentQuestion",questionNumber);
                }
            }, timeForThisQuestion*1000);
        }

        @Override
        public Response serve(IHTTPSession session) {
            //quiz.createQuestionSet();
            Map<String, String> parms = session.getParms();

            if (CreateQuizActivity.getQuizResume()) {

                if (parms.containsKey("resume")) {
                    numRejoinedPlayers += 1;
                    String oldQuiz = parms.get("resume");
                    prefsEditor.putString("quiz", oldQuiz);
                    quiz = gson.fromJson(oldQuiz, Quiz.class);
                    numPlayers = quiz.getNumPlayers();
                    numQuestions = quiz.questionList.size();

                    if(numRejoinedPlayers == numPlayers) {
                        startNextQuestion();
                        return new Response("ResumeSucceeded/"+quiz.currentQuestion);
                    }
                    else return new Response("WaitingForOtherPlayersToJoin");


                }
            } else if (parms.containsKey("join")) {
                //TODO Peter/Valentin: Client needs to check if name is already taken or not and if code is valid
                //TODO also server should send a message on failed join so client knows why it failed
                //hier mein vorschlag...
                if(false /*name schon benutzt*/){
                    return new Response("JoinFailed/Choose a different name");
                }else if(false /*code stimmt nicht */){
                    return new Response("JoinFailed/Invalid Code");
                }else {


                    numPlayers += 1;
                    String json = mPrefs.getString("quiz", "");
                    Quiz quiz = gson.fromJson(json, Quiz.class);
                    quiz.PlayerJoins(new Player(parms.get("join")));
                    String quizUpdated = gson.toJson(quiz);
                    prefsEditor.putString("quiz", quizUpdated);
                    prefsEditor.commit();

                    return new Response("JoinSucceeded");
                }

            } else if (parms.containsKey("startQuestion")) {

                String jsonQuiz = mPrefs.getString("quiz", "");
                startNextQuestion();
                return new Response("<QuestionStarted>" + jsonQuiz);

            //need this to get quiz, if client would use startQuestion, moderator would got to question activity...
            }else if(parms.containsKey("getQuiz")) {
                String jsonQuiz = mPrefs.getString("quiz", "");
                return new Response(jsonQuiz);

            }else if (parms.containsKey("isQuestionStarted")) {
                if (hasQuestionStarted) {
                    return new Response("true");
                } else return new Response("false");

            } else if (parms.containsKey("submitAnswer")) {
                //TODO

                /*
                * Implemented point calculation in client
                * Now server gets a player with an updated score
                * server just needs to update rank and send player back
                * then you also don't need a timestamp here
                * player contains a clock now(whatever you want to do with that)
                * */

                numAnswersSubmitted += 1;
                if (numAnswersSubmitted == numPlayers) {
                    numAnswersSubmitted = 0;
                    hasQuestionStarted = false;
                    questionNumber += 1;
                    Intent intent = new Intent(getApplicationContext(), ModeratorResultActivity.class);
                    startActivity(intent);
                    prefsEditor.putInt("currentQuestion",questionNumber);
                }
                if (questionNumber == numQuestions) {
                    Intent intent = new Intent(getApplicationContext(), ModeratorScoreboardActivity.class);
                    startActivity(intent);
                }
                //TODO return "AnswerReceived/" + Player (has points and ranking)

                return new Response("AnswerReceived/");
            }

            return new Response(null);

        }
    }
}

