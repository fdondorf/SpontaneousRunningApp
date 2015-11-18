package org.spontaneous.core;

import java.util.List;

import org.spontaneous.activities.model.TrackModel;

public interface ITrackingService {

	 public TrackModel readTrackById(Long trackId);

	 public List<TrackModel> getAllTracks();

}
