package com.example.nfc_combine;


import java.util.ArrayList;

import com.example.bluetooth.BluetoothActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


// Activity for displaying all the TAGs and adding one and so on...
public class DatabaseActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private static final int NEW_TAG_REQUEST = 1;
	private static final int EDIT_TAG_REQUEST = 2;

	private ArrayList<NfcTag> tagList;
	private ListView mListView;
	private DatabaseHelper db;
	private Button bltBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
		LayoutInflater inflater = LayoutInflater.from(this);
		View v = inflater.inflate(R.layout.database_main, null);
		setContentView(v);				
		tagList = new ArrayList<NfcTag>();
		db = new DatabaseHelper(this);
		
		mListView = (ListView) findViewById(R.id.tagList);
		
		refreshListView();
		
		bltBtn = (Button) findViewById(R.id.connectionButton);
		bltBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startBlt = new Intent(DatabaseActivity.this, BluetoothActivity.class);
				startBlt.setAction("started_from_Main");
				startActivity(startBlt);
			}
		});
	}
	
	public void refreshListView() {		
		tagList = db.getAllItems();
		ArrayAdapter<NfcTag> mAdapter = new TagAdapter(DatabaseActivity.this, 
				R.layout.tag_item, tagList);
		mListView.setAdapter(mAdapter);
	}

	public void newEntry(){
		Intent clickIntent = new Intent(DatabaseActivity.this, AddTagActivity.class);
		startActivityForResult(clickIntent, NEW_TAG_REQUEST);
	}
	
	public void editEntry(NfcTag mTag){
		Intent intent = new Intent(DatabaseActivity.this, AddTagActivity.class);
		Bundle data = new Bundle();
		
		//put all actual values to the Bundle
		data.putInt("item_id", mTag.getItemID());
		data.putString("tag_id", mTag.getTagID());
		data.putString("tag_name", mTag.getTagName());
		data.putBoolean("reminder", mTag.shouldRemind());
		data.putLong("last_scan", mTag.getScanDateInMillis());
		data.putBoolean("at_human", mTag.isWearing());
		data.putString("tag_category", mTag.getCategory());
		intent.putExtras(data);
		
		startActivityForResult(intent, EDIT_TAG_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent values){
		//TODO
		DatabaseHelper db = new DatabaseHelper(DatabaseActivity.this);
		NfcTag mTag = new NfcTag();
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
				//TODO: Make successful writing of a Tag
				data = values.getExtras();
				if(data != null){
					mTag.setTagID(data.getString("tag_id"));
					Log.i(TAG, "TagID: " + Integer.valueOf(data.getInt("tag_id")).toString());
					mTag.setTagName(data.getString("tag_name"));
					mTag.setCategory(data.getString("tag_category"));
					mTag.setRemind(data.getBoolean("reminder"));
					mTag.setWearing(data.getBoolean("at_human"));
					//add mTag to database
					mTag = db.addItem(mTag);
                    refreshListView();
				}
				break;
				
			case EDIT_TAG_REQUEST:
				//TODO: Change method to just use the Item, avoids too many calls to NfcTag
				data = values.getExtras();
				if(data != null){
					mTag.setItemID(data.getInt("item_id"));
					mTag.setTagID(data.getString("tag_id"));
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
			Log.i(TAG, "add-button pre-send");
			newEntry();			
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(onReceiverFromService, new IntentFilter(ReminderService.BROADCAST_UPDATEUI));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onReceiverFromService);
    }

    // TODO: NOT SURE IF ALSO REGISTER/UNREGISTER THE RECEIVER ON PAUSE/RESUME

    private BroadcastReceiver onReceiverFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            refreshListView();
        }
    };
}
