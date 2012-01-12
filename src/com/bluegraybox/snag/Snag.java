package com.bluegraybox.snag;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Snag extends ListActivity {
    private static final int ACTIVITY_ABOUT = 1;
	private static final int ACTIVITY_EDIT = 2;
	private DbAdapter mDb;
	private Snag mThis;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;  // For our inner classes & callbacks
        setContentView(R.layout.main);
        mDb = new DbAdapter(this);
        loadListData();
        
        Button addButton = (Button) findViewById(R.id.add_memo);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(mThis, EditMemo.class);
                startActivityForResult(i, ACTIVITY_EDIT);
            }
        });
    }

	private void loadListData() {
		Cursor memoCursor = mDb.getMemos();
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
		loadListData();
	}
}