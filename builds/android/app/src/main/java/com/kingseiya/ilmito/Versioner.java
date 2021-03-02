package com.kingseiya.ilmito;

public class Versioner {

    private static Versioner instance = null;
    private boolean standaloneMode = true;
    private int mainVersion = 120;
    private int patchVersion = 142;

    private Versioner() {}

    public static Versioner getInstance() {
        if(instance == null)
            instance = new Versioner();
        return instance;
    }

    public boolean isStandaloneMode() {
        return standaloneMode;
    }

    public int getMainVersion() {
        return mainVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }
}