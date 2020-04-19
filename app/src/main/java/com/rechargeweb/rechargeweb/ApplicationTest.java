package com.rechargeweb.rechargeweb;

import com.easypay.epmoney.epmoneylib.application.PaisaNikalApp;
import com.easypay.epmoney.epmoneylib.application.PaisanikalSDKApp;

/**
 * Created by dhruv on 30-09-2019.
 */
public class ApplicationTest extends PaisaNikalApp {

    @Override
    public void onCreate() {
        super.onCreate();
        PaisaNikalApp.init(this);
    }
}

