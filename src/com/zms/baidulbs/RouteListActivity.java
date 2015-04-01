package com.zms.baidulbs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RouteListActivity extends Activity {
	private ListView routeList;
	private final String ROUTE_PATH = "/sdcard/Route/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_list);

		routeList = (ListView) findViewById(R.id.routeList);

		File[] files = new File(ROUTE_PATH).listFiles();
		final List<String> fileNameList = new ArrayList<String>();
		for (File file : files) {
			fileNameList.add(file.getName());
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, fileNameList);
		routeList.setAdapter(adapter);
		routeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(android.widget.AdapterView<?> parent,
					android.view.View view, int position, long id) {
				Intent intent = new Intent(RouteListActivity.this,
						RouteShowActivity.class);
				intent.putExtra("filePath", fileNameList.get(position));
				startActivity(intent);
			}
		});
	}
}
