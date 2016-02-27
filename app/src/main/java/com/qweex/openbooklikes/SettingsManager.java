package com.qweex.openbooklikes;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.fragment.BookListFragment;
import com.qweex.openbooklikes.model.Me;
import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SettingsManager {
    public static boolean autoSort = false;
    public static HashSet<String> hiddenShelvesIds = new HashSet<>();
    public static Map<String, String> bookFormats = new HashMap<>();

    public static Map<Integer, Object> defaultPrefs = new HashMap<>();

    public static String[] bookstores, bookstoreUrls;

    public static final int FILTER_ALL = 0x7,
                            FILTER_READ = 0x3,
                            FILTER_PLANNING = 0x5,
                            FILTER_READING = 0x6,
                            FILTER_FAVOURITE = 0x8,
                            FILTER_WISHLIST = 0x10,
                            FILTER_PRIVATE = 0x20,
                            FILTER_REVIEWED = 0x30;
    public static final int[] FILTERS = new int[] {FILTER_ALL, FILTER_READ, FILTER_PLANNING, FILTER_READING, FILTER_FAVOURITE, FILTER_WISHLIST, FILTER_PRIVATE, FILTER_REVIEWED};


    public static void init(Context context) {
        if(hiddenShelvesIds.size()>0)
            return;
        SharedPreferences prefs = context.getSharedPreferences(Me.USER_DATA_PREFS, Context.MODE_PRIVATE);
        hiddenShelvesIds.addAll(Arrays.asList(prefs.getString("shelves_hidden", "").split(",")));

        Resources res = context.getResources();
        defaultPrefs.put(R.string.default_initial_fragment,
                res.getIdentifier(res.getString(R.string.default_initial_fragment), "id", context.getPackageName())
        );
        defaultPrefs.put(R.string.default_shelf_view,
                res.getIdentifier(res.getString(R.string.default_shelf_view), "id", context.getPackageName())
        );

        bookstores(context);

        String[] array = context.getResources().getStringArray(R.array.book_formats);
        for(int i=0; i<array.length; i++)
            bookFormats.put(Integer.toString(i), array[i]);

        try {
            MainActivity.shelves = SettingsManager.loadShelves(context);
        } catch (JSONException e) {
            e.printStackTrace();
            //FIXME: What do
        }
    }


    public static ArrayList<Shelf> mergeShelves(ArrayList<Shelf> shelves, ArrayList<Shelf> newShelves) throws JSONException {
        // "Merge" the arrays, taking ordering precedence from the old; new entries are tacked on to the end
        for (int i = 0; i < shelves.size(); ) {
            boolean found = false;
            for (Shelf n : newShelves) {
                Log.d("Comparing", shelves.get(i).getTitle(null) + "=" + n.getTitle(null) + " ? " + shelves.get(i).equals(n));
                if (found = shelves.get(i).equals(n)) {
                    shelves.set(i++, n);
                    newShelves.remove(n);
                    break;
                }
            }
            if (!found)
                shelves.remove(i);
        }
        shelves.addAll(newShelves);
        return shelves;
    }

    public static void saveShelves(ArrayList<Shelf> shelves, Context context) throws JSONException {
        JSONArray array = new JSONArray();
        HashSet<String> hiddenIds = new HashSet<String>();
        for(Shelf s : shelves)
            if(!s.isAllBooks()) {
                JSONObject j = new JSONObject();
                j.put("id_" + s.apiName(), s.id());
                j.put("id_user", MainActivity.me.id());
                j.put(s.apiName() + "_name", s.getTitle(null));
                j.put(s.apiName() + "_book_count", s.getI("book_count"));
                Log.d("saveShelves", j.toString());
                array.put(j);
                if(hiddenShelvesIds.contains(s.id()))
                    hiddenIds.add(s.id());
            }

        context.getSharedPreferences(Me.USER_DATA_PREFS, Context.MODE_PRIVATE).edit()
                .putLong("shelves_timestamp", Calendar.getInstance().getTimeInMillis())
                .putString("shelves", array.toString())
                .putString("shelves_hidden", hiddenIds.toString().replaceAll("\\[|\\]", ""))
                .commit();
    }

    public static void bookstores(Context context) {
        List<String> stores = new LinkedList<>(Arrays.asList(context.getResources().getStringArray(R.array.bookstores))),
                    urls = new LinkedList<>(Arrays.asList(context.getResources().getStringArray(R.array.bookstores_urls)));
        String pref = PreferenceManager.getDefaultSharedPreferences(context).getString("bookstores", "");
        for (String s : pref.split("\\|")) {
            if(s.length()>0) {
                stores.set(Integer.parseInt(s), "");
                urls.set(Integer.parseInt(s), "");
            }
        }
        while(stores.indexOf("") >= 0) {
            stores.remove("");
            urls.remove("");
        }
        stores.toArray(SettingsManager.bookstores = new String[stores.size()]);
        urls.toArray(SettingsManager.bookstoreUrls = new String[urls.size()]);
    }

    public static boolean userInfoExpired(Context c) {
        long then = c.getSharedPreferences(Me.USER_DATA_PREFS, Context.MODE_PRIVATE).getLong("shelves_timestamp", 0),
             now = Calendar.getInstance().getTimeInMillis();
        ;
        long elapsedHours = (now - then) / 1000 / 60;
        long expiredHours = getInt(c, "expiration_hours", R.integer.default_expiration_hours);
        Log.d("elapsed", now + " - " + then + " = " + elapsedHours + " >= " + expiredHours);
        return expiredHours>=0 && elapsedHours >= expiredHours;
    }

    public static ArrayList<Shelf> loadShelves(Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(Me.USER_DATA_PREFS, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray(prefs.getString("shelves", "[]"));
        ArrayList<Shelf> shelves = new ArrayList<>();
        shelves.add(Shelf.allBooksOfUser(MainActivity.me));
        for(int i=0; i<array.length(); i++) {
            Shelf s = new Shelf(array.getJSONObject(i), MainActivity.me);
            Log.d("loadShelves", s.getTitle(null));
            shelves.add(s);
        }
        if(autoSort)
            Collections.sort(shelves);
        return shelves;
    }

    public static void setFilters(Context context, BookListFragment.CheckTracker status, BookListFragment.CheckTracker special, int filters) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(filters == 0)
            filters = prefs.getInt("shelf_filters", FILTER_ALL);

        if((filters & FILTER_ALL)==FILTER_ALL)
            status.checkEx(R.id.filter_all);
        else if((filters & FILTER_READ)==FILTER_READ)
            status.checkEx(R.id.filter_read);
        else if((filters & FILTER_READING)==FILTER_READING)
            status.checkEx(R.id.filter_reading);
        else if((filters & FILTER_PLANNING)==FILTER_PLANNING)
            status.checkEx(R.id.filter_planning);

        if((filters & FILTER_FAVOURITE)==FILTER_FAVOURITE)
            special.checkEx(R.id.filter_favourite);
        if((filters & FILTER_WISHLIST)==FILTER_WISHLIST)
            special.checkEx(R.id.filter_wishlist);
        if((filters & FILTER_PRIVATE)==FILTER_PRIVATE)
            special.checkEx(R.id.filter_private);
    }


    public static void logout(MainActivity a) {
        a.getSharedPreferences(Me.USER_DATA_PREFS, Activity.MODE_PRIVATE)
                .edit().clear().apply();
        a.showLogin();
    }

    //TODO: Add this to Settings somewhere
    public static void clearEverything(MainActivity a) {
        clearCache(a);
        PreferenceManager.getDefaultSharedPreferences(a).edit().clear().apply();
        logout(a);
    }

    public static void clearCache(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(Me.USER_DATA_PREFS, Activity.MODE_PRIVATE);
        String usr_token = prefs.getString("usr_token", ""),
                usr_email = prefs.getString("usr_email", ""),
                id_user = prefs.getString("id_user", "");
        prefs.edit()
                .clear()
                .putString("usr_token", usr_token)
                .putString("usr_email", usr_email)
                .putString("id_user", id_user)
                .apply();
        MainActivity.imageLoader.clearDiskCache();
        MainActivity.imageLoader.clearMemoryCache();
    }

    public static int getInt(Context c, String name, int defaultId) {
        return Integer.parseInt(getIntAsStr(c, name, defaultId));
    }

    public static String getIntAsStr(Context c, String name, int defaultId) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(name, Integer.toString(c.getResources().getInteger(defaultId)));
    }

    public static String getString(Context c, String name, int defaultId) {
        return getString(c, name, defaultId > 0 ? c.getResources().getString(defaultId) : "");
    }

    public static String getString(Context c, String name, String defaultStr) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(name, defaultStr);
    }

    public static int getId(Context c, String name, int idOfDefaultString) {
        name = getString(c, name, idOfDefaultString);
        return c.getResources().getIdentifier(name, "id", c.getPackageName());
    }

//    public static int getId(Context c, String name, String defaultIdName) {
//        int defaultId = c.getResources().getIdentifier(defaultIdName, "id", c.getPackageName());
//        return getId(c, name, defaultId);
//    }

    public static boolean getBool(Context c, String name, int defaultId) {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(name, c.getResources().getBoolean(defaultId));
    }
}
