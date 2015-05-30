package com.marketstatistics;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.*;
import com.actionbarsherlock.app.ActionBar.*;
import com.actionbarsherlock.view.*;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends SherlockActivity
{
    private ListView listView;
	private static final int ACTIVITY_CREATE = 0;
	private static final int REMOVE_ITEM = 0;

    private TextView timeTV, dateTV;
	private Button mBtn;
	private String date;
	private EditText itemET, priceMaxET, priceMinET;

	private Tab listTab, newTab;
	private Context context = this;
	private boolean startBI = false;
	private int tabS = 0;
	private Intent i;
	public boolean iA = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		DBAdapter.init(this);
        super.onCreate(savedInstanceState);

		try
		{
			i = getIntent();
			if (i.getExtras().getString("ItemName") != null)
			{
				startBI = true;
			}
		}
		catch (RuntimeException e)
		{}
		setTabs();
    }

	private void setTabs()
	{
		// TODO: Implement this method
		ActionBar mActionB = getSupportActionBar();
		mActionB.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		listTab = mActionB.newTab();
		listTab.setText("Item List");
		listTab.setTabListener(newTabListener);
		mActionB.addTab(listTab);

		newTab = mActionB.newTab();
		newTab.setText("New Item");
		newTab.setTabListener(newTabListener);
		mActionB.addTab(newTab);
		
		if (startBI)newTab.select();
	}

	TabListener newTabListener = new TabListener()
	{
		// called when the selected Tab is re-selected
		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1)
		{} // end method onTabReselected

		// called when a previously unselected Tab is selected
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction arg1)
		{
			// display the information corresponding to the selected Tab
			selectTab(tab.getPosition());
			//Toast.makeText(MyListActivity.this, String.valueOf(tab.getPosition()), Toast.LENGTH_SHORT).show();
		} // end method onTabSelected

		// called when a tab is unselected
		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1)
		{} // end method onTabSelected

	}; // end WeatherTabListener

	private void selectTab(int position)
	{
		if (position == 0)
		{
			prepareForTab_list();
			tabS = 0;
		}
		else if (position == 1)
		{
			prepareForTab_newItem();
			tabS = 1;
		}
	}

	private void prepareForTab_list()
	{
		// TODO: Implement this method
		if (iA)finish();
		setContentView(R.layout.main_b);

		listView = (ListView) findViewById(android.R.id.list);
		listView.setOnItemClickListener(lVListener);

		fillData();
		registerForContextMenu(listView);
	}

	private void prepareForTab_newItem()
	{
		// TODO: Implement this method
		setContentView(R.layout.main);

		timeTV = (TextView) findViewById(R.id.timeTextView);
		dateTV = (TextView) findViewById(R.id.dateTextView);
		itemET = (EditText) findViewById(R.id.itemEditText);
		priceMaxET = (EditText) findViewById(R.id.priceMaxEditText);
		priceMinET = (EditText) findViewById(R.id.priceMinEditText);
		if (startBI)
		{
			itemET.setText(i.getExtras().getString("ItemName"));
			priceMaxET.requestFocus();
			iA = true;
		}
		else
		{
			itemET.requestFocus();
		}
	
		setTimeAndDate();

		mBtn = (Button) findViewById(R.id.mButton);
		mBtn.setOnClickListener(mBtnListener);
	}

	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Operations");
		menu.add(0, REMOVE_ITEM, Menu.NONE, "Delete");
    }

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item)
	{
		super.onContextItemSelected(item);
		switch (item.getItemId())
		{
			case (REMOVE_ITEM): {
					AdapterView.AdapterContextMenuInfo menuInfo;
					menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
					DBAdapter.deleteItemList(((TextView)menuInfo.targetView).getText().toString());
					fillData();
					Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
					return true;
				}
		}
		return false;
	}

	private void fillData()
	{
		Cursor userCursor = DBAdapter.getAllItemNames();
		startManagingCursor(userCursor);

		// Create an array to specify the fields we want (only the TITLE)
		String[] title = new String[] { DBAdapter.KEY_ITEM_NAME };

		// and an array of the fields we want to bind in the view
		int[] textView = new int[] { R.id.text1 };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter userD = new SimpleCursorAdapter(this, R.layout.listview, userCursor, title, textView);
		listView.setAdapter(userD);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(tabS == 0)fillData();
	}

	/*OnItemClickListener lVListener = new OnItemClickListener()
	 {
	 @Override
	 public void onItemClick(AdapterView<?> adapterV, View v, int id, long l)
	 {
	 String title = DBAdapter.getUserData(l).getItemName() + "    ;  Date : " + DBAdapter.getUserData(l).getDate();
	 String message = "Price(Max) : " + DBAdapter.getUserData(l).getPriceMax() + " , \nPrice(Min) : " + DBAdapter.getUserData(l).getPriceMin();

	 // TODO: Implement this method
	 AlertDialog.Builder builder = new AlertDialog.Builder(context);
	 builder.setTitle(title);

	 builder.setMessage(message);
	 builder.setCancelable(true);

	 AlertDialog mDialog = builder.create();
	 mDialog.show();
	 }
	 };*/

	OnItemClickListener lVListener = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> adapterV, View v, int index, long l)
		{
			getList(l);
		}
	};

	private void getList(long l)
	{
		// TODO: Implement this method
		Cursor mCursor = DBAdapter.fetchUserData(l);
		String searchNo = mCursor.getString(mCursor.getColumnIndexOrThrow(DBAdapter.KEY_ITEM_NAME));
		try
		{
			Intent i = new Intent(this, DetailList.class);
			i.putExtra("ItemName", searchNo);
			startActivity(i);
		}
		catch (RuntimeException e)
		{
			Toast.makeText(this, "Error ...", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_delete_all:
				deleteAll();
				return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void deleteAll()
	{
		// TODO: Implement this method
		//Intent i = new Intent(this, MainActivity.class);
		//startActivityForResult(i, ACTIVITY_CREATE);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Delete All");

		builder.setMessage("All items will be deleted. \nAre you sure you want to delete all items.");
		builder.setCancelable(true);
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int button)
				{
					//confirmDelete();

					DBAdapter.deleteAll();
					fillData();
				}
			});
		builder.setNegativeButton("Cancel", null);

		AlertDialog mDialog = builder.create();
		mDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
		Toast.makeText(this, "ActivityResult()", Toast.LENGTH_SHORT).show();
	}

	private void setTimeAndDate()
	{
		// TODO: Implement this method
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMMMMMMM, yyyy");
		date = dateFormat.format(Calendar.getInstance().getTime());
		dateTV.setText(date);

		SimpleDateFormat timeFormat = new SimpleDateFormat("kk:mm");
		String time = timeFormat.format(Calendar.getInstance().getTime());
		timeTV.setText(time);
	}

	// edit selected search
	OnClickListener mBtnListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (itemET.getText().toString().length() > 0 && priceMaxET.getText().toString().length() > 0 && priceMinET.getText().toString().length() > 0)
			{
				if (Integer.parseInt(priceMaxET.getText().toString()) > Integer.parseInt(priceMinET.getText().toString()))
				{
					String infoM = date + "  : Max " +  "₹ " + priceMaxET.getText().toString() + "  : Min " + "₹ " + priceMinET.getText().toString();
					DBAdapter.addUserData(new UserData(itemET.getText().toString(), "₹ " + priceMaxET.getText().toString(), "₹ " + priceMinET.getText().toString(), date, infoM));
					listTab.select();
					if (startBI)finish();
				}
				else
				{
					Toast.makeText(context, "Price entered in 'Min' must lower than of'Max'", Toast.LENGTH_SHORT).show();
					priceMinET.setText("");
					priceMinET.requestFocus();
				}
			}
			else
			{
				Toast.makeText(context, "First fill the fields", Toast.LENGTH_SHORT).show();
			}
		}
	};
}
