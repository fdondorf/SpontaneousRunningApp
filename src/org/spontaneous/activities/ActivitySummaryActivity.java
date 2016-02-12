package org.spontaneous.activities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.spontaneous.R;
import org.spontaneous.activities.adapter.SplitTimeArrayAdapter;
import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SegmentModel;
import org.spontaneous.activities.model.SplitTimeModel;
import org.spontaneous.activities.model.TimeModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.CustomExceptionHandler;
import org.spontaneous.activities.util.DateUtil;
import org.spontaneous.activities.util.StringUtil;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.TrackingUtil;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.trackservice.util.TrackingServiceConstants;
import org.spontaneous.utility.Constants;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.ViewFlipper;

public class ActivitySummaryActivity extends Activity {

  private static final String TAG = "ActivitySummaryActivity";

  private static final int RESULT_ACTIVITY_SAVED = 1;
  private static final int RESULT_ACTIVITY_RESUMED = 2;
  private static final int RESULT_DELETED = 3;
  private static final int RESULT_OK = 4;
  
  private int mRequestCode = 0;
  private int REQUESTCODE_VALUE_MYACTIVITIES = 1;

  private ITrackingService trackingService = TrackingServiceImpl.getInstance(this);

  private TrackModel mTrackModel = null;
  private List<GeoPointModel> geoPoints = null;

  private GoogleMap map;

  private TextView mDurationView;
  private TextView mDistanceView;
  private TextView mCaloriesView;
  private TextView mAverageSpeedView;
  private Button mSplitTimesBtn;
  private Button mSaveActivityBtn;
  private ViewFlipper viewFlipper;
  
  private ListView listView;
  
  private Toolbar toolbar;
  
  private float lastX;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  requestWindowFeature(Window.FEATURE_ACTION_BAR);

	  //setContentView(R.layout.fragment_activity_summary);
	  setContentView(R.layout.layout_activity_summary);
	  
	  registerExceptionHandler();
	   
	  // Toolbar		
	  toolbar = (Toolbar) findViewById(R.id.tool_bar);
	  toolbar.setTitle(R.string.title_activity_activity_summary);
	  toolbar.inflateMenu(R.menu.activity_summary);
	  toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
	  
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              onBackPressed();
          }
      });
      
      toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem menuItem) {

              switch (menuItem.getItemId()){
                  case R.id.action_delete_activity:
                	  createDeletionDialog();
                	  return true;
              }
              return false;
          }
      });
		    
	  ActionBar ab = getActionBar();
	  if (ab != null) {
		  ab.setHomeButtonEnabled(true);
	  }
	  
	  Bundle data = getIntent().getExtras();
	  
	  // Get track from db
	  mRequestCode = data.getInt(TrackingServiceConstants.REQUEST_CODE);
	  mTrackModel = trackingService.readTrackById(data.getLong(TrackingServiceConstants.TRACK_ID));

	  // Get the geopoint for the track from db
	  geoPoints = trackingService.getGeoPointsByTrack(data.getLong(TrackingServiceConstants.TRACK_ID));

	  viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
	  
	  // Initialize GUI-Components
	  map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
	        .getMap();
	  mDurationView = (TextView) findViewById(R.id.timeText);
	  mDurationView.setText("00:00");
	  mDistanceView = (TextView) findViewById(R.id.distanceText);
	  mCaloriesView = (TextView) findViewById(R.id.caloriesValue);
	  mCaloriesView.setText("0");
	  mAverageSpeedView = (TextView) findViewById(R.id.currentAveragePerUnit);
	  mAverageSpeedView.setText(StringUtil.getSpeedString(0F));

	  //mSplitTimesBtn = (Button) findViewById(R.id.btn_splittimes);
	  //mSplitTimesBtn.setOnClickListener(mSplitTimesBtnListener);
	  
	  mSaveActivityBtn = (Button) findViewById(R.id.btn_saveActivity);
	  mSaveActivityBtn.setOnClickListener(mSaveActivityBtnListener);

	  if (mTrackModel != null) {

		  mDistanceView.setText(StringUtil.getDistanceString(mTrackModel.getTotalDistance()));

		  // Compute duration and average speed
		  TimeModel timeModel = computeTotalDuration(mTrackModel);
		  mAverageSpeedView.setText(DateUtil.millisToShortDHMS(computeAverageTimePerKilometer(timeModel.getTotalDuration(), mTrackModel.getTotalDistance())));
		  mDurationView.setText(DateUtil.millisToShortDHMS(timeModel.getTotalDuration()));
		  mCaloriesView.setText(String.valueOf(
				  TrackingUtil.computeCalories(70, mTrackModel.getTotalDistance())));
		  
		  List<SplitTimeModel> splitTimes = TrackingUtil.computeAverageSpeedPerUnit(mTrackModel, geoPoints, Constants.KILOMETER);
		  listView = (ListView) findViewById(R.id.splitTimes);
		  listView.setAdapter(new SplitTimeArrayAdapter(this, splitTimes));
		  
		  if (!geoPoints.isEmpty()) {

			  // Draw line on map and add visual components like markers ...
			  LatLng start = new LatLng(geoPoints.get(0).getLatitude(), geoPoints.get(0).getLongitude());

			  for (PolylineOptions options : drawPolyLine(geoPoints)) {
				  map.addPolyline(options);
			  }
			  map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));
		  }
	  }
	  
	  if (mRequestCode == REQUESTCODE_VALUE_MYACTIVITIES) {
		  mSaveActivityBtn.setVisibility(View.INVISIBLE);
	  }
  }

  private TimeModel computeTotalDuration(TrackModel trackModel) {

	  TimeModel timeModel = new TimeModel();

	  // Compute duration
	  Long totalDurationInMillis = 0L;
	  Long timeCurrentSegement = null;

	  for (SegmentModel segmentModel : trackModel.getSegments()) {
		  timeCurrentSegement = segmentModel.getEndTimeInMillis() - segmentModel.getStartTimeInMillis();
		  totalDurationInMillis += timeCurrentSegement;
	  }

	  timeModel.setTotalDuration(totalDurationInMillis);
	  return timeModel;
  }

  private Map<Long, List<GeoPointModel>> createDataStructure(List<GeoPointModel> geoPoints) {

	  // Create data structure geoPoints by segments
	  Map<Long, List<GeoPointModel>> geoPointsBySegments = new TreeMap<Long, List<GeoPointModel>>();
	  for (GeoPointModel geoPointModel : geoPoints) {
		  if (geoPointsBySegments.get(geoPointModel.getSegmentId()) == null) {
			  geoPointsBySegments.put(geoPointModel.getSegmentId(), new ArrayList<GeoPointModel>());
		  }
		  geoPointsBySegments.get(geoPointModel.getSegmentId()).add(geoPointModel);
	  }

	  return geoPointsBySegments;
  }


  @Override
  public void onBackPressed() {
	  super.onBackPressed();
	  setResult(RESULT_ACTIVITY_RESUMED);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	  super.onActivityResult(requestCode, resultCode, intent);
  }

  /**
   * Computes the average time in minutes per kilometer (m/km).
   * @param totalDuration in Milliseconds (ms)
   * @param totalDistance in meters (m)
   * @return The average time per Km in Millis.
   */
  private Long computeAverageTimePerKilometer(Long totalDuration, Float totalDistance) {

	  // Average speed in m/s
	  if (totalDistance == null || totalDistance <= 0F) {
		  return 0L;
	  }
	  Float totalDistInKm = totalDistance / Constants.KILOMETER;

	  BigDecimal averageTimeInMillisPerKm = new BigDecimal(totalDuration / totalDistInKm);
	  return averageTimeInMillisPerKm.longValue();
  }

  private List<PolylineOptions> drawPolyLine(List<GeoPointModel> geoPoints) {

	  // Create data structure geoPoints by segments
	  Map<Long, List<GeoPointModel>> geoPointsBySegments = createDataStructure(geoPoints);

	  float kmMarker = Constants.KILOMETER;
	  int km = 1;
	  List<PolylineOptions> polyLineOptions = new ArrayList<PolylineOptions>();
	  boolean startOfFirstSegment = true;
	  Double totalDistanceCurrentPoint = 0D;
	  Double totalDistancePrevPoint = 0D;

	  GeoPointModel lastPointOfTrack = null;
	  for (Entry<Long, List<GeoPointModel>> entry : geoPointsBySegments.entrySet()) {
		  if (!entry.getValue().isEmpty()) {
			  PolylineOptions option = new PolylineOptions();
			  option.width(5).color(R.color.lightgreen);

			  boolean firstPointOfSegment = true;
			  for (GeoPointModel point : entry.getValue()) {
				  totalDistanceCurrentPoint += point.getDistance();
				  LatLng latLngPoint = new LatLng(point.getLatitude(), point.getLongitude());
				  if (startOfFirstSegment) {
					  map.addMarker(new MarkerOptions().position(latLngPoint)
								 .title("Stop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
					  startOfFirstSegment = false;
					  firstPointOfSegment = false;
				  }
				  else if (firstPointOfSegment) {
					  map.addMarker(new MarkerOptions().position(latLngPoint)
								 .title("Stop").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
					  firstPointOfSegment = false;
				  }

				  if (totalDistancePrevPoint <= kmMarker && totalDistanceCurrentPoint >= kmMarker) {

					  createKmMarker(latLngPoint, km);
					  kmMarker += Constants.KILOMETER;
					  km++;
				  }
				  option.add(latLngPoint);
				  lastPointOfTrack = point;
				  totalDistancePrevPoint = totalDistanceCurrentPoint;
			  }
			  polyLineOptions.add(option);
		  }
	  }

	  // Set Marker of last point
	  LatLng latLngPoint = new LatLng(lastPointOfTrack.getLatitude(), lastPointOfTrack.getLongitude());
	  map.addMarker(new MarkerOptions().position(latLngPoint)
		        .title("Ende")
		        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

	  return polyLineOptions;
  	}


  	private void createKmMarker(LatLng position, int km) {

  		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
  		Bitmap bitmap = Bitmap.createBitmap(32, 37, conf);

	  	// paint defines the text color,
	  	// stroke width, size
	  	Paint color = new Paint();
	  	color.setTextSize(15);
	  	color.setColor(Color.WHITE);

  		Canvas canvas = new Canvas(bitmap);
  		canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
  		    R.drawable.custom_marker), 0, 0, color);
  		canvas.drawText(km + " km", 3, 20, color);
  		MarkerOptions options = new MarkerOptions().position(position).
  				icon(BitmapDescriptorFactory.fromBitmap(bitmap)).anchor(0.5f, 1);
  		map.addMarker(options);
  	}

  	/****************************************
  	 * Listener
  	 * *************************************/

  	private OnClickListener mSaveActivityBtnListener = new OnClickListener() {

	    public void onClick(View v) {

	    	// Save Activity and return
	    	updateTrack(null);

	    	if (mRequestCode == 0) {
	    		startMainActivity();
	    	}
	    	else if (mRequestCode == 1) {
	    		setResult(1);
	    		finish();
	    	}
	    	else {
	    		startMainActivity();
	    	}
	    }
  	};

  	private OnClickListener mSplitTimesBtnListener = new OnClickListener() {

	    public void onClick(View v) {
	    	startSplitTimesActivityForResult();
	    }
  	};

	private void updateTrack(String name) {

		ContentValues content = new ContentValues();
		content.put(Tracks.NAME, mTrackModel.getName());
		content.put(Tracks.TOTAL_DISTANCE, mTrackModel.getTotalDistance());
		content.put(Tracks.TOTAL_DURATION, mTrackModel.getTotalDuration());

		Uri trackUpdateUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(mTrackModel.getId()));
		getContentResolver().update(trackUpdateUri, content, null, null);
	}

	private void startSplitTimesActivityForResult() {
		Intent intent = new Intent();
    	intent.setClass(this, SplitTimesActivity.class);
    	intent.putExtra(TrackingServiceConstants.TRACK_ID, mTrackModel.getId());
    	startActivityForResult(intent, RESULT_OK);
	}

  	@Override
  	public boolean onCreateOptionsMenu(Menu menu) {
  		getMenuInflater().inflate(R.menu.activity_summary, menu);
  		return true;
  	}

  	private void createDeletionDialog() {
  		new AlertDialog.Builder(this)
	    .setTitle(R.string.standardWarningHdr)
	    .setMessage(R.string.deleteActivityRequest)
	    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	dialog.cancel();
	        }
	    })
	    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	boolean result = deleteActivity(mTrackModel.getId());
				if (result) {
					startMainActivity();
				}
	        }
	    })
	    .create().show();
  	}
  	
  	 public boolean onTouchEvent(MotionEvent touchevent) {
  		 
  		 switch (touchevent.getAction()) {

  	  		case MotionEvent.ACTION_DOWN: 
  	            lastX = touchevent.getX();
  	            break;
  
  	        case MotionEvent.ACTION_UP: 
  	            float currentX = touchevent.getX();

  	            // Handling left to right screen swap.
            if (lastX < currentX) {
            	
                // If there aren't any other children, just break.
                if (viewFlipper.getDisplayedChild() == 0)
                    break;

                // Next screen comes in from left.
                viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);

                // Current screen goes out from right. 
  	                viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);
  
  	                // Display next screen.
                viewFlipper.showNext();
            }
            // Handling right to left screen swap.
             if (lastX > currentX) {
            	 
                 // If there is a child (to the left), kust break.
                 if (viewFlipper.getDisplayedChild() == 1)
                     break;

                 // Next screen comes in from right.
                 viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);

                // Current screen goes out from left. 
                 viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

                // Display previous screen.
                 viewFlipper.showPrevious();
             }
             break;
        }
         return false;
    }
  	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_delete_activity) {

			new AlertDialog.Builder(this)
		    .setTitle(R.string.standardWarningHdr)
		    .setMessage(R.string.deleteActivityRequest)
		    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	dialog.cancel();
		        }
		    })
		    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	boolean result = deleteActivity(mTrackModel.getId());
					if (result) {
						startMainActivity();
					}
		        }
		    })
		    .create().show();

			return result;
		}

		return result;
	}

	private void startMainActivity() {
		Intent intent = new Intent();
    	intent.setClass(this, MainActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
	}

	private boolean deleteActivity(Long trackId) {

		// Delete track, segements and waypoints
	    Uri trackDeleteUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId));
	    int rowsDeleted = this.getContentResolver().delete(trackDeleteUri, null, null);

	    if (rowsDeleted > 0) {
	    	return true;
	    }
	    return false;
	}

	private void registerExceptionHandler() {
		if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
					getExternalCacheDir().toString(), null));
		}
	}
	
}