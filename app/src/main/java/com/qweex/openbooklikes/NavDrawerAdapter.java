package com.qweex.openbooklikes;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class NavDrawerAdapter extends BaseAdapter {
    Menu menu;
    int lastCount = -1;
    Context context;
    ArrayList<MenuItem> flatList = new ArrayList<>();
    MenuItem selected;
    int[] selectedColors = new int[] {R.color.nav_selected_bg, R.color.nav_selected_text, R.color.nav_selected_icon},
          unselectedColors = new int[] {android.R.color.transparent, R.color.nav_unselected_text, R.color.nav_unselected_icon};


    public Menu getMenu() {
        return menu;
    }

    public NavDrawerAdapter(Menu m, Context c) {
        menu = m;
        context = c;
    }

    public void setSelected(MenuItem item) {
        selected = item;
    }

    public boolean isSelected(MenuItem item) {
        return selected == item;
    }

    public int indexOf(MenuItem item) {
        return flatList.indexOf(item);
    }

    @Override
    public void notifyDataSetInvalidated() {
        lastCount = -1;
        flatList.clear();
        super.notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return lastCount = lastCount == -1 ? count(menu, 0) : lastCount;
    }

    int count(Menu m, int sum) {
        if(m!=null) {
            for (int i = 0; i < m.size(); i++) {
                if(!m.getItem(i).isVisible())
                    continue;
                flatList.add(m.getItem(i));
                sum = count(m.getItem(i).getSubMenu(), sum+1);
            }
        }
        return sum;
    }

    @Override
    public Object getItem(int i) {
        return flatList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return ((MenuItem)getItem(i)).getItemId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        MenuItem item = (MenuItem)getItem(i);
        Resources res = context.getResources();

        if(item.hasSubMenu()) {
            view = LayoutInflater.from(context).inflate(R.layout.nav_list_header, null);
            view.setClickable(true);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.nav_list_entry, null);
            view.setClickable(false);

            if(item.getIntent()!=null) {
                int count = item.getIntent().getIntExtra("count", -1);
                ((TextView) view.findViewById(R.id.count)).setText(Integer.toString(count));
                if(count < 0)
                    view.findViewById(R.id.count).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.count).setVisibility(View.GONE);
            }
        }

        view.findViewById(R.id.separator).setVisibility(
                i > 0 &&
                        ((MenuItem) getItem(i - 1)).getGroupId() != item.getGroupId()
                        ||
                        item.hasSubMenu()
                        ? View.VISIBLE : View.GONE
        );


        TextView title = ((TextView)view.findViewById(R.id.title));
        int[] colors = item.equals(selected) ? selectedColors : unselectedColors;
        view.setBackgroundColor(res.getColor(colors[0]));
        title.setTextColor(res.getColor(colors[1]));
        if(item.getIcon()!=null && !item.hasSubMenu()) {
            Drawable icon = item.getIcon();
            ImageView imgV = ((ImageView) view.findViewById(R.id.image_view));
            imgV.setColorFilter(null);
            imgV.setColorFilter(res.getColor(colors[2]), PorterDuff.Mode.SRC_IN);
            imgV.setImageDrawable(icon);
        }

        title.setText(item.getTitle());
        return view;
    }


}

