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

    private ImageView container;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_animated_activity);
        container = findViewById(R.id.animation);
        if (animationCheck()) {
            container.setImageResource(R.drawable.splash_animation);
            animationDrawable = (AnimationDrawable) container.getDrawable();
        }

        if (getPermissionAnswer() == 99 &&
                !(new File(getApplication().getApplicationInfo().dataDir + "/game").exists())) {
            AssetUtils.askForStoragePermission(SplashActivity.this);
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) {
                startResourceCopyProcess();
            } else {
                createAlertDialog();
            }
        }

    }

    private boolean animationCheck() {
        return Versioner.checkMainVersion(getApplicationContext()) ||
                Versioner.checkPatchVersion(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (animationCheck()) {
            animationDrawable.start();
        }
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
                    manageMainAsset(dataDir);
                    managePatchAsset(dataDir);
                } catch (IOException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                if (Versioner.checkMainVersion(getApplicationContext())) {
                    try {
                        manageMainAsset(dataDir);
                        managePatchAsset(dataDir);
                    } catch (IOException | PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (Versioner.checkPatchVersion(getApplicationContext())) {
                    try {
                        managePatchAsset(dataDir);
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

        private void manageMainAsset(String dataDir) throws IOException, PackageManager.NameNotFoundException {
            AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                                                                    Versioner.getMainVersion(),
                                                                    Versioner.getPatchVersion(),
                                                                false);
            Versioner.setCurrentMainVersion(getApplicationContext());
            AssetUtils.removeExpansion(getApplicationContext(),
                                        Versioner.getMainVersion(),
                                        Versioner.getPatchVersion(),
                                        false);
        }

        private void managePatchAsset(String dataDir) throws IOException, PackageManager.NameNotFoundException {
            AssetUtils.copyFolderFromExpansion(getApplicationContext(), dataDir + "/",
                                                                    Versioner.getMainVersion(),
                                                                    Versioner.getPatchVersion(),
                                                                true);
            Versioner.setCurrentPatchVersion(getApplicationContext());
            AssetUtils.removeExpansion(getApplicationContext(),
                                        Versioner.getMainVersion(),
                                        Versioner.getPatchVersion(),
                                    true);
        }
    }
}
