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
	private Context gContext;
	private int layout;
	private ArrayList<NfcTag> tagList;	

	public TagAdapter(Context context, int resource, ArrayList<NfcTag> objects) {
		super(context, resource, objects);
		
		this.inflater = LayoutInflater.from(context);
		this.gContext = context;
		this.layout = resource;
		this.tagList = objects;
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
			holder.wearingIndicatorText = (TextView) convertView.findViewById(R.id.indicatorText);
            holder.wearingIndicator = (RadioButton) convertView.findViewById(R.id.wearingIndicator);
            holder.wearingIndicator.setEnabled(false);
			holder.main = (RelativeLayout) convertView.findViewById(R.id.item);			

            NfcTag tempTag = tagList.get(position);

            holder.name.setText(tempTag.getTagName());
            holder.reminder.setChecked(tempTag.shouldRemind());
            holder.wearingIndicator.setChecked(tempTag.isWearing());

            if (tempTag.getCategory().equals(gContext.getResources().getStringArray(R.array.tag_categories)[1])) {
                holder.reminder.setVisibility(View.GONE);
                holder.wearingIndicator.setVisibility(View.GONE);
                holder.wearingIndicatorText.setVisibility(View.GONE);
            }

            Log.i(TAG, "name: " + tempTag.getTagName());
            Log.i(TAG, "reminder: " + tempTag.shouldRemind());
            Log.i(TAG, "wearing: " + tempTag.isWearing());
            Log.i(TAG, "category: " + tempTag.getCategory());

            holder.reminder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				//update the items reminder status!!
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Implement it!!!
					// TODO Implement the method updateEntry(Tag mTag) in MainActivity
					DatabaseHelper db = new DatabaseHelper(gContext);
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
					// TODO implement the method deleteEntry(Tag mTag)
					DatabaseHelper db = new DatabaseHelper(gContext);
					NfcTag tempTag = tagList.get(position);
					db.deleteItem(tempTag);
					((DatabaseActivity) gContext).refreshListView();
				}
			});
			
			holder.main.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO implement the method editEntry(Tag mTag)
					NfcTag tempTag = tagList.get(position);
					((DatabaseActivity) gContext).editEntry(tempTag);
				}
			});
			
			convertView.setTag(holder);
		}
		


		return convertView;
	}
	
	
	public class ViewHolder {
        TextView name;        
        ToggleButton reminder;
        Button delete;
        RelativeLayout main;
        RadioButton wearingIndicator;
        TextView wearingIndicatorText;
    }

}
