package ch.ethz.inf.vs.quizio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Formatter;

public class ModeratorActivity extends AppCompatActivity {
    public TextView playersJoined;
    public Quiz quiz;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        TextView gameCode = (TextView) findViewById(R.id.gameCode);
        playersJoined = (TextView) findViewById(R.id.joinedPlayers);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = mPrefs.getString("quiz", "");
        quiz = gson.fromJson(json, Quiz.class);

        gameCode.setText(quiz.gameCode.toString());




        Handler handler = new Handler();
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                playersJoined.setText(quiz.getAllPlayerNamesString());
                handler.postDelayed(this, 200);
            }
        };
        handler.post(runnableCode);


        final Button button = findViewById(R.id.startQuiz);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{

                            WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
                            String ip = android.text.format.Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());


                            URL url = new URL("http://" + ip +":8080/?startQuestion");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            //BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            //PrintWriter pw = new PrintWriter(connection.getOutputStream());
                            int status = connection.getResponseCode();
                            int i = 0;
                        } catch (Exception e) {
                            e.printStackTrace();}
                    }
                }).start();



            }
        });


    }
}
