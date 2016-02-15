package com.qweex.openbooklikes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.model.Shelf;
import com.terlici.dragndroplist.DragNDropListView;
import com.terlici.dragndroplist.DragNDropSimpleAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ManageShelvesActivity extends AppCompatActivity {
    DragNDropListView listView;
    DragNDropSimpleAdapter adapter;
    boolean autoSorting = false;
    ProgressDialog progressDialog;

    EditText newShelfName;
    AlertDialog alertDialog;
    LoadingViewManager loadingManager;

    ArrayList<Shelf> shelvesInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newShelfName = new EditText(this);

        View content = getLayoutInflater().inflate(R.layout.manage_shelves, null);

        listView = (DragNDropListView) content.findViewById(R.id.list_view);
        listView.setDraggingEnabled(!autoSorting);
        content.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newShelfName.setText("");
                alertDialog.show();
            }
        });

        shelvesInProgress = new ArrayList<Shelf>(MainActivity.shelves);
        shelvesInProgress.remove(0);
        reloadAdapter();

        loadingManager = new LoadingViewManager();
        loadingManager.setInitial(
                (ViewGroup) getLayoutInflater().inflate(R.layout.loading, null),
                content,
                getLayoutInflater().inflate(R.layout.empty, null),
                getLayoutInflater().inflate(R.layout.error, null)
        );
        loadingManager.content();

        setContentView(loadingManager.wrapInitialInLayout(this));

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.new_shelf)
                .setView(newShelfName)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = newShelfName.getText().toString().trim();

                        if (name.length() == 0)
                            return;

                        RequestParams params = new RequestParams();
                        params.put("CatName", name);

                        progressDialog = ProgressDialog.show(ManageShelvesActivity.this, "Loading", null, true, false);

                        ApiClient.get(params, responseHandler);

                    }})
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        alertDialog.hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isFinishing())
            return;
        try {
            SettingsManager.saveShelves(getShelves(), this);
            setResult(9000);
        } catch (JSONException e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
        }
    }

    ArrayList<Shelf> getShelves() {
        ArrayList<Shelf> shelves = new ArrayList<>();
        for(int i=0; i<adapter.getCount(); i++) {
            String itemId = ((Map<String, String>) adapter.getItem(i)).get("id");
            for(Shelf s : shelvesInProgress)
                if(s.id().equals(itemId)) {
                    shelves.add(s);
                    Log.d("getShelves", s.title());
                    break;
                }
        }
        return shelves;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, R.id.option_browser, Menu.NONE, R.string.auto_sort)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setCheckable(true)
                .setChecked(autoSorting)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, R.id.option_reload, Menu.NONE, R.string.reload)
                .setIcon(android.R.drawable.ic_menu_revert)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!autoSorting)
            shelvesInProgress = getShelves();
        switch(item.getItemId()) {
            case R.id.option_browser:
                autoSorting = !item.isChecked();
                break;
            case R.id.option_reload:
                //TODO: Fetch shelves again
                loadingManager.show();
                ApiClient.get(new ShelvesHandler(loadingManager, new ArrayList<Shelf>(), MainActivity.me) {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            SettingsManager.mergeShelves(shelvesInProgress, shelves);
                            loadingManager.content();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loadingManager.error(e);
                        }
                    }
                });
                break;
        }
        item.setChecked(!item.isChecked());
        reloadAdapter();
        return true;
    }

    void reloadAdapter() {
        List<Shelf> shelves = new ArrayList<>(shelvesInProgress);
        if(autoSorting)
            Collections.sort(shelves);
        List<Map<String, String>> muhData = new ArrayList<>();
        for(Shelf s : shelves) {
            Log.d("reloadAdapter", s.title());
            Map<String, String> derp = new HashMap<>();
            derp.put("title", s.title());
            derp.put("id", s.id());
            muhData.add(derp);
        }
        adapter = new Adapter(this, muhData, R.layout.list_shelf,
                new String[]{"title"},
                new int[]{R.id.title},
                R.id.image_view);
        listView.setDragNDropAdapter(adapter);
    }

    class Adapter extends DragNDropSimpleAdapter {

        public Adapter(Context context, List<Map<String, String>> data, int resource, String[] from, int[] to, int handler) {
            super(context, data, resource, from, to, handler);
        }

        @Override
        public View getView(int position, View view, ViewGroup group) {
            view = super.getView(position, view, group);
            boolean isAll = ((Map<String, String>)getItem(position)).get("id").equals(Shelf.NO_SHELF_ID);
            view.findViewById(R.id.image_view).setVisibility(autoSorting || isAll ? View.GONE : View.VISIBLE);
            return view;
        }
    }

    ApiClient.ApiResponseHandler responseHandler = new ApiClient.ApiResponseHandler() {

        @Override
        protected String urlPath() {
            return "user/AddUserCategory";
        }

        @Override
        protected String countFieldName() {
            return null;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            progressDialog.dismiss();
            Log.e("Horrible failure", "?" + responseString);
            Snackbar.make(findViewById(R.id.fragment), "Uhhhhh problem", Snackbar.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            progressDialog.dismiss();

            // Create a new shelf for it
            Bundle b = new Bundle();
            try {
                b.putString("id", response.getString("id_category"));
                b.putString("name", response.getString("cat_name"));
            } catch (JSONException j) {

            }
            b.putString("user_id", MainActivity.me.id());
            b.putInt("book_count", 0);
            Bundle w = new Bundle();
            w.putBundle("category", b);
            Shelf s = new Shelf(w, MainActivity.me);

            progressDialog.dismiss();

            shelvesInProgress.add(s);
            try {
                SettingsManager.saveShelves(shelvesInProgress, ManageShelvesActivity.this);
                reloadAdapter();
                Snackbar.make(findViewById(R.id.fragment), getResources().getString(R.string.shelf_added), Snackbar.LENGTH_LONG)
                        .show();
            } catch (JSONException e) {
                e.printStackTrace();
                Snackbar snack = Snackbar.make(findViewById(R.id.fragment), e.getMessage(), Snackbar.LENGTH_LONG);
                ((TextView)snack.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                snack.show();
            }
        }
    };
}
