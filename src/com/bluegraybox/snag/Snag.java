package com.bluegraybox.snag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Snag extends ListActivity {
    private static final int ACTIVITY_ABOUT = 1;
	private static final int ACTIVITY_EDIT = 2;
	private static final int ACTIVITY_FILTER = 3;
	static final String FILTERS = "FILTERS";
	private DbAdapter mDb;
	private Snag mThis;
	private Button mFilterButton;
	private View.OnClickListener mFilterOnClickListener;
	private OnClickListener mClearFilterOnClickListener;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;  // For our inner classes & callbacks
        setContentView(R.layout.main);
        mDb = new DbAdapter(this);
        
        Button addButton = (Button) findViewById(R.id.add_memo);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(mThis, EditMemo.class);
                startActivityForResult(i, ACTIVITY_EDIT);
            }
        });
        
        mFilterButton = (Button) findViewById(R.id.filter_memos);
        mFilterOnClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(mThis, FilterMemos.class);
                startActivityForResult(i, ACTIVITY_FILTER);
            }
        };
        mClearFilterOnClickListener = new View.OnClickListener() {
            public void onClick(View view) {
            	loadFullListData();
            }
        };
        loadFullListData();
    }

	private void loadFullListData() {
    	mFilterButton.setText(R.string.filter_memos);
    	mFilterButton.setOnClickListener(mFilterOnClickListener);
		Cursor memoCursor = mDb.getMemos();
		loadListData(memoCursor);
	}

	private void loadFilteredListData(List<Long> tagIdList) {
    	mFilterButton.setText(R.string.clear_memo_filter);
    	mFilterButton.setOnClickListener(mClearFilterOnClickListener);
		Cursor memoCursor = mDb.getFilteredMemos(tagIdList);
		loadListData(memoCursor);
	}

	private void loadListData(Cursor memoCursor) {
		startManagingCursor(memoCursor);
		String[] from = new String[]{ DbAdapter.SLUG };
		int[] to = new int[]{ R.id.slug };
		SimpleCursorAdapter memos = new SimpleCursorAdapter(this, R.layout.memo_row, memoCursor, from, to);
		setListAdapter(memos);
	}

	
	/* Menu-related methods */
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int menuItemId = item.getItemId();
		switch(menuItemId) {
            case R.id.about:
                Intent i = new Intent(this, About.class);
                startActivityForResult(i, ACTIVITY_ABOUT);
                return true;
            case R.id.tidy_db:
            	mDb.tidy();
            	return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, EditMemo.class);
		i.putExtra(DbAdapter.ID, id);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ACTIVITY_FILTER) {
			List<Long> tagIdList = new ArrayList<Long>();
			long[] tagIds = data.getExtras().getLongArray(FILTERS);
			if (tagIds.length > 0) {
				for (long tagId : tagIds) {
					tagIdList.add(tagId);
				}
				loadFilteredListData(tagIdList);
			}
			else {
				loadFullListData();
			}
		}
		else {
			loadFullListData();
		}
	}
}