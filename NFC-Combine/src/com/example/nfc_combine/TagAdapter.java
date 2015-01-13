package com.example.nfc_combine;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TagAdapter extends ArrayAdapter<NfcTag> {
	
	private static final String TAG = "TagAdapter";
	private LayoutInflater inflater;
	private Context thiscontext;
	private int layout;
	private ArrayList<NfcTag> tagList;	

	public TagAdapter(Context context, int resource, ArrayList<NfcTag> objects) {
		super(context, resource, objects);
		
		this.inflater = LayoutInflater.from(context);
		this.thiscontext = context;
		this.layout = resource;
		this.tagList = objects;		
		this.thiscontext = context;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent){
		
		final ViewHolder holder;
		
		if(convertView != null){
			holder = (ViewHolder) convertView.getTag();
		} else {
			//setup the behaviour of the listview
			convertView = inflater.inflate(layout, null);
			
			//store references to the objects of a TagItem in the holder
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.tagName);
			holder.reminder = (ToggleButton) convertView.findViewById(R.id.remindButton);
			holder.delete = (Button) convertView.findViewById(R.id.deleteButton);	
			holder.wearingIndicator = (RadioButton) convertView.findViewById(R.id.wearingIndicator);
			holder.main = (RelativeLayout) convertView.findViewById(R.id.item);			
			
//			holder.wearingIndicator.setChecked(tagList.get(position).isWearing());			
//			Log.i(TAG, "wearingIndicator: " + tagList.get(position).isWearing());
			
//			holder.reminder.setChecked(tagList.get(position).shouldRemind());
//			Log.i(TAG, "reminder state: " + tagList.get(position).shouldRemind());
			holder.reminder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				//update the items reminder status!!
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Implement it!!!
					// TODO Implement the method updateEntry(Tag mtag) in MainActivity
					DatabaseHelper db = new DatabaseHelper(thiscontext);
					NfcTag tempTag = tagList.get(position);
//					boolean state = tempTag.shouldRemind();					
					if(isChecked){
						tempTag.setRemind(isChecked);
					} else tempTag.setRemind(isChecked);
					
					db.updateItem(tempTag);					
					Log.i(TAG, "reminder toggled for " + tempTag.getTagName() + " to " + tempTag.shouldRemind());					
				
				}
			});
			
			holder.delete.setOnClickListener(new OnClickListener() {
				//delete the item!
				@Override
				public void onClick(View v) {
					// TODO implement the method deleteEntry(Tag mtag)
					DatabaseHelper db = new DatabaseHelper(thiscontext);
					NfcTag tempTag = tagList.get(position);
					
					db.deleteItem(tempTag);
					
					((DatabaseActivity) thiscontext).refreshListView();
					
				}
			});
			
			holder.main.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO implement the method editEntry(Tag mtag)
					NfcTag tempTag = tagList.get(position);
					((DatabaseActivity) thiscontext).editEntry(tempTag);					
				}
			});
			
			convertView.setTag(holder);
		}
		
		NfcTag tempTag = tagList.get(position);
		
		holder.name.setText(tempTag.getTagName());
		holder.reminder.setChecked(tempTag.shouldRemind());
		holder.wearingIndicator.setChecked(tempTag.isWearing());
		
		Log.i(TAG, "name: " + tempTag.getTagName());
		Log.i(TAG, "reminder: " + tempTag.shouldRemind());
		Log.i(TAG, "wearing: " + tempTag.isWearing());
		Log.i(TAG, "category: " + tempTag.getCategory());
		
		return convertView;
	}
	
	
	public class ViewHolder {
        TextView name;        
        ToggleButton reminder;
        Button delete;
        RelativeLayout main;
        RadioButton wearingIndicator;
    }

}
