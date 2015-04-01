package com.zms.baidulbs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RouteActivity extends Activity {
	private Button btnSetting;
	private Button btnStartRecord;
	private Button btnStopRecord;
	private Button btnList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_main);
		btnSetting = (Button) findViewById(R.id.btnSetting);
		btnStartRecord = (Button) findViewById(R.id.btnStartRecord);
		btnStopRecord = (Button) findViewById(R.id.btnStopRecord);
		btnList = (Button) findViewById(R.id.btnList);

		btnSetting.setOnClickListener(new MyOnClickListener());
		btnStartRecord.setOnClickListener(new MyOnClickListener());
		btnStopRecord.setOnClickListener(new MyOnClickListener());
		btnList.setOnClickListener(new MyOnClickListener());
	}

	class MyOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == btnSetting) {
				Intent intent = new Intent(RouteActivity.this,
						RouteSettingActivity.class);
				startActivity(intent);
			} else if (v == btnStartRecord) {
				Intent intent = new Intent(RouteActivity.this,
						RouteRecordService.class);
				startService(intent);

			} else if (v == btnStopRecord) {
				Intent intent = new Intent(RouteActivity.this,
						RouteRecordService.class);
				stopService(intent);
			} else if (v == btnList) {
				Intent intent = new Intent(RouteActivity.this,
						RouteListActivity.class);
				startActivity(intent);
			}

		}
	}

}
