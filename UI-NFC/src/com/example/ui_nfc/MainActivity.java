package com.example.ui_nfc;


import java.util.ArrayList;

import com.example.ui_nfc.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


// Activity for displaying all the TAGs and adding one and so on...
public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private static final int NEW_TAG_REQUEST = 1;
	private static final int EDIT_TAG_REQUEST = 2;
//	NFC_Adapter mAdapter;

	private ArrayList<Tag> tagList;
	private ListView lv;
	private DatabaseHelper db;
//	private TextView header;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		LayoutInflater inflater = LayoutInflater.from(this);
		View v = inflater.inflate(R.layout.activity_main, null);
		setContentView(v);				
		tagList = new ArrayList<Tag>();
		db = new DatabaseHelper(this);
		
		lv = (ListView) findViewById(R.id.tagList);
//		TextView header = (TextView) findViewById(R.id.headerText);		
		
		refreshListView();
	}

	
	public void refreshListView() {		
		tagList = db.getAllItems();
		
		ArrayAdapter<Tag> mAdapter = new TagAdapter(MainActivity.this, 
				R.layout.tag_item, tagList);
		lv.setAdapter(mAdapter);
	}

	
	public void newEntry(){		
		Intent clickIntent = new Intent(MainActivity.this, AddTagActivity.class);
		startActivityForResult(clickIntent, NEW_TAG_REQUEST);
	}
	
	public void editEntry(Tag mTag){
		
		Intent intent = new Intent(MainActivity.this, AddTagActivity.class);
		Bundle data = new Bundle();
		
		//put all actual values to the Bundle
		data.putInt("item_id", mTag.getItemID());
		data.putInt("tag_id", mTag.getTagID());
		data.putString("tag_name", mTag.getTagName());
		data.putBoolean("reminder", mTag.shouldRemind());
		data.putLong("last_scan", mTag.getScanDateInMillis());
		data.putBoolean("at_human", mTag.isWearing());
		data.putString("tag_category", mTag.getCategory());
		intent.putExtras(data);
		
		startActivityForResult(intent, EDIT_TAG_REQUEST);
	}
	
//	public void updateEntry(Tag mTag){
//		//TODO
//	}
//	
//	public void deleteEntry(Tag mTag){
//		//TODO
//	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent values){
		//TODO
		DatabaseHelper db = new DatabaseHelper(MainActivity.this);
		Tag mTag = new Tag();
		Bundle data;
		
		switch(resultCode){
		case RESULT_CANCELED:
			//nothing to do...
			Log.i(TAG, "Result canceled");
			break;
			
		case RESULT_OK:
			switch(requestCode){
			case NEW_TAG_REQUEST:				
				Log.i(TAG, "New Tag Request");
				//TODO
				data = values.getExtras();
				if(data != null){
					mTag.setTagID(data.getInt("tag_id"));
					Log.i(TAG, "TagID: " + Integer.valueOf(data.getInt("tag_id")).toString());
					mTag.setTagName(data.getString("tag_name"));
					mTag.setCategory(data.getString("tag_category"));
					
					//add mTag to database
					mTag = db.addItem(mTag);
					
					refreshListView();
				}
				
				break;
				
			case EDIT_TAG_REQUEST:
				//TODO
				data = values.getExtras();
				if(data != null){
					mTag.setItemID(data.getInt("item_id"));
					mTag.setTagID(data.getInt("tag_id"));
					mTag.setTagName(data.getString("tag_name"));
					mTag.setRemind(data.getBoolean("reminder"));
					mTag.setScanDateInMillis(data.getLong("last_scan"));
					mTag.setWearing(data.getBoolean("at_human"));
					mTag.setCategory(data.getString("tag_category"));
					
					//update the database
					db.updateItem(mTag);
					
					refreshListView();
				}
				break;
			}
			break;
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case R.id.action_settings:
			return true;
			
		case R.id.delete_all:
			//TODO delete all the tags!
			return true;
			
		case R.id.action_add:
			Log.i(TAG, "add-button pressend");
			newEntry();			
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
