package com.zms.baidulbs;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine.DrivingStep;
import com.zms.baidulbs.R;

public class RouteShowActivity extends Activity {
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private InfoWindow mInfoWindow;
	private Marker mMarkerStart;
	private Marker mMarkerEnd;

	public double mRouteLatitude = 0.0;
	public double mRouteLongitude = 0.0;

	private final String ROUTE_PATH = "/sdcard/Route/";
	private RouteAdapter routeAdapter = new RouteAdapter();

	// 初始化全局 bitmap 信息，不用时及时 recycle
	BitmapDescriptor iconStart = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_st);
	BitmapDescriptor iconEnd = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_en);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_show);

		String filePath = "";
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			filePath = extras.getString("filePath");
			setTitle(filePath.substring(0, filePath.length() - 4));
		} else {
			Toast.makeText(getApplicationContext(), "轨迹文件不存在",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		mMapView = (MapView) findViewById(R.id.routeMap);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMarkerClickListener(new MyOnMarkerClickListener());
		addRouteToMap(filePath);

	}

	/**
	 * 从文件读取经纬度表单
	 * 
	 * @param fileName
	 * @return
	 */
	public List<RoutePoint> readFileSdcard(String fileName) {
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
		if (res.equals("")) {
			res = " ";
		}
		return routeAdapter.getJsonString(res);
	}

	/**
	 * 添加轨迹到地图
	 * 
	 * @param path
	 */
	public void addRouteToMap(String path) {

		// 初始化轨迹点
		List<LatLng> points = getRoutePoints(path);

		if (points.size() < 2) {
			Toast.makeText(getApplicationContext(), "轨迹点数目小于2个",
					Toast.LENGTH_SHORT).show();
			finish();
		} else {
			// 绘制轨迹
			OverlayOptions ooPolyline = new PolylineOptions().width(5)
					.color(0xAA0000FF).points(points);
			mBaiduMap.addOverlay(ooPolyline);

			// 定位地图到轨迹起点位置
			MapStatusUpdate u1 = MapStatusUpdateFactory
					.newLatLng(points.get(1));
			mBaiduMap.setMapStatus(u1);

			MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(getZoomLevel());
			mBaiduMap.animateMapStatus(msu);

			// 绘制起始点Marker
			LatLng llStart = points.get(0);
			LatLng llEnd = points.get(points.size() - 1);
			OverlayOptions ooStart = new MarkerOptions().position(llStart)
					.icon(iconStart).zIndex(9).draggable(true);
			mMarkerStart = (Marker) (mBaiduMap.addOverlay(ooStart));
			OverlayOptions ooEnd = new MarkerOptions().position(llEnd)
					.icon(iconEnd).zIndex(9).draggable(true);
			mMarkerEnd = (Marker) (mBaiduMap.addOverlay(ooEnd));
		}
	}

	public List<LatLng> getRoutePoints(String fileName) {

		List<RoutePoint> list = readFileSdcard(ROUTE_PATH + fileName);
		list = optimizePoints(list);

		List<LatLng> points = new ArrayList<LatLng>(list.size());
		for (int i = 0; i < list.size(); i++) {
			i = i + getOffset() - 1;
			if (i < list.size())
				points.add(new LatLng(list.get(i).getLat(), list.get(i)
						.getLng()));
		}
		return points;
	}

	/**
	 * 轨迹点经纬度优化，使连线平滑
	 * 
	 * @param inPoint
	 * @return
	 */
	public List<RoutePoint> optimizePoints(List<RoutePoint> inPoint) {
		int size = inPoint.size();
		List<RoutePoint> outPoint;

		int i;
		if (size < 5) {
			return inPoint;
		} else {
			// 经度优化
			inPoint.get(0)
					.setLat((3.0 * inPoint.get(0).getLat() + 2.0
							* inPoint.get(1).getLat() + inPoint.get(2).getLat() - inPoint
							.get(4).getLat()) / 5.0);
			inPoint.get(1)
					.setLat((4.0 * inPoint.get(0).getLat() + 3.0
							* inPoint.get(1).getLat() + 2
							* inPoint.get(2).getLat() + inPoint.get(3).getLat()) / 10.0);

			for (i = 2; i <= size - 3; i++) {
				inPoint.get(i).setLat(
						(inPoint.get(i - 2).getLat()
								+ inPoint.get(i - 1).getLat()
								+ inPoint.get(i).getLat()
								+ inPoint.get(i + 1).getLat() + inPoint.get(
								i + 2).getLat()) / 5.0);
			}
			inPoint.get(size - 2).setLat(
					(4.0 * inPoint.get(size - 1).getLat() + 3.0
							* inPoint.get(size - 2).getLat() + 2
							* inPoint.get(size - 3).getLat() + inPoint.get(
							size - 4).getLat()) / 10.0);
			inPoint.get(size - 1).setLat(
					(3.0 * inPoint.get(size - 1).getLat() + 2.0
							* inPoint.get(size - 2).getLat()
							+ inPoint.get(size - 3).getLat() - inPoint.get(
							size - 5).getLat()) / 5.0);

			// 纬度优化
			inPoint.get(0)
					.setLng((3.0 * inPoint.get(0).getLng() + 2.0
							* inPoint.get(1).getLng() + inPoint.get(2).getLng() - inPoint
							.get(4).getLng()) / 5.0);
			inPoint.get(1)
					.setLng((4.0 * inPoint.get(0).getLng() + 3.0
							* inPoint.get(1).getLng() + 2
							* inPoint.get(2).getLng() + inPoint.get(3).getLng()) / 10.0);

			for (i = 2; i <= size - 3; i++) {
				inPoint.get(i).setLng(
						(inPoint.get(i - 2).getLng()
								+ inPoint.get(i - 1).getLng()
								+ inPoint.get(i).getLng()
								+ inPoint.get(i + 1).getLng() + inPoint.get(
								i + 2).getLng()) / 5.0);
			}
			inPoint.get(size - 2).setLng(
					(4.0 * inPoint.get(size - 1).getLng() + 3.0
							* inPoint.get(size - 2).getLng() + 2
							* inPoint.get(size - 3).getLng() + inPoint.get(
							size - 4).getLng()) / 10.0);
			inPoint.get(size - 1).setLng(
					(3.0 * inPoint.get(size - 1).getLng() + 2.0
							* inPoint.get(size - 2).getLng()
							+ inPoint.get(size - 3).getLng() - inPoint.get(
							size - 5).getLng()) / 5.0);
		}
		return inPoint;
	}

	class MyOnMarkerClickListener implements OnMarkerClickListener {
		public boolean onMarkerClick(final Marker marker) {
			Button button = new Button(getApplicationContext());
			button.setBackgroundResource(R.drawable.popup);
			OnInfoWindowClickListener listener = null;
			if (marker == mMarkerStart) {
				button.setText("起始位置");
				listener = new OnInfoWindowClickListener() {
					public void onInfoWindowClick() {
						// LatLng ll = marker.getPosition();
						// LatLng llNew = new LatLng(ll.latitude + 0.005,
						// ll.longitude + 0.005);
						// marker.setPosition(llNew);
						// mBaiduMap.hideInfoWindow();
					}
				};
				LatLng ll = marker.getPosition();
				mInfoWindow = new InfoWindow(
						BitmapDescriptorFactory.fromView(button), ll, -47,
						listener);
				mBaiduMap.showInfoWindow(mInfoWindow);
			} else if (marker == mMarkerEnd) {
				button.setText("终点位置");
			}
			return true;
		}
	}

	/**
	 * 读取设置的缩放倍数
	 * 
	 * @return
	 */
	public float getZoomLevel() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"RouteSetting", getApplicationContext().MODE_PRIVATE);
		return sharedPreferences.getFloat("zoomLevel", 19f);
	}

	/**
	 * 读取轨迹点采样偏移量
	 * 
	 * @return
	 */
	public int getOffset() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"RouteSetting", getApplicationContext().MODE_PRIVATE);
		return sharedPreferences.getInt("offset", 1);
	}

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		mMapView.onDestroy();
		super.onDestroy();
		// 回收 bitmap 资源
		iconStart.recycle();
		iconEnd.recycle();
	}

}
