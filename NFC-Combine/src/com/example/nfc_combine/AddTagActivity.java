package com.example.nfc_combine;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.RFduinoService;
import com.example.bluetooth.HexAsciiHelper;

import org.apache.http.impl.cookie.RFC2109DomainHandler;



// The Activity, which gets called, when I want to add or edit a Tag
public class AddTagActivity extends Activity {
	
	private static final String TAG = "AddTagActivity";
	private Bundle oldData;
	NfcTag tmpTag;

	private DialogFragment mDialog;
	private Spinner mSpinner;
	TextView tmpID;
	EditText tmpName;

    private BluetoothAdapter bteAdapter;
    private BluetoothDevice bteDevice;

    private PendingIntent mPending;
	private NfcAdapter mNFCadapter;
	private Boolean newElement;
	private DatabaseHelper db;

    private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Log.w("Main","rfduinoReceiver called with " + action);
            setResultData("");
            if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action) && intent.getBooleanExtra(RFduinoService.ACTION_DATA_TAG,false)) {

                Log.i(TAG, "BTE Dialog Dismiss");
                byte[] id = intent.getByteArrayExtra(RFduinoService.EXTRA_DATA);
                setResultData("Tag_Activity");
                if(id != null) {
//                    int tmp = getDec(id);
//                	int tmp = HexAsciiHelper.bytesToDec(id);
                	String tmp = HexAsciiHelper.bytesToAsciiMaybe(id);
                	Log.i(TAG, "hex string from rf..." + HexAsciiHelper.bytesToAsciiMaybe(id));
                    if (db.idCheck(tmp)) tmpID.setText(tmp);
                    else {
                        Toast errorToast = Toast.makeText(getApplicationContext(),
                                "You already have a Tag with this ID in your database", Toast.LENGTH_SHORT);
                        errorToast.show();
                        Log.i(TAG, "you have this id!!!");
//                        final Intent mReminderIntent = new Intent(getApplicationContext(), ReminderService.class);
//                        getApplicationContext().stopService(mReminderIntent);                                         
                        finish();
                    }
                }
                mDialog.dismiss();
            }
        }
    };

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.addtagrequest);
		
		//define UI elements
		tmpID = (TextView) findViewById(R.id.TagID);
		tmpName = (EditText) findViewById(R.id.NameField);		
		mSpinner = (Spinner) findViewById(R.id.category_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 
				R.array.tag_categories, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
								
		oldData = getIntent().getExtras();		
		tmpTag = new NfcTag();
		db = new DatabaseHelper(getApplicationContext());
		
		//edit an existing item, pass the data to oldTag
		if(oldData != null){
			Log.i(TAG, "edit Tag, I fetch the data");
			newElement = false;
			//assign the values to the tmpTag
			tmpTag.setTagID(oldData.getString("tag_id"));			
			tmpTag.setItemID(oldData.getInt("item_id"));
			tmpTag.setTagName(oldData.getString("tag_name"));
			tmpTag.setRemind(oldData.getBoolean("remind_me"));
			tmpTag.setScanDateInMillis(oldData.getLong("last_scan"));
			tmpTag.setWearing(oldData.getBoolean("at_human"));
			tmpTag.setCategory(oldData.getString("tag_category"));
			//proper assignment of the layout parts
			tmpID.setText(tmpTag.getTagID());
			tmpName.setText(tmpTag.getTagName());
			mSpinner.setSelection(adapter.getPosition(tmpTag.getCategory()));
			
		} else { // new Item - Create an empty layout
			Log.i(TAG, "new Tag, wait for scan!!!");
			newElement = true;
			mDialog = NFCprompt.newInstance(); //show the prompt to scan a Tag
			mDialog.show(getFragmentManager(), "NFCprompt");
			mNFCadapter = NfcAdapter.getDefaultAdapter(this);
			mPending = PendingIntent.getActivity(this, 0, 
			new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);		
			
		}				
	}
	
//	@Override
//	public void onBackPressed(){
//		if(mDialog.isVisible()){
//			Log.i(TAG, "back pressed with dialog!");
//			mDialog.dismiss();
//			finish();
//		} else{
//			Log.i(TAG, "back pressed withOUT dialog!");
//			super.onBackPressed();
//		}
//	}

	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		if(newElement){
			setIntent(intent);
			Log.i(TAG, "in onNewIntent!");

			if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){					
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			if(checkTech(tag)){				
				byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
						//getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
				
				if(id != null){
//					int tmp = getDec(id);
					String tmp = getHex(id);
					Log.i(TAG, "in hex string from phone..." + getHex(id));
//					Log.i(TAG, "in hex HexAscii... " + HexAsciiHelper.bytesToHex(id));
//					Log.i(TAG, "in dec..." + getDec(id));
//					Log.i(TAG, "in dec HexAscii... " + HexAsciiHelper.bytesToDec(id));
//					Log.i(TAG, "in decReversed..." + getDecReversed(id));
					if(db.idCheck(tmp)) tmpID.setText(tmp);
					else {
						Toast errorToast = Toast.makeText(getApplicationContext(), 
								"You already have a Tag with this ID in your database", Toast.LENGTH_SHORT);
						errorToast.show();					
						finish();
					}
				} else {
					//case: ID of the Tag isn't readable
					Toast errorToast = Toast.makeText(getApplicationContext(), 
							"Your Tag has no readable ID, please attach a proper Tag", Toast.LENGTH_SHORT);
					errorToast.show();
					finish();
				}
			} else {
				//case: Tag is not of type NfcA
				Toast errorToast = Toast.makeText(getApplicationContext(), 
						"Your Tag has the wrong technology!", Toast.LENGTH_SHORT);
				errorToast.show();
				finish();
			}		
			mDialog.dismiss();
			}
		}		
	}

	private boolean checkTech(Tag pTag) {
		String[] techlist = pTag.getTechList();
		for(String tech : techlist){
			if(tech.equals(NfcA.class.getName())) return true;
		}		
		return false;
	}

	private int getDec(byte[] bytes) {		
	int result = 0;
	String hex = getHex(bytes);
	StringBuilder msb = new StringBuilder();
	for(int i = 0; i < hex.length(); i++){
		char c = hex.charAt(i);
		if(c != ' ') msb.append(c);			
	}
		
    result = (int) Long.parseLong(msb.toString(), 16);    
    return result;
	}
		
	private String getHex(byte[] bytes) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < bytes.length; i++) {
		sb.append(String.format(" %02X", bytes[i]));
		}
	sb.deleteCharAt(0);
	return sb.toString();	
	}
	
	private void saveDataLeave(){
		Intent returnIntent = new Intent();
		Bundle data = new Bundle();
		
		tmpTag.setTagName(tmpName.getText().toString());
		tmpTag.setTagID(tmpID.getText().toString());
		tmpTag.setCategory(mSpinner.getSelectedItem().toString());
		tmpTag.setRemind(false);
		tmpTag.setWearing(false);
		
		data.putString("tag_name", tmpTag.getTagName());		
		data.putString("tag_id", tmpTag.getTagID());
		data.putString("tag_category", tmpTag.getCategory());	
		data.putInt("item_id", tmpTag.getItemID());
		data.putBoolean("reminder", tmpTag.shouldRemind());
		data.putBoolean("at_human", tmpTag.isWearing());
		data.putLong("last_scan", tmpTag.getScanDateInMillis());
		
		returnIntent.putExtras(data);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	public void onClick(View v){
		
		switch(v.getId()){
		
		case R.id.btn_cancel:
			Intent canceledIntent = new Intent();
			setResult(RESULT_CANCELED, canceledIntent);
			finish();
			break;
			
		case R.id.btn_save:			
			saveDataLeave();
			break;
		}
	}

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("AddTagActivity","onStart called");
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
//        bteAdapter.stopLeScan(this);
        //unregisterReceiver(rfduinoReceiver);
    }
	
	public void onPause(){
		super.onPause();
        unregisterReceiver(rfduinoReceiver);
		if(newElement) mNFCadapter.disableForegroundDispatch(this);	
	}
	
	public void onResume(){
		super.onResume();
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());
		if(newElement) mNFCadapter.enableForegroundDispatch(this, mPending, null, null);	
	}
	
	public void onDestroy(){
		super.onDestroy();		
	}
}

