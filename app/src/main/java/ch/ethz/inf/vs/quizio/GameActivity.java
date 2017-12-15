package ch.ethz.inf.vs.quizio;

import android.app.Service;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.InetAddress;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager;

public class GameActivity
        extends AppCompatActivity
        implements JoinFragment.Listener, QuizClient.Listener, QuestionFragment.Listener {

    private QuizClient client;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.ResolveListener mResolveListener;
    NsdManager mNsdManager;
    NsdServiceInfo mService;
    int mServerPort;
    InetAddress mServerHost;
    private final String TAG = "GameActiviy";
    private int points = 0;
    private boolean retryConnection = false;



    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        discover();



        if (client == null) {

            client = new QuizClient(this);
            client.start();

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.game_content, new JoinFragment())
                    .commit();
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        client.killClient();
    }

    // Gets called when server responded, if join was successful or not
    @Override public void onJoinResult(boolean success, String message) {

        if (success)
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.game_content, new WaitingFragment())
                    .commit();
        else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.game_content);
            if (currentFragment instanceof JoinFragment) {
                runOnUiThread(() -> ((JoinFragment) currentFragment).toastedError(message));
            }
        }

    }

    @Override  public void discover(){
        mNsdManager = (NsdManager)getSystemService(NSD_SERVICE);
        initializeDiscoveryListener();
        mNsdManager.discoverServices("_nsdchat._tcp",
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        initializeResolveListener();
    }


    @Override public void onStartQuestion(Question question) {

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.game_content, QuestionFragment.newInstance(15,question))
                .commit();
    }

    @Override public void onResult(boolean correct, int points, int rank) {
        this.points = points;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.game_content, ResultFragment.newInstance(correct, points, rank))
                .commit();
    }


    @Override public void goHome(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }



    // Gets called when user accepts quiz (in JoinFragment)
    @Override public void onJoin(String user, String code) { client.joinQuiz(mServerHost,mServerPort,user, code); }

    // Gets called when time is up (in QuestionFragment)
    @Override public void submitAnswer(QuestionFragment.Answer ans,int correctAns, int timeRemaining) {
        client.submitAnswer(ans,correctAns,timeRemaining);

    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.

                   Log.d(TAG, "Service discovery success" + service);
                   if (service.getServiceName().contains("NsdChat")) {
                       mNsdManager.resolveService(service, mResolveListener);
                   }

            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);

                //we lost connection, go to waiting screen
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.game_content, new WaitingFragment())
                        .commit();

                //try to find service again
                retryConnection = true;
                discover();
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                // mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                //we're trying this for the first time
                if(!retryConnection) {
                    mService = serviceInfo;
                    mServerPort = mService.getPort();
                    mServerHost = mService.getHost();
                //moderator lost connection and now we want to reconnect
                }else{
                   while(!client.tryReconnect(mServerHost,mServerPort)){
                       try {
                           client.sleep(2000);
                       }catch (InterruptedException e){
                            Log.d(TAG,"interrupted on trying to reconnect");
                            e.printStackTrace();
                       }
                   };
                }
            }
        };
    }
}
