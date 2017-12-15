package ch.ethz.inf.vs.quizio;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ModeratorResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_result);


        //TODO: Current Question laden

        Question question = null;
        Quiz quiz = null;

        TextView rank1 = (TextView) findViewById(R.id.rank1);
        TextView rank2 = (TextView) findViewById(R.id.rank2);
        TextView rank3 = (TextView) findViewById(R.id.rank3);
        TextView answer = (TextView) findViewById(R.id.answer);
        TextView background = (TextView) findViewById(R.id.background);

        Player playerRank1 = quiz.getPlayerOnRank(1);
        Player playerRank2 = quiz.getPlayerOnRank(2);
        Player playerRank3 = quiz.getPlayerOnRank(3);

        rank1.setText("1. " + playerRank1.name + " with " + playerRank1.getScore().toString() + " Points");
        rank2.setText("2. " + playerRank2.name + " with " + playerRank2.getScore().toString() + " Points");
        rank3.setText("3. " + playerRank3.name + " with " + playerRank3.getScore().toString() + " Points");


        //Todo: Farben richtig setzen

        if (question.CorrectAnswer == 1) {
            answer.setText(question.Answer1);
            background.setBackgroundColor(1);
        }
        if (question.CorrectAnswer == 2) {
            answer.setText(question.Answer2);
            background.setBackgroundColor(1);
        }
        if (question.CorrectAnswer == 3) {
            answer.setText(question.Answer3);
            background.setBackgroundColor(1);
        }
        if (question.CorrectAnswer == 4) {
            answer.setText(question.Answer4);
            background.setBackgroundColor(1);
        }


    }
}
