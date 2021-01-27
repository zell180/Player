package com.kingseiya.ilmito;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.kingseiya.ilmito.player.AssetUtils;
import java.io.File;
import java.io.IOException;

public class SplashActivity extends AppCompatActivity {
    private boolean standaloneMode = Versioner.getInstance().isStandaloneMode();
    private int mainVersion = Versioner.getInstance().getMainVersion();
    private int patchVersion = Versioner.getInstance().getPatchVersion();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen_activity);
        startResourceCopyProcess();
    }

    private void startResourceCopyProcess(){
        new ResourceOperation().execute("");
    }

    private class ResourceOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            AssetUtils.askForStoragePermission(SplashActivity.this);
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
                    setPatchVersion();
                } catch (IOException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                if (mainVersion > getMainVersion()) {
                    try {
                        AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                                mainVersion, patchVersion, false);
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
            return settings.getInt("mainVersion", 100);
        }

        private void setPatchVersion() throws PackageManager.NameNotFoundException {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("patchVersion", mainVersion);
            editor.apply();
        }

        private int getPatchVersion() {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("sharedPref", 0);
            return settings.getInt("patchVersion", 100);
        }
    }

}
