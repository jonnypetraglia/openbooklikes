package com.qweex.openbooklikes.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.RequestParams;
import com.qweex.openbooklikes.ApiClient;
import com.qweex.openbooklikes.fragment.FragmentBase;
import com.qweex.openbooklikes.handler.LoadingResponseHandler;
import com.qweex.openbooklikes.LoadingViewManager;
import com.qweex.openbooklikes.LoadingViewManagerDialog;
import com.qweex.openbooklikes.R;
import com.qweex.openbooklikes.SettingsManager;
import com.qweex.openbooklikes.handler.ShelvesHandler;
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
    Adapter adapter;
    boolean autoSorting = false;
    ProgressDialog progressDialog;

    EditText newShelfName;
    AlertDialog alertDialog;
    LoadingViewManager loadingManager;
    LoadingViewManagerDialog loadingDialogManager;

    ArrayList<Shelf> shelvesInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newShelfName = new EditText(this);

        listView = new DragNDropListView(this);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setDraggingEnabled(!autoSorting);
        listView.setOnItemDragNDropListener(new DragNDropListView.OnItemDragNDropListener() {
            @Override
            public void onItemDrag(DragNDropListView parent, View view, int position, long id) {
                view.setBackgroundColor(0xffffffff);
            }

            @Override
            public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
                view.setBackgroundColor(0x00000000);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String itemId = ((Map<String, String>) adapter.getItem(i)).get("id");
                if (SettingsManager.hiddenShelvesIds.contains(itemId))
                    SettingsManager.hiddenShelvesIds.remove(itemId);
                else
                    SettingsManager.hiddenShelvesIds.add(itemId);
                adapter.colorEye(itemId, (ImageView) view.findViewById(R.id.image_view));
            }
        });

        shelvesInProgress = new ArrayList<Shelf>(MainActivity.shelves);
        shelvesInProgress.remove(0);
        reloadAdapter();

        View error = getLayoutInflater().inflate(R.layout.error, null);
        loadingManager = new LoadingViewManager();
        loadingManager.setInitial(
                getLayoutInflater().inflate(R.layout.loading, null),
                listView,
                getLayoutInflater().inflate(R.layout.empty, null),
                error

        );
        loadingManager.content();
        loadingDialogManager = new LoadingViewManagerDialog(listView, R.string.shelf_added);
        error.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

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
                    break;
                }
        }
        return shelves;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi;

        mi = menu.add(Menu.NONE, R.id.option_browser, Menu.NONE, R.string.auto_sort)
//                .setIcon(android.R.drawable.ic_menu_agenda) //TODO: Icon for autosort?
                .setCheckable(true)
                .setChecked(autoSorting);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        mi = menu.add(Menu.NONE, R.id.option_reload, Menu.NONE, R.string.reload)
                .setIcon(R.drawable.reload_np61130);
        FragmentBase.optionIcon(mi);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = menu.add(Menu.NONE, R.id.option_add, Menu.NONE, R.string.create_shelf)
                .setIcon(android.R.drawable.ic_input_add) //TODO: Icon for Add
                .setCheckable(true)
                .setChecked(autoSorting);
        FragmentBase.optionIcon(mi);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!autoSorting)
            shelvesInProgress = getShelves();
        switch(item.getItemId()) {
            case R.id.option_add:
                newShelfName.setText("");
                alertDialog.show();
                break;
            case R.id.option_browser:
                autoSorting = !item.isChecked();
                break;
            case R.id.option_reload:
                reload();
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
            if(s.isAllBooks()) continue;
            Log.d("reloadAdapter", s.getTitle(getResources()));
            Map<String, String> derp = new HashMap<>();
            derp.put("title", s.getTitle(getResources()));
            derp.put("id", s.id());
            muhData.add(derp);
        }
        adapter = new Adapter(this, muhData, R.layout.list_shelf,
                new String[]{"title"},
                new int[]{R.id.title},
                R.id.special);
        listView.setDragNDropAdapter(adapter);
    }

    class Adapter extends DragNDropSimpleAdapter {
        int eyeUnhiddenColor, eyeHiddenColor;

        public Adapter(Context context, List<Map<String, String>> data, int resource, String[] from, int[] to, int handler) {
            super(context, data, resource, from, to, handler);
            eyeUnhiddenColor = getResources().getColor(R.color.shelf_unhidden);
            eyeHiddenColor = getResources().getColor(R.color.shelf_hidden);
        }

        @Override
        public View getView(int position, View view, ViewGroup group) {
            view = super.getView(position, view, group);
            String itemId = ((Map<String, String>)getItem(position)).get("id");
            boolean isAll = itemId.equals(Shelf.NO_SHELF_ID);
            view.findViewById(getDragHandler()).setVisibility(autoSorting || isAll ? View.GONE : View.VISIBLE);
            ImageView eye = (ImageView) view.findViewById(R.id.image_view);
            eye.setVisibility(View.VISIBLE);
            colorEye(itemId, eye);
            return view;
        }

        public void colorEye(String itemId, ImageView eye) {
            eye.setColorFilter(null);
            eye.setColorFilter(
                    SettingsManager.hiddenShelvesIds.contains(itemId)
                            ? eyeHiddenColor : eyeUnhiddenColor
                    , PorterDuff.Mode.SRC_IN);
        }
    }

    void reload() {
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
    }

    ApiClient.ApiResponseHandler responseHandler = new LoadingResponseHandler(loadingDialogManager) {

        @Override
        protected String urlPath() {
            return "user/AddUserCategory";
        }

        @Override
        protected String countFieldName() {
            return null;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            // Create a new shelf for it
            Bundle b = new Bundle();
            try {
                b.putString("id", response.getString("id_category"));
                b.putString("name", response.getString("cat_name"));
                b.putString("user_id", MainActivity.me.id());
                b.putInt("book_count", 0);
                Bundle w = new Bundle();
                w.putBundle("category", b);
                Shelf s = new Shelf(w, MainActivity.me);

                shelvesInProgress.add(s);

                SettingsManager.saveShelves(shelvesInProgress, ManageShelvesActivity.this);
                reloadAdapter();
            } catch (JSONException e) {
                super.onFailure(statusCode, headers, e, response);
            }
        }
    };
}
