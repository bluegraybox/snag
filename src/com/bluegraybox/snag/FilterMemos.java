package com.bluegraybox.snag;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class FilterMemos extends ListActivity {

	private List<Long> mFilterTagIds = new ArrayList<Long>();
	private FilterMemos mThis;
	private DbAdapter mDb;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;  // So inner classes can reference us
        mDb = DbAdapter.instance(this);
        
        setContentView(R.layout.filter_memos);
        setTitle(R.string.filter_memos_title);

        populateFields();

        Button saveButton = (Button) findViewById(R.id.filter_by_tags);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Intent data = new Intent();
            	int tagCount = mFilterTagIds.size();
				long[] tagIds = new long[tagCount];
            	for (int i = 0; i < tagCount; i++) {
					tagIds[i] = mFilterTagIds.get(i);
				}
//				data.getExtras().putLongArray(Snag.FILTERS, tagIds);
            	data.putExtra(Snag.FILTERS, tagIds);
            	long[] check = data.getExtras().getLongArray(Snag.FILTERS);
            	setResult(RESULT_OK, data);
                finish();
            }
        });
    }


	private void populateFields() {
		Cursor tagCursor = mDb.getTags();
		startManagingCursor(tagCursor);
		String[] from = new String[]{ DbAdapter.NAME };
		int[] to = new int[]{ R.id.tag_name };
		SimpleCursorAdapter tags = new SimpleCursorAdapter(this, R.layout.tag_row, tagCursor, from, to);
		setListAdapter(tags);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Toggle list membership
		boolean deleted = mFilterTagIds.remove(id);
		if (! deleted) {
			mFilterTagIds.add(id);
		}
		
		TextView name = (TextView) v.findViewById(R.id.tag_name);
		name.setTextColor(deleted ? Color.BLACK : Color.GREEN);
	}

}
