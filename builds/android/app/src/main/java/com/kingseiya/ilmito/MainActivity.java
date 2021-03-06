package com.kingseiya.ilmito;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.kingseiya.ilmito.game_browser.GameBrowserActivity;
import com.kingseiya.ilmito.game_browser.GameBrowserHelper;
import com.kingseiya.ilmito.game_browser.GameInformation;
import com.kingseiya.ilmito.player.AssetUtils;
import com.kingseiya.ilmito.settings.SettingsManager;

import java.io.File;
import java.io.IOException;

/**
 * The activity called at launch.
 * Prepare data, launch the standalone mode or the proper gamebrowser (depending on api's version)
 * To start the standalone mode : put your project in assets/games
 * ("game" is the project directory, no sub folder)
 */
public class MainActivity extends Activity {
    private boolean standaloneMode = Versioner.getInstance().isStandaloneMode();
    private int mainVersion = Versioner.getInstance().getMainVersion();
    private int patchVersion = Versioner.getInstance().getPatchVersion();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //prepareData();

        // if the app is called in a game folder : start the game
        try {
            startGameStandaloneWithExpansion();
        } catch (PackageManager.NameNotFoundException | IOException e) {
            e.printStackTrace();
        }

        // else : launch the gamebrowser activity
        if (!standaloneMode) {
            launchProperBrowser();
        }
    }

    /**
     * Copies required runtime data from assets folder to data directory
     */
    public void prepareData() {
        AssetManager assetManager = getAssets();
        String dataDir = getApplication().getApplicationInfo().dataDir;

        // Copy timidity to data folder
        if (AssetUtils.exists(assetManager, "timidity")) {
            if (!(new File(dataDir + "/timidity").exists())) {
                AssetUtils.copyFolder(assetManager, "timidity", dataDir + "/timidity");
            }
        }
    }

    /**
     * Standalone Mode-> if there is a game folder in assets: that folder is
     * copied to internal memory and executed.
     */
    private void startGameStandaloneWithExpansion() throws PackageManager.NameNotFoundException, IOException {
        String dataDir = getApplication().getApplicationInfo().dataDir;

        if (standaloneMode) {
            // Launch the game
            GameInformation project = new GameInformation(getApplicationContext(),dataDir + "/game");
            GameBrowserHelper.launchGame(this, project);
            finish();
        }
    }

    /**
     * Standalone Mode-> if there is a game folder in assets: that folder is
     * copied to internal memory and executed.
     */
    private void startGameStandalone() {
        AssetManager assetManager = getAssets();
        String dataDir = getApplication().getApplicationInfo().dataDir;

        // Standalone mode: Copy game in game folder to data folder and launch
        // it
        if (AssetUtils.exists(assetManager, "game")) {
            Log.i("EasyRPG", "Standalone mode : a \"game\" folder is present in asset folder");
            standaloneMode = true;

            // Copy game in internal memory
            if (!(new File(dataDir + "/game").exists())) {
                AssetUtils.copyFolder(assetManager, "game", dataDir + "/game");
            }
        }

        // Standalone mode: Unzip game.zip
        if (AssetUtils.fileExists(assetManager, "game.zip")) {
            Log.i("EasyRPG", "Standalone mode : a \"game.zip\" file is present inside the asset folder");
            standaloneMode = true;

            // Unzip game to internal memory
            if (!(new File(dataDir + "/game").exists())) {
                AssetUtils.unzipFile(assetManager, "game.zip", dataDir + "/game");
            }
        }

        if (standaloneMode) {
            // Launch the game
            GameInformation project = new GameInformation(dataDir + "/game");
            GameBrowserHelper.launchGame(this, project);
            finish();
        }
    }

    /**
     * Launch the proper game browser depending on the API.
     */
    private void launchProperBrowser() {
        // Retrieve user's preferences (for application's folder)
        SettingsManager.init(getApplicationContext());

        // Create the easyrpg's directories if they don't exist
        Helper.createEasyRPGDirectories(SettingsManager.getEasyRPGFolder());

        //Launch the proper game browser
        Intent intent;
        intent = new Intent(this, GameBrowserActivity.class);
        startActivity(intent);
    }

}
