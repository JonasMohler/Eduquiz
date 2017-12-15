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
        int numAnswersSubmitted = 0;
        int questionNumber = 0;
        boolean quizResumeDataAvailable = true;
        boolean hasQuestionStarted = false;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = mPrefs.getString("quiz", "");
        Quiz quiz = gson.fromJson(json, Quiz.class);
        int numQuestions = quiz.questionList.size();
        int numPlayers = quiz.getNumPlayers();
        int numRejoinedPlayers = 0;


        public QuizServer() throws IOException {
            super(8080);
            start();
            System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        }

        public void startNextQuestion() {

            int timeForThisQuestion = 10;

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
                    Intent intent = new Intent(getApplicationContext(), ModeratorResultActivity.class);
                    startActivity(intent);
                    prefsEditor.putInt("currentQuestion",questionNumber);
                }
            }, timeForThisQuestion*1000);
        }

        @Override
        public Response serve(IHTTPSession session) {
            quiz.createQuestionSet();
            Map<String, String> parms = session.getParms();
            int oldNumPlayers = 0;
            if (CreateQuizActivity.getQuizResume()) {
                if (parms.containsKey("resume")) {
                    numRejoinedPlayers += 1;
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    String oldQuiz = parms.get("resume");
                    prefsEditor.putString("quiz", oldQuiz);

                    Gson gson = new Gson();
                    Quiz quiz = gson.fromJson(oldQuiz, Quiz.class);
                    numPlayers = quiz.getNumPlayers();

                    if(numRejoinedPlayers == numPlayers) {
                        //TODO start next question
                        //TODO: please tell me (jonas) if client needs to send Quiz on resume or just the currentQuestion (int)
                        return new Response("ResumeSucceeded");
                    }
                    else return new Response("WaitingForOtherPlayersToJoin");


                }
            } else if (parms.containsKey("join")) {
                numPlayers += 1;
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = mPrefs.getString("quiz", "");
                Quiz quiz = gson.fromJson(json, Quiz.class);
                quiz.PlayerJoins(new Player(parms.get("join")));
                String quizUpdated = gson.toJson(quiz);

                //prefsEditor.putInt("gamecode", quiz.gameCode);
                //prefsEditor.putStringSet("Questions",quiz.questionStringSet);
                //prefsEditor.putInt("oldNumPlayers",numPlayers);

                prefsEditor.putString("quiz", quizUpdated);
                //int gamecode = mPrefs.getInt("gamecode", 0);

                prefsEditor.commit();
                //TODO:
                //client send name and code in header, see if they fit
                //also respond with <JoinSucceeded><Quiz> where Quiz is a JSONObject
                //Quiz: int QuestionCount
                //      JSONOBject Questions
                //where Questions:
                //                  "q0" : JSONObject Question
                //                  "q1" : JSONObject Question
                //                          ...
                //where JSONObject Question:
                //                          "theQuestion" : question
                //                          "answer1" : answer 1
                //                              ...
                //                          "correctAnswer" : correctAnswer (int)
                //
                //
                //on fail please send <JoinFailed><FailureMessage>

                return new Response("<JoinSucceeded><"+ quizUpdated + ">");


            } else if (parms.containsKey("startQuestion")) {
                startNextQuestion();
                return new Response("QuestionStarted");

            } else if (parms.containsKey("isQuestionStarted")) {
                if (hasQuestionStarted) {

                    return new Response("<true>");
                } else return new Response("<false>");

            } else if (parms.containsKey("submitAnswer")) {
                //TODO: needs to take a boolean to know if answer was right

                //calcualte points, add them to playerscore, send playerscores
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
                    //start ScoreBoardActivity
                }
                //TODO
                //client expects <AnswerReceived><ranking> where ranking is a JSONObject
                // ranking{"points":points , "rank":rank"} (both ints)
                return new Response("AnswerReceived");
            }

            return new Response(null);

        }
    }
}

