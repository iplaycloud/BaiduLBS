package com.zms.baidulbs;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class RouteAdapter {
	Gson gson = new Gson();

	public String setJsonString(List<RoutePoint> routePoint) {
		String str = gson.toJson(routePoint);
		return str;
	}

	// 解析 读取
	public List<RoutePoint> getJsonString(String str) {

		List<RoutePoint> ps = gson.fromJson(str,
				new TypeToken<List<RoutePoint>>() {
				}.getType());
		return ps;
	}

}
