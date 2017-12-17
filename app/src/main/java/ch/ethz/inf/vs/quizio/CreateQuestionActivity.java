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
        EditText question = findViewById(R.id.editText0);
        EditText answer1 = findViewById(R.id.editText1);
        EditText answer2 = findViewById(R.id.editText2);
        EditText answer3 = findViewById(R.id.editText3);
        EditText answer4 = findViewById(R.id.editText4);
        EditText correctAnswer = findViewById(R.id.editText5);


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Intent intent = getIntent();
            int id = intent.getIntExtra("questionNr",-1);


            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            String json = mPrefs.getString("quiz", "");
            Quiz quiz = gson.fromJson(json, Quiz.class);
            Question theQuestion = quiz.getQuestion(id);

            question.setText(theQuestion.TheQuestion, TextView.BufferType.EDITABLE);
            answer1.setText(theQuestion.Answer1, TextView.BufferType.EDITABLE);
            answer2.setText(theQuestion.Answer2, TextView.BufferType.EDITABLE);
            answer3.setText(theQuestion.Answer3, TextView.BufferType.EDITABLE);
            answer4.setText(theQuestion.Answer4, TextView.BufferType.EDITABLE);
            Integer cA = theQuestion.CorrectAnswer+1;
            correctAnswer.setText(cA.toString(), TextView.BufferType.EDITABLE);
        }
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (extras != null) {
                    Integer CorAns = Integer.valueOf(correctAnswer.getText().toString());

                    Intent intent = getIntent();
                    int id = intent.getIntExtra("questionNr",-1);

                    Question question1 = new Question(question.getText().toString(), answer1.getText().toString(), answer2.getText().toString(), answer3.getText().toString(), answer4.getText().toString(), CorAns - 1);

                    Gson gson = new Gson();
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    String json = mPrefs.getString("quiz", "");
                    Quiz quiz = gson.fromJson(json, Quiz.class);
                    quiz.editQuestion(id,question1);
                    String quizUpdated = gson.toJson(quiz);
                    prefsEditor.putString("quiz", quizUpdated);
                    prefsEditor.commit();

                }

                else {
                    //TODO check all inputs and only proceed if inputs are correct
                    Integer CorAns = Integer.valueOf(correctAnswer.getText().toString());


                    Question question1 = new Question(question.getText().toString(), answer1.getText().toString(), answer2.getText().toString(), answer3.getText().toString(), answer4.getText().toString(), CorAns - 1);

                    Gson gson = new Gson();
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    String json = mPrefs.getString("quiz", "");
                    Quiz quiz = gson.fromJson(json, Quiz.class);
                    quiz.addQuestion(question1);
                    String quizUpdated = gson.toJson(quiz);
                    prefsEditor.putString("quiz", quizUpdated);
                    prefsEditor.commit();
                }

                finish();

            }
        });

    }

}
