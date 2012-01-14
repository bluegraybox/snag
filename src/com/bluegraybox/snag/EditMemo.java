package com.bluegraybox.snag;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditMemo extends Activity {

    protected static final int ACTIVITY_TAG = 1;
	private EditText mBodyText;
    private Long mMemoId;
	private DbAdapter mDb;
	private EditMemo mThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDb = new DbAdapter(this);
        mThis = this;  // So inner classes can reference us
        
        setContentView(R.layout.edit_memo);
        setTitle(R.string.edit_memo);

        mBodyText = (EditText) findViewById(R.id.body);

        Button saveButton = (Button) findViewById(R.id.save_memo);
        Button deleteButton = (Button) findViewById(R.id.delete_memo);
        Button tagButton = (Button) findViewById(R.id.tag_memo);

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

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	// FIXME: add a pop-up confirmation here
                mDb.deleteMemo(mMemoId);
                setResult(RESULT_OK);
                finish();
            }
        });

        tagButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
        		Intent i = new Intent(mThis, TagMemo.class);
        		i.putExtra(DbAdapter.ID, mMemoId);
        		startActivityForResult(i, ACTIVITY_TAG);
            }
        });
    }

	private void populateFields() {
		if (mMemoId != null) {
			Cursor memo = mDb.getMemo(mMemoId);
	    	int index = memo.getColumnIndexOrThrow(DbAdapter.BODY);
			mBodyText.setText(memo.getString(index));
			memo.close();
		}
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
		String body = mBodyText.getText().toString();
		
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
