package org.spontaneous.db;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.spontaneous.db.GPSTracking.Media;
import org.spontaneous.db.GPSTracking.MetaData;
import org.spontaneous.db.GPSTracking.Segments;
import org.spontaneous.db.GPSTracking.SegmentsColumns;
import org.spontaneous.db.GPSTracking.Tracks;
import org.spontaneous.db.GPSTracking.Waypoints;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.net.Uri;
import android.provider.LiveFolders;
import android.util.Log;

public class GPSTrackingProvider extends ContentProvider {

   private static final String TAG = "OGT.GPStrackingProvider";

   /* Action types as numbers for using the UriMatcher */
   private static final int TRACKS            = 1;
   private static final int TRACK_ID          = 2;   
   private static final int TRACK_MEDIA       = 3;
   private static final int TRACK_WAYPOINTS   = 4;
   private static final int SEGMENTS          = 5;
   private static final int SEGMENT_ID        = 6;
   private static final int SEGMENT_MEDIA     = 7;
   private static final int WAYPOINTS         = 8;
   private static final int WAYPOINT_ID       = 9;
   private static final int WAYPOINT_MEDIA    = 10;
   private static final int SEARCH_SUGGEST_ID = 11;
   private static final int LIVE_FOLDERS      = 12;
   private static final int MEDIA             = 13;
   private static final int MEDIA_ID          = 14;   
   private static final int TRACK_METADATA    = 15;
   private static final int SEGMENT_METADATA  = 16;
   private static final int WAYPOINT_METADATA = 17;
   private static final int METADATA          = 18;
   private static final int METADATA_ID       = 19;
   private static final String[] SUGGEST_PROJECTION = 
      new String[] 
        { 
            Tracks._ID, 
            Tracks.NAME+" AS "+ SearchManager.SUGGEST_COLUMN_TEXT_1,
            "datetime("+Tracks.CREATION_TIME+"/1000, 'unixepoch') as "+SearchManager.SUGGEST_COLUMN_TEXT_2,
            Tracks._ID+" AS "+ SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
            
        };
   private static final String[] LIVE_PROJECTION = 
      new String[] 
        {
            Tracks._ID+" AS "+ LiveFolders._ID,
            Tracks.NAME+" AS "+ LiveFolders.NAME,
            "datetime("+Tracks.CREATION_TIME+"/1000, 'unixepoch') as "+LiveFolders.DESCRIPTION
        };

   private static UriMatcher sURIMatcher = new UriMatcher( UriMatcher.NO_MATCH );

   /**
    * Although it is documented that in addURI(null, path, 0) "path" should be an absolute path this does not seem to work. A relative path gets the jobs done and matches an absolute path.
    */
   static
   {
      GPSTrackingProvider.sURIMatcher = new UriMatcher( UriMatcher.NO_MATCH );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks", GPSTrackingProvider.TRACKS );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#", GPSTrackingProvider.TRACK_ID );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/media", GPSTrackingProvider.TRACK_MEDIA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/metadata", GPSTrackingProvider.TRACK_METADATA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/waypoints", GPSTrackingProvider.TRACK_WAYPOINTS );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments", GPSTrackingProvider.SEGMENTS );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments/#", GPSTrackingProvider.SEGMENT_ID );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments/#/media", GPSTrackingProvider.SEGMENT_MEDIA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments/#/metadata", GPSTrackingProvider.SEGMENT_METADATA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments/#/waypoints", GPSTrackingProvider.WAYPOINTS );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments/#/waypoints/#", GPSTrackingProvider.WAYPOINT_ID );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments/#/waypoints/#/media", GPSTrackingProvider.WAYPOINT_MEDIA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "tracks/#/segments/#/waypoints/#/metadata", GPSTrackingProvider.WAYPOINT_METADATA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "media", GPSTrackingProvider.MEDIA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "media/#", GPSTrackingProvider.MEDIA_ID );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "metadata", GPSTrackingProvider.METADATA );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "metadata/#", GPSTrackingProvider.METADATA_ID );
      
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "live_folders/tracks", GPSTrackingProvider.LIVE_FOLDERS );
      GPSTrackingProvider.sURIMatcher.addURI( GPSTracking.AUTHORITY, "search_suggest_query", GPSTrackingProvider.SEARCH_SUGGEST_ID );

   }

   private DatabaseHelper mDbHelper;

   /**
    * (non-Javadoc)
    * @see android.content.ContentProvider#onCreate()
    */
   @Override
   public boolean onCreate() {
   
      if (this.mDbHelper == null) {
         this.mDbHelper = new DatabaseHelper( getContext() );
      }
      return true;
   }

   /**
    * (non-Javadoc)
    * @see android.content.ContentProvider#getType(android.net.Uri)
    */
   @Override
   public String getType( Uri uri ) {
      int match = GPSTrackingProvider.sURIMatcher.match( uri );
      String mime = null;
      switch (match)
      {
         case TRACKS:
            mime = Tracks.CONTENT_TYPE;
            break;
         case TRACK_ID:
            mime = Tracks.CONTENT_ITEM_TYPE;
            break;
         case SEGMENTS:
            mime = Segments.CONTENT_TYPE;
            break;
         case SEGMENT_ID:
            mime = Segments.CONTENT_ITEM_TYPE;
            break;
         case WAYPOINTS:
            mime = Waypoints.CONTENT_TYPE;
            break;
         case WAYPOINT_ID:
            mime = Waypoints.CONTENT_ITEM_TYPE;
            break;
         case MEDIA_ID:
         case TRACK_MEDIA:
         case SEGMENT_MEDIA:
         case WAYPOINT_MEDIA:
            mime = Media.CONTENT_ITEM_TYPE;
            break;
         case METADATA_ID:
         case TRACK_METADATA:
         case SEGMENT_METADATA:
         case WAYPOINT_METADATA:
            mime = MetaData.CONTENT_ITEM_TYPE;
            break;
         case UriMatcher.NO_MATCH:
         default:
            Log.w(TAG, "There is not MIME type defined for URI "+uri);
            break;
      }
      return mime;
   }

   /**
       * (non-Javadoc)
       * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
       */
      @Override
      public Uri insert( Uri uri, ContentValues values ) {
    	  
         //Log.d( TAG, "insert on "+uri );
         Uri insertedUri = null;
         int match = GPSTrackingProvider.sURIMatcher.match( uri );
         List<String> pathSegments = null;
         long trackId = -1;
         long segmentId = -1;
         long waypointId = -1;
         long mediaId = -1;
         String key;
         String value;
         switch (match)
         {
            case WAYPOINTS:
               pathSegments     = uri.getPathSegments();
               trackId          = Long.parseLong( pathSegments.get( 1 ) );
               segmentId        = Long.parseLong( pathSegments.get( 3 ) );
               Location loc     = new Location( TAG );
               Double latitude  = values.getAsDouble( Waypoints.LATITUDE );
               Double longitude = values.getAsDouble( Waypoints.LONGITUDE );
               Long time        = values.getAsLong( Waypoints.TIME );
               Float speed      = values.getAsFloat( Waypoints.SPEED );
               Double distance 	= values.getAsDouble( Waypoints.DISTANCE );
               
               if( time == null )
               {
                  time = System.currentTimeMillis();
               }
               if( speed == null )
               {
                  speed  = 0f;
               }
               if( distance == null )
               {
                  distance  = 0D;
               }
               loc.setLatitude( latitude );
               loc.setLongitude( longitude );
               loc.setTime( time );
               loc.setSpeed( speed );
               
               if( values.containsKey( Waypoints.ACCURACY ) )
               {
                  loc.setAccuracy( values.getAsFloat( Waypoints.ACCURACY ) );
               }
               if( values.containsKey( Waypoints.ALTITUDE ) )
               {
                  loc.setAltitude( values.getAsDouble( Waypoints.ALTITUDE ) );
                  
               }
               if( values.containsKey( Waypoints.BEARING ) )
               {
                  loc.setBearing( values.getAsFloat( Waypoints.BEARING ) );
               }
               waypointId = this.mDbHelper.insertWaypoint( 
                     trackId, 
                     segmentId, 
                     loc,
                     distance);
   //            Log.d( TAG, "Have inserted to segment "+segmentId+" with waypoint "+waypointId );
               insertedUri = ContentUris.withAppendedId( uri, waypointId );
               break;
            case WAYPOINT_MEDIA:
               pathSegments    = uri.getPathSegments();
               trackId         = Long.parseLong( pathSegments.get( 1 ) );
               segmentId       = Long.parseLong( pathSegments.get( 3 ) );
               waypointId      = Long.parseLong( pathSegments.get( 5 ) );
               String mediaUri = values.getAsString( Media.URI );
               mediaId         = this.mDbHelper.insertMedia( trackId, segmentId, waypointId, mediaUri );
               insertedUri     = ContentUris.withAppendedId( Media.CONTENT_URI, mediaId );
               break;
            case SEGMENTS:
               pathSegments = uri.getPathSegments();
               trackId      = Integer.parseInt( pathSegments.get( 1 ) );
               segmentId    = this.mDbHelper.toNextSegment( trackId );
               insertedUri  = ContentUris.withAppendedId( uri, segmentId );
               break;
            case TRACKS:
               String name = ( values == null ) ? "" : values.getAsString( Tracks.NAME );
               trackId     = this.mDbHelper.toNextTrack( name );
               insertedUri = ContentUris.withAppendedId( uri, trackId );
               break;
            case TRACK_METADATA:
               pathSegments = uri.getPathSegments();
               trackId      = Long.parseLong( pathSegments.get( 1 ) );
               key          = values.getAsString( MetaData.KEY );
               value        = values.getAsString( MetaData.VALUE );
               mediaId      = this.mDbHelper.insertOrUpdateMetaData( trackId, -1L, -1L, key, value );
               insertedUri  = ContentUris.withAppendedId( MetaData.CONTENT_URI, mediaId );
               break;
            case SEGMENT_METADATA:
               pathSegments = uri.getPathSegments();
               trackId      = Long.parseLong( pathSegments.get( 1 ) );
               segmentId    = Long.parseLong( pathSegments.get( 3 ) );
               key          = values.getAsString( MetaData.KEY );
               value        = values.getAsString( MetaData.VALUE );
               mediaId      = this.mDbHelper.insertOrUpdateMetaData( trackId, segmentId, -1L, key, value );
               insertedUri  = ContentUris.withAppendedId( MetaData.CONTENT_URI, mediaId );
               break;
            case WAYPOINT_METADATA:
               pathSegments = uri.getPathSegments();
               trackId      = Long.parseLong( pathSegments.get( 1 ) );
               segmentId    = Long.parseLong( pathSegments.get( 3 ) );
               waypointId   = Long.parseLong( pathSegments.get( 5 ) );
               key          = values.getAsString( MetaData.KEY );
               value        = values.getAsString( MetaData.VALUE );
               mediaId      = this.mDbHelper.insertOrUpdateMetaData( trackId, segmentId, waypointId, key, value );
               insertedUri  = ContentUris.withAppendedId( MetaData.CONTENT_URI, mediaId );
               break;
            default:
               Log.e( GPSTrackingProvider.TAG, "Unable to match the insert URI: " + uri.toString() );
               insertedUri =  null;
               break;
         }
         return insertedUri;
      }

   /**
       * (non-Javadoc)
       * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
       */
      @Override
      public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder )
      {
   //      Log.d( TAG, "Query on Uri:"+uri ); 
        
         int match = GPSTrackingProvider.sURIMatcher.match( uri );
   
         String tableName = null;
         String innerSelection = "1";
         String[] innerSelectionArgs = new String[]{};
         String sortorder = sortOrder;
         List<String> pathSegments = uri.getPathSegments();
         switch (match)
         {
            case TRACKS:
               tableName = Tracks.TABLE;
               break;
            case TRACK_ID:
               tableName = Tracks.TABLE;
               innerSelection = Tracks._ID + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ) };
               break;
            case SEGMENTS:
               tableName = Segments.TABLE;
               innerSelection = Segments.TRACK + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ) };
               break;
            case SEGMENT_ID:
               tableName = Segments.TABLE;
               innerSelection = Segments.TRACK + " = ?  and " + Segments._ID   + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ), pathSegments.get( 3 ) };
               break;
            case WAYPOINTS:
               tableName = Waypoints.TABLE;
               innerSelection = Waypoints.SEGMENT + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 3 ) };
               break;
            case WAYPOINT_ID:
               tableName = Waypoints.TABLE;
               innerSelection = Waypoints.SEGMENT + " =  ?  and " + Waypoints._ID     + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 3 ),  pathSegments.get( 5 ) };
               break;
            case TRACK_WAYPOINTS:
               tableName = Waypoints.TABLE + " INNER JOIN " + Segments.TABLE + " ON "+ Segments.TABLE+"."+Segments._ID +"=="+ Waypoints.SEGMENT;
               innerSelection = Segments.TRACK + " = ? ";
               innerSelectionArgs = new String[]{  pathSegments.get( 1 ) };
               break;
            case GPSTrackingProvider.MEDIA:
               tableName = Media.TABLE;
               break;
            case GPSTrackingProvider.MEDIA_ID:
               tableName = Media.TABLE;
               innerSelection = Media._ID + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ) };
               break;
            case TRACK_MEDIA:
               tableName = Media.TABLE;
               innerSelection = Media.TRACK + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ) };
               break;
            case SEGMENT_MEDIA:
               tableName = Media.TABLE;
               innerSelection = Media.TRACK + " = ? and " + Media.SEGMENT + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ), pathSegments.get( 3 ) };
               break;
            case WAYPOINT_MEDIA:
               tableName = Media.TABLE;
               innerSelection = Media.TRACK  + " = ?  and " + Media.SEGMENT  + " = ? and " + Media.WAYPOINT + " = ? ";
               innerSelectionArgs = new String[]{  pathSegments.get( 1 ),pathSegments.get( 3 ), pathSegments.get( 5 )};
               break;
            case TRACK_METADATA:
               tableName = MetaData.TABLE;
               innerSelection = MetaData.TRACK  + " = ? and " + MetaData.SEGMENT  + " = ? and " + MetaData.WAYPOINT + " = ? ";
               innerSelectionArgs = new String[]{  pathSegments.get( 1 ), "-1", "-1" };
               break;
            case SEGMENT_METADATA:
               tableName = MetaData.TABLE;
               innerSelection = MetaData.TRACK  + " = ? and " + MetaData.SEGMENT  + " = ? and " + MetaData.WAYPOINT + " = ? ";
               innerSelectionArgs = new String[]{pathSegments.get( 1 ), pathSegments.get( 3 ),  "-1" };
               break;
            case WAYPOINT_METADATA:
               tableName = MetaData.TABLE;
               innerSelection = MetaData.TRACK  + " = ? and " + MetaData.SEGMENT  + " = ? and " + MetaData.WAYPOINT + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ), pathSegments.get( 3 ),  pathSegments.get( 5 ) };
               break;
            case GPSTrackingProvider.METADATA:
               tableName = MetaData.TABLE;
               break;
            case GPSTrackingProvider.METADATA_ID:
               tableName = MetaData.TABLE;
               innerSelection = MetaData._ID + " = ? ";
               innerSelectionArgs = new String[]{ pathSegments.get( 1 ) };
               break;
            case SEARCH_SUGGEST_ID:
               tableName = Tracks.TABLE;
               if( selectionArgs[0] == null || selectionArgs[0].equals( "" ) )
               {
                  selection = null;
                  selectionArgs = null;
                  sortorder = Tracks.CREATION_TIME+" desc";
               }
               else
               {
                  selectionArgs[0] = "%" +selectionArgs[0]+ "%";
               }
               projection = SUGGEST_PROJECTION;
               break;
            case LIVE_FOLDERS:
               tableName = Tracks.TABLE;
               projection = LIVE_PROJECTION;
               sortorder = Tracks.CREATION_TIME+" desc";
               break;
            default:
               Log.e( GPSTrackingProvider.TAG, "Unable to come to an action in the query uri: " + uri.toString() );
               return null;
         }
   
         // SQLiteQueryBuilder is a helper class that creates the
         // proper SQL syntax for us.
         SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
   
         // Set the table we're querying.
         qBuilder.setTables( tableName );
         
         if( selection == null )
         {
            selection = innerSelection;
         }
         else
         {
            selection = "( "+ innerSelection + " ) and " + selection;
         }
         LinkedList<String> allArgs = new LinkedList<String>();
         if( selectionArgs == null )
         {
            allArgs.addAll(Arrays.asList(innerSelectionArgs));
         }
         else
         {
            allArgs.addAll(Arrays.asList(innerSelectionArgs));
            allArgs.addAll(Arrays.asList(selectionArgs));
         }
         selectionArgs = allArgs.toArray(innerSelectionArgs);
         
         // Make the query.
         SQLiteDatabase mDb = this.mDbHelper.getWritableDatabase();
         Cursor c = qBuilder.query( mDb, projection, selection, selectionArgs, null, null, sortorder  );
         c.setNotificationUri( getContext().getContentResolver(), uri );
         return c;
      }

   /**
    * (non-Javadoc)
    * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
    */
   @Override
   public int update( Uri uri, ContentValues givenValues, String selection, String[] selectionArgs ) {
      int updates = -1 ;
      long trackId;
      long segmentId;
      long waypointId;
      long metaDataId;
      List<String> pathSegments;

      int match = GPSTrackingProvider.sURIMatcher.match( uri );
      String value;
      switch (match) {
         case TRACK_ID:
            trackId = new Long( uri.getLastPathSegment() ).longValue();
            String name = givenValues.getAsString( Tracks.NAME );
            String totalDistance = givenValues.getAsString(Tracks.TOTAL_DISTANCE);
            Long totalDuration = givenValues.getAsLong(Tracks.TOTAL_DURATION);
            updates = mDbHelper.updateTrack(trackId, name, totalDistance, totalDuration);   
            break;
         case SEGMENT_ID:
             segmentId = new Long( uri.getLastPathSegment() ).longValue();
             updates = mDbHelper.updateSegment(segmentId, givenValues.getAsLong(SegmentsColumns.END_TIME));   
             break;
         case TRACK_METADATA:
            pathSegments = uri.getPathSegments();
            trackId      = Long.parseLong( pathSegments.get( 1 ) );
            value = givenValues.getAsString( MetaData.VALUE );
            updates = mDbHelper.updateMetaData( trackId, -1L, -1L, -1L, selection, selectionArgs, value);
            break;
         case SEGMENT_METADATA:
            pathSegments = uri.getPathSegments();
            trackId      = Long.parseLong( pathSegments.get( 1 ) );
            segmentId    = Long.parseLong( pathSegments.get( 3 ) );
            value = givenValues.getAsString( MetaData.VALUE );
            updates = mDbHelper.updateMetaData( trackId, segmentId, -1L, -1L, selection, selectionArgs, value);
            break;
         case WAYPOINT_METADATA:
            pathSegments = uri.getPathSegments();
            trackId      = Long.parseLong( pathSegments.get( 1 ) );
            segmentId    = Long.parseLong( pathSegments.get( 3 ) );
            waypointId   = Long.parseLong( pathSegments.get( 5 ) );
            value = givenValues.getAsString( MetaData.VALUE );
            updates = mDbHelper.updateMetaData( trackId, segmentId, waypointId, -1L, selection, selectionArgs, value);
            break;
         case METADATA_ID:
            pathSegments = uri.getPathSegments();
            metaDataId   = Long.parseLong( pathSegments.get( 1 ) );
            value = givenValues.getAsString( MetaData.VALUE );
            updates = mDbHelper.updateMetaData( -1L, -1L, -1L, metaDataId, selection, selectionArgs, value);
            break;
         default:
            Log.e( GPSTrackingProvider.TAG, "Unable to come to an action in the query uri" + uri.toString() );
            return -1;
      }
      
      
      
      return updates;
   }


   /**
    * (non-Javadoc)
    * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
    */
   @Override
   public int delete( Uri uri, String selection, String[] selectionArgs )
   {
      int match = GPSTrackingProvider.sURIMatcher.match( uri );
      int affected = 0; 
      switch( match )
      {
         case GPSTrackingProvider.TRACK_ID:
            affected = this.mDbHelper.deleteTrack( new Long( uri.getLastPathSegment() ).longValue() );
            break;
         case GPSTrackingProvider.MEDIA_ID:
            affected = this.mDbHelper.deleteMedia( new Long( uri.getLastPathSegment() ).longValue() );
            break;
         case GPSTrackingProvider.METADATA_ID:
            affected = this.mDbHelper.deleteMetaData( new Long( uri.getLastPathSegment() ).longValue() );
            break;
         default:
            affected = 0;
            break;   
      }
      return affected;
   }

   @Override
   public int bulkInsert( Uri uri, ContentValues[] valuesArray )
   {
      int inserted = 0;
      int match = GPSTrackingProvider.sURIMatcher.match( uri );
      switch (match)
      {
         case WAYPOINTS:
            List<String> pathSegments = uri.getPathSegments();
            int trackId = Integer.parseInt( pathSegments.get( 1 ) );
            int segmentId = Integer.parseInt( pathSegments.get( 3 ) );
            inserted = this.mDbHelper.bulkInsertWaypoint( trackId, segmentId, valuesArray );
            break;
         default:
            inserted = super.bulkInsert( uri, valuesArray );
            break;
      }
      return inserted;
   }

}
