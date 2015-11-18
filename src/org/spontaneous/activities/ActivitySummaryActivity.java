package org.spontaneous.activities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.spontaneous.R;
import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SegmentModel;
import org.spontaneous.activities.model.TimeModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.DateUtil;
import org.spontaneous.activities.util.StringUtil;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.db.GPSTracking.Segments;
import org.spontaneous.db.GPSTracking.SegmentsColumns;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.Waypoints;
import org.spontaneous.db.GPSTracking.WaypointsColumns;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActivitySummaryActivity extends Activity {

  private static final String TAG = "ActivitySummaryActivity";

  private static final int RESULT_OK = 4;
  private static final int RESULT_ACTIVITY_SAVED = 1;
  private static final int RESULT_ACTIVITY_RESUMED = 2;
  private static final int RESULT_DELETED = 3;

  private int mRequestCode = 0;

  private static final Float KILOMETER = 1000f;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  setContentView(R.layout.fragment_activity_summary);

	  // Get track from db
	  Bundle data = getIntent().getExtras();
	  mRequestCode = data.getInt(TrackingServiceConstants.REQUEST_CODE);

	  mTrackModel = trackingService.readTrackById(data.getLong(TrackingServiceConstants.TRACK_ID)); //readTrackById(data.getLong(TrackingServiceConstants.TRACK_ID));

	  // Get the geopoint for the track from db
	  geoPoints = readGeoPointsForTrack(data.getLong(TrackingServiceConstants.TRACK_ID));

	  // Initialize GUI-Components
	  map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
	        .getMap();
	  mDurationView = (TextView) findViewById(R.id.view_stats_duration);
	  mDurationView.setText("00:00");
	  mDistanceView = (TextView) findViewById(R.id.view_stats_distance);
	  mCaloriesView = (TextView) findViewById(R.id.view_stats_calories);
	  mCaloriesView.setText("0");
	  mAverageSpeedView = (TextView) findViewById(R.id.view_stats_average);
	  mAverageSpeedView.setText(StringUtil.getSpeedString(0F));

	  mSplitTimesBtn = (Button) findViewById(R.id.btn_splittimes);
	  mSplitTimesBtn.setOnClickListener(mSplitTimesBtnListener);

	  mSaveActivityBtn = (Button) findViewById(R.id.btn_saveActivity);
	  mSaveActivityBtn.setOnClickListener(mSaveActivityBtnListener);

	  if (mTrackModel != null) {

		  mDistanceView.setText(StringUtil.getDistanceString(mTrackModel.getTotalDistance()));

		  // Compute duration and average speed
		  TimeModel timeModel = computeTotalDuration(mTrackModel);
		  mAverageSpeedView.setText(DateUtil.millisToShortDHMS(computeAverageTimePerKilometer(timeModel.getTotalDuration(), mTrackModel.getTotalDistance())));
		  mDurationView.setText(DateUtil.millisToShortDHMS(timeModel.getTotalDuration()));

		  if (!geoPoints.isEmpty()) {

			  // Draw line on map and add visual components like markers ...
			  LatLng start = new LatLng(geoPoints.get(0).getLatitude(), geoPoints.get(0).getLongitude());

			  for (PolylineOptions options : drawPolyLine(geoPoints)) {
				  map.addPolyline(options);
			  }
			  map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));
		  }
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

  private Long computeTotalDuration(List<GeoPointModel> geoPoints) {

	  // Create data structure geoPoints by segments
	  Map<Long, List<GeoPointModel>> geoPointsBySegments = createDataStructure(geoPoints);

	  // Compute duration
	  Long totalDurationInMillis = 0L;
	  Long timeCurrentGeoPoint = null;
	  Long timePreviousGeoPoint = null;

	  for (Entry<Long, List<GeoPointModel>> entry : geoPointsBySegments.entrySet()) {

		  for (GeoPointModel geoPoint : entry.getValue()) {
			  timeCurrentGeoPoint = geoPoint.getTime();
			  if (timePreviousGeoPoint != null) {
				  totalDurationInMillis += timeCurrentGeoPoint - timePreviousGeoPoint;
			  }
			  timePreviousGeoPoint = timeCurrentGeoPoint;
		  }
		  timeCurrentGeoPoint = null;
		  timePreviousGeoPoint = null;
	  }

	  return totalDurationInMillis;
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
	  Float totalDistInKm = totalDistance / KILOMETER;

	  BigDecimal averageTimeInMillisPerKm = new BigDecimal(totalDuration / totalDistInKm);
	  return averageTimeInMillisPerKm.longValue();
  }


  private TrackModel readTrackById(Long trackId) {

	  if (trackId != null) {
	      String[] mTrackColumns = {
	    		Tracks._ID,
	   		   	Tracks.NAME,
	   		   	Tracks.TOTAL_DISTANCE,
	   		   	Tracks.TOTAL_DURATION,
	   		   	Tracks.CREATION_TIME
	      };

	      // Read track
	      Uri trackReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId));
	      Cursor mCursor = this.getContentResolver().query(trackReadUri, mTrackColumns, null, null, Tracks.CREATION_TIME);
	      if (mCursor != null && mCursor.moveToNext()) {

	    	// TODO: Quickfix wieder entfernen
				Long totalDuration = 0L;
				String value = null;
				if (mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION)) != null) {
					try {
						totalDuration = Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION)));
					} catch (Exception exc) {
						value = String.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION)));
						Log.i(TAG, "TotalDuration:" + value);
					}
				}


		      TrackModel trackModel = new TrackModel(
		    		Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks._ID))),
		    		mCursor.getString(mCursor.getColumnIndex(Tracks.NAME)),
					Float.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DISTANCE))),
					totalDuration,
					//Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION))),
					Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.CREATION_TIME)))
					);

		      // Read Segments and wayPoints
		      String[] mSegmentsColumns = {
		    		  BaseColumns._ID,
			   		  SegmentsColumns.TRACK,
			   		  SegmentsColumns.START_TIME,
			   		  SegmentsColumns.END_TIME
			      };
		      Uri segmentReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId) + "/segments/");
		      Cursor mSegmentsCursor = this.getContentResolver().query(segmentReadUri, mSegmentsColumns, null, null, Segments._ID);

		      if (mSegmentsCursor != null) {
			      SegmentModel segmentModel = null;
			      while (mSegmentsCursor.moveToNext()) {
			    	  segmentModel = new SegmentModel();
			    	  segmentModel.setId(Long.valueOf(mSegmentsCursor.getString(mSegmentsCursor.getColumnIndex(Segments._ID))));
			    	  segmentModel.setTrackId(Long.valueOf(mSegmentsCursor.getString(mSegmentsCursor.getColumnIndex(Segments.TRACK))));
			    	  segmentModel.setStartTimeInMillis(mSegmentsCursor.getLong(mSegmentsCursor.getColumnIndex(Segments.START_TIME)));
			    	  segmentModel.setEndTimeInMillis(mSegmentsCursor.getLong(mSegmentsCursor.getColumnIndex(Segments.END_TIME)));
			    	  trackModel.addSegment(segmentModel);
			      }
		      }
		      return trackModel;
	      }
	  }
	  return null;
  }

  private List<GeoPointModel> readGeoPointsForTrack(Long trackId) {

	  List<GeoPointModel> geoPoints = new ArrayList<GeoPointModel>();
	  if (trackId != null) {
		  String[] mWayPointColumns = {
				"waypoints."+BaseColumns._ID,
				WaypointsColumns.LATITUDE,
	   		   	WaypointsColumns.LONGITUDE,
	   		   	WaypointsColumns.TIME,
	   		   	WaypointsColumns.SPEED,
	   		   	WaypointsColumns.ACCURACY,
	   		   	WaypointsColumns.ALTITUDE,
	   		   	WaypointsColumns.BEARING,
	   			WaypointsColumns.DISTANCE,
	   		   	WaypointsColumns.SEGMENT
	      };

	      // Read track
	      Uri wayPointsReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId) + "/waypoints/");
	      Cursor mCursor = this.getContentResolver().query(wayPointsReadUri, mWayPointColumns, null, null, "waypoints."+Waypoints._ID);
	      while (mCursor.moveToNext()) {
	    	  GeoPointModel geoPointModel = new GeoPointModel();
	    	  geoPointModel.setId(Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Waypoints._ID))));
	    	  geoPointModel.setLatitude(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.LATITUDE)));
	    	  geoPointModel.setLongitude(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.LONGITUDE)));
	    	  geoPointModel.setTime(mCursor.getLong(mCursor.getColumnIndex(Waypoints.TIME)));
	    	  geoPointModel.setSpeed(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.SPEED)));
	    	  geoPointModel.setAccurracy(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.ACCURACY)));
	    	  geoPointModel.setAltitude(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.ALTITUDE)));
	    	  geoPointModel.setBearing(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.BEARING)));
	    	  geoPointModel.setDistance(mCursor.getDouble(mCursor.getColumnIndex(Waypoints.DISTANCE)));
	    	  geoPointModel.setSegmentId(Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Waypoints.SEGMENT))));

	    	  geoPoints.add(geoPointModel);
	      }
	  }
	  return geoPoints;
  }

  private List<PolylineOptions> drawPolyLine(List<GeoPointModel> geoPoints) {

	  // Create data structure geoPoints by segments
	  Map<Long, List<GeoPointModel>> geoPointsBySegments = createDataStructure(geoPoints);

	  float kmMarker = KILOMETER;
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
//					  map.addMarker(new MarkerOptions().position(latLngPoint)
//		     			        .title("Km " + kmMarker)
//		     			        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
					  kmMarker += KILOMETER;
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
  		Marker newMarker = map.addMarker(options);
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
		content.put(Tracks.NAME, name);
		content.put(Tracks.TOTAL_DISTANCE, mTrackModel.getTotalDistance());

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

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
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

}