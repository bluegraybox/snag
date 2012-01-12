package com.bluegraybox.snag;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditMemo extends Activity {

    private EditText mBodyText;
    private Long mMemoId;
	private DbAdapter mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDb = new DbAdapter(this);
        
        setContentView(R.layout.edit_memo);
        setTitle(R.string.edit_memo);

        mBodyText = (EditText) findViewById(R.id.body);

        Button saveButton = (Button) findViewById(R.id.save_memo);
        Button deleteButton = (Button) findViewById(R.id.delete_memo);

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
    }

	private void populateFields() {
		if (mMemoId != null) {
			Cursor memo = mDb.getMemo(mMemoId);
			startManagingCursor(memo);
	    	int index = memo.getColumnIndexOrThrow(DbAdapter.BODY);
			mBodyText.setText(memo.getString(index));
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
