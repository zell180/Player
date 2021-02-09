package com.kingseiya.ilmito;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import com.kingseiya.ilmito.player.AssetUtils;
import java.io.File;
import java.io.IOException;

public class SplashActivity extends AppCompatActivity {
    private boolean standaloneMode = Versioner.getInstance().isStandaloneMode();
    private int mainVersion = Versioner.getInstance().getMainVersion();
    private int patchVersion = Versioner.getInstance().getPatchVersion();

    private ImageView container;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_animated_activity);

        container = findViewById(R.id.animation);
        container.setImageResource(R.drawable.splash_animation);
        animationDrawable = (AnimationDrawable) container.getDrawable();
        if (getPermissionAnswer() == 99) {
            AssetUtils.askForStoragePermission(SplashActivity.this);
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) {
                startResourceCopyProcess();
            } else {
                createAlertDialog();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        animationDrawable.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Code necessary to track write permission
        if (requestCode == PackageManager.PERMISSION_GRANTED && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                if (grantResults[0] == 0) {
                    setPermissionAnswer(0);
                    startResourceCopyProcess();
                } else {
                    setPermissionAnswer(-1);
                    createAlertDialog();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPermissionAnswer(int value) throws PackageManager.NameNotFoundException {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("permissionAnswer", value);
        editor.apply();
    }

    private int getPermissionAnswer() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
        return settings.getInt("permissionAnswer", 99);
    }

    private void createAlertDialog() {
        Dialog alertDialog = new Dialog(this, R.style.AppTheme_NoActionBar_FullScreen_Transparent);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.alert_dialog);
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        //alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //alertDialog.getWindow().setBackgroundDrawableResource(R.color.colorAccent);
        //alertDialog.getWindow().setLayout(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT);
        alertDialog.show();

        Log.i("App", "Write permission denied");
    }

    private void startResourceCopyProcess(){
        new ResourceOperation().execute("");
    }

    private class ResourceOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String dataDir = getApplication().getApplicationInfo().dataDir;

            // Standalone mode: Copy game in game folder to data folder and launch
            // it
            Log.i("EasyRPG", "Standalone mode : a \"game\" folder is present in asset folder");

            // Copy game in internal memory
            if (!(new File(dataDir + "/game").exists())) {
                try {
                    AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                            mainVersion, patchVersion, false);
                    setMainVersion();
                    AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                            mainVersion, patchVersion, true);
                    setPatchVersion();
                } catch (IOException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                if (mainVersion > getMainVersion()) {
                    try {
                        AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                                mainVersion, patchVersion, false);
                        AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                                mainVersion, patchVersion, true);
                        setMainVersion();
                        setPatchVersion();
                    } catch (IOException | PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (patchVersion > getPatchVersion()) {
                    try {
                        AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                                mainVersion, patchVersion, true);
                        setPatchVersion();
                    } catch (IOException | PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

        private void setMainVersion() throws PackageManager.NameNotFoundException {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("mainVersion", mainVersion);
            editor.apply();
        }

        private int getMainVersion() {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
            return settings.getInt("mainVersion", 000);
        }

        private void setPatchVersion() throws PackageManager.NameNotFoundException {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("patchVersion", patchVersion);
            editor.apply();
        }

        private int getPatchVersion() {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
            return settings.getInt("patchVersion", 000);
        }
    }
}
