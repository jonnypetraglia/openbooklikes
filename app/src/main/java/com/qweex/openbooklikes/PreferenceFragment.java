package com.qweex.openbooklikes;


import android.content.res.Resources;
import android.os.Bundle;
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

import com.qweex.openbooklikes.model.Shelf;

import java.util.ArrayList;
import java.util.Arrays;

public class PreferenceFragment extends PreferenceFragmentCompat implements FragmentBaseTitleable{
    ListPreference initialFragment, shelfView;
    EditTextPreference initialArg;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        initialFragment = (ListPreference) getPreferenceScreen().findPreference("initial_fragment");
        initialArg = (EditTextPreference) getPreferenceScreen().findPreference("initial_arg");
        shelfView = (ListPreference) getPreferenceScreen().findPreference("shelf_view");

        getPreferenceScreen().findPreference("shelf_background")
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        boolean show = (boolean) o;
                        preference.setSummary(show ? "Show shelf image" : "No image");
                        return true;
                    }
                });

        setupInitialFragment();
        setupShelfView();
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
                labels.add("Other User");
                values.add("9001"); //FIXME: More specific ID
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
            String itemName = getValueFor(preference, (String) o);
            preference.setSummary("Show " + itemName + " on launch");
            switch(choice) {
                default: //i.e. a shelf ID
                case R.id.nav_blog:
                case R.id.nav_challenge:
                    if(initialArg.isVisible())
                        initialArg.setVisible(false);
                    break;
                case R.id.nav_search:
                    if(!initialArg.isVisible())
                        initialArg.setVisible(true);
                    initialArg.setTitle("Search term");
                    initialArg.setSummary("Term to search");
                    break;
                case 9001: //Other User
                    if(!initialArg.isVisible())
                        initialArg.setVisible(true);
                    initialArg.setTitle("Show User");
                    initialArg.setSummary("Other user's username");
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
                preference.setSummary("Show " + getValueFor(preference, (String) o) + " by default");
                return true;
            }
        });
    }

    String getValueFor(Preference pref, String s) {
        return (String) ((ListPreference) pref).getEntries()[
                Arrays.asList(((ListPreference) pref).getEntryValues()).indexOf(s)
                ];
    }


    void setupShelfFilters() {
        PopupMenu p = new PopupMenu(getActivity(), null);
        Menu navMenu = p.getMenu();
        new SupportMenuInflater(getActivity()).inflate(R.menu.menu_shelf, navMenu);

    }
    @Override
    public String getTitle(Resources resources) {
        return resources.getString(R.string.settings);
    }
}
