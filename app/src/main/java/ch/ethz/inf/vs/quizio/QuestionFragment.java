package ch.ethz.inf.vs.quizio;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

public class QuestionFragment extends Fragment {

    private static final String KEY_TTL = "qf-ttl";
    private static final String KEY_QUESTION = "question";
    private static final String KEY_ANSWER1 = "ans1";
    private static final String KEY_ANSWER2 = "ans2";
    private static final String KEY_ANSWER3 = "ans3";
    private static final String KEY_ANSWER4 = "ans4";
    private static final String KEY_CORRECT = "corrAns";

    public interface Listener { void submitAnswer(Answer ans,int correctAns, int timeRemaining); }
    public enum Answer { NONE, RED, YELLOW, GREEN, BLUE };

    private Listener listener;
    private RadioGroup answerGroup;
    private int remaining,remainingOnClick;
    int correctAnswer;
    private CountDownTimer timer;

    public QuestionFragment() { /* Required empty public constructor */ }

    public static QuestionFragment newInstance(int ttl,Question question) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TTL, ttl);
        args.putString(KEY_QUESTION,question.TheQuestion);
        args.putString(KEY_ANSWER1,question.Answer1);
        args.putString(KEY_ANSWER2,question.Answer2);
        args.putString(KEY_ANSWER3,question.Answer3);
        args.putString(KEY_ANSWER4,question.Answer4);
        args.putInt(KEY_CORRECT,question.CorrectAnswer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        TextView countdown = (TextView) view.findViewById(R.id.countdown);
        ProgressBar timeRemaining = (ProgressBar) view.findViewById(R.id.time_remaining);
        answerGroup = (RadioGroup) view.findViewById(R.id.answer_group);
        /*
        answerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                remainingOnClicked = remaining;
            }
        });
        */

        Button button = (Button) view.findViewById(R.id.commitAnswerButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.onFinish();
                timer = null;

            }
        });


            Bundle args = getArguments();
        correctAnswer = args.getInt(KEY_CORRECT);
        if (args == null) throw new RuntimeException("QuestionFragment: No args found");

        int max = 1000 * args.getInt(KEY_TTL);

        timeRemaining.setMax(max);

        timer = new CountDownTimer(max, 40) {

            public void onTick(long remainingMillis) {
                remaining = (int) remainingMillis;
                countdown.setText(String.format("%d", remaining / 1000));
                timeRemaining.setProgress(timeRemaining.getMax() - remaining);
            }

            public void onFinish() {
                countdown.setText(getString(R.string.time_up));
                timeRemaining.setProgress(timeRemaining.getMax());

                for (View rb : answerGroup.getTouchables())
                    rb.setEnabled(false);
                answerGroup.setEnabled(false);
                listener.submitAnswer(getAnswer(),correctAnswer,remaining/1000);
                /*
                listener.submitAnswer(getAnswer(),correctAnswer,remainingOnClick/1000);
                */

                cancel();
            }
        }.start();

        return view;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    public Answer getAnswer() {
        switch (answerGroup.getCheckedRadioButtonId()) {
            case R.id.redRadio:
                return Answer.RED;
            case R.id.yellowRadio:
                return Answer.YELLOW;
            case R.id.greenRadio:
                return Answer.GREEN;
            case R.id.blueRadio:
                return Answer.BLUE;
            default:
                return Answer.NONE;
        }
    }
}
