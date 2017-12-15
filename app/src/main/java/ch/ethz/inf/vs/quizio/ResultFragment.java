package ch.ethz.inf.vs.quizio;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResultFragment extends Fragment {

    private static final String KEY_CORRECT = "rf-correct";
    private static final String KEY_POINTS = "rf-points";
    private static final String KEY_RANK = "rf-rank";

    public ResultFragment() { /* Required empty public constructor */ }

    public static ResultFragment newInstance(boolean correct, int points, int rank) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_CORRECT, correct);
        args.putInt(KEY_POINTS, points);
        args.putInt(KEY_RANK, rank);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        Bundle args = getArguments();
        if (args == null) throw new RuntimeException("ResultFragment: No args found");

        if (!args.getBoolean(KEY_CORRECT)) {
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            ((TextView) view.findViewById(R.id.result_string)).setText(getString(R.string.incorrect));
        }
        ((TextView) view.findViewById(R.id.points)).setText(String.format("%d",args.getInt(KEY_POINTS)));
        ((TextView) view.findViewById(R.id.rank)).setText(String.format("%d",args.getInt(KEY_RANK)));

        return view;
    }

}
