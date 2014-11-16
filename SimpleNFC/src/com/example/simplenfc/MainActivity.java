package com.example.simplenfc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.*;

//For options menu
//import android.view.Menu;
//import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
	
	private static String TAG = "MainActivity";
	private static final int NOTIFICATION_ID = 2;
	private NfcAdapter mNFCadapter;
	private TextView mTextView;
	private PendingIntent mPending;
	
	LinearLayout mLayout;
	private TextView idValue;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.nfc);
		
		mLayout = (LinearLayout) findViewById(R.id.nfc_layout);
		mTextView = (TextView) findViewById(R.id.textView1);
		idValue = new TextView(this);
		mLayout.addView(idValue);
		
//		resolveIntent(getIntent());
		
		mNFCadapter = NfcAdapter.getDefaultAdapter(this);
				
		mPending = PendingIntent.getActivity(this, 0, 
				new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		resolveIntent(getIntent());
	}
	
	private void resolveIntent(Intent intent) {
		// TODO Auto-generated method stub
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())){
			Log.i(TAG, "resolveIntent");
					
			byte[] id = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
			if(id != null){

				long temp = getDec(id);
				Log.i(TAG, Long.toString(temp));
				mTextView.setText("The ID of the Tag (in dec): ");
				idValue.setText(Long.toString(temp));
				makeNotify(temp);

			}							
		}
	}


	private void makeNotify(long temp) {
		// TODO Auto-generated method stub
		
		Notification.Builder notificationBuilder = new Notification.Builder(
				getApplicationContext())
				.setTicker("You scanned a Tag!")
				.setContentTitle("Here is your ID (in hex)")
//				.setContentText(Long.toString(temp))
				.setContentText(Long.toHexString(temp))
				.setSmallIcon(android.R.drawable.ic_dialog_info)
				.setContentIntent(mPending)
				.setAutoCancel(true);
		
		NotificationManager mManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		mManager.notify(NOTIFICATION_ID, notificationBuilder.build());
		
	}

	protected void onResume(){		
		super.onResume();
		
		if(mNFCadapter != null){
			mTextView.setText("Read a Tag");
		} else{
			mTextView.setText("NFC Reading not enabled");
		}
		
		if(!mNFCadapter.isEnabled()){
			mTextView.setText("NFC is disabled");
		}
		
		mNFCadapter.enableForegroundDispatch(this, mPending, null, null);
		Log.i(TAG, "onResume!");					
	}
		
	protected void onPause(){
		super.onPause();
		Log.i(TAG,"onPause()");
		if(mNFCadapter != null){
			mNFCadapter.disableForegroundDispatch(this);
		}			
	}
	
			
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
		Log.i(TAG, "newIntent!!");
		resolveIntent(intent);
	}
	
	private long getDec(byte[] bytes) {
		// TODO Auto-generated method stub
	long result = 0;
    long factor = 1;
    for (int i = 0; i < bytes.length; ++i) {
        long value = bytes[i] & 0xffl;
        result += value * factor;
        factor *= 256l;
    }
    return result;		
	}
	
	//Options menu, till now not needed!
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
