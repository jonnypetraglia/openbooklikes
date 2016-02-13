package com.qweex.openbooklikes;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qweex.openbooklikes.challenge.ReadingChallengeParser;
import com.qweex.openbooklikes.model.User;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import at.grabner.circleprogress.CircleProgressView;


public class ReadingChallengeFragment extends FragmentBase implements TabLayout.OnTabSelectedListener {
    User owner;
    String selectedYear = "";

    Map<String, ReadingChallengeParser> parsers = new HashMap<>();
    TabLayout tabLayout;
    View content, errorView;

    @Override
    public void setArguments(Bundle args) {
        owner = new User(args);
        super.setArguments(args);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("year", selectedYear);
        owner.wrapInBundle(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState==null)
            reloadCurrentTab();
    }


    @Override
    String getTitle(Resources res) {
        return res.getString(R.string.reading_challenge);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View.OnClickListener retry = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadCurrentTab();
            }
        };

        Resources res = getActivity().getResources();
        tabLayout = new TabLayout(getActivity());
        tabLayout.setBackgroundColor(res.getColor(R.color.colorPrimaryDark));
        tabLayout.setTabTextColors(res.getColor(android.R.color.white), res.getColor(R.color.colorAccent));

        content = inflater.inflate(R.layout.reading_challenge, null);
        content.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ViewGroup loadingMore = (ViewGroup) inflater.inflate(R.layout.loading, null);

        View empty = inflater.inflate(R.layout.empty, null);
        ((TextView)empty.findViewById(R.id.title)).setText(R.string.no_reading_challenges);

        errorView = inflater.inflate(R.layout.error, null);
        errorView.findViewById(R.id.retry).setOnClickListener(retry);

        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(tabLayout);
        ll.addView(content);
        ll.addView(loadingMore);
        ll.addView(empty);
        ll.addView(errorView);

        loadingManager.addMore(loadingMore, content, empty, errorView);

        View superView = super.createProgressView(inflater, container, ll);
        superView.findViewById(R.id.retry).setOnClickListener(retry);
        return superView;
    }

    public void reloadCurrentTab() {
        new FetchChallengeAsync(selectedYear).execute();
    }

    @Override
    public void onTabSelected(Tab tab) {
        selectedYear = tab.getText().toString();
        if(!parsers.containsKey(tab.getText().toString())) {
            loadingManager.show();
            new FetchChallengeAsync(tab.getText().toString()).execute();
        } else {
            onTabReselected(tab);
        }
    }

    @Override
    public void onTabUnselected(Tab tab) {
    }

    @Override
    public void onTabReselected(Tab tab) {
        ReadingChallengeParser parser = parsers.get(tab.getText());
        Resources res = getResources();

        CircleProgressView progress = (CircleProgressView) content.findViewById(R.id.progress);
        progress.setMaxValue(parser.total);
        progress.setValue(parser.current);

        ((TextView)content.findViewById(R.id.current_of_total)).setText(res.getString(R.string.challenge_read, parser.current + "/" + parser.total));

        int viewStatus;
        if(tab.getText().equals(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))) {
            ((TextView) content.findViewById(R.id.percent)).setText(res.getString(R.string.challenge_year_percent, ceil(parser.percentageOfYear)));
            ((TextView) content.findViewById(R.id.days)).setText(res.getString(R.string.challenge_days_remaining, ceil(parser.daysRemaining())));
            ((TextView) content.findViewById(R.id.status)).setText(
                    res.getString(
                            parser.behind() > 0 ? R.string.challenge_behind : R.string.challenge_ahead,
                            dec((parser.behind() < 0 ? -1 : 1) * parser.behind())
                    ));

            TextView perDay = ((TextView) content.findViewById(R.id.per_day));
            if (parser.current >= parser.total)
                perDay.setText(R.string.challenge_completed);
            else if (parser.perDay() >= 2)
                perDay.setText(res.getString(R.string.challenge_needed_daily, ceil(parser.perDay())));
            else
                perDay.setText(res.getString(R.string.challenge_needed_hourly, ceil(parser.perDay() * 24)));
            viewStatus = View.VISIBLE;
        } else {
            viewStatus = View.GONE;
        }

        for(int i : new int[] {R.id.percent, R.id.days, R.id.status, R.id.per_day})
            content.findViewById(i).setVisibility(viewStatus);


        loadingManager.content();
    }

    private class FetchChallengeAsync extends AsyncTask<String, Void, Throwable> {
        ReadingChallengeParser parser;
        String yearToLoad;

        public FetchChallengeAsync() {
            super();
            yearToLoad = "";
        }

        public FetchChallengeAsync(String year) {
            super();
            yearToLoad = year;
        }

        @Override
        protected void onPreExecute() {
            loadingManager.show();
            content.setVisibility(View.GONE);
        }

        @Override
        protected Throwable doInBackground(String... strings) {
            try {
                parser = new ReadingChallengeParser(owner.id(), yearToLoad);
            } catch (IOException e) {
                e.printStackTrace();
                return e;
            } catch(ReadingChallengeParser.NoSuchChallengeException n) {
                return n;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Throwable error) {
            if(error!=null) {
                if(error instanceof ReadingChallengeParser.NoSuchChallengeException)
                    loadingManager.empty();
                else
                    loadingManager.error(error);
                return;
            }
            if(getActivity()==null || (tabLayout.getTabCount()>0 && !yearToLoad.equals(selectedYear)))
                return;

            Tab tab = null;
            if(tabLayout.getTabCount()==0) {
                for (String y : parser.past) {
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(y)
                            .setContentDescription(y)
                    );
                }


                tab = tabLayout.newTab()
                        .setText(parser.year)
                        .setContentDescription(parser.year);
                tabLayout.addTab(tab);
                tabLayout.setOnTabSelectedListener(ReadingChallengeFragment.this);
                loadingManager.content();
                loadingManager.changeState(LoadingViewManager.State.MORE);
            } else {
                for(int i=0; i<tabLayout.getTabCount(); i++)
                    if(parser.year.equals(tabLayout.getTabAt(i).getText())) {
                        tab = tabLayout.getTabAt(i);
                        break;
                    } else if(i==tabLayout.getTabCount()-1) {
                        loadingManager.error(new Exception("Tab not found: " + parser.year));
                        return;
                    }

            }

            assert tab != null;
            parsers.put(parser.year, parser);
            tab.select();
        }
    }

    String dec(double d) {
        return new DecimalFormat("#0.0").format(d);
    }

    int ceil(double d) {
        return (int) Math.ceil(d);
    }
}
