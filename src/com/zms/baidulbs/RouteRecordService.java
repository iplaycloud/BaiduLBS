package com.zms.baidulbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class RouteRecordService extends Service {
	private LocationClient mLocationClient;
	private final String ROUTE_PATH = Environment.getExternalStorageDirectory()
			.getPath() + "/Route/";
	private String startTime = "";
	private String stopTime = "";

	private List<RoutePoint> list = new ArrayList<RoutePoint>();
	private RouteAdapter adapter = new RouteAdapter();

	private int startId = 1; // 轨迹点初始ID
	private int defaultDelay = 500; // 采集轨迹点间隔(ms)
	private final static double ERROR_CODE = 55.555;
	private double routeLng, routeLat;

	private boolean isEncrypt = true; // true:读取百度加密经纬度 false:读取设备提供经纬度
	private boolean isDebug = true;

	// 设备定位经纬度
	private enum DeviceLocType {
		LATITUDE, LONGITUDE
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
		// 3600
	}

	@Override
	public void onCreate() {
		super.onCreate();

		InitLocation(LocationMode.Hight_Accuracy, "bd09ll", defaultDelay, false);
		// 初始化路径
		File filestoreMusic = new File(ROUTE_PATH);
		if (!filestoreMusic.exists()) {
			filestoreMusic.mkdir();
		}
		startTime = getTimeStr();
		if (isDebug) {
			Toast.makeText(getApplicationContext(), "Start Record Route",
					Toast.LENGTH_SHORT).show();
		}
		// 开启轨迹记录线程
		new Thread(new RouteRecordThread()).start();
	}

	public class RouteRecordThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(defaultDelay);
					Message message = new Message();
					message.what = 1;
					recordHandler.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	final Handler recordHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				startRecordRoute();
			}
			super.handleMessage(msg);
		}
	};

	private void startRecordRoute() {
		// 获取设备经纬度
		if (!isEncrypt) {
			routeLat = getDeviceLocation(DeviceLocType.LATITUDE);
			routeLng = getDeviceLocation(DeviceLocType.LONGITUDE);
			if (isDebug)
				Toast.makeText(getApplicationContext(),
						"Device Loc:" + routeLat + "," + routeLng,
						Toast.LENGTH_SHORT).show();
		}

		RoutePoint routePoint = new RoutePoint();
		if (routeLng != 5.55 && routeLat != 5.55 && routeLng != 0.0
				&& routeLat != 0.0) {
			if (list.size() > 0
					&& list.get(list.size() - 1).getLat() == routeLat
					&& (list.get(list.size() - 1).getLng() == routeLng)) {
				if (isDebug) {
					// Toast.makeText(getApplicationContext(),
					// "Route not change",
					// Toast.LENGTH_SHORT).show();
				}
			} else {
				routePoint.setId(startId++);
				routePoint.setLng(routeLng);
				routePoint.setLat(routeLat);
				list.add(routePoint);
			}
		}
	}

	/**
	 * 获取设备提供的经纬度，Network或GPS
	 * 
	 * @param type
	 *            请求经度还是纬度
	 * @return
	 */
	private double getDeviceLocation(DeviceLocType type) {
		double deviceLat = ERROR_CODE;
		double deviceLng = ERROR_CODE;

		LocationManager locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				deviceLat = location.getLatitude();
				deviceLng = location.getLongitude();
			} else {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, defaultDelay, 0,
						new deviceLocationListener());
				Location location1 = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location1 != null) {
					deviceLat = location1.getLatitude(); // 经度
					deviceLng = location1.getLongitude(); // 纬度
				}
			}
		} else {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, defaultDelay, 0,
					new deviceLocationListener());
			Location location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				deviceLat = location.getLatitude(); // 经度
				deviceLng = location.getLongitude(); // 纬度
			}
		}
		if (type == DeviceLocType.LATITUDE)
			return deviceLat;
		else if (type == DeviceLocType.LONGITUDE)
			return deviceLng;
		else
			return ERROR_CODE;
	}

	/**
	 * 设备位置监听器
	 * 
	 */
	class deviceLocationListener implements LocationListener {

		// Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		// Provider被enable时触发此函数，比如GPS被打开
		@Override
		public void onProviderEnabled(String provider) {

		}

		// Provider被disable时触发此函数，比如GPS被关闭
		@Override
		public void onProviderDisabled(String provider) {
		}

		// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
		@Override
		public void onLocationChanged(Location location) {
			// routeLat = location.getLatitude(); // 经度
			// routeLng = location.getLongitude(); // 纬度
		}
	};

	public String readFileSdcard(String fileName) {
		String res = "";
		try {
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 此处为res 赋值空格即" "而不是"",否则运行不通过。
		if (res.equals("")) {
			res = " ";
		}
		return res;
	}

	public void writeFileSdcard(String fileName, String message) {
		if (message.equals("[]")) {
			message = "";
		}
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param tempMode
	 *            LocationMode.Hight_Accuracy-高精度
	 *            LocationMode.Battery_Saving-低功耗
	 *            LocationMode.Device_Sensors-仅设备
	 * @param tempCoor
	 *            gcj02-国测局加密经纬度坐标 bd09ll-百度加密经纬度坐标 bd09-百度加密墨卡托坐标
	 * @param frequence
	 *            间隔时间
	 * @param isNeedAddress
	 *            是否需要地址
	 */
	private void InitLocation(LocationMode tempMode, String tempCoor,
			int frequence, boolean isNeedAddress) {

		mLocationClient = new LocationClient(this.getApplicationContext());
		mLocationClient.registerLocationListener(new MyLocationListener());
		// mGeofenceClient = new GeofenceClient(getApplicationContext());

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);
		option.setCoorType(tempCoor);
		option.setScanSpan(frequence);
		option.setIsNeedAddress(isNeedAddress);
		mLocationClient.setLocOption(option);

		mLocationClient.start();
	}

	private String getTimeStr() {
		long nowTime = System.currentTimeMillis();
		Date date = new Date(nowTime);
		String strs = "" + ERROR_CODE;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			strs = sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strs;
	}

	/**
	 * 初始化轨迹文件路径和名称
	 * 
	 * @return String
	 */
	private String getFilePath() {
		stopTime = getTimeStr();
		String format = ".json";
		if (isDebug)
			format = ".txt";
		return ROUTE_PATH + startTime + "-" + stopTime + format;
	}

	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 读取百度加密经纬度
			if (isEncrypt) {
				routeLng = location.getLongitude();
				routeLat = location.getLatitude();

			}
			// StringBuffer sb = new StringBuffer(256);
			// sb.append("time : "+location.getTime());
			// sb.append("\nerror code : ");
			// sb.append(location.getLocType());
			// sb.append("\nlatitude : "+location.getLatitude());
			// sb.append("\nlongitude : "+location.getLongitude());
			// sb.append("\nradius : "+location.getRadius());
			// if (location.getLocType() == BDLocation.TypeGpsLocation) {
			// sb.append("\nspeed : "+location.getSpeed());
			// sb.append("\nsatellite : "+location.getSatelliteNumber());
			// sb.append("\ndirection : "+location.getDirection());
			// sb.append("\naddr : "+location.getAddrStr());
			// } else if (location.getLocType() ==
			// BDLocation.TypeNetWorkLocation) {
			// sb.append("\naddr : "+location.getAddrStr());
			// sb.append("\noperationers : "+location.getOperators());
			// }
			// logMsg(sb.toString());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLocationClient.stop();

		// 服务销毁时，保存轨迹点到文件
		if (isDebug) {
			Toast.makeText(getApplicationContext(), "Saving Route Files",
					Toast.LENGTH_SHORT).show();
		}
		String saveString = adapter.setJsonString(list);
		writeFileSdcard(getFilePath(), saveString);
	}

}
