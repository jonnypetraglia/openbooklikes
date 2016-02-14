package com.qweex.openbooklikes;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class MuhAdapter extends BaseAdapter {
    Menu menu;
    int lastCount = 0;
    Context context;

    public Menu getMenu() {
        return menu;
    }

    public MuhAdapter(Menu m, Context c) {
        menu = m;
        context = c;
    }

    @Override
    public int getCount() {
        int c = count(menu, 0);
        if(c!=lastCount)
            this.notifyDataSetInvalidated();
        Log.d("WWW", lastCount + " vs " + c + " -- " + menu.size());
        return lastCount = c;
    }

    int count(Menu m, int sum) {
        if(m!=null) {
            for (int i = 0; i < m.size(); i++) {
                if(m.getItem(i).isVisible())
                    sum = count(m.getItem(i).getSubMenu(), sum+1);
            }
        }
        return sum;
    }

    MenuItem get(Menu m, int[] want) {
        Log.d("getting", want[0] + "!");
        if(m!=null)
            for(int i=0; i<m.size(); i++) {
                Log.d("got", m.getItem(i).getTitle().toString());
                if(!m.getItem(i).isVisible())
                    continue;
                if(want[0]==0)
                    return m.getItem(i);

                want[0]--;

                MenuItem mi = get(m.getItem(i).getSubMenu(), want);
                Log.d("after", m.getItem(i).getTitle().toString());
                if(mi!=null)
                    return mi;
            }
        return null;
    }

    @Override
    public Object getItem(int i) {
        MenuItem mi = get(menu, new int[] { i });
        Log.d("getItem", mi.getTitle() + " = " + i);
        return mi;
    }

    @Override
    public long getItemId(int i) {
        return ((MenuItem)getItem(i)).getItemId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        MenuItem item = (MenuItem)getItem(i);

        if(item.hasSubMenu()) {
            view = LayoutInflater.from(context).inflate(R.layout.nav_list_header, null);
            view.setClickable(true);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.nav_list_entry, null);
            view.setClickable(false);
            ((ImageView)view.findViewById(R.id.image_view)).setImageDrawable(item.getIcon());

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
                i>0 &&
                        ((MenuItem)getItem(i-1)).getGroupId() != item.getGroupId()
                        ||
                        item.hasSubMenu()
                ? View.VISIBLE : View.GONE
        );

        ((TextView)view.findViewById(R.id.title)).setText(item.getTitle());
        return view;
    }
}

