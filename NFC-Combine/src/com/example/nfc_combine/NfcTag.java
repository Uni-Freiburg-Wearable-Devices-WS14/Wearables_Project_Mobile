package com.example.nfc_combine;

import java.util.Calendar;

// a class, for describing the TAG itself, with all its entities
public class NfcTag {
	
	//members of the Tag
//	public static final String ITEM_ID = "item_id";
//	public static final String TAG_ID = "tag_id";
//	public static final String NAME = "tag_name";
//	public static final String REMIND = "reminder";
//	public static final String CATEGORY = "tag_category";
//	public static final String SCAN = "last_scan";
//	public static final String WEARING = "at_human";
	
	//fields of the Tag Item
	private int itemID;
	private String tagID;
	private String tagName;
	private boolean remindMe;
	private long scanDate;
	private boolean atHuman;
	private String category;
	
	public NfcTag(){ ; }
	
	public NfcTag(int itemID, String tagID, String tagName, boolean remindMe,
			long scanDate, boolean atHuman, String category){
		super();
		this.itemID = itemID;
		this.tagID = tagID;
		this.tagName = tagName;
		this.remindMe = remindMe;
		this.scanDate = scanDate;
		this.atHuman = atHuman;
		this.category = category;
	}
	
	public int getItemID(){
		return itemID;
	}
	
	public void setItemID(int itemId){
		this.itemID = itemId;
	}
	
	public String getTagID(){
		return tagID;
	}
	
	public void setTagID(String id){
		this.tagID = id;
	}
	
	public String getTagName(){
		return tagName;
	}
	
	public void setTagName(String name){
		this.tagName = name;
	}
	
	public boolean shouldRemind(){
		return remindMe;
	}
	
	public void setRemind(boolean remind){
		this.remindMe = remind;
	}
	
	public boolean isWearing(){
		return atHuman;
	}
	
	public void setWearing(boolean atHuman){
		this.atHuman = atHuman;
	}
	
	public String getCategory() {		
		return category;
	}
	
	public void setCategory(String category){
		this.category = category;
	}
	
	public Calendar getScanDate(){
		Calendar myCal = Calendar.getInstance();
		myCal.setTimeInMillis(scanDate);
		return myCal;
	}
	
	public void setScanDate(Calendar cal){
		this.scanDate = cal.getTimeInMillis();
	}
	
	public long getScanDateInMillis(){
		return scanDate;
	}
	
	public void setScanDateInMillis(long scanDate){
		this.scanDate = scanDate;
	}

}
