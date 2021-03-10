package com.kingseiya.ilmito;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.kingseiya.ilmito.player.AssetUtils;

import java.io.File;
import java.io.IOException;

public class SplashActivity extends AppCompatActivity {

    private ImageView container;
    private TextView install_textview;
    private AnimationDrawable animationDrawable;
    private Handler progressHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_animated_activity);
        container = findViewById(R.id.animation);
        install_textview = (TextView) findViewById(R.id.progress_text);
        Shader gradient_msg_shader = new LinearGradient(
                0, 0, 0, 100, Color.parseColor("#f3dfc2"), Color.parseColor("#734d22"), Shader.TileMode.REPEAT );
        install_textview.getPaint().setShader(gradient_msg_shader);
        progressHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == 1) {
                    ((TextView) findViewById(R.id.progress_text)).setVisibility(View.VISIBLE);
                }

                if (!(boolean) msg.obj) {
                    ((TextView) findViewById(R.id.progress_text)).setText(getResources().getString(R.string.install_audio, msg.arg1, msg.arg2));
                } else {
                    ((TextView) findViewById(R.id.progress_text)).setText(getResources().getString(R.string.install_game_file, msg.arg1, msg.arg2));
                }

                if (msg.arg1 == msg.arg2) {
                    ((TextView) findViewById(R.id.progress_text)).setVisibility(View.INVISIBLE);
                }
                super.handleMessage(msg);
            }
        };
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
        Thread thread = new Thread() {
            @Override
            public void run() {
                new ResourceOperation(progressHandler).run();
            }
        };
        thread.start();
    }

    private class ResourceOperation implements Runnable {

        private final Handler progressHandler;

        public ResourceOperation(Handler progressHandler) {
            this.progressHandler = progressHandler;
        }

        @Override
        public void run() {
            String dataDir = getApplication().getApplicationInfo().dataDir;

            // Standalone mode: Copy game in game folder to data folder and launch
            // it
            Log.i("EasyRPG", "Standalone mode : a \"game\" folder is present in asset folder");

            // Copy game in internal memory
            if (!(new File(dataDir + "/game").exists())) {
                try {
                    manageMainAsset(dataDir, install_textview);
                    managePatchAsset(dataDir, install_textview);
                } catch (IOException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                if (Versioner.checkMainVersion(getApplicationContext())) {
                    try {
                        manageMainAsset(dataDir, install_textview);
                        managePatchAsset(dataDir, install_textview);
                    } catch (IOException | PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (Versioner.checkPatchVersion(getApplicationContext())) {
                    try {
                        managePatchAsset(dataDir, install_textview);
                    } catch (IOException | PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        private void manageMainAsset(String dataDir, TextView install_textview) throws IOException, PackageManager.NameNotFoundException {
            AssetUtils.copyFolderFromExpansion(SplashActivity.this, dataDir + "/",
                                                                    progressHandler,
                                                                    Versioner.getMainVersion(),
                                                                    Versioner.getPatchVersion(),
                                                                false);
            Versioner.setCurrentMainVersion(SplashActivity.this);
            AssetUtils.removeExpansion(SplashActivity.this,
                                        Versioner.getMainVersion(),
                                        Versioner.getPatchVersion(),
                                        false);
        }

        private void managePatchAsset(String dataDir, TextView install_textview) throws IOException, PackageManager.NameNotFoundException {
            AssetUtils.copyFolderFromExpansion(SplashActivity.this, dataDir + "/",
                                                                    progressHandler,
                                                                    Versioner.getMainVersion(),
                                                                    Versioner.getPatchVersion(),
                                                                true);
            Versioner.setCurrentPatchVersion(SplashActivity.this);
            AssetUtils.removeExpansion(SplashActivity.this,
                                        Versioner.getMainVersion(),
                                        Versioner.getPatchVersion(),
                                    true);
        }
    }
}
