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
	static final String MEMO_ID = "memo_id";
	private static final String MEMO_TAG = "memo_tag";
	private static final String TAG = "tag";
	static final String NAME = "name";
	static final String BODY = "body";
	static final String SLUG = "slug";
	private static final String MEMO = "memo";
	public static final int DB_VERSION = 4;
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
            db.execSQL(mRes.getString(R.string.drop_memo_tag_view));
            db.execSQL(mRes.getString(R.string.drop_memo_tag));
            db.execSQL(mRes.getString(R.string.drop_memo));
            db.execSQL(mRes.getString(R.string.drop_tag));
            onCreate(db);
		}
    }

	private Helper mHelper;
	private SQLiteDatabase mDb;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public DbAdapter(Context context) {
		mHelper = new Helper(context);
		mDb = mHelper.getWritableDatabase();
	}
	
	public void close() {
		mHelper.close();
	}

	/* Memo operations */
	
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

	public boolean updateMemo(Long memoId, String text) {
        ContentValues values = new ContentValues();
		String slug = getSlug(text);
		String now = dateFormat.format(new Date());
        values.put(BODY, text);
		values.put(SLUG, slug);
		values.put(UPDATED, now);
		String where = ID+"="+memoId; // have to do it like this or memoId gets treated like a string.
        return mDb.update(MEMO, values, where, null) > 0;
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
	
	public void deleteMemo(Long memoId) {
		String where = ID+"="+memoId; // have to do it like this or memoId gets treated like a string.
		mDb.delete(MEMO, where, null);
	}

	public Cursor getMemos() {
		return mDb.query(MEMO, new String[]{ ID, SLUG, BODY }, null, null, null, null, null);
	}
	
	public Cursor getMemo(Long memoId) {
		String where = ID+"="+memoId; // have to do it like this or memoId gets treated like a string.
		Cursor memo = mDb.query(MEMO, new String[]{ ID, SLUG, BODY }, where, null, null, null, null);
		if (memo != null) {
			memo.moveToFirst();
		}
		return memo;
	}
	
	/* Tag operations */

	public void createTag(Long memoId, String name) {
		name = name.trim();
		if ("".equals(name))
				return;
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		Long tagId = getTagId(name);
		try {
			mDb.beginTransaction();
			if (tagId == null) {
				tagId = mDb.insert(TAG, null, values);
			}
			addMemoTag(memoId, tagId);
			mDb.setTransactionSuccessful();
		}
		finally {
			mDb.endTransaction();
		}
		return;
	}

	private Long getTagId(String name) {
		Long tagId = null;
		Cursor tag = mDb.query(TAG, new String[]{ ID }, NAME+"=?", new String[]{ name }, null, null, null);
		if (tag != null) {
			if (tag.moveToFirst()) {
				tagId = tag.getLong(tag.getColumnIndexOrThrow(ID));
			}
		}
		tag.close();
		return tagId;
	}
	
	/* Memo_Tag operations */

	public void addMemoTag(Long memo_id, Long tag_id) {
		ContentValues values = new ContentValues();
		values.put(MEMO_ID, memo_id);
		values.put(TAG_ID, tag_id);
		// If this is a duplicate, insert() will just return -1.
		mDb.insert(MEMO_TAG, null, values);
	}
	
	public int removeMemoTag(Long memoId, Long tagId) {
		String where = MEMO_ID+"="+memoId+" and "+TAG_ID+"="+tagId;
		return mDb.delete(MEMO_TAG, where, null);
	}
	
	public void setMemoTags(Long memoId, List<Long> tagIds) {
		try {
			mDb.beginTransaction();
			String where = MEMO_ID+"="+memoId; // have to do it like this or memoId gets treated like a string.
			// Delete all previous tags for this memo, then add the new ones.
			mDb.delete(MEMO_TAG, where, null);
			for (Long tagId : tagIds) {
				addMemoTag(memoId, tagId);
			}
			mDb.setTransactionSuccessful();
		}
		finally {
			mDb.endTransaction();
		}
	}
	
	public Cursor getMemoTags(Long memoId) {
		// This didn't work as an outer join because memo_id is always set.
		String query = "select tag._id as _id, tag.name as name, memo_tag.memo_id as memo_id" +
				" from tag, memo_tag where tag._id = memo_tag.tag_id and memo_tag.memo_id = " + memoId +
				" UNION ALL select tag._id as _id, tag.name as name, 0 from tag" + 
				" WHERE NOT EXISTS (select memo_tag._id from memo_tag where memo_tag.tag_id = tag._id and memo_tag.memo_id = " + memoId +")";
		return mDb.rawQuery(query, null);
	}

	public boolean toggleTag(Long memoId, Long tagId) {
		try {
			mDb.beginTransaction();
			int deleted = removeMemoTag(memoId, tagId);
			if (deleted == 0) {
				// if not, add one
				addMemoTag(memoId, tagId);
			}
			mDb.setTransactionSuccessful();
			return (deleted == 0);
		}
		finally {
			mDb.endTransaction();
		}
	}
}
