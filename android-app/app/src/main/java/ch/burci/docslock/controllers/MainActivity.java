package ch.burci.docslock.controllers;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import ch.burci.docslock.R;
import ch.burci.docslock.models.HomeKeyLocker;
import java.util.ArrayList;
import ch.burci.docslock.models.MainModel;
import ch.burci.docslock.models.PDFModel;


public class MainActivity extends AppCompatActivity {
    // ---------------------------------------------------------------
    // Fields  -------------------------------------------------------
    // ---------------------------------------------------------------
    private ArrayList<PDFModel> listPDFs;
    private Fragment fragment;
    private MainModel mainModel;

    private HomeKeyLocker homeKeyLocker;

    Timer timer;
    MyTimerTask myTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable simple press on shutdown button
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        // Home Key Locker
        homeKeyLocker = new HomeKeyLocker();
        homeKeyLocker.lock(this);

        this.mainModel = new MainModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        //read list pdf in assets and create ArrayList of PDF
        this.listPDFs = readPDFs("pdf");
        this.mainModel.setPdfs(this.listPDFs);


        //creat container and commit de fragment
        this.fragment = new ListPDFFragment(); //first fragment open is listOfAlarm
        this.commitFragmentTransaction();
    }

    /***
     * @desc Method to read all pdf and create List of pdf
     */
    public ArrayList<PDFModel> readPDFs(String path)
    {
        ArrayList<PDFModel> result = new ArrayList<PDFModel>();
        String [] listAssets;
        try {
            listAssets = getAssets().list(path);
            if (listAssets.length > 0) {
                for (String fileName : listAssets) {
                    PDFModel pdf = new PDFModel(R.mipmap.ic_pdf,fileName);
                    result.add(pdf);
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "No pdf",Toast.LENGTH_LONG).show();
        }
        return result;
    }


    /***
     * @desc Method to commit a fragment transaction without animation
     */
    public void commitFragmentTransaction()
    {
        // Commit the fragment transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, this.fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        Log.d("MainActivity", "back pressed");
    }

    // Disable long power button press
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            bringApplicationToFront();
        }
    }

    // Bring application to front for home key locker
    private void bringApplicationToFront()
    {
        KeyguardManager myKeyManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if( myKeyManager.inKeyguardRestrictedInputMode())
            return;
        Log.d("MainActivity", "Bringging Application to Front");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        try
        {
            pendingIntent.send();
        }
        catch (PendingIntent.CanceledException e)
        {
            e.printStackTrace();
        }
    }

    protected void onPause() {
        if (timer == null) {
            myTimerTask = new MyTimerTask();
            timer = new Timer();
            timer.schedule(myTimerTask, 10, 10);
        }

        super.onPause();
        Log.d("MainActivity", "onPause");
        // On pause (recent apps button) : move task to front
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    // ---------------------------------------------------------------
    // Getter/Setter  ------------------------------------------------
    // ---------------------------------------------------------------
    public Fragment getFragment() { return this.fragment; }
    public void setFragment(Fragment f) { this.fragment = f; }
    public MainModel getMainModel(){return this.mainModel; }
}
