package com.example.baggiiinterfaces;

import java.util.ArrayList;

public class MenuHelper {
	public static ArrayList<MenuItem> getMenuItems() {
		ArrayList<MenuItem> items = new ArrayList<MenuItem>();
		items.add(new MenuItem("Dummy", R.id.icon));
		items.add(new MenuItem("Baggies", R.drawable.menu_baggies, true, String
				.valueOf(4)));
		items.add(new MenuItem("Add new Baggii", R.drawable.menu_add));
		items.add(new MenuItem("Settings", R.drawable.menu_settings));
		items.add(new MenuItem("Sign out", R.drawable.menu_logout));
		return items;
	}
}
