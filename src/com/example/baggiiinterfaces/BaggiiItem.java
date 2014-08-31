package com.example.baggiiinterfaces;

import android.graphics.drawable.Drawable;

public class BaggiiItem {
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private String title;
	private boolean isActive;
	private long distnace;
	private String picture;
	private String address;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public long getDistnace() {
		return distnace;
	}

	public void setDistnace(long distnace) {
		this.distnace = distnace;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	@Override
	public String toString() {
		String result = "Baggii " + this.getId() + " with title "
				+ this.getTitle() + " with distance " + this.getDistnace()
				+ " and picture " + this.getPicture() + " is "
				+ this.isActive();
		return result;
	}

}
