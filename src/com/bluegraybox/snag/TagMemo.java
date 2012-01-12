package com.bluegraybox.snag;

import java.util.Set;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TagMemo extends ListActivity {

    protected static final int ACTIVITY_TAG = 1;
	private EditText mNewTagText;
    private Long mMemoId;
	private DbAdapter mDb;
	private TagMemo mThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;  // So inner classes can reference us
        mDb = new DbAdapter(this);
        
        setContentView(R.layout.tag_memo);
        setTitle(R.string.tag_memo);

        mNewTagText = (EditText) findViewById(R.id.new_tag);
        Button addTagButton = (Button) findViewById(R.id.add_new_tag);
        Button saveButton = (Button) findViewById(R.id.save_tags);

        mMemoId = null;
        if (savedInstanceState != null) {
        	// use getSerializable because getLong defaults to 0, not null
        	mMemoId = (Long) savedInstanceState.getSerializable(DbAdapter.ID);
        }
        if (mMemoId == null) {
        	Bundle extras = getIntent().getExtras();
        	if (extras != null) {
        		mMemoId = extras.getLong(DbAdapter.ID);
        	}
        }
        
        populateFields();

        addTagButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mDb.createTag(mMemoId, mNewTagText.getText().toString());
                populateFields();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

	private void populateFields() {
		if (mMemoId != null) {
			Cursor tags = mDb.getMemoTags(mMemoId);
			startManagingCursor(tags);
			String[] from = new String[]{ DbAdapter.NAME };
			int[] to = new int[]{ R.id.tag_name };
			SimpleCursorAdapter memos = new SimpleCursorAdapter(this, R.layout.tag_row, tags, from, to);
			setListAdapter(memos);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mDb.toggleTag(mMemoId, id);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(DbAdapter.ID, mMemoId);
	}

	private void saveState() {
		Editable text = mNewTagText.getText();
		String body = text.toString();
		if (text != null && ! "".equals(body.trim())) {
			if (mMemoId == null) {
				long id = mDb.createMemo(body);
				if (id > 0) {
					mMemoId = id;
				}
			}
			else {
				mDb.updateMemo(mMemoId, body);
			}
		}
	}

}
