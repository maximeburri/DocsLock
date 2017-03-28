package ch.burci.docslock.controllers;

import android.Manifest;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import ch.burci.docslock.R;
import ch.burci.docslock.models.HomeKeyLocker;
import java.util.ArrayList;
import ch.burci.docslock.models.MainModel;
import ch.burci.docslock.models.PDFModel;
import ch.burci.docslock.models.PrefUtils;

import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;


public class MainActivity extends AppCompatActivity {

    // ---------------------------------------------------------------
    // Fields  -------------------------------------------------------
    // ---------------------------------------------------------------
    private ArrayList<PDFModel> listPDFs;
    private Fragment currentFragment;
    private ListPDFFragment listFragment;
    private ViewerFragment viewerFragment;
    private MainModel mainModel;

    private HomeKeyLocker homeKeyLocker;

    Timer timer;
    TaskCheckApplicationInFront myTimerTask;

    private boolean isLocked;
    private MenuItem menuItemLockUnlock;
    private Menu menu;

    public final static int REQUEST_CODE_PERMISSION_OVERLAY = 1;
    private static final int REQUEST_CODE_PERMISSION_READ = 2;
    private static final int REQUEST_CODE_PERMISSION_WRITE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable simple press on shutdown button
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        //read list pdf in assets and create ArrayList of PDF
        this.mainModel = new MainModel();
        updatePdfsList();

        //create container and commit de currentFragment
        this.listFragment =  new ListPDFFragment(); //first currentFragment open is listOfAlarm
        this.currentFragment = this.listFragment; //first currentFragment open is listOfAlarm
        this.commitFragmentTransaction(false);

        // Home Key Locker
        homeKeyLocker = new HomeKeyLocker();
        isLocked = PrefUtils.isLocked(this);

        // Permission
        checkDrawOverlayPermission();
        checkReadWriteFilesPermission();

        this.viewerFragment = new ViewerFragment();
    }

    private void updatePdfsList() {
        this.listPDFs = readPDFs();
        this.mainModel.setPdfs(this.listPDFs);
    }

    protected void setLock(boolean locked){
        this.isLocked = locked;

        // Save in preference (for service and receiver)
        PrefUtils.setLock(locked, this);

        // Lock/unlock home key screen
        if(locked)
            homeKeyLocker.lock(this);
        else
            homeKeyLocker.unlock();

        updateLockIcon();
    }

    public void updateLockIcon(){
        // Set icon
        if(this.menuItemLockUnlock != null){
            Drawable icon;
            if(this.isLocked)
                icon = ContextCompat.getDrawable(this, R.mipmap.ic_docs_locked);
            else
                icon = ContextCompat.getDrawable(this, R.mipmap.ic_docs_unlocked);

            this.menuItemLockUnlock.setIcon(icon);
        }
    }

    public void goToPdf(PDFModel pdf){
        this.viewerFragment.setPDFName(pdf.getPdfName());
        this.currentFragment = this.viewerFragment;
        this.commitFragmentTransaction();
    }

    public boolean checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_PERMISSION_OVERLAY);
            return false;
        }
        return true;
    }

    public void checkReadWriteFilesPermission(){
        // Check read
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            Log.d("MainActivity", "checkSelfPermission READ false");
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_READ);
        }

        // Check write
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            Log.d("MainActivity", "checkSelfPermission WRITE false");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_WRITE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        // Check overlay permission to fix #18
        if (requestCode == REQUEST_CODE_PERMISSION_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Log.d("MainActivity", "permission was granted");
            }
        }
        // Update PDF list if write/read permission is granted
        if (requestCode == REQUEST_CODE_PERMISSION_WRITE ||
                requestCode == REQUEST_CODE_PERMISSION_READ)
            updatePdfsList();

    }

    protected void switchLock(){
        this.setLock(!isLocked);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /***
     * @desc Method to read all pdf and create List of pdf
     */
    public ArrayList<PDFModel> readPDFs() {
        ArrayList<PDFModel> result = new ArrayList<PDFModel>();
        File[] listAssets;

        // Folder to external storage DocsLock
        File pdfsFolder = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath(),
                PrefUtils.getFilesFolderName()
        );

        // Create folder
        if (!pdfsFolder.mkdirs()) {
            Log.e("Pdfs", "Directory not created");
        }

        // List pdf files
        listAssets = pdfsFolder.listFiles();
        if (listAssets != null && listAssets.length > 0) {
            for (File file : listAssets) {
                PDFModel pdf = new PDFModel(R.mipmap.ic_pdf, file.getName());
                result.add(pdf);
            }
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Get menu and itemLock
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lock_menu, menu);
        this.menu = menu;
        this.menuItemLockUnlock = menu.getItem(0);

        // Update item lock icon
        updateLockIcon();
        return true;
    }

    // Need to hide keyboard because HomeKeyLocker.lock() doesn't do that
    private void hideKeyboard(EditText editText){
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item == menuItemLockUnlock) {

            LayoutInflater li = this.getLayoutInflater();
            View promptsView = li.inflate(R.layout.password_prompts, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            alertDialogBuilder.setView(promptsView);
            final EditText editPassword = (EditText) promptsView
                    .findViewById(R.id.editPassword);
            final TextView textBadPassword = (TextView) promptsView
                    .findViewById(R.id.textBadPassword);
            textBadPassword.setVisibility(View.INVISIBLE);

            alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", null /*set after*/)
                .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // Need to hide keyboard because HomeKeyLocker.lock() doesn't do that
                            hideKeyboard(editPassword);
                            dialog.cancel();
                        }
                    });

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    // Need to show keyboard because HomeKeyLocker.lock() hide
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editPassword, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            // Set type alert dialog because HomeKeyLocker.lock() hide keyboard
            alertDialog.getWindow().setType(TYPE_SYSTEM_ERROR);

            alertDialog.show();

            // Ok Button
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String password = editPassword.getText().toString();
                    boolean closeDialog = false;

                    // Already locked
                    if(isLocked){
                        // Check if good password
                        if(PrefUtils.checkPassword(password, getApplicationContext())) {
                            switchLock();
                            Toast.makeText(MainActivity.this,
                                    "Application unlocked", Toast.LENGTH_LONG);
                            closeDialog = true;
                        }
                        // Bad password
                        else{
                            editPassword.setText("");
                            textBadPassword.setVisibility(View.VISIBLE);
                        }
                    }
                    // Not locked
                    else{
                        // Save good password
                        PrefUtils.setPassword(password, getApplicationContext());
                        switchLock();
                        Toast.makeText(MainActivity.this,
                                "Application locked", Toast.LENGTH_LONG);
                        closeDialog = true;
                    }

                    if(closeDialog) {
                        // Need to hide keyboard because HomeKeyLocker.lock() doesn't do that
                        InputMethodManager imm = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editPassword.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);

                        alertDialog.cancel();
                    }
                }
            });

        }
        return super.onOptionsItemSelected(item);
    }

    /***
     * @desc Method to commit a currentFragment transaction without animation
     */
    public void commitFragmentTransaction(boolean addToBackStack){
        // Commit the currentFragment transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, this.currentFragment);
        if(addToBackStack)
            ft.addToBackStack(currentFragment.getClass().getName());
        ft.commit();
    }

    public void commitFragmentTransaction(){
        commitFragmentTransaction(true);
    }

    @Override
    public void onBackPressed() {
        Log.d("MainActivity", "back pressed");
        Log.d("MainActivity::c",""+ getSupportFragmentManager().getBackStackEntryCount());

        // If not locked, or if not the last backlstackentry : go back
        if(!this.isLocked || getSupportFragmentManager().getBackStackEntryCount() > 0)
            super.onBackPressed();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Disable long power button press if locked
        if(!hasFocus && this.isLocked) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    class TaskCheckApplicationInFront extends TimerTask {
        @Override
        public void run() {
            if(isLocked)
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
        if (timer == null && this.isLocked) {
            myTimerTask = new TaskCheckApplicationInFront();
            timer = new Timer();
            timer.schedule(myTimerTask, 10, 10);
        }

        super.onPause();
        Log.d("MainActivity", "onPause");

        // On pause (recent apps button) : move task to front
        if(this.isLocked) {
            ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.moveTaskToFront(getTaskId(), 0);
        }
    }

    // ---------------------------------------------------------------
    // Getter/Setter  ------------------------------------------------
    // ---------------------------------------------------------------
    public Fragment getCurrentFragment() { return this.currentFragment; }
    public void setCurrentFragment(Fragment f) { this.currentFragment = f; }
    public MainModel getMainModel(){return this.mainModel; }
}
