package com.example.ui_nfc;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TagAdapter extends ArrayAdapter<Tag> {
	
	private static final String TAG = "TagAdapter";
	private LayoutInflater inflater;
	private Context thiscontext;
	private int layout;
	private ArrayList<Tag> tagList;	

	public TagAdapter(Context context, int resource, ArrayList<Tag> objects) {
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
			holder.main = (RelativeLayout) convertView.findViewById(R.id.item);
			//TODO check whether the reminder is set right!!!
			holder.reminder.setChecked(tagList.get(position).shouldRemind());
			
			holder.reminder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				//update the items reminder status!!
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Implement it!!!
					// TODO Implement the method updateEntry(Tag mtag) in MainActivity
					DatabaseHelper db = new DatabaseHelper(thiscontext);
					Tag tempTag = tagList.get(position);
//					boolean state = tempTag.shouldRemind();					
					if(isChecked){
						tempTag.setRemind(isChecked);
					} else tempTag.setRemind(isChecked);
					
					db.updateItem(tempTag);					
					Log.i(TAG, "reminder toggled" + tempTag.shouldRemind());
					
					//perhaps...
					//((MainActivity) thiscontext).refreshListView();
					
//					if(isChecked){
//						
//					} else {
//						
//					}
				}
			});
			
			holder.delete.setOnClickListener(new OnClickListener() {
				//delete the item!
				@Override
				public void onClick(View v) {
					// TODO implement the method deleteEntry(Tag mtag)
					DatabaseHelper db = new DatabaseHelper(thiscontext);
					Tag tempTag = tagList.get(position);
					
					db.deleteItem(tempTag);
					
					((MainActivity) thiscontext).refreshListView();
					
				}
			});
			
			holder.main.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO implement the method editEntry(Tag mtag)
					Tag tempTag = tagList.get(position);
					((MainActivity) thiscontext).editEntry(tempTag);					
				}
			});
			
			convertView.setTag(holder);
		}
		
		Tag tempTag = tagList.get(position);
		
		holder.name.setText(tempTag.getTagName());
		holder.reminder.setChecked(tempTag.shouldRemind());
		
		return convertView;
	}
	
	
	public class ViewHolder {
        TextView name;        
        ToggleButton reminder;
        Button delete;
        RelativeLayout main;
    }

}
