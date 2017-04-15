package dk.jfjf.dragit;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by thomasbhomburg on 15/04/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
