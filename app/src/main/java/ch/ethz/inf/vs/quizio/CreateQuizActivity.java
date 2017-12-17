package ch.ethz.inf.vs.quizio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import static ch.ethz.inf.vs.quizio.Util.encode;

public class CreateQuizActivity extends AppCompatActivity {
    private static boolean quizResume = false;
    SharedPreferences mPrefs;
    SharedPreferences.Editor prefsEditor;
    Gson gson;

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        ListView listView = (ListView) findViewById(R.id.questions);
        ArrayAdapter<String> adapter =         new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, gson.fromJson(mPrefs.getString("quiz", ""), Quiz.class).getQuestionList());
        adapter.clear();
        adapter.addAll(gson.fromJson(mPrefs.getString("quiz", ""), Quiz.class).getQuestionList());
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);




    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsEditor = mPrefs.edit();
        gson = new Gson();

        if(!quizResume){
            Quiz quizInit = new Quiz();
            String quiz = gson.toJson(quizInit);
            prefsEditor.putString("quiz", quiz);
            prefsEditor.commit();
        }




        //Create Sammple Quiz
        Question question1 = new Question("Frage No. 1", "Antwort Möglichkeit 1", "Antwort Möglichkeit 2", "Antwort Möglichkeit 3", "Antwort Möglichkeit 4", 0);
        Question question2 = new Question("Frage No. 2", "Antwort Möglichkeit 1", "Antwort Möglichkeit 2", "Antwort Möglichkeit 3", "Antwort Möglichkeit 4", 1);
        Question question3 = new Question("Frage No. 3", "Antwort Möglichkeit 1", "Antwort Möglichkeit 2", "Antwort Möglichkeit 3", "Antwort Möglichkeit 4", 2);

        Gson gson = new Gson();
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = mPrefs.getString("quiz", "");
        Quiz quiz = gson.fromJson(json, Quiz.class);
        quiz.addQuestion(question1);
        quiz.addQuestion(question2);
        quiz.addQuestion(question3);
        String quizUpdated = gson.toJson(quiz);
        prefsEditor.putString("quiz", quizUpdated);
        prefsEditor.commit();

        //Finish

        startService(new Intent(getApplicationContext(), ServerService.class));


        Button addQuestion = (Button) findViewById(R.id.add);
        Button submitQuiz = (Button) findViewById(R.id.submit);
        Button resumeQuiz = (Button) findViewById(R.id.resume);
        ListView listView = (ListView) findViewById(R.id.questions);

        //TODO check if quiz is empty otherwise you get error trying to access it




        //TODO add delete old version of QUESTION
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter, View v, int position, long id){
                Integer questionNr = (int)adapter.getItemIdAtPosition(position);

                Intent intent = new Intent(CreateQuizActivity.this,CreateQuestionActivity.class);
                intent.putExtra("questionNr", questionNr);
                startActivity(intent);
            }
        });

        resumeQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setQuizResume();
            }
        });

        //TODO show question in list, have to use adapter not list.add
        TextView question = new TextView(this);
        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();

                Intent intent = new Intent(CreateQuizActivity.this,CreateQuestionActivity.class);
                startActivity(intent);

            }

        });
        //TODO check if quiz is empty
        submitQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //join game screen
                //startService(new Intent(getApplicationContext(), ServerService.class));

                Intent intent = new Intent(CreateQuizActivity.this,ModeratorActivity.class);
                startActivity(intent);


                //hide buttons and display message
                TextView header = findViewById(R.id.header);
                ListView questions = findViewById(R.id.questions);
                Button addButton = findViewById(R.id.add);
                Button submitQuiz = findViewById(R.id.submit);

                TextView createdQuiz = findViewById(R.id.QuizCreatedText);
                TextView ShowCode = findViewById(R.id.ShowCode);

                header.setVisibility(View.GONE);
                questions.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE);
                submitQuiz.setVisibility(View.GONE);

                createdQuiz.setVisibility(View.VISIBLE);
                //TODO: Find IP and print suffix to screen to tell future clients about it
                WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String code = encode(wifiMan);
                ShowCode.setText(code);
                ShowCode.setVisibility(View.VISIBLE);

            }

        });

    }

    public static boolean getQuizResume(){
        return quizResume;
    }
    public static void setQuizResume(){quizResume = true;}

}
