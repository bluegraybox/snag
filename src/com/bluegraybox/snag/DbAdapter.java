package com.bluegraybox.snag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {
	
    private static final String TAG_ID = "tag_id";
	private static final String MEMO_ID = "memo_id";
	private static final String MEMO_TAG = "memo_tag";
	private static final String TAG = "tag";
	private static final String NAME = "name";
	static final String BODY = "body";
	static final String SLUG = "slug";
	private static final String MEMO = "memo";
	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "snag";
	private static final int SLUG_MAX = 30;
	private static final String CREATED = "created";
	private static final String UPDATED = "updated";
	public static final String ID = "_id";

	private static class Helper extends SQLiteOpenHelper {

    	private Resources mRes;

		Helper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
    		mRes = context.getResources();
        }
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
            db.execSQL(mRes.getString(R.string.enable_foreign_keys));
            db.execSQL(mRes.getString(R.string.create_memo));
            db.execSQL(mRes.getString(R.string.create_tag));
            db.execSQL(mRes.getString(R.string.create_memo_tag));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(this.getClass().getName(),
            		"Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL(mRes.getString(R.string.drop_memo_tag));
            db.execSQL(mRes.getString(R.string.drop_memo));
            db.execSQL(mRes.getString(R.string.drop_tag));
            onCreate(db);
		}
    }

	private Helper mHelper;
	private SQLiteDatabase mDb;
	private Resources mResources;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public DbAdapter(Context context) {
		mHelper = new Helper(context);
		mDb = mHelper.getWritableDatabase();
		mResources = context.getResources();
	}
	
	public void close() {
		mHelper.close();
	}

	public long createMemo(String text) {
		ContentValues values = new ContentValues();
		String slug = getSlug(text);
		String now = dateFormat.format(new Date());
		values.put(BODY, text);
		values.put(SLUG, slug);
		values.put(CREATED, now);
		values.put(UPDATED, now);
		return mDb.insert(MEMO, null, values);
	}

	private String getSlug(String text) {
		String slug = text;
		if (slug.length() > SLUG_MAX) {
			slug = slug.substring(0, SLUG_MAX);
		}
		int eol = slug.indexOf('\n');
		if (eol > 1) {
			slug = slug.substring(0, eol);
		}
		return slug;
	}
	
	public void deleteMemo(Long memo_id) {
		mDb.delete(MEMO, "?=?", new String[]{MEMO_ID, memo_id.toString()});
	}
	
	public long createTag(String name) {
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		return mDb.insert(TAG, null, values);
	}
	
	public void addMemoTag(Long memo_id, Long tag_id) {
		ContentValues values = new ContentValues();
		values.put(MEMO_ID, memo_id);
		values.put(TAG_ID, tag_id);
		mDb.insert(MEMO_TAG, null, values);
	}
	
	public void removeMemoTag(Long memo_id, Long tag_id) {
		mDb.delete(MEMO_TAG, "?=? and ?=?", new String[]{MEMO_ID, memo_id.toString(), TAG_ID, tag_id.toString()});
	}
	
	public void setMemoTags(Long memo_id, List<Long> tag_ids) {
		mDb.beginTransaction();
		// Delete all previous tags for this memo, then add the new ones.
		mDb.delete(MEMO_TAG, "?=?", new String[]{MEMO_ID, memo_id.toString()});
		for (Long tag_id : tag_ids) {
			ContentValues values = new ContentValues();
			values.put(MEMO_ID, memo_id);
			values.put(TAG_ID, tag_id);
			mDb.insert(MEMO_TAG, null, values);
		}
		mDb.endTransaction();
	}

	public Cursor getMemos() {
		return mDb.query(MEMO, new String[]{ BODY }, null, null, null, null, null);
	}
	
	public Cursor getMemo(Long memo_id) {
		return mDb.query(MEMO, new String[]{ BODY }, "where ? = ?", new String[]{ ID, memo_id.toString() }, null, null, null);
	}

	public boolean updateMemo(Long memo_id, String body) {
        ContentValues values = new ContentValues();
        values.put(BODY, body);
        return mDb.update(MEMO, values, "? = ?", new String[]{ ID, memo_id.toString() }) > 0;
	}
}
