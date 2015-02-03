package com.example.nfc_combine;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

	
	public class DatabaseHelper extends SQLiteOpenHelper{
		
		//information for database
		private static final String DB_FILENAME = "tagDB";
		private static final int DB_VERSION = 1;
		private static final String TAG_TABLE_NAME = "tags";
        private static Resources mRes;
		//for debugging
		private static final String TAG = "DatabaseAdapter";
		
		public static final String KEY_ITEM_ID = "item_id";
		public static final String KEY_TAG_ID = "tag_id";
		public static final String KEY_NAME = "tag_name";
		public static final String KEY_REMIND = "reminder";
		public static final String KEY_CATEGORY = "tag_category";
		public static final String KEY_SCAN = "last_scan";
		public static final String KEY_WEARING = "at_human";

        private static final String[] TAG_REMIND_COLUMN = { KEY_ITEM_ID, KEY_TAG_ID, KEY_NAME, KEY_CATEGORY };
        private static final String[] TAG_CAT_COLUMN = { KEY_CATEGORY };
        private static final String[] TAG_WEAR = {KEY_TAG_ID, KEY_WEARING};
		
		private static final String DATABASE_CREATE = "CREATE TABLE "
				+ TAG_TABLE_NAME	+ "( " 
				+ KEY_ITEM_ID		+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_TAG_ID		+ " INTEGER, "
				+ KEY_NAME			+ " STRING, "
				+ KEY_REMIND		+ " INTEGER, "
				+ KEY_CATEGORY		+ " STRING, "
				+ KEY_SCAN			+ " LONG, "
				+ KEY_WEARING		+ " INTEGER "
									+ ")";
		
		public DatabaseHelper(Context context){
			super(context, DB_FILENAME, null, DB_VERSION);
            mRes = context.getResources();
			Log.i(TAG, "DB constructor");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {			
			db.execSQL(DATABASE_CREATE);			
			Log.i(TAG, "DB onCreate!");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE_NAME);
			onCreate(db);			
		}
		
		//add a new Tag item
		public NfcTag addItem(NfcTag item){
			//TODO
			SQLiteDatabase db = this.getWritableDatabase();
			Log.i(TAG, "addItem");
			
			//putting the data into a value container
			ContentValues values = new ContentValues();
			values.put(KEY_TAG_ID, item.getTagID());
			values.put(KEY_CATEGORY, item.getCategory());
			values.put(KEY_NAME, item.getTagName());
			values.put(KEY_REMIND, item.shouldRemind() ? 1 : 0);
			values.put(KEY_SCAN, item.getScanDateInMillis());
			values.put(KEY_WEARING, item.isWearing() ? 1 : 0);
			
			item.setItemID((int)db.insert(TAG_TABLE_NAME , null, values) );
			db.close();						
			return item;			
		}
		
		//updates an existing Tag item
		public int updateItem(NfcTag item){
			//TODO
			int rows = 0;
			SQLiteDatabase db = getWritableDatabase();
			
			//putting the data into a value container
			ContentValues values = new ContentValues();
			values.put(KEY_TAG_ID, item.getTagID());
			values.put(KEY_CATEGORY, item.getCategory());
			values.put(KEY_NAME, item.getTagName());
			values.put(KEY_REMIND, item.shouldRemind() ? 1 : 0);
			values.put(KEY_SCAN, item.getScanDateInMillis());
			values.put(KEY_WEARING, item.isWearing() ? 1 : 0);
			
			rows = db.update(TAG_TABLE_NAME, values, KEY_ITEM_ID + " = ?",
					new String[] {String.valueOf(item.getItemID())});
			
			Log.i(TAG, String.valueOf(rows));
			
			if(db != null) db.close();
			
			return rows;
		}
		
		//deletes a single Tag
		public int deleteItem(NfcTag item){
			//TODO
			int rows = 0;
			SQLiteDatabase db = getWritableDatabase();
			
			rows = db.delete(TAG_TABLE_NAME, KEY_ITEM_ID + " = ?",
					new String[] {String.valueOf(item.getItemID())});
			
			if(db != null) db.close();
			
			return rows;
		}
		
		//deletes all Tag items in DB
		public int deleteAllItems(){
			//TODO
			int rows = 0;
			SQLiteDatabase db = getWritableDatabase();
			
			rows = db.delete(TAG_TABLE_NAME, null, null);
			
			if(db != null) db.close();
			
			return rows;
		}
		
		//returns all Tag items in the DB
		public ArrayList<NfcTag> getAllItems(){
			//TODO
			SQLiteDatabase db = this.getReadableDatabase();
			ArrayList<NfcTag> tagList = new ArrayList<NfcTag>();
			Cursor cursor = null;
			
			Log.i(TAG, "in getAllItems()");
			
			//Select all entries in db???
			cursor = db.rawQuery(" SELECT * FROM " + TAG_TABLE_NAME, null);
			
			if (cursor.moveToFirst()){
				do{
					NfcTag item = new NfcTag();
					item.setItemID(Integer.parseInt(cursor.getString(0)));
					item.setTagID(Integer.parseInt(cursor.getString(1)));
					item.setTagName(cursor.getString(2));
					item.setRemind((Integer.parseInt(cursor.getString(3)) == 1) ? true:false);
					item.setCategory(cursor.getString(4));
					item.setScanDateInMillis(Long.parseLong(cursor.getString(5)));
					item.setWearing((Integer.parseInt(cursor.getString(6)) == 1) ? true:false);
					
					tagList.add(item);
				}while(cursor.moveToNext());
			}
			if(cursor!= null)
				cursor.close();
			if(db!= null)
				db.close();			
			
			return tagList;		
		}

		public boolean idCheck(int newId) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = null;
			cursor = db.rawQuery(" SELECT * FROM " + TAG_TABLE_NAME, null);
			
			if(cursor.moveToFirst()){
				do{					
					int oldId = cursor.getInt(cursor.getColumnIndex(KEY_TAG_ID));
					Log.i(TAG, "id to check: " + Integer.valueOf(newId).toString() + 
							" actual id: " + Integer.valueOf(oldId).toString());
					if(oldId == newId) return false;
				} while(cursor.moveToNext());
				
			}
			return true;
		}

        /**
         * @param mIdToCheck of the status we are looking for
         * @return String with the type of the TAG
         */
        public String checkIfObject(int mIdToCheck) {
            SQLiteDatabase db = this.getReadableDatabase(); // Or should it be Writable?
            Cursor mCursor = null;
            mCursor = db.query(TAG_TABLE_NAME, TAG_CAT_COLUMN, KEY_TAG_ID + " = " + mIdToCheck,
                    null, null, null, null);
            String result = mCursor.moveToNext() ? mCursor.getString(0) : "";

            if(mCursor != null) { mCursor.close(); }

            if(db != null) { db.close(); }


            return result; //
        }

        /**
         * @return List of NfcTag Items to be reminded. If list is empty, means you have all the
         * objects that you want at the door.
         */
        public ArrayList<NfcTag> getItemsToRemind(){
            SQLiteDatabase db = this.getReadableDatabase();
            ArrayList<NfcTag> mTagList = new ArrayList<NfcTag>();
            Cursor mCursor = null;

            Log.i(TAG, "getItemsToRemind()");
            String[] mCategories = mRes.getStringArray(R.array.tag_categories);

            mCursor = db.query(TAG_TABLE_NAME, TAG_REMIND_COLUMN,
                    KEY_CATEGORY + " = '" + mCategories[0] + "' AND " + KEY_REMIND + " = '1' AND " + KEY_WEARING + " = '0'",
                    null,null, null, KEY_NAME + " ASC");

            if (mCursor.moveToFirst()){
                do{
                    NfcTag item = new NfcTag();
                    item.setItemID(Integer.parseInt(mCursor.getString(0)));
                    item.setTagID(Integer.parseInt(mCursor.getString(1)));
                    item.setTagName(mCursor.getString(2));
                    item.setCategory(mCursor.getString(3));
                    mTagList.add(item);
                }while(mCursor.moveToNext());
            }
            if(mCursor != null)
                mCursor.close();
            if(db != null)
                db.close();

            return mTagList;
        }

        /**
         * @return Number of changed rows
         */
        public int toggleItem(int tmpID) {
            int rows = 0;
            Log.i(TAG, "toggleItem()");
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();//putting the data into a value container
            //values.put(KEY_NAME, item.getTagName());
            values.put(KEY_WEARING, 1);

            rows = db.update(TAG_TABLE_NAME, values, KEY_TAG_ID + " = ?",
                    new String[] {String.valueOf(tmpID)});

            Log.i(TAG, "Rows Updated = " + String.valueOf(rows));
            if(db != null) db.close();
            return rows;
        }
    }
	
		


