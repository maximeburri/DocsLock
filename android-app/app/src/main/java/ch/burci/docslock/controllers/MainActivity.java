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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ch.burci.docslock.BuildConfig;
import ch.burci.docslock.Config;
import ch.burci.docslock.DeviceWithGroup;
import ch.burci.docslock.DocsLockService;
import ch.burci.docslock.Document;
import ch.burci.docslock.R;
import ch.burci.docslock.models.HomeKeyLocker;
import ch.burci.docslock.models.MainModel;
import ch.burci.docslock.models.PDFModel;
import ch.burci.docslock.models.PrefUtils;
import ch.burci.docslock.models.StatusBarExpansionLocker;
//import ch.burci.docslock.providers.GenericFileProvider;

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

        PrefUtils.setLock(false, this);
        PrefUtils.setLastDevice(null, this);

        ArrayList<String> pdfsToDownload = new ArrayList<String>();
        pdfsToDownload.add("https://www.w3.org/Amaya/Distribution/manuel.pdf");
        pdfsToDownload.add("http://cdn-10.nikon-cdn.com/pdf/manuals/dslr/D60_fr.pdf");
        pdfsToDownload.add("http://www.who.int/hrh/resources/WISN_FR_Software-manual.pdf?ua=1");

        ArrayList<String> pdfsToDelete = new ArrayList<String>();
        pdfsToDelete.add("manuel.pdf");
        pdfsToDelete.add("D60_fr.pdf");

        //downloadPDFs(pdfsToDownload);
        //deletePdfs(null);
        //deletePdfs(pdfsToDelete);

        updatePdfsList();

        //create container and commit de currentFragment
        this.listFragment =  new ListPDFFragment(); //first currentFragment open is listOfPDF
        this.currentFragment = this.listFragment; //first currentFragment open is listOfPDF
        this.commitFragmentTransaction(false);

        // Check if is locked
        isLocked = PrefUtils.isLocked(this);

        // Permission
        checkDrawOverlayPermission();
        checkReadWriteFilesPermission();

        this.viewerFragment = new ViewerFragment();

        updateDeviceStatus(null);

        DocsLockService.setStateDevice(true);
    }

    private void updateDeviceStatus(DeviceWithGroup newDevice) {
        // Read status
        DeviceWithGroup oldDevice = PrefUtils.getLastDevice(this);
        boolean deviceNull = false;

        if(newDevice == null) {
            newDevice = oldDevice;
            deviceNull = true;
        }

        Log.d(MainActivity.class.toString(),"Update device");
        if(newDevice != null) {
            boolean lock = newDevice.getGroup() != null && newDevice.getGroup().isLocked();

            // Change lock ?
            if(lock != isLocked)
                setLock(lock);
        }


        // !! Check :
        // - oldDocuments are not null (if new installation)
        // - old group can be null -> add all documents
        // - new group can be null -> remove all documents


        ArrayList<Document> oldDocuments = new ArrayList<>();
        ArrayList<Document> newDocuments = new ArrayList<>();
        ArrayList<String> documentsToDeleteStr = new ArrayList<>();
        ArrayList<String> documentsToAddStr = new ArrayList<>();
        ArrayList<String> documentsToAddNamesStr = new ArrayList<>();

        // Check newGroup = null
        if(newDevice != null && newDevice.getGroup() != null)
            newDocuments = newDevice.getGroup().getDocuments();
        else
            deletePdfs(null);  // Remove all

        // Check oldGroup = null
        if(oldDevice != null && oldDevice.getGroup() != null)
            oldDocuments = oldDevice.getGroup().getDocuments();

        // Get document to delete (oldDocuments notIn newDocuments)
        for(Document document: oldDocuments )
            if(!newDocuments.contains(document)) // TODO: Or newDocument.update > document.update
                documentsToDeleteStr.add(document.getFilename());

        // Get document to add (newDocuments notIn oldDocuments
        for(Document document: newDocuments )
            if(!oldDocuments.contains(document)) { // TODO: Or oldDocument.update < document.update
                documentsToAddStr.add(document.getDownloadLink());
                documentsToAddNamesStr.add(document.getFilename());
            }

        deletePdfs(documentsToDeleteStr);
        downloadPDFs(documentsToAddStr, documentsToAddNamesStr);

        if(newDevice != null)
            PrefUtils.setLastDevice(newDevice, this);
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
    }


    @Override
    protected void onStart() {
        super.onStart();
        //disable state on device
        if(!isLocked){
            DocsLockService.setStateDevice(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isLocked){
            DocsLockService.setStateDevice(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MainActivity-Intent", intent.toString());

        // If it's a "update" intent
        if(intent.getBooleanExtra("UPDATE", false)) {
            String json = intent.getStringExtra("device");
            DeviceWithGroup newDevice = DeviceWithGroup.fromJSON(json);
            updateDeviceStatus(newDevice);
        }

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

    public void deletePdfs(ArrayList<String> pdfList){
        // Folder to external storage /Android/data/ch.burci.docslock/files/pdf
        File pdfsFolder = getExternalFilesDir("pdf");

        if (pdfsFolder.isDirectory()) {
            for (File c : pdfsFolder.listFiles()) {
                if(pdfList==null || pdfList.contains (c.getName())){
                    c.delete();
                }
            }
        }

        updateListFragment();
    }

    /***
     * @desc Method to download List of pdf
     * @param pdfsUrl list of strin who contain pdf url
     */
    public void downloadPDFs(ArrayList<String> pdfsUrl, ArrayList<String> filesNames) {
        // Folder to external storage /Android/data/ch.burci.docslock/files/pdf
        File pdfsFolder = getExternalFilesDir("pdf");
        int i = 0;

        for(String pdfUrl:pdfsUrl){
            boolean download = true;
            String name = filesNames.get(i);
            int endString = name.indexOf(".pdf")+4; //get end of name .pdf

            //create check if pdf exist
            for (File c : pdfsFolder.listFiles()) {
                //check if pdf doesn't exist
                if(name.equals(c.getName())){
                    download = false;
                }
            }

            //if pdf doesn't exist
            if(download){

                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl)); /*init a request*/
                    request.setDescription("Document DocksLock downloading"); //this description apears inthe android notification
                    request.setTitle("Document");//this description apears in the android notification
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
            i+=1;
        }
    }

    BroadcastReceiver receiverDowloadsCompleted=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            updateListFragment();
        }
    };

    public void updateListFragment(){
        MainActivity.this.updatePdfsList();// Do Something
        //create container and commit de currentFragment
        MainActivity.this.listFragment =  new ListPDFFragment(); //first currentFragment open is listOfPDF
        MainActivity.this.currentFragment = MainActivity.this.listFragment; //first currentFragment open is listOfPDF
        MainActivity.this.commitFragmentTransaction(false);
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

        switch (item.getItemId()) {
            case R.id.action_lock_unlock:
                clickedLockUnlockItem();
            break;
            case R.id.action_update:
                clickedUpdateApp();
            break;
        }

        return super.onOptionsItemSelected(item);
    }


    class DownloadInstallAPK extends AsyncTask<Void,Void,Void>
    {

        protected Void doInBackground(Void... params) {
            try {
                final Context context = MainActivity.this;
                URL url = new URL(Config.APK_SERVER_URL);

                HttpURLConnection c = (HttpURLConnection) url.openConnection();

                /*c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();*/


                String PATH = Environment.getExternalStorageDirectory() + "/Download/";
                File file = new File(PATH);
                file.mkdirs();

                File outputFile = new File(file, "app-debug.apk");

                if (outputFile.exists()) {
                    outputFile.delete();
                }

                // New file
                File newAPKFile = new File(context.getExternalFilesDir(null), "app-debug.apk");
                FileOutputStream fos = new FileOutputStream(newAPKFile);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fos = context.openFileOutput(newAPKFile.getName(), context.MODE_PRIVATE);
                } else {
                    fos = context.openFileOutput(newAPKFile.getName(), context.MODE_WORLD_READABLE | context.MODE_WORLD_WRITEABLE);
                }
// Download the new APK file
                InputStream is = c.getInputStream();
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.flush();
                fos.close();
                is.close();
// Start the standard installation window

                /*File fileLocation = new File(context.getExternalFilesDir(null), "app-debug.apk");
                Intent downloadIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = GenericFileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", fileLocation);
                    downloadIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    downloadIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    downloadIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    //downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //downloadIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                } else {
                    downloadIntent = new Intent(Intent.ACTION_VIEW);
                    downloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    downloadIntent.setDataAndType(Uri.fromFile(fileLocation), "application/vnd.android.package-archive");
                }
                context.startActivity(downloadIntent);*/

                File toInstall = new File(context.getExternalFilesDir(null), "app-debug.apk");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(apkUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                } else {
                    Uri apkUri = Uri.fromFile(toInstall);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Log.e("", e.getMessage());

            }
            return null;
        }
    }

    public void clickedUpdateApp(){
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        String fileName = "docslock.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        File file = new File(destination);
        if (file.exists())
            file.delete();

        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(Config.APK_SERVER_URL));
        request.setDestinationUri(uri);
        dm.enqueue(request);

        final String finalDestination = destination;
        final BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri contentUri = FileProvider.getUriForFile(ctxt, BuildConfig.APPLICATION_ID + ".provider", new File(finalDestination));
                    Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                    openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    openFileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    openFileIntent.setData(contentUri);
                    startActivity(openFileIntent);
                    unregisterReceiver(this);
                    finish();
                } else {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(uri,
                            "application/vnd.android.package-archive");
                    startActivity(install);
                    unregisterReceiver(this);
                    finish();
                }
            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void clickedLockUnlockItem(){

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
