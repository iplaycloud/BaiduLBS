package com.zms.baidulbs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RouteSettingActivity extends Activity {
	private Button btnOffset, btnZoomLevel;
	private TextView tvSetting;
	private EditText etOffset, etZoomLevel;

	private SharedPreferences sharedPreferences;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_setting);

		tvSetting = (TextView) findViewById(R.id.tvSetting);
		etOffset = (EditText) findViewById(R.id.etOffset);
		etZoomLevel = (EditText) findViewById(R.id.etZoomLevel);
		btnZoomLevel = (Button) findViewById(R.id.btnZoomLevel);
		btnOffset = (Button) findViewById(R.id.btnOffset);

		btnZoomLevel.setOnClickListener(new MyOnClickListener());
		btnOffset.setOnClickListener(new MyOnClickListener());

		sharedPreferences = getSharedPreferences("RouteSetting",
				getApplicationContext().MODE_PRIVATE);
		editor = sharedPreferences.edit();

		showSetting();
	}

	class MyOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (v == btnOffset) {
				String str = etOffset.getText().toString();
				if (str != null && str.length() > 0) {
					int offset = Integer.valueOf(str);
					if (offset >= 1 && offset <= 20) {
						setOffset(offset);
						showSetting();
					} else {
						Toast.makeText(getApplicationContext(), "取样级别为1-20",
								Toast.LENGTH_SHORT).show();
					}
				}

			} else if (v == btnZoomLevel) {
				String str = etZoomLevel.getText().toString();
				if (str != null && str.length() > 0) {
					float level = Float.parseFloat(str);
					if (level >= 0 && level <= 19) {
						setZoomLevel(level);
						showSetting();
					} else {
						Toast.makeText(getApplicationContext(), "缩放级别为0-19",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	public void showSetting() {
		tvSetting.setText("Route Config:\n" + "offset:" + getOffset()
				+ "\nZoomLevel:" + getZoomLevel());
	}

	public int getOffset() {
		return sharedPreferences.getInt("offset", 1);
	}

	public void setOffset(int offset) {
		editor.putInt("offset", offset);
		editor.commit();
	}

	public float getZoomLevel() {
		return sharedPreferences.getFloat("zoomLevel", 19f);
	}

	public void setZoomLevel(float zoomLevel) {
		editor.putFloat("zoomLevel", zoomLevel);
		editor.commit();
	}

}
