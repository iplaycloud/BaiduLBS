package com.zms.baidulbs;

public class RoutePoint {
	private int id;
	private double lng; // longitude 经度
	private double lat; // latitude 纬度

	@Override
	public String toString() {
		return "[id=" + id + ", lng=" + lng + ", lat=" + lat + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}
}
