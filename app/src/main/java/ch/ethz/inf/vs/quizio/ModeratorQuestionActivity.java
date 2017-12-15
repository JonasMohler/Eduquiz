package ch.ethz.inf.vs.quizio;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;

public class ModeratorQuestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_question);




        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = mPrefs.getString("quiz", "");
        Quiz quiz = gson.fromJson(json, Quiz.class);

        Question question = quiz.getQuestion(quiz.currentQuestion);

        TextView answer1TextView = (TextView) findViewById(R.id.answer1);
        TextView answer2TextView = (TextView) findViewById(R.id.answer2);
        TextView answer3TextView = (TextView) findViewById(R.id.answer3);
        TextView answer4TextView = (TextView) findViewById(R.id.answer4);
        TextView questionTextView = (TextView) findViewById(R.id.question);

        answer1TextView.setText(question.Answer1);
        answer2TextView.setText(question.Answer2);
        answer3TextView.setText(question.Answer3);
        answer4TextView.setText(question.Answer4);

        questionTextView.setText(question.TheQuestion);


    }
}
