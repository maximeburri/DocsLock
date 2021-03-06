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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
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
import java.util.ArrayList;
import java.util.Arrays;
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
import ch.burci.docslock.services.WebSocketService;

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
    private MenuItem menuItemConnected;
    private MenuItem menuItemInstall;
    private MenuItem menuItemSetServer;
    private boolean lastConnectedStatus = false;
    private Menu menu;

    public final static int REQUEST_CODE_PERMISSION_OVERLAY = 1;
    private static final int REQUEST_CODE_PERMISSION_READ = 2;
    private static final int REQUEST_CODE_PERMISSION_WRITE = 3;

    private WebSocketService mWebSocketService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            Log.d("ServiceConnection", "Connected");
            Log.d("ServiceConnection", ""+ mWebSocketService.isConnected());
            if(mWebSocketService != null)
                updateConnectedIcon(mWebSocketService.isConnected());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mWebSocketService = null;
        }
    };;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == WebSocketService.ACTION_NAME) {
                boolean connected = intent.getBooleanExtra("connected", false);
                Log.d("WebSocketStatusReceiver", connected + "");
                updateConnectedIcon(connected);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DocsLockService.init(this, new DocsLockService.OnInitFinish() {
            @Override
            public void onInitFinish(int error, Exception e) {
                DocsLockService.setStateDevice(true);
                startWebSocketService();
            }
        });

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

        if(!checkProcessIntentUdpate(getIntent()))
            updateDeviceStatus(null);

    }

    private void startWebSocketService(){
        WebSocketService webSocketService = new WebSocketService();
        Intent serviceIntent = new Intent(this, webSocketService.getClass());

        if (!isServiceRunning(webSocketService.getClass())) {
            startService(serviceIntent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName()) && service.started) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
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
        if(this.listFragment != null)
            this.listFragment.update();
        Log.d(this.getClass().toString(), "List pdfs udpated : " + Arrays.toString(this.listPDFs.toArray()));
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

        updateEnablementItems();

        // Send the isLocked status to server
        DocsLockService.setIsLockedDevice(locked);
    }

    public void updateEnablementItems() {
        // Disable/enable list items
        if(menuItemSetServer != null)
            menuItemSetServer.setEnabled(!isLocked);
        if(menuItemInstall != null)
            menuItemInstall.setEnabled(!isLocked);
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

    public void updateConnectedIcon(boolean connected){
        // Set icon
        Log.d("updateConnectedIcon", connected+"");

        lastConnectedStatus = connected;
        if(this.menuItemConnected != null){
            Drawable icon;
            if(connected)
                icon = ContextCompat.getDrawable(this, R.mipmap.ic_connected);
            else
                icon = ContextCompat.getDrawable(this, R.mipmap.ic_not_connected);

            this.menuItemConnected.setIcon(icon);
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

        // Update list pdf
        updatePdfsList();
    }


    @Override
    protected void onStart() {
        super.onStart();
        //disable state on device
        if(!isLocked){
            DocsLockService.setStateDevice(true);
        }

        // Bind service
        Intent intent = new Intent(MainActivity.this, WebSocketService.class);

        bindService(intent, mConnection, 0);

        // Register websocket status provider
        IntentFilter i = new IntentFilter();
        i.addAction(WebSocketService.ACTION_NAME);
        registerReceiver(mReceiver, i);

        registerReceiver(receiverDowloadsCompleted, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isLocked){
            DocsLockService.setStateDevice(false);
        }
        unbindService(mConnection);
        unregisterReceiver(mReceiver);

        unregisterReceiver(receiverDowloadsCompleted);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MainActivity-Intent", intent.toString());

        checkProcessIntentUdpate(intent);

        super.onNewIntent(intent);
    }

    // Check and process if it's an update intent
    protected boolean checkProcessIntentUdpate(Intent intent){
        // If it's a "update" intent
        if(intent.getBooleanExtra("UPDATE", false)) {
            String json = intent.getStringExtra("device");
            DeviceWithGroup newDevice = DeviceWithGroup.fromJSON(json);
            updateDeviceStatus(newDevice);
            return true;
        }
        return false;
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
        Log.d(this.getClass().toString(), "Download pdfs list");
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
        this.menuItemConnected = menu.findItem(R.id.action_is_connected);
        this.menuItemLockUnlock = menu.findItem(R.id.action_lock_unlock);
        this.menuItemInstall = menu.findItem(R.id.action_install);
        this.menuItemSetServer = menu.findItem(R.id.action_set_server);

        updateEnablementItems();

        // Update item lock icon
        updateLockIcon();
        updateConnectedIcon(lastConnectedStatus);

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
            case R.id.action_install:
                clickedUpdateApp();
            break;
            case R.id.action_set_server:
                openDialogSetServerIp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDialogSetServerIp() {
        LayoutInflater li = this.getLayoutInflater();
        View promptsView = li.inflate(R.layout.server_prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);
        alertDialogBuilder.setView(promptsView);
        final EditText editServer = (EditText) promptsView
                .findViewById(R.id.editServer);
        editServer.setText(PrefUtils.getServerURL(MainActivity.this));

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", null /*set after*/)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // Need to hide keyboard because HomeKeyLocker.lock() doesn't do that
                                hideKeyboard(editServer);
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
                imm.showSoftInput(editServer, InputMethodManager.SHOW_IMPLICIT);
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
                    String newServerUrl = editServer.getText().toString();

                    // Reset prefutil
                    PrefUtils.setDeviceId(null, MainActivity.this);
                    PrefUtils.setServerURL(newServerUrl, MainActivity.this);

                    // Set icon
                    updateConnectedIcon(false);

                    // Stop websocket
                    if(mWebSocketService != null)
                        mWebSocketService.stop();

                    // Reset service
                    DocsLockService.resetClient();
                    DocsLockService.init(MainActivity.this, new DocsLockService.OnInitFinish() {
                        @Override
                        public void onInitFinish(int error, Exception e) {
                            if(error == DocsLockService.OnInitFinish.SUCCESS) {
                                DocsLockService.setStateDevice(true);
                                if(mWebSocketService != null)
                                    mWebSocketService.start();
                            }else
                                Log.d("MainAcitivity", "Cannot finish init");
                        }
                    });

                    // Close dialog
                    // Need to hide keyboard because HomeKeyLocker.lock() doesn't do that
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editServer.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    alertDialog.cancel();

                }
            });
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
                Uri.parse(Config.getApkServerUrl(this)));
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

        Log.d("MainActivity", "onPause");

        // On pause (recent apps button) : move task to front
        if(this.isLocked) {
            ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.moveTaskToFront(getTaskId(), 0);
        }

        super.onPause();
    }

    // ---------------------------------------------------------------
    // Getter/Setter  ------------------------------------------------
    // ---------------------------------------------------------------
    public Fragment getCurrentFragment() { return this.currentFragment; }
    public void setCurrentFragment(Fragment f) { this.currentFragment = f; }
    public MainModel getMainModel(){return this.mainModel; }
}
