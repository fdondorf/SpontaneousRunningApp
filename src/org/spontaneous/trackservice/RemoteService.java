/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spontaneous.trackservice;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.spontaneous.R;
import org.spontaneous.activities.CurrentActivityActivity;
import org.spontaneous.activities.model.GeoPoint;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.db.GPSTracking.SegmentsColumns;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.TracksColumns;
import org.spontaneous.db.GPSTracking.Waypoints;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
// Need the following import to get access to the app resources, since this
// class is in a sub-package.

//import com.google.android.gms.internal.nl;

/**
 * This is an example of implementing an application service that runs in a different process than the application.
 * Because it can be in another process, we must use IPC to interact with it. The {@link Controller} and {@link Binding}
 * classes show how to interact with the service.
 *
 * <p>
 * Note that most applications <strong>do not</strong> need to deal with the complexity shown here. If your application
 * simply has a service running in its own process, the {@link LocalService} sample shows a much simpler way to interact
 * with it.
 */
public class RemoteService extends Service implements LocationListener {

  private static final Boolean DEBUG = false;

  private static final boolean VERBOSE = false;

  private static final String TAG = "RemoteService";

  private long mTrackId = -1;

  private long mSegmentId = -1;

  private long mWaypointId = -1;

  private static final float FINE_DISTANCE = 5F;

  private static final long FINE_INTERVAL = 1000l;

  private static final float FINE_ACCURACY = 20f;

  private static final float NORMAL_DISTANCE = 10F;

  private static final long NORMAL_INTERVAL = 15000l;

  private static final float NORMAL_ACCURACY = 30f;

  private static final float COARSE_DISTANCE = 25F;

  private static final long COARSE_INTERVAL = 30000l;

  private static final float COARSE_ACCURACY = 75f;

  private static final float GLOBAL_DISTANCE = 500F;

  private static final long GLOBAL_INTERVAL = 300000l;

  private static final float GLOBAL_ACCURACY = 1000f;

  /**
   * <code>MAX_REASONABLE_SPEED</code> is about 324 kilometer per hour or 201 mile per hour.
   */
  private static final int MAX_REASONABLE_SPEED = 90;

  /**
   * <code>MAX_REASONABLE_ALTITUDECHANGE</code> between the last few waypoints and a new one the difference should be
   * less then 200 meter.
   */
  private static final int MAX_REASONABLE_ALTITUDECHANGE = 200;

  private LocationManager mLocationManager;

  private NotificationManager mNoticationManager;

  private PowerManager.WakeLock mWakeLock;

  private int mLoggingState = TrackingServiceConstants.STOPPED;

  private int mPrecision;

  private boolean mShowingGpsDisabled;

  private WayPointModel mWayPointModel;

  private boolean mStartNextSegment;

  private String mSources;

  private Location mPreviousLocation;

  private float mDistance;

  private float mTotalDistance;

  private Notification mNotification;

  private Vector<Location> mWeakLocations;

  private Queue<Double> mAltitudes;

  private long mLastTimeBroadcast;

  /**
   * If speeds should be checked to sane values
   */
  private boolean mSpeedSanityCheck;

  /**
   * Time thread to runs tasks that check whether the GPS listener has received enough to consider the GPS system alive.
   */
  private Timer mHeartbeatTimer;

  /**
   * <code>mAcceptableAccuracy</code> indicates the maximum acceptable accuracy of a waypoint in meters.
   */
  private float mMaxAcceptableAccuracy = 20;

  private int mSatellites = 0;

  /**
   * Number of milliseconds that a functioning GPS system needs to provide a location. Calculated to be either 120
   * seconds or 4 times the requested period, whichever is larger.
   */
  private long mCheckPeriod;

  /**
   * Task that will be run periodically during active logging to verify that the logging really happens and that the GPS
   * hasn't silently stopped.
   */
  private TimerTask mHeartbeat = null;

  /**
   * Task to determine if the GPS is alive
   */
  class Heartbeat extends TimerTask {

    private String mProvider;

    public Heartbeat(String provider) {

      this.mProvider = provider;
    }

    @Override
    public void run() {

      if (isLogging()) {
        // Collect the last location from the last logged location or a more recent from the last weak location
        Location checkLocation = RemoteService.this.mPreviousLocation;
        synchronized (RemoteService.this.mWeakLocations) {
          if (!RemoteService.this.mWeakLocations.isEmpty()) {
            if (checkLocation == null) {
              checkLocation = RemoteService.this.mWeakLocations.lastElement();
            } else {
              Location weakLocation = RemoteService.this.mWeakLocations.lastElement();
              checkLocation = weakLocation.getTime() > checkLocation.getTime() ? weakLocation : checkLocation;
            }
          }
        }
        // Is the last known GPS location something nearby we are not told?
        Location managerLocation = RemoteService.this.mLocationManager.getLastKnownLocation(this.mProvider);
        if (managerLocation != null && checkLocation != null) {
          if (checkLocation.distanceTo(managerLocation) < 2 * RemoteService.this.mMaxAcceptableAccuracy) {
            checkLocation = managerLocation.getTime() > checkLocation.getTime() ? managerLocation : checkLocation;
          }
        }

        if (checkLocation == null || checkLocation.getTime() + RemoteService.this.mCheckPeriod < new Date().getTime()) {
          Log.w(TAG, "GPS system failed to produce a location during logging: " + checkLocation);
          RemoteService.this.mLoggingState = TrackingServiceConstants.PAUSED;
          resumeLogging();

          // if (mStatusMonitor) {
          // soundGpsSignalAlarm();
          // }

        }
      }
    }
  };

  /**
   * This is a list of callbacks that have been registered with the service. Note that this is package scoped (instead
   * of private) so that it can be accessed more efficiently from inner classes.
   */
  private final RemoteCallbackList<IRemoteServiceCallback> mCallbacks =
      new RemoteCallbackList<IRemoteServiceCallback>();

  private int mValue = 0;

  // private NotificationManager mNM;

  @Override
  public void onCreate() {

    // mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    // Get start location if available

    // Display a notification about us starting.
    showNotification();

    this.mHeartbeatTimer = new Timer("heartbeat", true);

    this.mWeakLocations = new Vector<Location>(3);
    this.mAltitudes = new LinkedList<Double>();
    this.mLoggingState = TrackingServiceConstants.STOPPED;
    this.mStartNextSegment = false;
    this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    this.mNoticationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // stopNotification();

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    this.mSpeedSanityCheck = sharedPreferences.getBoolean(TrackingServiceConstants.SPEEDSANITYCHECK, true);
    // mStreamBroadcast = sharedPreferences.getBoolean(Constants.BROADCAST_STREAM, false);
    // boolean startImmidiatly = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.LOGATSTARTUP,
    // false);

    // While this service is running, it will continually increment a
    // number. Send the first message that is used to perform the
    // increment.
    this.mHandler.sendEmptyMessage(REPORT_MSG);
  }

  @Override
  public void onDestroy() {

    // Cancel the persistent notification.
    this.mNoticationManager.cancel(0);
    // Tell the user we stopped.
    Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    // Unregister all callbacks.
    this.mCallbacks.kill();
    // Remove the next pending message to increment the counter, stopping
    // the increment loop.
    this.mHandler.removeMessages(REPORT_MSG);

    updateWakeLock();

    stopListening();

  }

  // BEGIN_INCLUDE(exposing_a_service)
  @Override
  public IBinder onBind(Intent intent) {

    // Select the interface to return. If your service only implements
    // a single interface, you can just return it here without checking
    // the Intent.
    try {
    	Log.i(TAG, IRemoteService.class.getName().toString());
    	Log.i(TAG, intent.getComponent().getClassName());
		if (Class.forName(RemoteService.class.getName()).toString().equals(intent.getComponent().getClassName())) {
		  return this.mBinder;
		}
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return this.mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {

    stopListening();
    return super.onUnbind(intent);
  }

  /**
   * The IRemoteInterface is defined through IDL
   */
  private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {

    @Override
    public void registerCallback(IRemoteServiceCallback cb) {

      if (cb != null)
        RemoteService.this.mCallbacks.register(cb);
    }

    @Override
    public void unregisterCallback(IRemoteServiceCallback cb) {

      if (cb != null)
        RemoteService.this.mCallbacks.unregister(cb);
    }

    @Override
    public int loggingState() throws RemoteException {

      return RemoteService.this.mLoggingState;
    }

    @Override
    public long startLogging(Location startLocation) throws RemoteException {

      RemoteService.this.startLogging(startLocation);
      return RemoteService.this.mTrackId;
    }

    @Override
    public void pauseLogging() throws RemoteException {

      RemoteService.this.pauseLogging();
    }

    @Override
    public long resumeLogging() throws RemoteException {

      RemoteService.this.resumeLogging();
      return RemoteService.this.mSegmentId;
    }

    @Override
    public void stopLogging() throws RemoteException {

      RemoteService.this.mLoggingState = TrackingServiceConstants.STOPPED;
      stopListening();
      updateWakeLock();
      updateSegment();
      updateTrack();
    }
  };

  // END_INCLUDE(exposing_a_service)
  @Override
  public void onTaskRemoved(Intent rootIntent) {

    Toast.makeText(this, "Task removed: " + rootIntent, Toast.LENGTH_LONG).show();
  }

  private static final int REPORT_MSG = 1;

  private static final int LOCATION_MSG = 2;

  /**
   * Our Handler used to execute operations on the main thread. This is used to schedule increments of our value.
   */
  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {

      switch (msg.what) {
      // It is time to bump the value!
      case REPORT_MSG: {
        // Up it goes.
        int value = ++RemoteService.this.mValue;
        // Broadcast to all clients the new value.
        final int N = RemoteService.this.mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
          try {
            RemoteService.this.mCallbacks.getBroadcastItem(i).valueChanged(value);
          } catch (RemoteException e) {
            // The RemoteCallbackList will take care of removing
            // the dead object for us.
          }
        }
        RemoteService.this.mCallbacks.finishBroadcast();
        // Repeat every 1 second. LOCATION_MSG
        // sendMessageDelayed(obtainMessage(REPORT_MSG), 1*1000);
      }
      case LOCATION_MSG: {
        // Broadcast to all clients the new location.
        final int N = RemoteService.this.mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
          try {
            Location location = (Location) msg.getData().getParcelable(TrackingServiceConstants.EXTRA_LOCATION);

            GeoPoint geoPoint = new GeoPoint();
            geoPoint.setLatitude(location.getLatitude());
            geoPoint.setLongitude(location.getLongitude());
            geoPoint.setDistance(RemoteService.this.mDistance);
            geoPoint.setSpeed(location.getSpeed());
            RemoteService.this.mWayPointModel.setTotalDistance(RemoteService.this.mTotalDistance);
            RemoteService.this.mWayPointModel.setTrackId(Long.valueOf(RemoteService.this.mTrackId));
            RemoteService.this.mWayPointModel.setSegmentId(Long.valueOf(RemoteService.this.mSegmentId));
            RemoteService.this.mWayPointModel.setWayPointId(Long.valueOf(RemoteService.this.mWaypointId));
            RemoteService.this.mWayPointModel.setGeopoint(geoPoint);
            RemoteService.this.mCallbacks.getBroadcastItem(i).locationChanged(RemoteService.this.mWayPointModel);
          } catch (RemoteException e) {
            // The RemoteCallbackList will take care of removing
            // the dead object for us.
          }
        }
        RemoteService.this.mCallbacks.finishBroadcast();
      }
        break;
      default:
        super.handleMessage(msg);
      }
    }
  };

  /**
   * Show a notification while this service is running.
   */
  private void showNotification() {

    // In this sample, we'll use the same text for the ticker and the expanded notification
    CharSequence text = getText(R.string.remote_service_label);

    // define sound URI, the sound to be played when there's a notification
    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    // Set the icon, scrolling text and timestamp
    // Notification notification = new Notification(R.drawable.stat_sample, text,
    // System.currentTimeMillis());

    // The PendingIntent to launch our activity if the user selects this notification

    Intent resumeIntent = new Intent(this, CurrentActivityActivity.class);
    resumeIntent.putExtra(TrackingServiceConstants.TRACK_ID, this.mTrackId);
    resumeIntent.putExtra(TrackingServiceConstants.SEGMENT_ID, this.mSegmentId);

    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    // this is it, we'll build the notification!
    // in the addAction method, if you don't want any icon, just set the first param to 0
    Notification mNotification =
        new NotificationCompat.Builder(this).setContentTitle(text)
            .setContentText(getText(R.string.remote_service_started)).setSmallIcon(R.drawable.ic_process_launcher)
            .setContentIntent(contentIntent).setSound(soundUri)
            .addAction(R.drawable.ic_process_launcher, "View", contentIntent)
            // .addAction(0, "Remind", contentIntent)
            .build();

    this.mNoticationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    // If you want to hide the notification after it was selected, do the code below
    // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

    // mNoticationManager.notify(0, mNotification);

    // Notification notification = new Notification(R.drawable.ic_process_launcher, text,
    // System.currentTimeMillis());
    //
    // long [] vibrate = {0,100,200,300};
    // notification.vibrate = vibrate;
    //
    // // Set the info for the views that show in the notification panel.
    // notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
    // text, contentIntent);
    // Send the notification.
    // We use a string id because it is a unique number. We use it later to cancel.
    this.mNoticationManager.notify(0, mNotification);
  }

  @Override
  public void onLocationChanged(Location location) {

    if (VERBOSE) {
      Log.v(TAG, "onLocationChanged( Location " + location + " )");
    }

    // Might be claiming GPS disabled but when we were paused this changed and this location proves so
    if (this.mShowingGpsDisabled) {
      notifyOnEnabledProviderNotification(R.string.service_gpsenabled);
    }

    if (isLogging()) {
      Location filteredLocation = locationFilter(location);
      if (filteredLocation != null) {
        if (this.mStartNextSegment) {
          this.mStartNextSegment = false;
          startNewSegment();
          // Obey the start segment if the previous location is unknown or far away
          // if (mPreviousLocation == null || filteredLocation.distanceTo(mPreviousLocation) > 4 *
          // mMaxAcceptableAccuracy) {
          // startNewSegment();
          // }
        } else if (this.mPreviousLocation != null) {
          this.mDistance = this.mPreviousLocation.distanceTo(filteredLocation);
          this.mTotalDistance += this.mDistance;
        }

        storeLocation(filteredLocation);
        updateTrack();
        broadcastLocation(filteredLocation);
        this.mPreviousLocation = filteredLocation;
      }
    }

  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {

    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderEnabled(String provider) {

    this.mStartNextSegment = true;
  }

  @Override
  public void onProviderDisabled(String provider) {

    // TODO Auto-generated method stub

  }

  public synchronized void startLogging(Location startLocation) {

    if (DEBUG) {
      Log.d(TAG, "startLogging()");
    }

    if (this.mLoggingState == TrackingServiceConstants.STOPPED) {
      startNewTrack(startLocation);
      // sendRequestLocationUpdatesMessage();
      // sendRequestStatusUpdateMessage();
      this.mLoggingState = TrackingServiceConstants.LOGGING;
      updateWakeLock();
      // startNotification();
      // crashProtectState();
      // broadCastLoggingState();

      long intervaltime = 0;
      float distance = 0;
      this.mMaxAcceptableAccuracy = COARSE_ACCURACY;
      intervaltime = FINE_INTERVAL;
      distance = FINE_DISTANCE;
      startListening(LocationManager.GPS_PROVIDER, intervaltime, distance);
    }
  }

  public synchronized void pauseLogging() {

    this.mLoggingState = TrackingServiceConstants.PAUSED;
    updateTrack();
    updateSegment();
  }

  public synchronized void resumeLogging() {

    if (DEBUG) {
      Log.d(TAG, "resumeLogging()");
    }

    if (this.mLoggingState == TrackingServiceConstants.PAUSED) {
      if (this.mPrecision != TrackingServiceConstants.LOGGING_GLOBAL) {
        this.mStartNextSegment = true;
      }
      // sendRequestLocationUpdatesMessage();
      // sendRequestStatusUpdateMessage();

      this.mLoggingState = TrackingServiceConstants.LOGGING;
      updateWakeLock();
      // updateNotification();
      // crashProtectState();
      // broadCastLoggingState();
    }
  }

  private void startListening(String provider, long intervaltime, float distance) {

    this.mLocationManager.removeUpdates(this);
    this.mLocationManager.requestLocationUpdates(provider, intervaltime, distance, this);
    this.mWayPointModel = new WayPointModel();
    this.mWayPointModel.setStartTime(SystemClock.currentThreadTimeMillis());

    this.mCheckPeriod = Math.max(12 * intervaltime, 120 * 1000);
    if (this.mHeartbeat != null) {
      this.mHeartbeat.cancel();
      this.mHeartbeat = null;
    }
    this.mHeartbeat = new Heartbeat(provider);
    this.mHeartbeatTimer.schedule(this.mHeartbeat, this.mCheckPeriod, this.mCheckPeriod);
  }

  private void stopListening() {

    if (this.mHeartbeat != null) {
      this.mHeartbeat.cancel();
      this.mHeartbeat = null;
    }
    this.mLocationManager.removeUpdates(this);
  }

  /**
   * (non-Javadoc)
   *
   * @see nl.sogeti.android.gpstracker.IGPSLoggerService#getLoggingState()
   */
  protected boolean isLogging() {

    return this.mLoggingState == TrackingServiceConstants.LOGGING;
  }

  /**
   * Some GPS waypoints received are of to low a quality for tracking use. Here we filter those out.
   *
   * @param proposedLocation
   * @return either the (cleaned) original or null when unacceptable
   */
  // TODO: Diese Methode auslagern und auch in StartFragment verwenden
  public Location locationFilter(Location proposedLocation) {

    // Do no include log wrong 0.0 lat 0.0 long, skip to next value in while-loop
    if (proposedLocation != null && (proposedLocation.getLatitude() == 0.0d || proposedLocation.getLongitude() == 0.0d)) {
      Log.w(TAG, "A wrong location was received, 0.0 latitude and 0.0 longitude... ");
      proposedLocation = null;
    }

    // Do not log a waypoint which is more inaccurate then is configured to be acceptable
    if (proposedLocation != null && proposedLocation.getAccuracy() > this.mMaxAcceptableAccuracy) {
      Log.w(TAG, String.format("A weak location was received, lots of inaccuracy... (%f is more then max %f)",
          proposedLocation.getAccuracy(), this.mMaxAcceptableAccuracy));
      proposedLocation = addBadLocation(proposedLocation);
    }

    // Do not log a waypoint which might be on any side of the previous waypoint
    if (proposedLocation != null && this.mPreviousLocation != null
        && proposedLocation.getAccuracy() > this.mPreviousLocation.distanceTo(proposedLocation)) {
      Log.w(TAG, String.format(
          "A weak location was received, not quite clear from the previous waypoint... (%f more then max %f)",
          proposedLocation.getAccuracy(), this.mPreviousLocation.distanceTo(proposedLocation)));
      proposedLocation = addBadLocation(proposedLocation);
    }

    // Speed checks, check if the proposed location could be reached from the previous one in sane speed
    // Common to jump on network logging and sometimes jumps on Samsung Galaxy S type of devices
    if (this.mSpeedSanityCheck && proposedLocation != null && this.mPreviousLocation != null) {
      // To avoid near instant teleportation on network location or glitches cause continent hopping
      float meters = proposedLocation.distanceTo(this.mPreviousLocation);
      long seconds = (proposedLocation.getTime() - this.mPreviousLocation.getTime()) / 1000L;
      float speed = meters / seconds;
      if (speed > MAX_REASONABLE_SPEED) {
        Log.w(TAG, "A strange location was received, a really high speed of " + speed + " m/s, prob wrong...");
        proposedLocation = addBadLocation(proposedLocation);
        // Might be a messed up Samsung Galaxy S GPS, reset the logging
        if (speed > 2 * MAX_REASONABLE_SPEED && this.mPrecision != TrackingServiceConstants.LOGGING_GLOBAL) {
          Log.w(TAG, "A strange location was received on GPS, reset the GPS listeners");
          stopListening();
          // mLocationManager.removeGpsStatusListener(mStatusListener);
          this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
          // sendRequestStatusUpdateMessage();
          // sendRequestLocationUpdatesMessage();
        }
      }
    }

    // Remove speed if not sane
    if (this.mSpeedSanityCheck && proposedLocation != null && proposedLocation.getSpeed() > MAX_REASONABLE_SPEED) {
      Log.w(TAG, "A strange speed, a really high speed, prob wrong...");
      proposedLocation.removeSpeed();
    }

    // Remove altitude if not sane
    if (this.mSpeedSanityCheck && proposedLocation != null && proposedLocation.hasAltitude()) {
      if (!addSaneAltitude(proposedLocation.getAltitude())) {
        Log.w(TAG, "A strange altitude, a really big difference, prob wrong...");
        proposedLocation.removeAltitude();
      }
    }
    // Older bad locations will not be needed
    if (proposedLocation != null) {
      this.mWeakLocations.clear();
    }
    return proposedLocation;
  }

  /**
   * Store a bad location, when to many bad locations are stored the the storage is cleared and the least bad one is
   * returned
   *
   * @param location bad location
   * @return null when the bad location is stored or the least bad one if the storage was full
   */
  private Location addBadLocation(Location location) {

    this.mWeakLocations.add(location);
    if (this.mWeakLocations.size() < 3) {
      location = null;
    } else {
      Location best = this.mWeakLocations.lastElement();
      for (Location whimp : this.mWeakLocations) {
        if (whimp.hasAccuracy() && best.hasAccuracy() && whimp.getAccuracy() < best.getAccuracy()) {
          best = whimp;
        } else {
          if (whimp.hasAccuracy() && !best.hasAccuracy()) {
            best = whimp;
          }
        }
      }
      synchronized (this.mWeakLocations) {
        this.mWeakLocations.clear();
      }
      location = best;
    }
    return location;
  }

  /**
   * Builds a bit of knowledge about altitudes to expect and return if the added value is deemed sane.
   *
   * @param altitude
   * @return whether the altitude is considered sane
   */
  private boolean addSaneAltitude(double altitude) {

    boolean sane = true;
    double avg = 0;
    int elements = 0;
    // Even insane altitude shifts increases alter perception
    this.mAltitudes.add(altitude);
    if (this.mAltitudes.size() > 3) {
      this.mAltitudes.poll();
    }
    for (Double alt : this.mAltitudes) {
      avg += alt;
      elements++;
    }
    avg = avg / elements;
    sane = Math.abs(altitude - avg) < MAX_REASONABLE_ALTITUDECHANGE;

    return sane;
  }

  /**
   * Consult broadcast options and execute broadcast if necessary
   *
   * @param location
   */
  public void broadcastLocation(Location location) {

    final long nowTime = location.getTime();
    if (this.mLastTimeBroadcast == 0) {
      this.mLastTimeBroadcast = nowTime;
    }
    long passedTime = (nowTime - this.mLastTimeBroadcast);

    Message msg = this.mHandler.obtainMessage(REPORT_MSG);
    Bundle data = new Bundle();
    data.putLong(TrackingServiceConstants.TRACK_ID, this.mTrackId);
    data.putLong(TrackingServiceConstants.SEGMENT_ID, this.mSegmentId);
    data.putLong(TrackingServiceConstants.WAYPOINT_ID, this.mWaypointId);
    data.putLong(TrackingServiceConstants.EXTRA_TIME, passedTime);
    data.putParcelable(TrackingServiceConstants.EXTRA_LOCATION, location);
    data.putFloat(TrackingServiceConstants.EXTRA_DISTANCE, this.mDistance);
    data.putFloat(TrackingServiceConstants.TOTAL_DISTANCE, this.mTotalDistance);
    data.putFloat(TrackingServiceConstants.EXTRA_SPEED, location.getSpeed());
    msg.setData(data);
    this.mHandler.sendMessage(msg);

  }

  private void notifyOnEnabledProviderNotification(int resId) {

    this.mNoticationManager.cancel(R.string.service_connectiondisabled);
    this.mShowingGpsDisabled = false;
    CharSequence text = this.getString(resId);
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
    toast.show();
  }

  /**
   * Use the ContentResolver mechanism to store a received location
   *
   * @param location
   */
  private void storeLocation(Location location) {

    if (!isLogging()) {
      Log.e(TAG, String.format("Not logging but storing location %s, prepare to fail", location.toString()));
    }
    ContentValues args = new ContentValues();

    args.put(Waypoints.LATITUDE, Double.valueOf(location.getLatitude()));
    args.put(Waypoints.LONGITUDE, Double.valueOf(location.getLongitude()));
    args.put(Waypoints.SPEED, Float.valueOf(location.getSpeed()));
    args.put(Waypoints.TIME, Long.valueOf(System.currentTimeMillis()));
    args.put(Waypoints.DISTANCE, this.mDistance);

    if (location.hasAccuracy()) {
      args.put(Waypoints.ACCURACY, Float.valueOf(location.getAccuracy()));
    }
    if (location.hasAltitude()) {
      args.put(Waypoints.ALTITUDE, Double.valueOf(location.getAltitude()));

    }
    if (location.hasBearing()) {
      args.put(Waypoints.BEARING, Float.valueOf(location.getBearing()));
    }

    Uri waypointInsertUri =
        Uri.withAppendedPath(Tracks.CONTENT_URI, this.mTrackId + "/segments/" + this.mSegmentId + "/waypoints");
    Uri inserted = getContentResolver().insert(waypointInsertUri, args);
    this.mWaypointId = Long.parseLong(inserted.getLastPathSegment());
  }

  private TrackModel getTracDataById(Long trackId) {

    String[] mTrackColumns =
        { Tracks._ID, Tracks.NAME, Tracks.TOTAL_DISTANCE, Tracks.TOTAL_DURATION, Tracks.CREATION_TIME };

    // Read track
    Uri trackReadUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(trackId));
    Cursor mCursor = getContentResolver().query(trackReadUri, mTrackColumns, null, null, Tracks.CREATION_TIME);
    if (mCursor != null && mCursor.moveToNext()) {
      TrackModel trackModel =
          new TrackModel(Long.valueOf(mCursor.getString(mCursor.getColumnIndex(Tracks._ID))), mCursor.getString(mCursor
              .getColumnIndex(Tracks.NAME)), Float.valueOf(mCursor.getString(mCursor
              .getColumnIndex(Tracks.TOTAL_DISTANCE))), Long.valueOf(mCursor.getString(mCursor
              .getColumnIndex(Tracks.TOTAL_DURATION))), Long.valueOf(mCursor.getString(mCursor
              .getColumnIndex(Tracks.CREATION_TIME))));
      return trackModel;
    }
    return null;
  }

  /**
   * Update Track Store the current total distance of the track. Store the current total duration of the track.
   */
  private void updateTrack() {

    // Get current total duration
    TrackModel trackModel = getTracDataById(this.mTrackId);

    ContentValues args = new ContentValues();
    args.put(TracksColumns.TOTAL_DISTANCE, this.mTotalDistance);
    args.put(TracksColumns.TOTAL_DURATION, System.currentTimeMillis() - Long.valueOf(trackModel.getCreationDate()));

    Uri trackUpdateUri = Uri.withAppendedPath(Tracks.CONTENT_URI, String.valueOf(this.mTrackId));
    getContentResolver().update(trackUpdateUri, args, null, null);
  }

  /**
   * Trigged by events that start a new track
   */
  private void startNewTrack(Location startLocation) {

    this.mTotalDistance = 0;
    this.mDistance = 0;
    Uri newTrack = getContentResolver().insert(Tracks.CONTENT_URI, new ContentValues(0));
    this.mTrackId = Long.valueOf(newTrack.getLastPathSegment()).longValue();

    startNewSegment();
    if (startLocation != null) {
      storeLocation(startLocation);
    }
  }

  /**
   * Trigged by events that start a new segment
   */
  private void startNewSegment() {

    this.mDistance = 0;
    this.mPreviousLocation = null;

    Uri newSegment =
        getContentResolver().insert(Uri.withAppendedPath(Tracks.CONTENT_URI, this.mTrackId + "/segments"),
            new ContentValues(0));
    this.mSegmentId = Long.valueOf(newSegment.getLastPathSegment()).longValue();
    // crashProtectState();
  }

  private void updateSegment() {

    if (this.mSegmentId > 0L) {
      ContentValues args = new ContentValues();
      args.put(SegmentsColumns.END_TIME, System.currentTimeMillis());

      Uri segmentUpdateUri = Uri.withAppendedPath(Tracks.CONTENT_URI, this.mTrackId + "/segments/" + this.mSegmentId);
      getContentResolver().update(segmentUpdateUri, args, null, null);
    }
  }

  private void updateWakeLock() {

    if (this.mLoggingState == TrackingServiceConstants.LOGGING) {
      // PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      if (this.mWakeLock != null) {
        this.mWakeLock.release();
        this.mWakeLock = null;
      }
      this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
      this.mWakeLock.acquire();
    } else {
      if (this.mWakeLock != null) {
        this.mWakeLock.release();
        this.mWakeLock = null;
      }
    }
  }
}