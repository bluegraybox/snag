package com.bluegraybox.snag;

import android.app.ListActivity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

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
        mDb = DbAdapter.instance(this);

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
            memos.setViewBinder(new ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    int nameIndex = cursor.getColumnIndexOrThrow(DbAdapter.NAME);
                    int memoIdIndex = cursor.getColumnIndexOrThrow(DbAdapter.MEMO_ID);
                    String tagName = cursor.getString(nameIndex);
                    long memoId = cursor.getLong(memoIdIndex);
                    TextView name = (TextView) view;  // view.findViewById(R.id.tag_name);
                    name.setText(tagName);
                    name.setTextColor((memoId != 0) ? Color.GREEN : Color.BLACK);
                    return true;
                }
            });
            setListAdapter(memos);
            mNewTagText.setText("");
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        boolean added = mDb.toggleTag(mMemoId, id);
        TextView name = (TextView) v.findViewById(R.id.tag_name);
        name.setTextColor(added ? Color.GREEN : Color.BLACK);
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
            mDb.createTag(mMemoId, body);
        }
    }

}
