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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        PDFModel pdf_1 = new PDFModel(R.mipmap.ic_pdf,"icon pdf");
        PDFModel pdf_2 = new PDFModel(R.mipmap.ic_pdf,"icon_launch");
        PDFModel pdf_3 = new PDFModel(R.mipmap.ic_pdf,"ergegeg pdf");
        PDFModel pdf_4 = new PDFModel(R.mipmap.ic_pdf,"ifegtgrth_launch");
        PDFModel pdf_5 = new PDFModel(R.mipmap.ic_pdf,"iconetherhe ger");
        PDFModel pdf_6= new PDFModel(R.mipmap.ic_pdf,"iconeherherh_launch");
        PDFModel pdf_7 = new PDFModel(R.mipmap.ic_pdf,"icoergheh  n pdf");
        PDFModel pdf_8 = new PDFModel(R.mipmap.ic_pdf,"icon_lerghe erh er haunch");
        this.mainModel = new MainModel();
        ArrayList<PDFModel> listPDF = new ArrayList<PDFModel>();
        listPDF.add(pdf_1);
        listPDF.add(pdf_2);
        listPDF.add(pdf_3);
        listPDF.add(pdf_4);
        listPDF.add(pdf_5);
        listPDF.add(pdf_6);
        listPDF.add(pdf_7);
        listPDF.add(pdf_8);
        this.mainModel.setPdfs(listPDF);

        //creat container and commit de fragment
        this.fragment = new ListPDFFragment(); //first fragment open is listOfAlarm
        this.commitFragmentTransaction();
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
