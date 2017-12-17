package ch.ethz.inf.vs.quizio;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URL;

public class ModeratorResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_result);




        final Button button = findViewById(R.id.nextbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO Call startNextQuestion in ServerService
                try{

                    WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
                    String ip = android.text.format.Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());


                    URL url = new URL("http://localhost:8080/?startQuestion");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    int status = connection.getResponseCode();
                    int i = 0;
                } catch (Exception e) {
                    e.printStackTrace();}
            }
        });

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = mPrefs.getString("quiz", "");
        Quiz quiz = gson.fromJson(json, Quiz.class);

        Question question = quiz.getQuestion(quiz.currentQuestion );


        TextView rank1 = (TextView) findViewById(R.id.rank1);
        TextView rank2 = (TextView) findViewById(R.id.rank2);
        TextView rank3 = (TextView) findViewById(R.id.rank3);
        TextView answer = (TextView) findViewById(R.id.answer);
        TextView background = (TextView) findViewById(R.id.background);
        if (quiz.playerList.size()>0) {
            Player playerRank1 = quiz.getPlayerOnRank(0);
            Player playerRank2 = quiz.getPlayerOnRank(1);
            Player playerRank3 = quiz.getPlayerOnRank(2);

            rank1.setText("1. " + playerRank1.name + " with " + playerRank1.getScore().toString() + " Points");
            rank2.setText("2. " + playerRank2.name + " with " + playerRank2.getScore().toString() + " Points");
            rank3.setText("3. " + playerRank3.name + " with " + playerRank3.getScore().toString() + " Points");


        }

        //Todo: Farben richtig setzen

        if (question.CorrectAnswer == 1) {
            answer.setText(question.Answer1);
            background.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        }
        if (question.CorrectAnswer == 2) {
            answer.setText(question.Answer2);
            background.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
        }
        if (question.CorrectAnswer == 3) {
            answer.setText(question.Answer3);
            background.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
        }
        if (question.CorrectAnswer == 4) {
            answer.setText(question.Answer4);
            background.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
        }


    }
}
