package com.zms.baidulbs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RouteSettingActivity extends Activity {
	private Button btnOffset, btnZoomLevel;
	private TextView tvSetting;
	private EditText etOffset, etZoomLevel;
	private ToggleButton toggleDebug;

	private SharedPreferences sharedPreferences;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_setting);

		sharedPreferences = getSharedPreferences("RouteSetting",
				getApplicationContext().MODE_PRIVATE);
		editor = sharedPreferences.edit();

		tvSetting = (TextView) findViewById(R.id.tvSetting);
		etOffset = (EditText) findViewById(R.id.etOffset);
		etZoomLevel = (EditText) findViewById(R.id.etZoomLevel);
		btnZoomLevel = (Button) findViewById(R.id.btnZoomLevel);
		btnOffset = (Button) findViewById(R.id.btnOffset);
		toggleDebug = (ToggleButton) findViewById(R.id.toggleDebug);
		toggleDebug.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
		toggleDebug.setChecked(getIsDebug());

		btnZoomLevel.setOnClickListener(new MyOnClickListener());
		btnOffset.setOnClickListener(new MyOnClickListener());

		showSetting();
	}

	class MyOnCheckedChangeListener implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				setIsDebug(true);
			} else {
				setIsDebug(false);
			}
			showSetting();
		}
	}

	class MyOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (v == btnOffset) {
				String str = etOffset.getText().toString();
				if (str != null && str.length() > 0) {
					int offset = Integer.valueOf(str);
					if (offset >= 1 && offset <= 10) {
						setOffset(offset);
						showSetting();
					} else {
						Toast.makeText(getApplicationContext(), "取样级别为1-10",
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
		tvSetting
				.setText("Route Config Now:\n" + "offset:" + getOffset()
						+ "\nZoomLevel:" + getZoomLevel() + "\nisDebug:"
						+ getIsDebug());
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

	public boolean getIsDebug() {
		return sharedPreferences.getBoolean("isDebug", false);
	}

	public void setIsDebug(boolean isDebug) {
		editor.putBoolean("isDebug", isDebug);
		editor.commit();
	}

}
