package ch.ethz.inf.vs.quizio;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;


public class MainActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        Log.d("IP", Util.ipFormat(wm.getConnectionInfo().getIpAddress()));
        Log.d("Code", Util.encode(wm));

    }

    public void onClickCreate(View view) {
        //changed from ModeratorActivity
        final Intent intent = new Intent(this, CreateQuizActivity.class);
        startActivity(intent);
    }

    public void onClickJoin(View view) {
        final Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}
