package org.spontaneous.activities;

import org.spontaneous.R;
import org.spontaneous.activities.model.GeoPoint;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.StringUtil;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.trackservice.IRemoteService;
import org.spontaneous.trackservice.IRemoteServiceCallback;
import org.spontaneous.trackservice.RemoteService;
import org.spontaneous.trackservice.WayPointModel;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class CurrentActivityActivity extends Activity {

	private static final String TAG = "CurrentActivityActivity";

	private static final int RESULT_ACTIVITY_SAVED = 1;
	private static final int RESULT_DELETED = 3;

	private ITrackingService trackingService = TrackingServiceImpl.getInstance(this);

	private Context mContext;

    /** The primary interface we will be calling on the service. */
    IRemoteService mService = null;

    private int mLoggingState = TrackingServiceConstants.UNKNOWN;

	// GUI
	private Button mStopButton;
    private Button mPauseButton;
    private Button mResumeButton;
	private TextView latituteField;
	private TextView longitudeField;
	private TextView distanceField;
	private TextView speedField;
	private Chronometer mChronometer;
    private TextView mCallbackText;
    private boolean mIsBound;

	// Data
	private WayPointModel mTrackData = new WayPointModel();
	private Location mStartLocation;

	// Helper
	private long timeWhenStopped = 0;


	/** Called when the activity is first created. */
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.fragment_currentactivity);

	    mContext = this;

	    // Read start location
	    Bundle data = getIntent().getExtras();
	    if (data != null) {
	    	Long tId = data.getLong(TrackingServiceConstants.TRACK_ID);
	    	mStartLocation = (Location) data.getParcelable(TrackingServiceConstants.START_LOCATION);
	    }

	    latituteField = (TextView) findViewById(R.id.latitudeField);
	    longitudeField = (TextView) findViewById(R.id.longitudeField);
	    distanceField = (TextView) findViewById(R.id.distanceField);
	    distanceField.setText("0.0 km");
	    speedField = (TextView) findViewById(R.id.speedField);
	    speedField.setText("0 km/h");

	    mChronometer = (Chronometer) findViewById(R.id.chronometer);

	    // TODO: Muss das hier gesetzt werden? StartListening macht das auch!!!
	    mTrackData.setStartTime(System.currentTimeMillis());

	    mCallbackText = (TextView)findViewById(R.id.callback);
	    mCallbackText.setText("Not attached.");

	    mStopButton = (Button)findViewById(R.id.btn_stop);
	    mStopButton.setOnClickListener(mStopListener);

	    mResumeButton = (Button) findViewById(R.id.btn_resume);
	    mPauseButton = (Button) findViewById(R.id.btn_pause);

	    mPauseButton.setVisibility(View.VISIBLE);
	    mPauseButton.setOnClickListener(mPauseListener);

	    mResumeButton.setVisibility(View.GONE);
	    mResumeButton.setOnClickListener(mResumeListener);

	    // Starte und Binde den Background-Logging-Service
	    startAndBindService();

	    // Starte Chronometer
	    mChronometer.start();
	  }

	  private void startAndBindService() {
		  // Make sure the service is started. It will continue running
		  // until someone calls stopService().
		  // We use an action code here, instead of explictly supplying
		  // the component name, so that other packages can replace
		  // the service.
		  //startService(new Intent("com.example.remoteserviceexample.REMOTE_SERVICE"));
		  Intent service = null;
		  try {
			  Log.i(TAG, Class.forName(RemoteService.class.getName()).toString());
			  service = new Intent(mContext,  Class.forName(RemoteService.class.getName()));
			  //Log.i(TAG, service.getAction());
		  } catch (ClassNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  startService(service);

		  // Bind Service
		  bindService(service);
	  }

	  private void pauseLogging () {
		  try {
				mService.pauseLogging();
			} catch (RemoteException e) {
				e.printStackTrace();
				DialogHelper.createStandardErrorDialog(this);
			}
	  }

	  private void resumeLogging () {
		  try {
				mService.resumeLogging();
			} catch (RemoteException e) {
				e.printStackTrace();
				DialogHelper.createStandardErrorDialog(this);
			}
	  }

	  private void stopAndUnbindService() {
		  try {
				mService.stopLogging();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    	// Unbind service
	    	unbindService();

		    // Cancel a previous call to startService(). Note that the
		    // service will not actually stop at this point if there are
		    // still bound clients.
	    	Intent service = null;
			try {
				service = new Intent(mContext,  Class.forName(RemoteService.class.getName()));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	stopService(service);

	    	this.mService = null;
	    	this.mIsBound = false;
	  }

	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	      // Inflate the menu items for use in the action bar
	      MenuInflater inflater = getMenuInflater();
	      inflater.inflate(R.menu.current_activity, menu);
	      return super.onCreateOptionsMenu(menu);
	  }

	  @Override
	  protected void onStart() {
		  // TODO Auto-generated method stub
		  super.onStart();
	  }

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the current tracking state
		if (mTrackData != null) {
			if (mTrackData.getTrackId() != null)
				savedInstanceState.putLong(TrackingServiceConstants.TRACK_ID, mTrackData.getTrackId());
			if (mTrackData.getSegmentId() != null)
				savedInstanceState.putLong(TrackingServiceConstants.SEGMENT_ID, mTrackData.getSegmentId());
		}
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}



	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
	}

	/**
	   *
	   * Die onResume-Methode wird aufgerufen wowohl wenn aus der ActivitySummaryActivity via
	   * Back-Button zurückgekehrt wird als auch wenn die App lange inaktiv war un reaktiviert wird.
	   * Im ersten Fall ist der Remote-Tracking-Service gestoppt und muss neu gestartet werden, im
	   * zweiten Fall ist der Service bereits gestartet und darf nicht neu gestartet werden.
	   */
	  @Override
	  protected void onResume() {
	    super.onResume();

	    // Starte und Binde den Background-Logging-Service
	    if (mService == null && !mIsBound) {
	    	startAndBindService();
	    }
	    else if (!mIsBound) {

	    	Intent service = null;
			  try {
				  service = new Intent(mContext,  Class.forName(RemoteService.class.getName()));
			  } catch (ClassNotFoundException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
	    	bindService(service);
	    }

	    // Szenarien:
	    // 1. Gerät lange nicht verwendet, Status "Bind" and "LOGGING" --> hier
	    // 2. Von ActivitySummary zurück --> Status "Unbind" and "STOPPED" --> ServiceConnection
	    // 3. Activity "paused" und nicht verwendet --> Status "Bind" and "PAUSED" --> hier

	    // TODOs
	    // 1. Abfrage des Logging-Status des Services
	    // 1.1 Checke ob Service läuft und ob gebunden ist (Wenn nein, tue dies)
	    // 2. Logging-Status = Pause
	    // 2.1. Lade letzten Stand der Daten und zeige diese an
	    // 2.2. Setze Buttons korrekt
	    // 2.3. Setze chronometer auf korrekten wert aber gestoppt
	    // 3. Logging-Status = Stopped
	    // 3.1 Aufruf resumeLogging und speichern des Segments
	    // 4 Logging-Status = Logging
	    // 4.1 Lade aktuellen Stand der Daten und zeige diese an
	    // 4.2 Setze Buttons korrekt
	    // 4.3 Setze Chronometer auf korrekten Wert und Starte

	    try {
			if (mService != null && mService.loggingState() == TrackingServiceConstants.STOPPED) {
				mTrackData.setSegmentId(mService.resumeLogging());
				setButtonState(View.VISIBLE, View.GONE);
				setChronometerState(true);
			}
			else if (mService != null && mService.loggingState() == TrackingServiceConstants.PAUSED) {
				setButtonState(View.GONE, View.VISIBLE);
				setChronometerState(false);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//	    mPauseButton.setVisibility(View.VISIBLE);
//	    mResumeButton.setVisibility(View.GONE);
//
//	    timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
//	   	mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
//	   	mChronometer.start();
	}

	private void setChronometerState(boolean start) {
		timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
		//mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
		TrackModel trackModel = trackingService.readTrackById(mTrackData.getTrackId()); //getTrackDataById(mTrackData.getTrackId());
		mTrackData.setTotalTime(Long.valueOf(String.valueOf(trackModel.getTotalDuration())));

		mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped -
				mTrackData.getTotalTime());
		if (start)
			mChronometer.start();
		else {
			mChronometer.stop();
		}
	}

	private TrackModel getTrackDataById(Long trackId) {
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
			      TrackModel trackModel = new TrackModel(
			    		Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks._ID))),
			    		mCursor.getString(mCursor.getColumnIndex(Tracks.NAME)),
						Float.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DISTANCE))),
						Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.TOTAL_DURATION))),
						Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks.CREATION_TIME)))
						);
			      return trackModel;
		      }
		      return null;
	   }

	private void setButtonState(int pauseBtnState, int resumeBtnState) {
		mPauseButton.setVisibility(pauseBtnState);
		mResumeButton.setVisibility(resumeBtnState);
	}

	@Override
	  protected void onPause() {
	    super.onPause();
	    timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
	  }

	  @Override
	  public void onBackPressed() {
		// Do nothing to disable back-button
	  }

	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		  super.onActivityResult(requestCode, resultCode, intent);

		  if (resultCode == RESULT_DELETED) {
			  finish();
		  }
		  else if (resultCode == RESULT_ACTIVITY_SAVED){
			  finish();
		  }
	  }

	  @Override
	  protected void onStop() {
		  super.onStop();
	  }

	  /**
	   * Class for interacting with the main interface of the service.
	   */
	  private ServiceConnection mConnection = new ServiceConnection() {

		    public void onServiceConnected(ComponentName className,
		    		IBinder service) {

			    // This is called when the connection with the service has been
			    // established, giving us the service object we can use to
			    // interact with the service. We are communicating with our
			    // service through an IDL interface, so get a client-side
			    // representation of that from the raw service object.
			    mService = IRemoteService.Stub.asInterface(service);
			    mCallbackText.setText("Attached");

			    // We want to monitor the service for as long as we are
			    // connected to it.
			    try {

			    	if (mLoggingState == TrackingServiceConstants.STOPPED) {
			    		mService.registerCallback(mCallback);
				    	mTrackData.setSegmentId(
				    			mService.resumeLogging());
			    	}
			    	else {
				    	mService.registerCallback(mCallback);
				    	mTrackData.setTrackId(
				    			mService.startLogging(mStartLocation));
			    	}
			    	mLoggingState = mService.loggingState();
			    } catch (RemoteException e) {
				    // In this case the service has crashed before we could even
				    // do anything with it; we can count on soon being
				    // disconnected (and then reconnected if it can be restarted)
				    // so there is no need to do anything here.
			    }

			    // As part of the sample, tell the user what happened.
			    Toast.makeText(CurrentActivityActivity.this, R.string.remote_service_connected,
			    Toast.LENGTH_SHORT).show();
		    }

		    public void onServiceDisconnected(ComponentName className) {

			    // This is called when the connection with the service has been
			    // unexpectedly disconnected -- that is, its process crashed.
			    mService = null;
			    mCallbackText.setText("Disconnected.");
			    latituteField.setText("...");
			    longitudeField.setText("...");
			    distanceField.setText("...");

			    // As part of the sample, tell the user what happened.
			    Toast.makeText(CurrentActivityActivity.this, R.string.remote_service_disconnected,
			    Toast.LENGTH_SHORT).show();
		    }
	    };

	/****************************************
	 * Listener
	 * *************************************/

    private OnClickListener mPauseListener = new OnClickListener() {
	    public void onClick(View v) {

	    	// Pasue logging
	    	pauseLogging();

	    	// Button Dynamic
	    	setButtonState(View.GONE, View.VISIBLE);
//	    	mPauseButton.setVisibility(View.GONE);
//        	mResumeButton.setVisibility(View.VISIBLE);

	    	// Update Chronometer
	    	setChronometerState(false);
//	    	timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
//        	mChronometer.stop();
//        	mTrackData.setTotalTime(mChronometer.getBase());


	    }
    };

    private OnClickListener mResumeListener = new OnClickListener() {
	    public void onClick(View v) {

	    	// Button Dynamic
	    	setButtonState(View.VISIBLE, View.GONE);

	    	resumeLogging();

	    	// Update Chronometer
	    	setChronometerState(true);
//	    	mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
//        	mChronometer.start();
//        	mTrackData.setTotalTime(mChronometer.getBase());

	    }
    };

    private OnClickListener mStopListener = new OnClickListener() {
	    public void onClick(View v) {

	    	// Update Chronometer TODO Notwendig?
	    	// Muss vor stopAndUnbind geschehen
	    	setChronometerState(false);
//	    	timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
//        	mChronometer.stop();
//        	mTrackData.setTotalTime(mChronometer.getBase());

        	stopAndUnbindService();

        	mLoggingState = TrackingServiceConstants.STOPPED;

	    	Intent intent = new Intent();
	    	intent.setClass(mContext, ActivitySummaryActivity.class);
	    	intent.putExtra(TrackingServiceConstants.TRACK_ID, mTrackData.getTrackId());
	    	intent.putExtra(TrackingServiceConstants.REQUEST_CODE, 2);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(intent);
	    }
    };


    /******************************************
     *  Private Helper
     * ****************************************/

    private void bindService(Intent service) {
	    bindService(service, mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	    mCallbackText.setText("Binding...");
    }

    private void unbindService() {

    	if (mIsBound) {
		    // If we have received the service, and hence registered with
		    // it, then now is the time to unregister.
		    if (mService != null) {
			    try {
			    	mService.unregisterCallback(mCallback);
			    } catch (RemoteException e) {
				    // There is nothing special we need to do if the service
				    // has crashed.
			    }
		    }

		    // Detach our existing connection.
		    unbindService(mConnection);

		    //mKillButton.setEnabled(false);
		    mIsBound = false;
		    mCallbackText.setText("Unbinding.");
	    }
    }

    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------
    /**
    * This implementation is used to receive callbacks from the remote
    * service.
    */
    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {

    	/**
	    * This is called by the remote service regularly to tell us about
	    * new values. Note that IPC calls are dispatched through a thread
	    * pool running in each process, so the code executing here will
	    * NOT be running in our main thread like most other things -- so,
	    * to update the UI, we need to use a Handler to hop over there.
	    */
	    public void valueChanged(int value) {
		    mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
		}

		@Override
		public void locationChanged(WayPointModel wayPointModel) throws RemoteException {
			Message msg = mHandler.obtainMessage(LOCATION_MSG);

			Bundle data = new Bundle();
			data.putParcelable(TrackingServiceConstants.LOCATION, wayPointModel);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	};

    private static final int BUMP_MSG = 1;
    private static final int LOCATION_MSG = 2;

    private Handler mHandler = new Handler() {

    	@Override
	    public void handleMessage(Message msg) {
		    switch (msg.what) {
			    case BUMP_MSG:
				    mCallbackText.setText("Received from service: " + msg.arg1);
				    break;
			    case LOCATION_MSG:
			    	WayPointModel wayPointModel = (WayPointModel) msg.getData().getParcelable(TrackingServiceConstants.LOCATION);
			    	mTrackData.setTrackId(wayPointModel.getTrackId());
			    	mTrackData.setSegmentId(wayPointModel.getSegmentId());
			    	mTrackData.setWayPointId(wayPointModel.getWayPointId());
			    	mTrackData.setGeopoint(wayPointModel.getGeopoint());
			    	GeoPoint lastGeoPoint = wayPointModel.getGeopoint();
				    latituteField.setText(String.valueOf(lastGeoPoint.getLatitude()));
				    longitudeField.setText(String.valueOf(lastGeoPoint.getLongitude()));
				    distanceField.setText(StringUtil.getDistanceString(wayPointModel.getTotalDistance()));
				    speedField.setText(StringUtil.getSpeedString(lastGeoPoint.getSpeed()));
				    break;
			    default:
			    	super.handleMessage(msg);
		    }
	    }
    };

//    private GeoPoint geLastGeoPoint(WayPointModel wayPointModel) {
//
//    	int size = wayPointModel.getGeopoints().size();
//    	if (size > 0) {
//    		return wayPointModel.getGeopoints().get(size -1);
//    	}
//    	return null;
//    }
}