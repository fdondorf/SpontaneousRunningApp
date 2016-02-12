package org.spontaneous.core;

import java.util.List;

import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.model.UserModel;

public interface ITrackingService {

	/******************************
	  * Tracking-Management
	  ******************************/
	
	 public TrackModel readTrackById(Long trackId);

	 public List<TrackModel> getAllTracks();
	 
	 public List<GeoPointModel> getGeoPointsByTrack(Long trackId);
	 
	 /******************************
	  * User-Management
	  ******************************/
	 
	 public UserModel login(String email, String password, boolean stayLoggedIn);
	 
	 public boolean checkUserAlreadyLoggedin();
	 
	 public boolean register(UserModel user);
	 
	 public UserModel findUserByMail(String email);

}
