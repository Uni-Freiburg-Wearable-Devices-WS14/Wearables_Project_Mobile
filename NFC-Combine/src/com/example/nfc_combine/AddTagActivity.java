package com.example.nfc_combine;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
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


// The Activity, which gets called, when I want to add or edit a Tag
public class AddTagActivity extends Activity {
	
	private static final String TAG = "AddTagActivity";
	private Bundle oldData;
	NfcTag tmpTag;

	private DialogFragment mDialog;
	private Spinner mSpinner;
	TextView tmpID;
	EditText tmpName;
	private PendingIntent mPending;
	private NfcAdapter mNFCadapter;
	private Boolean newElement;
	private DatabaseHelper db;
	
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
			tmpTag.setTagID(oldData.getInt("tag_id"));			
			tmpTag.setItemID(oldData.getInt("item_id"));
			tmpTag.setTagName(oldData.getString("tag_name"));
			tmpTag.setRemind(oldData.getBoolean("remind_me"));
			tmpTag.setScanDateInMillis(oldData.getLong("last_scan"));
			tmpTag.setWearing(oldData.getBoolean("at_human"));
			tmpTag.setCategory(oldData.getString("tag_category"));
			//proper assignment of the layout parts
			tmpID.setText(Integer.valueOf(tmpTag.getTagID()).toString());
			tmpName.setText(tmpTag.getTagName());
			mSpinner.setSelection(adapter.getPosition(tmpTag.getCategory()));
			
		} else{
			Log.i(TAG, "new Tag, wait for scan!!!");
			newElement = true;
			//show the prompt to scan a Tag
			mDialog = NFCprompt.newInstance();
			mDialog.show(getFragmentManager(), "NFCprompt");
			
			mNFCadapter = NfcAdapter.getDefaultAdapter(this);
			mPending = PendingIntent.getActivity(this, 0, 
			new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);			
		}				
	}
	
	
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		if(newElement){
			setIntent(intent);
			Log.i(TAG, "in onNewIntent!");
			
			if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){					
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			if(checkTech(tag)){				
				byte[] id = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
				if(id != null){
					int tmp = getDec(id);
					if(db.idCheck(tmp)) tmpID.setText(Integer.toString(tmp));
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





	private boolean checkTech(Tag mtag) {
		String[] techlist = mtag.getTechList();
		for(String tech : techlist){
			if(tech.equals(NfcA.class.getName())) return true;
		}		
		return false;
	}


	private int getDec(byte[] bytes) {		
	int result = 0;
    int factor = 1;
    for (int i = 0; i < bytes.length; ++i) {
        int value = (int) (bytes[i] & 0xffl);
        result += value * factor;
        factor *= 256l;
    }
    return result;		
	}
	
	private void saveDataLeave(){
		Intent returnIntent = new Intent();
		Bundle data = new Bundle();
		
		tmpTag.setTagName(tmpName.getText().toString());
		tmpTag.setTagID(Integer.valueOf(tmpID.getText().toString()));
		tmpTag.setCategory(mSpinner.getSelectedItem().toString());
		tmpTag.setRemind(false);
		tmpTag.setWearing(false);
		
		data.putString("tag_name", tmpTag.getTagName());		
		data.putInt("tag_id", tmpTag.getTagID());
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
	
	public void onPause(){
		super.onPause();
		if(newElement) mNFCadapter.disableForegroundDispatch(this);	
	}
	
	public void onResume(){
		super.onResume();
		if(newElement) mNFCadapter.enableForegroundDispatch(this, mPending, null, null);	
	}
	
	public void onDestroy(){
		super.onDestroy();		
	}
}

