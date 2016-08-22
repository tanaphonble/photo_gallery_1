package ayp.aug.photogallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Tanaphon on 8/19/2016.
 */
public class PhotoGalleryPreference {
    private static final String TAG = "PhotoGalleryPreference";
    private static final String PREF_SEARCH_KEY = "PhotoGalleryPref";

    /**
     *
     *
     * @param context
     * @return
     */
    public static String getStoreSearchKey(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("SEARCK_KEY", null);
    }

    /**
     *
     * @param context
     * @param key
     */
    public static void setStoredSearchKey(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit()
                .putString(PREF_SEARCH_KEY, key)
                .apply();
    }
}