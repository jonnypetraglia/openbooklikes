package com.qweex.openbooklikes;


import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class PreferenceFragment extends PreferenceFragmentCompat implements FragmentBaseTitleable {
    ListPreference initialFragment, shelfView;
    EditTextPreference initialArg;
    CheckBoxPreference shelfBackground;
    Preference shelfFilters;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        initialFragment = (ListPreference) getPreferenceScreen().findPreference("initial_fragment");
        initialArg = (EditTextPreference) getPreferenceScreen().findPreference("initial_arg");
        shelfView = (ListPreference) getPreferenceScreen().findPreference("shelf_view");
        shelfBackground = (CheckBoxPreference) getPreferenceScreen().findPreference("shelf_background");
        shelfFilters = getPreferenceScreen().findPreference("shelf_filters");

        initialFragment.setDefaultValue(Integer.toString(R.id.nav_blog));
        initialArg.setDefaultValue("");
        shelfView.setDefaultValue(Integer.toString(R.id.grid_view));
        shelfBackground.setDefaultValue(true);
        try {
            JSONObject j = new JSONObject();
            j.put(Integer.toString(R.id.filter_all), getResources().getString(R.string.all));
            shelfFilters.setDefaultValue("[" + j.toString() + "]");
        } catch(JSONException je) {
            je.printStackTrace();
        }

        setupInitialFragment();
        setupShelfView();
        setupShelfFilters();
        initialArg.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary((CharSequence) o);
                return true;
            }
        });
        shelfBackground.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean show = (boolean) o;
                preference.setSummary(show ? "Show shelf image" : "No image");
                return true;
            }
        });
        shelfFilters.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Log.d("WEEEEE", o.toString());
                try {
                    JSONArray array = new JSONArray((String)o);
                    StringBuilder sb = new StringBuilder();
                    for(int i=0; i<array.length(); i++) {
                        if(i>0) sb.append(", ");
                        JSONObject item = array.getJSONObject(i);
                        String id = item.keys().next();
                        sb.append(item.getString(id));
                    }
                    preference.setSummary(sb.toString());
                    getPreferenceScreen().getSharedPreferences()
                            .edit()
                            .putString("shelf_filters", array.toString())
                            .commit();
                }catch(JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        initialFragment.getOnPreferenceChangeListener().onPreferenceChange(initialFragment,
                getValueFor(initialFragment, (String) initialFragment.getEntry())
        );
        initialArg.getOnPreferenceChangeListener().onPreferenceChange(initialArg, initialArg.getText());
        shelfView.getOnPreferenceChangeListener().onPreferenceChange(shelfView,
                getValueFor(shelfView, (String) shelfView.getEntry())
        );
        shelfBackground.getOnPreferenceChangeListener().onPreferenceChange(shelfBackground, shelfBackground.isChecked());
        shelfFilters.getOnPreferenceChangeListener().onPreferenceChange(shelfFilters,
                getPreferenceScreen().getSharedPreferences().getString(shelfFilters.getKey(), "{}")
        );
    }

    void setupInitialFragment() {
        ArrayList<CharSequence> labels = new ArrayList<>(),
                values = new ArrayList<>();

        PopupMenu p = new PopupMenu(getActivity(), null);
        Menu navMenu = p.getMenu();
        new SupportMenuInflater(getActivity()).inflate(R.menu.drawer_menu_main, navMenu);
        for(int i=0; i<navMenu.size(); i++) {
            MenuItem item = navMenu.getItem(i);
            if(item.getItemId()==R.id.nav_shelves) {
                for(Shelf shelf : MainActivity.shelves) {
                    labels.add(shelf.title());
                    values.add(shelf.id());
                }
                break;
            }
            if(!item.isVisible())
                continue;
            labels.add(item.getTitle());
            values.add(Integer.toString(item.getItemId()));
        }

        CharSequence[] l = new CharSequence[labels.size()],
                v = new CharSequence[values.size()];
        labels.toArray(l);
        values.toArray(v);
        initialFragment.setEntries(l);
        initialFragment.setEntryValues(v);
        initialFragment.setOnPreferenceChangeListener(setupInitialArg);
    }

    Preference.OnPreferenceChangeListener setupInitialArg = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            Integer choice = Integer.parseInt((String) o);
            String itemName = getEntryFor(preference, (String) o);
            preference.setSummary("Show " + itemName + " on launch");
            switch(choice) {
                default: //i.e. a shelf ID
                case R.id.nav_challenge:
                    if(initialArg.isVisible())
                        initialArg.setVisible(false);
                    break;
                case R.id.nav_search:
                    if(!initialArg.isVisible())
                        initialArg.setVisible(true);
                    initialArg.setTitle("Search term");
                    initialArg.setDialogTitle("Enter search term");
                    break;
                case R.id.nav_blog:
                    if(!initialArg.isVisible())
                        initialArg.setVisible(true);
                    initialArg.setTitle("Show User");
                    initialArg.setDialogTitle("User's username, if other than you");
                    break;

            }
            initialArg.setText("");
            return true;
        }
    };

    void setupShelfView() {
        shelfView.setEntries(new String[]{
                "Grid", "List"
        });
        shelfView.setEntryValues(new String[]{
                Integer.toString(R.id.grid_view),
                Integer.toString(R.id.list_view)
        });
        shelfView.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary("Show " + getEntryFor(preference, (String) o) + " by default");
                return true;
            }
        });
    }

    String getEntryFor(Preference pref, String s) {
        return (String) ((ListPreference) pref).getEntries()[
                Arrays.asList(((ListPreference) pref).getEntryValues()).indexOf(s)
                ];
    }
    String getValueFor(Preference pref, String s) {
        return (String) ((ListPreference) pref).getEntryValues()[
                Arrays.asList(((ListPreference) pref).getEntries()).indexOf(s)
                ];
    }


    void setupShelfFilters() {
        PopupMenu p = new PopupMenu(getActivity(), null);
        Menu mainMenu = p.getMenu();
        new SupportMenuInflater(getActivity()).inflate(R.menu.menu_shelf, mainMenu);
        Menu statusMenu = mainMenu.findItem(R.id.filter_status).getSubMenu(),
             specialMenu = mainMenu.findItem(R.id.filter_special).getSubMenu();


        final RadioGroup group = new RadioGroup(getActivity());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(group);
        ll.setLayoutParams(lp);

        for(int i=0; i<statusMenu.size(); i++) {
            if(!statusMenu.getItem(i).isEnabled())
                continue;
            CheckedTextView rb = (CheckedTextView) getActivity().getLayoutInflater().inflate(android.R.layout.select_dialog_singlechoice, null);
            rb.setId(android.R.id.text1);
            rb.setText(statusMenu.getItem(i).getTitle());
            rb.setEnabled(statusMenu.getItem(i).isEnabled());

            TypedArray ta = getActivity().obtainStyledAttributes(new int[] { R.attr.selectableItemBackground});
            Drawable drawableFromTheme = ta.getDrawable(0 /* index */);
            ta.recycle();

            FrameLayout frame = new FrameLayout(getActivity());
            frame.setLayoutParams(lp);
            frame.setId(statusMenu.getItem(i).getItemId());
            frame.addView(rb);
            frame.setForeground(drawableFromTheme);
            frame.setClickable(true);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < group.getChildCount(); i++) {
                        CheckedTextView ctv = (CheckedTextView) group.getChildAt(i).findViewById(android.R.id.text1);
                        ctv.setChecked(view == group.getChildAt(i));
                    }
                }
            });

            group.addView(frame, lp);
        }
        for (int i=0; i<specialMenu.size(); i++) {
            final CheckedTextView cb = (CheckedTextView) getActivity().getLayoutInflater().inflate(android.R.layout.select_dialog_multichoice, null);
            cb.setText(specialMenu.getItem(i).getTitle());
            cb.setClickable(false);
            cb.setId(android.R.id.text1);
            cb.setEnabled(specialMenu.getItem(i).isEnabled());

            TypedArray ta = getActivity().obtainStyledAttributes(new int[] { R.attr.selectableItemBackground});
            Drawable drawableFromTheme = ta.getDrawable(0 /* index */);
            ta.recycle();

            FrameLayout frame = new FrameLayout(getActivity());
            frame.setLayoutParams(lp);
            frame.setId(specialMenu.getItem(i).getItemId());
            frame.addView(cb);
            frame.setForeground(drawableFromTheme);
            frame.setClickable(true);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckedTextView ctv = (CheckedTextView) view.findViewById(android.R.id.text1);
                    ctv.setChecked(!ctv.isChecked());
                }
            });
            ll.addView(frame);
        }

        ScrollView sv = new ScrollView(getActivity());
        sv.addView(ll);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(shelfFilters.getTitle())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int p) {
                        //((AlertDialog)dialogInterface).hide();
                        JSONArray array = new JSONArray();
                        CheckedTextView ctv;
                        try {
                            for(ViewGroup vg : new ViewGroup[] {group, ll}) {
                                for (int i = 0; i < vg.getChildCount(); i++) {
                                    if(vg.getChildAt(i) == group)
                                        continue;
                                    ctv = ((CheckedTextView) vg.getChildAt(i).findViewById(android.R.id.text1));
                                    if (ctv.isChecked()) {
                                        JSONObject j = new JSONObject();
                                        j.put(Integer.toString(vg.getChildAt(i).getId()), ctv.getText().toString());
                                        array.put(j);
                                    }
                                }
                            }
                            shelfFilters.getOnPreferenceChangeListener().onPreferenceChange(shelfFilters, array.toString());
                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setView(sv)
                .show();
        dialog.hide();

        shelfFilters.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String s = getPreferenceScreen().getSharedPreferences().getString(preference.getKey(), "[]");
                try {
                    JSONArray a = new JSONArray(s);
                    for(int i=0; i<a.length(); i++) {
                        JSONObject item = a.getJSONObject(i);
                        int id = Integer.parseInt(item.keys().next());
                        ((CheckedTextView)dialog.findViewById(id).findViewById(android.R.id.text1)).setChecked(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Showing dialog", "yup");
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public String getTitle(Resources resources) {
        return resources.getString(R.string.settings);
    }
}