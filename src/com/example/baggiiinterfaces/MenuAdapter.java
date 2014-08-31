package com.example.baggiiinterfaces;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<MenuItem> navDrawerItems;

	public MenuAdapter(Context context, ArrayList<MenuItem> navDrawerItems) {
		this.context = context;
		this.navDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position > 0) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) context
						.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater
						.inflate(R.layout.drawer_list_item, null);
			}

			ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
			TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
			TextView txtCount = (TextView) convertView
					.findViewById(R.id.counter);

			imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
			txtTitle.setText(navDrawerItems.get(position).getTitle());

			// displaying count
			// check whether it set visible or not
			if (navDrawerItems.get(position).getCounterVisibility()) {
				txtCount.setText(navDrawerItems.get(position).getCount());
			} else {
				// hide the counter view
				txtCount.setVisibility(View.GONE);
			}
		} else {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.drawer_list_first_item,
					null);
		}

		return convertView;
	}
}
