package ch.burci.docslock.controllers;

import android.Manifest;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import ch.burci.docslock.Config;
import ch.burci.docslock.DeviceWithGroup;
import ch.burci.docslock.DocsLockClient;
import ch.burci.docslock.DocsLockService;
import ch.burci.docslock.R;
import ch.burci.docslock.models.HomeKeyLocker;
import ch.burci.docslock.models.MainModel;
import ch.burci.docslock.models.PDFModel;
import ch.burci.docslock.models.PrefUtils;
import ch.burci.docslock.models.StatusBarExpansionLocker;

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
        
        DocsLockService.init(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Auto unlock device when run activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_main);

        //read list pdf in assets and create ArrayList of PDF
        this.mainModel = new MainModel();

        String[] pdfsToDownload ={"https://www.w3.org/Amaya/Distribution/manuel.pdf",
                "http://cdn-10.nikon-cdn.com/pdf/manuals/dslr/D60_fr.pdf",
                "http://www.who.int/hrh/resources/WISN_FR_Software-manual.pdf?ua=1"};
        //downloadPDFs(pdfsToDownload);
        //deletePdfs();

        updatePdfsList();

        //create container and commit de currentFragment
        this.listFragment =  new ListPDFFragment(); //first currentFragment open is listOfAlarm
        this.currentFragment = this.listFragment; //first currentFragment open is listOfAlarm
        this.commitFragmentTransaction(false);

        // Check if is locked
        isLocked = PrefUtils.isLocked(this);

        // Permission
        checkDrawOverlayPermission();
        checkReadWriteFilesPermission();

        this.viewerFragment = new ViewerFragment();

        updateDeviceStatus();
    }

    private void updateDeviceStatus() {
        // Read status
        DeviceWithGroup device = PrefUtils.getLastDevice(this);

        boolean lock = device.getGroup() != null && device.getGroup().isLocked();
        setLock(lock);
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
            HomeKeyLocker.lock(this);
        else
            HomeKeyLocker.unlock();

        // Disable simple press on shutdown button
        if(locked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Disable status bar expension
        if(locked)
            StatusBarExpansionLocker.lock(this);
        else
            StatusBarExpansionLocker.unlock(this);

        // Update icon list
        updateLockIcon();

        if(locked)
            Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();

        // Send the isLocked status to server
        DocsLockService.setIsLockedDevice(locked);
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

    public void goToPdf(String pdfPath){
        this.viewerFragment.setPDFPath(pdfPath);
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

        // Hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.hide();

        //disable state on device
        DocsLockService.setStateDevice(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MainActivity-Intent", intent.toString());

        // If it's a "update" intent
        if(intent.getBooleanExtra("UPDATE", false))
            updateDeviceStatus();

        super.onNewIntent(intent);
    }

    /***
     * @desc Method to read all pdf and create List of pdf
     */
    public ArrayList<PDFModel> readPDFs() {
        ArrayList<PDFModel> result = new ArrayList<PDFModel>();
        File[] listAssets;

        // Folder to external storage /Android/data/ch.burci.docslock/files/pdf
        File pdfsFolder = getExternalFilesDir("pdf");

        // List pdf files
        listAssets = pdfsFolder.listFiles();
        if (listAssets != null && listAssets.length > 0) {
            for (File file : listAssets) {
                PDFModel pdf = new PDFModel(R.mipmap.ic_pdf, file.getName(), file.toString());
                result.add(pdf);
            }
        }
        return result;
    }

    public void deletePdfs(){
        // Folder to external storage /Android/data/ch.burci.docslock/files/pdf
        File pdfsFolder = getExternalFilesDir("pdf");
        pdfsFolder.delete();
    }

    /***
     * @desc Method to download List of pdf
     * @param pdfsUrl list of strin who contain pdf url
     */
    public void downloadPDFs(String[] pdfsUrl) {
        for(String pdfUrl:pdfsUrl) {
            try {
                String[] pdfSplit = pdfUrl.split("/");
                String name = pdfSplit[pdfSplit.length-1];

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl)); /*init a request*/
                request.setDescription("My description"); //this description apears inthe android notification
                request.setTitle("My Title");//this description apears in the android notification

                // Folder to external storage /Android/data/ch.burci.docslock/files/pdf
                request.setDestinationInExternalFilesDir(getApplicationContext(),
                        "/pdf",
                        name); //set destination
                DownloadManager manager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                //start the download and return the id of the download. this id can be use to get info of download
                final long downloadId = manager.enqueue(request);
                registerReceiver(receiverDowloadsCompleted, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    BroadcastReceiver receiverDowloadsCompleted=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.updatePdfsList();// Do Something
            //lorsque un pdf a finis de telecharger
        }
    };

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
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(addToBackStack)
            ft.setCustomAnimations(R.animator.fragment_slide_left_enter,
                    R.animator.fragment_slide_left_exit,
                    R.animator.fragment_slide_right_enter,
                    R.animator.fragment_slide_right_exit);
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
        Log.d("MainActivity::c",""+ getFragmentManager().getBackStackEntryCount());

        // If not locked, or if not the last backlstackentry : go back
        if(!this.isLocked || getFragmentManager().getBackStackEntryCount() > 0)
            super.onBackPressed();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(!hasFocus && this.isLocked) {
            // Disable long power button press if locked
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);

            // Close status bar
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            this.getApplicationContext().sendBroadcast(it);
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
        DocsLockService.setStateDevice(false);

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
