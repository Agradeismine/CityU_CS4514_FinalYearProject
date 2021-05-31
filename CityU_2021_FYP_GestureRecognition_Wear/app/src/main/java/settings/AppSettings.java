package settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.cityu_2021_fyp.gesturerecognition_wear.Application;

public class AppSettings extends Settings {

    private final SharedPreferences prefs;

    public AppSettings(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static AppSettings getAppSettings(Context context) {
        if (context instanceof Application)
            return ((Application) context).getSettings();

        Context appContext = context.getApplicationContext();
        if (appContext instanceof Application)
            return ((Application) appContext).getSettings();

        return null;
    }

    public void load() {
        load(prefs);
    }

    public void save() {
        save(prefs);
    }

    public void saveDeferred() {
        saveDeferred(prefs);
    }
}
