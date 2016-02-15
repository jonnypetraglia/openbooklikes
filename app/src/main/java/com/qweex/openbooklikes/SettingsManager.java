package com.qweex.openbooklikes;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.qweex.openbooklikes.model.Me;
import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;

public class SettingsManager {
    public static boolean autoSort = false;
    public static HashSet<String> hiddenShelvesIds = new HashSet<>();

    static void init(Context context) {
        if(hiddenShelvesIds.size()>0)
            return;
        SharedPreferences prefs = context.getSharedPreferences(Me.USER_DATA_PREFS, Context.MODE_PRIVATE);
        hiddenShelvesIds.addAll(Arrays.asList(prefs.getString("shelves_hidden", "").split(",")));
    }


    public static ArrayList<Shelf> mergeShelves(ArrayList<Shelf> shelves, ArrayList<Shelf> newShelves) throws JSONException {
        // "Merge" the arrays, taking ordering precedence from the old; new entries are tacked on to the end
        for (int i = 0; i < shelves.size(); ) {
            boolean found = false;
            for (Shelf n : newShelves) {
                Log.d("Comparing", shelves.get(i).title() + "=" + n.title() + " ? " + shelves.get(i).equals(n));
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
        for(Shelf s : shelves)
            Log.d("-result", s.title());
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
                j.put(s.apiName() + "_name", s.title());
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

    public static boolean shelvesExpired(Context c) {
        long then = c.getSharedPreferences(Me.USER_DATA_PREFS, Context.MODE_PRIVATE).getLong("shelves_timestamp", 0),
             now = Calendar.getInstance().getTimeInMillis();
        ;
        long elapsedHours = (now - then) / 1000 / 60 / 60;
        return elapsedHours >= 24


                && false; //TODO: Settings
    }

    public static ArrayList<Shelf> loadShelves(Context context) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(Me.USER_DATA_PREFS, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray(prefs.getString("shelves", "[]"));
        ArrayList<Shelf> shelves = new ArrayList<>();
        shelves.add(Shelf.allBooksOfUser(MainActivity.me));
        Log.d("Cocaine Hurricane", array.toString() + "!");
        for(int i=0; i<array.length(); i++) {
            Shelf s = new Shelf(array.getJSONObject(i), MainActivity.me);
            Log.d("loadShelves", s.title());
            shelves.add(s);
        }
        if(autoSort)
            Collections.sort(shelves);
        return shelves;
    }
}