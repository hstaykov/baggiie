package com.example.baggiiinterfaces;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemAdapter extends ArrayAdapter<BaggiiItem> {
	private Context context;
	private int layout_id;
	private List<BaggiiItem> items;

	public ItemAdapter(Context context, int resource, int textViewResourceId,
			List<BaggiiItem> objects) {
		super(context, resource, textViewResourceId, objects);
		this.context = context;
		this.layout_id = resource;
		this.items = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater infl = ((Activity) context).getLayoutInflater();
		View itemRow = infl.inflate(layout_id, parent, false);
		TextView txtItemName = (TextView) itemRow
				.findViewById(R.id.txt_item_name);
		txtItemName.setText(items.get(position).getTitle());

		RelativeLayout relBack = (RelativeLayout) itemRow
				.findViewById(R.id.rel_item_background);

		Resources res = context.getResources();
		BitmapDrawable back;
		if (items.get(position).getPicture() != null) {
			back = new BitmapDrawable(items.get(position)
					.getPicture());
		}
		else{
			back = (BitmapDrawable) res.getDrawable(R.drawable.baggii);
		}
		relBack.setBackground(back);
		// if (items.get(position).getPicture().equals("keys"))
		//
		// relBack.setBackground(res.getDrawable(R.drawable.keys));
		// else if (items.get(position).getPicture().equals("shoes")) {
		// relBack.setBackground(res.getDrawable(R.drawable.shoes));
		// } else if (items.get(position).getPicture().equals("bag"))
		// relBack.setBackground(res.getDrawable(R.drawable.bag));
		// else if (items.get(position).getPicture().equals("phone"))
		// relBack.setBackground(res.getDrawable(R.drawable.phone));
		return itemRow;
	}
}
