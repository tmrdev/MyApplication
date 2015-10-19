package org.timreynolds.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * SearchEntryDB - sqlite database helper for storing searches
 */
public class SearchEntryDB extends SQLiteOpenHelper {
	
	/** Database name */
	private static String DBNAME = "googleplus_search.db";
	
	/** Version number of the database */
	private static int VERSION = 1;
	
	/** Field 1 of the table, which is the primary key */
	public static final String KEY_ROW_ID = "_id";
	
	/** Field 2 of the table, stores the entry name */
    public static final String KEY_NAME = "name";
    
    /** Field 3 of the table entry, stores the type entry */
    public static final String KEY_TYPE = "type";
    
    /** A constant, stores the the table name */
    private static final String DATABASE_TABLE = "search_entries";
    
    /** An instance variable for SQLiteDatabase */
    private SQLiteDatabase mDB;
    

    /** Constructor */
	public SearchEntryDB(Context context) {
		super(context, DBNAME, null, VERSION);	
		this.mDB = getWritableDatabase();
	}
	

	/** This is a callback method, invoked when the method getReadableDatabase() / getWritableDatabase() is called 
	  * provided the database does not exists 
	* */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = 	"create table energyentries (_id integer primary key autoincrement , "
                		+ " name text not 	null , type text not null ) " ;		
		db.execSQL(sql);
	}
	
	/** Inserts a new entry to the table */
	public long insert(ContentValues contentValues){
		long rowID = mDB.insert(DATABASE_TABLE, null, contentValues);
		return rowID;
		
	}
	
	/** Updates an entry */
	public int update(ContentValues contentValues,String entryID){
		int cnt = mDB.update(DATABASE_TABLE, contentValues, "_id=" + entryID, null);
		return cnt;
	}
	
	/** Deletes an entry from the table */
	public int del(String entryID){
		int cnt = mDB.delete(DATABASE_TABLE, "_id="+entryID, null);		
		return cnt;
	}
	
	/** Returns all the entries in the table */
	public Cursor getAllEntries(){
        return mDB.query(DATABASE_TABLE, new String[] { KEY_ROW_ID,  KEY_NAME , KEY_TYPE } , null, null, null, null, KEY_NAME + " asc ");
	}
	
	/** Returns an entry by passing its id */
	public Cursor getEntryByID(String entryID){
        return mDB.query(DATABASE_TABLE, new String[] { KEY_ROW_ID,  KEY_NAME , KEY_TYPE } , "_ID="+entryID, null, null, null, KEY_NAME + " asc ");
	}	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
