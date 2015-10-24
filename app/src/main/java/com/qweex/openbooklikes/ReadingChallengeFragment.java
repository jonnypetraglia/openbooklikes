package com.qweex.openbooklikes;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.qweex.openbooklikes.challenge.ReadingChallengeParser;
import com.qweex.openbooklikes.model.User;

import java.io.IOException;
import java.text.DecimalFormat;

import at.grabner.circleprogress.CircleProgressView;


public class ReadingChallengeFragment extends FragmentBase {
    int year;
    User owner;

    ReadingChallengeParser parser;
    View layout;

    @Override
    public void setArguments(Bundle args) {
        year = args.getInt("year");
        owner = new User(args);
        super.setArguments(args);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("year", year);
        owner.wrapInBundle(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState==null)
            new MyTask().execute();
    }


    @Override
    String getTitle() {
        return "Reading Challenge " + year;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.reading_challenge, null);
        return super.createProgressView(inflater, container, layout);
    }

    private class MyTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                parser = new ReadingChallengeParser(owner.id(), year);
                getArguments().putInt("total", parser.total);
                getArguments().putInt("current", parser.current);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            CircleProgressView progress = (CircleProgressView) layout.findViewById(R.id.progress);
            progress.setMaxValue(parser.total);
            progress.setValue(parser.current);

            ((TextView)layout.findViewById(R.id.currentOfTotal)).setText(parser.current + "/" + parser.total + " books read");
            ((TextView)layout.findViewById(R.id.percentRemaining)).setText(ceil(parser.percentageOfYear) + "% through year");
            ((TextView)layout.findViewById(R.id.daysRemaining)).setText(ceil(parser.daysRemaining()) + " days remaining");
            ((TextView)layout.findViewById(R.id.booksBehind)).setText(parser.behind() <= 0 ? dec(parser.behind()*-1) + " books ahead" : dec(parser.behind()) + " books behind");

            TextView perDay = ((TextView)layout.findViewById(R.id.perDay));
            if(parser.current >= parser.total)
                perDay.setText("Congratulations!");
            else if(parser.perDay()>=2)
                perDay.setText("One book every " + ceil(parser.perDay()) + " days");
            else
                perDay.setText("One book every " + dec(parser.perDay()*24) + " hours");

            showContent();
        }
    }

    String dec(double d) {
        return new DecimalFormat("#0.0").format(d);
    }

    int ceil(double d) {
        return (int) Math.ceil(d);
    }
}
