package org.spontaneous.activities;

import java.util.ArrayList;

import org.spontaneous.R;
import org.spontaneous.activities.adapter.DrawerListAdapter;
import org.spontaneous.activities.util.CustomExceptionHandler;
import org.spontaneous.activities.util.NavItem;
import org.spontaneous.fragment.LogoutFragment;
import org.spontaneous.fragment.MyActivitiesFragment;
import org.spontaneous.fragment.MyActivitiesFragmentOld;
import org.spontaneous.fragment.MyActivitiesRESTFragment;
import org.spontaneous.fragment.NavigationDrawerFragment;
import org.spontaneous.fragment.StartFragment;
import org.spontaneous.fragment.StartFragmentMap;
import org.spontaneous.utility.Constants;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	private static String TAG = MainActivity.class.toString();
	
	private SharedPreferences sharedPrefs;
	private Context mContext;
	
	private int backPressedCounter = 0;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private ListView mDrawerList;
	private RelativeLayout mDrawerPane;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	
	private TextView mUsername;
	private TextView mLastname;
	
	private ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
	 
	private Toolbar toolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = this;

		registerExceptionHandler(); 
		
		mUsername = (TextView) findViewById(R.id.userName);
		sharedPrefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
		if (mUsername != null) {
			String firstname = sharedPrefs.getString(Constants.PREF_FIRSTNAME, "unknown");
			mUsername.setText("Hallo " + firstname + "!");
		}
		
//	    mNavItems.add(new NavItem(getResources().getString(R.string.title_start), 
//	    		"Starte eine Aktivität", R.drawable.ic_activity));
	    mNavItems.add(new NavItem(getResources().getString(R.string.title_startMap),
	    		"Starte eine Aktivität", R.drawable.ic_activity));
//	    mNavItems.add(new NavItem(getResources().getString(R.string.title_myActivities),
//	    		"Das habe ich erreicht", R.drawable.ic_list));
	    mNavItems.add(new NavItem(getResources().getString(R.string.title_myActivitiesList),
	    		"Das habe ich erreicht", R.drawable.ic_list));
//	    mNavItems.add(new NavItem(getResources().getString(R.string.title_myActivitiesREST),
//	    		"Das habe ich erreicht", R.drawable.ic_list));
	    mNavItems.add(new NavItem(getResources().getString(R.string.title_Logout),
	    		"Ich will hier raus", R.drawable.ic_logout));

	    // DrawerLayout
	    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
	 
	    // Populate the Navigtion Drawer with options
	    mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
	    mDrawerList = (ListView) findViewById(R.id.navList);
	    DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
	    mDrawerList.setAdapter(adapter);

	    // Drawer Item click listeners
	    mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            selectItemFromDrawer(position);
	        }
	    });
	    
	    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
	    		R.string.navigation_drawer_open, 
	    		R.string.navigation_drawer_close) {

			@Override
	        public void onDrawerOpened(View drawerView) {
	            super.onDrawerOpened(drawerView);
	     
	            invalidateOptionsMenu();
	        }
	     
			@Override
	        public void onDrawerClosed(View drawerView) {
	            super.onDrawerClosed(drawerView);
	            Log.d(TAG, "onDrawerClosed: " + getTitle());
	     
	            invalidateOptionsMenu();
	        }
	    };
	     
	    mDrawerLayout.setDrawerListener(mDrawerToggle);

	    // Toolbar		
		toolbar = (Toolbar) findViewById(R.id.tool_bar);
	    toolbar.setTitle(R.string.app_name);
	    toolbar.setSubtitle(R.string.app_name_subtitle);

	    toolbar.inflateMenu(R.menu.toolbar_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.action_music:
                        Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
                        startActivity(intent);
                        return true;
                }

                return false;
            }
        });
        
        //Navigation Icon
        toolbar.setNavigationIcon(R.drawable.ic_burger_white);
        //toolbar.setLogo(R.drawable.ic_launcher);
        //toolbar.setLogoDescription(R.string.logo_description);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Navigation",Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });
        
        selectItemFromDrawer(0);
	}

	private void registerExceptionHandler() {
		if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
					getExternalCacheDir().toString(), null));
		}
	}

	/*
	* Called when a particular item from the navigation drawer
	* is selected.
	* */
	private void selectItemFromDrawer(int position) {
	    //Fragment fragment = new PreferencesFragment();
	 
	    FragmentManager fragmentManager = getFragmentManager();
 
	    if (position == 0) {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					StartFragmentMap.newInstance(position, this)).commit();
		}
		else if (position == 1) {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					MyActivitiesFragmentOld.newInstance(position, this)).commit();
		}
		else {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					LogoutFragment.newInstance(position, this)).commit();
		}

	    mDrawerList.setItemChecked(position, true);
	    setTitle(mNavItems.get(position).getmTitle());
	 
	    // Close the drawer
	    mDrawerLayout.closeDrawer(mDrawerPane);
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();

		if (position == 0) {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					StartFragment.newInstance(position, this)).commit();
		}
		else if (position == 1) {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					StartFragmentMap.newInstance(position, this)).commit();
		}
		else if (position == 2) {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					MyActivitiesFragmentOld.newInstance(position, this)).commit();
		}
		else if (position == 3) {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					MyActivitiesFragment.newInstance(position, this)).commit();
		}
		else if (position == 4) {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					MyActivitiesRESTFragment.newInstance(position, this)).commit();
		}
		else {
			fragmentManager
			.beginTransaction()
			.replace(R.id.mainContent,
					LogoutFragment.newInstance(position, this)).commit();
		}
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 0:
			mTitle = getString(R.string.title_start);
			break;
		case 1:
			mTitle = getString(R.string.title_startMap);
			break;
		case 2:
			mTitle = getString(R.string.title_myActivities);
			break;
		case 3:
			mTitle = getString(R.string.title_myActivitiesREST);
			break;
		case 4:
			mTitle = getString(R.string.title_Logout);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		//actionBar.setTitle(mTitle);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			Integer i = getArguments().getInt(ARG_SECTION_NUMBER);
			if (i == 1) {
				View rootView = inflater.inflate(R.layout.fragment_main, container,
						false);
				return rootView;
			}
			else {
				View rootView = inflater.inflate(R.layout.list_layout, container,
						false);
				return rootView;
			}
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

	@Override
	public void onBackPressed() {
		if (backPressedCounter <= 0) {
			Toast.makeText(this, "Nochmaliges Tippen beendet die App.", Toast.LENGTH_SHORT).show();
			backPressedCounter++;
			Thread timer = new Thread() {  
	            public void run() {  
	                try {  
	                    sleep(1000); // wait 1 second  
	                } catch (InterruptedException e) {  
	                    e.printStackTrace();  
	                } finally {  
	                	backPressedCounter = 0;
	                }  
	            }  
	        };  
	        timer.start();
		}
		else {
			backPressedCounter = 0;
			this.finish();
		}
	}

	public Long getUserId() {
		
		if (sharedPrefs == null)
			sharedPrefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
		if (sharedPrefs != null) {
			return sharedPrefs.getLong(Constants.PREF_USERID, -1L);
		}
		return null;
	}
	
}
