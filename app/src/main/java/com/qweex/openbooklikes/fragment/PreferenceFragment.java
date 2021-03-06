package com.qweex.openbooklikes.fragment;


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

import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.LoadingViewManagerDialog;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.SettingsManager;
import com.qweex.openbooklikes.Titleable;
import com.qweex.openbooklikes.activity.MainActivity;
import com.qweex.openbooklikes.handler.UserHandler;
import com.qweex.openbooklikes.model.Shelf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class PreferenceFragment extends PreferenceFragmentCompat implements Titleable {
    ListPreference initialFragment, shelfView;
    EditTextPreference initialArg, expirationHours;
    CheckBoxPreference shelfBackground;
    Preference shelfFilters, bookstores;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        findFragments();

        setDefaultValues();

        setupTheRest();
        setupBookstores();
        setupInitialFragment();
        setupShelfView();
        setupShelfFilters();
    }

    void findFragments() {
        initialArg = (EditTextPreference) getPreferenceScreen().findPreference("initial_arg");
        initialFragment = (ListPreference) getPreferenceScreen().findPreference("initial_fragment");
        shelfView = (ListPreference) getPreferenceScreen().findPreference("shelf_view");
        shelfBackground = (CheckBoxPreference) getPreferenceScreen().findPreference("shelf_background");
        shelfFilters = getPreferenceScreen().findPreference("shelf_filters");
        expirationHours = (EditTextPreference) getPreferenceScreen().findPreference("expiration_hours");

        bookstores = getPreferenceScreen().findPreference("bookstores");
    }

    void setDefaultValues() {
        Resources res = getResources();
        initialFragment.setDefaultValue(SettingsManager.defaultPrefs.get(R.string.default_initial_fragment));
        initialArg.setDefaultValue("");
        shelfView.setDefaultValue(
                Integer.toString(res.getIdentifier(res.getString(R.string.default_shelf_view), "id", getActivity().getPackageName()))
        );
        shelfBackground.setDefaultValue(res.getBoolean(R.bool.default_shelf_background));
        expirationHours.setDefaultValue(res.getInteger(R.integer.default_expiration_hours));
        shelfFilters.setDefaultValue(SettingsManager.FILTER_ALL);
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
                    labels.add(shelf.getTitle(null));
                    values.add(shelf.id());
                }
                break;
            }
            if(!item.isVisible())
                continue;
            labels.add(item.getTitle());
            values.add(getResources().getResourceEntryName(item.getItemId()));
        }

        CharSequence[] l = new CharSequence[labels.size()],
                v = new CharSequence[values.size()];
        labels.toArray(l);
        values.toArray(v);
        initialFragment.setEntries(l);
        initialFragment.setEntryValues(v);
        initialFragment.setOnPreferenceChangeListener(clickInitialFragmentSetupInitialArg);
        String resIdName = SettingsManager.getString(getActivity(), initialFragment.getKey(), R.string.default_initial_fragment);
        initialFragment.getOnPreferenceChangeListener().onPreferenceChange(initialFragment, resIdName);
    }

    Preference.OnPreferenceChangeListener clickInitialFragmentSetupInitialArg = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String resIdName = (String) o;
            boolean changed = !resIdName.equals(initialFragment.getValue());
            initialFragment.setValue(resIdName);
            String itemName = getEntryFor(preference, resIdName);
            int resId;
            try {
                resId = Integer.parseInt(resIdName);
            } catch(Exception e) {
                resId = getResources().getIdentifier(resIdName, "id", getContext().getPackageName());
            }
            preference.setSummary("Show " + itemName + " on launch"); //TODO: String
            switch(resId) {
                default:
                    if(initialArg.isVisible())
                        initialArg.setVisible(false);
                    if(!shelfFilters.isVisible())
                        shelfFilters.setVisible(true);
                    break;
                case R.id.nav_challenge:
                    if(initialArg.isVisible())
                        initialArg.setVisible(false);
                    if(shelfFilters.isVisible())
                        shelfFilters.setVisible(false);
                    break;
                case R.id.nav_search:
                    if(!initialArg.isVisible())
                        initialArg.setVisible(true);
                    if(shelfFilters.isVisible())
                        shelfFilters.setVisible(false);
                    initialArg.setTitle("Search term"); //TODO: String
                    initialArg.setDialogTitle("Enter search term"); //TODO: String
                    break;
                case R.id.nav_blog:
                    if(!initialArg.isVisible())
                        initialArg.setVisible(true);
                    if(shelfFilters.isVisible())
                        shelfFilters.setVisible(false);
                    initialArg.setTitle("Show Blog"); //TODO: String
                    initialArg.setDialogTitle("User's username, if other than you"); //TODO: String
                    break;

            }
            if(changed)
                initialArg.getOnPreferenceChangeListener().onPreferenceChange(initialArg, "");
            return true;
        }
    };

    void setupShelfView() {
        shelfView.setEntries(new String[]{
                "Grid", "List" //TODO: String
        });
        shelfView.setEntryValues(new String[]{
                getResources().getResourceEntryName(R.id.grid_view),
                getResources().getResourceEntryName(R.id.list_view)
        });
        shelfView.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary("Show " + getEntryFor(preference, (String) o) + " by default"); // TODO STring
                return false;
            }
        });
        String resIdName = SettingsManager.getString(getActivity(), shelfView.getKey(), R.string.default_shelf_view);
        shelfView.getOnPreferenceChangeListener().onPreferenceChange(shelfView, resIdName);
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

        TypedArray ta = getActivity().obtainStyledAttributes(new int[] { R.attr.selectableItemBackground});
        Drawable drawableFromTheme = ta.getDrawable(0 /* index */);
        ta.recycle();

        int i;
        for(i=0; i<statusMenu.size(); i++) {
            if(!statusMenu.getItem(i).isEnabled())
                continue;

            CheckedTextView rb = (CheckedTextView) getActivity().getLayoutInflater().inflate(android.R.layout.select_dialog_singlechoice, null);
            rb.setId(android.R.id.text1);
            rb.setText(statusMenu.getItem(i).getTitle());
            rb.setEnabled(statusMenu.getItem(i).isEnabled());
            rb.setClickable(!statusMenu.getItem(i).isEnabled());

            FrameLayout frame = new FrameLayout(getActivity());
            frame.setLayoutParams(lp);

            frame.setId(SettingsManager.FILTERS[i]);
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
        for (int j=0; j<specialMenu.size(); j++) {
            final CheckedTextView cb = (CheckedTextView) getActivity().getLayoutInflater().inflate(android.R.layout.select_dialog_multichoice, null);
            cb.setText(specialMenu.getItem(j).getTitle());
            cb.setClickable(false);
            cb.setId(android.R.id.text1);
            cb.setEnabled(specialMenu.getItem(j).isEnabled());
            cb.setClickable(!specialMenu.getItem(j).isEnabled());

            FrameLayout frame = new FrameLayout(getActivity());
            frame.setLayoutParams(lp);
            frame.setId(SettingsManager.FILTERS[i++]);
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
                        CheckedTextView ctv;
                        int ans = 0;

                        for(ViewGroup vg : new ViewGroup[] {group, ll}) {
                            for (int i = 0; i < vg.getChildCount(); i++) {
                                if(vg.getChildAt(i) == group)
                                    continue;
                                ctv = ((CheckedTextView) vg.getChildAt(i).findViewById(android.R.id.text1));
                                if (ctv.isChecked())
                                    ans |= vg.getChildAt(i).getId();
                            }
                        }
                        shelfFilters.getOnPreferenceChangeListener().onPreferenceChange(shelfFilters, ans);
                    }
                })
                .setView(sv)
                .show();
        dialog.hide();

        shelfFilters.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int filters = getPreferenceScreen().getSharedPreferences().getInt(preference.getKey(), SettingsManager.FILTER_ALL);
                for (int f : SettingsManager.FILTERS) {
                    if((filters & SettingsManager.FILTER_ALL)==SettingsManager.FILTER_ALL
                            && (filters & f) == f
                            && f != SettingsManager.FILTER_ALL)
                        continue;
                    ((CheckedTextView) dialog.findViewById(f).findViewById(android.R.id.text1)).setChecked((filters & f) == f);
                }
                dialog.show();
                return true;
            }
        });
        shelfFilters.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                int filter = (int) o;
//                    StringBuilder sb = new StringBuilder();
//                    for (int i = 0; i < array.length(); i++) {
//                        if (i > 0) sb.append(", ");
//                        String idName = array.getString(i);
//                        int id = getResources().getIdentifier(idName, "id", getContext().getPackageName());
//                        sb.append(getResources().getString(id));
//                    }
//                preference.setSummary(sb.toString());
                getPreferenceScreen().getSharedPreferences()
                        .edit()
                        .putInt(shelfFilters.getKey(), filter)
                        .commit();
                return true;
            }
        });
        shelfFilters.getOnPreferenceChangeListener().onPreferenceChange(shelfFilters, SettingsManager.FILTER_ALL);
    }

    void setupBookstores() {
        bookstores.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean[] results = new boolean[getResources().getStringArray(R.array.bookstores).length];

                String pref = getPreferenceManager().getSharedPreferences().getString(bookstores.getKey(), "");
                for(int i=0; i<results.length; i++)
                    results[i] = true;


                String[] selected = pref.split("\\|");
                for (String s : selected) {
                    if(s.length()>0) {
                        results[Integer.parseInt(s)] = false;
                    }
                }

                new AlertDialog.Builder(getContext())
                        .setTitle("Bookstores")
                        .setMultiChoiceItems(R.array.bookstores, results, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StringBuilder sb = new StringBuilder();
                                for(int i=0; i<((AlertDialog)dialog).getListView().getCount(); i++) {
                                    if(! ((AlertDialog)dialog).getListView().isItemChecked(i))
                                        sb.append(i).append("|");
                                }
                                getPreferenceManager().getSharedPreferences()
                                        .edit()
                                        .putString(bookstores.getKey(), sb.toString())
                                        .commit();
                                SettingsManager.bookstores(getContext());
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    void setupTheRest() {
        String argVal = SettingsManager.getString(getActivity(), initialArg.getKey(), "");
        String expVal = SettingsManager.getIntAsStr(getActivity(), expirationHours.getKey(), R.integer.default_expiration_hours);
        boolean bgVal = SettingsManager.getBool(getActivity(), shelfBackground.getKey(), R.bool.default_shelf_background);

        initialArg.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String argggg = (String) o;
                initialArg.setText(argggg);
                String fragmentItemIdName = SettingsManager.getString(getActivity(), initialFragment.getKey(), R.string.default_initial_fragment); //initialFragment.getValue();
                int fragmentItemId = getResources().getIdentifier(fragmentItemIdName, "id", getContext().getPackageName());
                switch (fragmentItemId) {
                    case R.id.nav_blog:
                        preference.setSummary("Show " + (argggg.length() == 0 ? "your own" : argggg));
                        break;
                    case R.id.nav_search:
                        preference.setSummary(argggg.length() > 0 ? "Open search with \"" + argggg + "\"" : "Open without searching");
                    default: // includes case R.id.nav_challenge:
                }
                return true;
            }
        });
        initialArg.getOnPreferenceChangeListener().onPreferenceChange(initialArg, argVal);


        expirationHours.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                expirationHours.setText((String) o);
                if (o == null)
                    o = Integer.toString(getActivity().getResources().getInteger(R.integer.default_expiration_hours));

                if (o.equals("0"))
                    preference.setSummary("Every time the app launches"); //TODO: String
                else
                    preference.setSummary("Every " + o + " hours"); //TODO: String
                return true;
            }
        });
        expirationHours.getOnPreferenceChangeListener().onPreferenceChange(expirationHours, expVal);

        shelfBackground.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                shelfBackground.setChecked((Boolean) o);
                boolean show = (boolean) o;
                preference.setSummary(show ? "Show shelf image" : "No image"); //TODO: String
                return true;
            }
        });
        shelfBackground.getOnPreferenceChangeListener().onPreferenceChange(shelfBackground, bgVal);


        getPreferenceScreen().findPreference("reload_user_data")
                .setSummary("Signed in as " + MainActivity.me.getS("email")); //TODO: String
        getPreferenceScreen().findPreference("reload_user_data").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ApiClient.get(new UserHandler(new LoadingViewManagerDialog(
                        getActivity().findViewById(R.id.fragment),
                        R.string.app_name //TODO: String
                ), getActivity()) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        ((MainActivity) getActivity()).recreateShelvesNav();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                        super.onFailure(statusCode, headers, error, responseBody);
                    }
                });
                return true;
            }
        });

        getPreferenceScreen().findPreference("logout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.logout)
                        .setMessage(R.string.confirm_logout)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SettingsManager.logout((MainActivity) getActivity());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });

        getPreferenceScreen().findPreference("clear_cache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Clear app cache") //TODO: Strings
                        .setMessage("Are you sure you want to proceed?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SettingsManager.clearCache(getActivity());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });

        getPreferenceScreen().findPreference("clear_all").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Clear all app data") //TODO: Strings
                        .setMessage("This will clear your settings and cache in addition to logging you out. Are you sure you want to do this?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SettingsManager.clearEverything((MainActivity) getActivity());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
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

    @Override
    public String getTitle(Resources resources) {
        return resources.getString(R.string.settings);
    }
}
