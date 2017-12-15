package ch.ethz.inf.vs.quizio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

public class CreateQuestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);



        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /*
                TextView question = (TextView) findViewById(R.id.textView1);
                TextView answer1 = (TextView) findViewById(R.id.textView3);
                TextView answer2 = (TextView) findViewById(R.id.textView5);
                TextView answer3 = (TextView) findViewById(R.id.textView7);
                TextView answer4 = (TextView) findViewById(R.id.textView9);
                TextView correctAns = (TextView) findViewById(R.id.textView11);
                */



                EditText question = findViewById(R.id.editText0);
                EditText answer1 = findViewById(R.id.editText1);
                EditText answer2 = findViewById(R.id.editText2);
                EditText answer3 = findViewById(R.id.editText3);
                EditText answer4 = findViewById(R.id.editText4);
                EditText correctAnswer = findViewById(R.id.editText5);
                Integer CorAns = Integer.valueOf(correctAnswer.getText().toString());



                Question question1 = new Question(question.getText().toString(), answer1.getText().toString(), answer2.getText().toString(), answer3.getText().toString(),answer4.getText().toString(),CorAns);

                Gson gson = new Gson();
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                String json = mPrefs.getString("quiz", "");
                Quiz quiz = gson.fromJson(json, Quiz.class);
                quiz.addQuestion(question1);
                String quizUpdated = gson.toJson(quiz);
                prefsEditor.putString("quiz", quizUpdated);
                prefsEditor.commit();


                finish();

            }
        });

    }

}
