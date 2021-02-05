package com.kingseiya.ilmito.ext_downloader;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

public class ExtDownloaderService extends DownloaderService {
    public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl5ciIuERUhokYEgVmLASadrnCUbKCHpCB/sf4vzMTVy5mXBa4ITE6pJxcypip9ZGGkKpLn0NcTgpYAa7da00z04fZVzP2xIzfo7xHNVjak+hpCeS6TSUNT63BC0Jz1+t7ebO8/yW4Z6kCs4v3CDTtPl7VqLcNVKx18TDmmvAlVV7H0a9U5hU5H85Pl6WxkZY1q8pZAyZRYANWRkuuvOhu0K/udOsX+3Q3wc0jd5ToP+feO/8YYt0BRqCn3z7d1GenKeHouK/ddb7lPwmtLqIEr5VuYbm5o0FK96iEFnZL5t3c8o++yYnO9LNBeZY6mBI4PvZfUi3dQfyr6DrHFFwsQIDAQAB";
    public static final byte[] SALT = new byte[] { 1, 42, -12, -1, 54, 98,
            -100, -12, 43, 2, -8, -4, 9, 5, -106, -107, -33, 45, -1, 84
    };

    @Override
    public String getPublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    @Override
    public byte[] getSALT() {
        return SALT;
    }

    @Override
    public String getAlarmReceiverClassName() {
        return ExtAlarmReceiver.class.getName();
    }
}
