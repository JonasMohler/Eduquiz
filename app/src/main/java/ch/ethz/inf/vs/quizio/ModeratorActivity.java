package ch.ethz.inf.vs.quizio;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

public class ModeratorActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator);


        TextView gameCode = (TextView) findViewById(R.id.gameCode);
        TextView playersJoined = (TextView) findViewById(R.id.joinedPlayers);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = mPrefs.getString("quiz", "");
        Quiz quiz = gson.fromJson(json, Quiz.class);

        gameCode.setText(quiz.gameCode.toString());


        Handler handler = new Handler();

        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                playersJoined.setText(quiz.getAllPlayerNamesString());
                handler.postDelayed(this, 100);
            }
        };

        handler.post(runnableCode);

        final Button button = findViewById(R.id.nextbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO Call startNextQuestion in ServerService
            }
        });

    }
}
