package com.pritam.noteskeeper.notes;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pritam.noteskeeper.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Pritam on 10/14/2017.
 */

class CustomListAdapter extends BaseAdapter implements View.OnClickListener {
    private Activity activity;
    private LayoutInflater inflater;
    private List<HashMap<String, Object>> rowItems;
   // private View promptsView;

    public CustomListAdapter(Activity activity, List<HashMap<String, Object>> rowItems) {
        this.activity = activity;
        this.rowItems = rowItems;
    }

    public List getRowItems() {
        return this.rowItems;
    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int location) {
        return rowItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.listitem, null);

        // getting data for the row
        String details = rowItems.get(position).get("details").toString();
        if( details != null && details .length() > 0) {
            ((TextView) convertView.findViewById(R.id.details_item)).setText(details);
        } else  {
            ((TextView) convertView.findViewById(R.id.details_item)).setVisibility(View.GONE);
        }

        String title = rowItems.get(position).get("title").toString();
        if( title != null && title .length() > 0) {
            ((TextView) convertView.findViewById(R.id.title_item)).setText(title);
        } else  {
            ((TextView) convertView.findViewById(R.id.title_item)).setVisibility(View.GONE);
        }

        //((TextView) convertView.findViewById(R.id.time_item)).setText(rowItems.get(position).get("time").toString());

        String color = rowItems.get(position).get("marked").toString();
        if(color != null && color .length() > 0) {
            try{
                ((LinearLayout) convertView.findViewById(R.id.LinearLayout)).setBackgroundColor(Integer.parseInt(color));
            } catch (Exception e) {
                ((LinearLayout) convertView.findViewById(R.id.LinearLayout)).setBackgroundColor(activity.getResources().getColor(R.color.md_grey_100));
            }
        } else {
            ((LinearLayout) convertView.findViewById(R.id.LinearLayout)).setBackgroundColor(activity.getResources().getColor(R.color.md_grey_100));
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {

    }
}
