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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);


        Quiz quizInit = new Quiz();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String quiz = gson.toJson(quizInit);
        prefsEditor.putString("quiz", quiz);
        prefsEditor.commit();

        Button addQuestion = (Button) findViewById(R.id.add);
        Button submitQuiz = (Button) findViewById(R.id.submit);
        Button resumeQuiz = (Button) findViewById(R.id.resume);

        ListView listView = (ListView) findViewById(R.id.questions);

        //TODO check if quiz is empty otherwise you get error trying to access it
        ArrayAdapter<Question> adapter = new ArrayAdapter<Question>(this, android.R.layout.activity_list_item, gson.fromJson(mPrefs.getString("quiz", ""), Quiz.class).questionList);
        listView.setAdapter(adapter);

        //TODO add delete old version of QUESTION
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter, View v, int position, long id){
                int questionNr = (int)adapter.getItemIdAtPosition(position);

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
                question.setText(Integer.toString(gson.fromJson(mPrefs.getString("quiz", ""), Quiz.class).questionList.size()));
                listView.addFooterView(question);

            }

        });
        //TODO check if quiz is empty
        submitQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //join game screen
                startService(new Intent(getApplicationContext(), ServerService.class));
                //TODO create JoinScreenActivity (look at proposal) and make this intent to go there
                Intent intent = new Intent(CreateQuizActivity.this,CreateQuestionActivity.class);
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
