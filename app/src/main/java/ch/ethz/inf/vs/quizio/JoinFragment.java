package ch.ethz.inf.vs.quizio;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


public class JoinFragment extends Fragment {



    public interface Listener { void onJoin(String name, String code); }

    private Listener listener;
    NsdManager nsdMan;

    public JoinFragment() { /* Required empty public constructor */ }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_join, container, false);

        ((ImageButton) view.findViewById(R.id.join_button)).setOnClickListener((View v) -> {
            if (((EditText) view.findViewById(R.id.player_name)).getText().toString().trim().equals(""))
                Toast.makeText(getContext(), getString(R.string.name_missing), Toast.LENGTH_SHORT).show();
            else if (((EditText) view.findViewById(R.id.quiz_code)).getText().toString().trim().equals(""))
                Toast.makeText(getContext(), getString(R.string.code_missing), Toast.LENGTH_SHORT).show();
            else
                listener.onJoin(((EditText) view.findViewById(R.id.player_name)).getText().toString().trim(),
                        ((EditText) view.findViewById(R.id.quiz_code)).getText().toString().trim());





        });

        return view;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    public void toastedError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
