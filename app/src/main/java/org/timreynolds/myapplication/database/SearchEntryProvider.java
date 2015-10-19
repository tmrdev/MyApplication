package org.timreynolds.myapplication.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import java.sql.SQLException;

/*
 *  A custom Content Provider to do the database operations - sqlite3
 */
public class SearchEntryProvider extends ContentProvider {
	
	public static final String PROVIDER_NAME = "org.timreynolds.myapplication.searchentries";
	
	/** A uri to do operations on entries table. A content provider is identified by its uri */
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/searchentries");
	
	/** Constants to identify the requested operation */
	private static final int SEARCH_ENTRIES = 1;
	private static final int SEARCH_ENTRIES_ID = 2;
	
	private static final UriMatcher uriMatcher ;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "searchentries", SEARCH_ENTRIES);
		uriMatcher.addURI(PROVIDER_NAME, "searchentries/#", SEARCH_ENTRIES_ID);
	}
	
	/** This content provider does the database operations by this object */
	SearchEntryDB mSearchEntriesDB;
	
	/** A callback method which is invoked when the content provider is starting up */
	@Override
	public boolean onCreate() {
		mSearchEntriesDB = new SearchEntryDB(getContext());
		return true;
	}	

	/** A callback method which is invoked when delete operation is requested on this content provider */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int cnt = 0;
		if(uriMatcher.match(uri)== SEARCH_ENTRIES_ID){
			String entryID = uri.getPathSegments().get(1);
			cnt = mSearchEntriesDB.del(entryID);
		}
		return cnt;
	}

	
	@Override
	public String getType(Uri uri) {
		return null;
	}	
	
	/** A callback method which is invoked when insert operation is requested on this content provider */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = mSearchEntriesDB.insert(values);
		Uri _uri=null;
		if(rowID>0){
			_uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
		}else {		
			try {
				throw new SQLException("Failed to insert : " + uri);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return _uri;
	
	}	

	/** A callback method which is the default entry uri */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		if(uriMatcher.match(uri)== SEARCH_ENTRIES){
			return mSearchEntriesDB.getAllEntries();
		}else{
			String entryID = uri.getPathSegments().get(1);
			return mSearchEntriesDB.getEntryByID(entryID);
		}
	}
	
	/** A callback method which is invoked when update operation is requested on this content provider */
	@Override
	public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
		int cnt = 0;
		if(uriMatcher.match(uri)== SEARCH_ENTRIES_ID){
			String entryID = uri.getPathSegments().get(1);
			cnt = mSearchEntriesDB.update(contentValues, entryID);
			
		}
		return cnt;
	}
}
