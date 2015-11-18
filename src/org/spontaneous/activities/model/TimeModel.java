package org.spontaneous.activities.model;

import java.util.Map;

public class TimeModel {

	private Long totalDuration;
	private Map<Integer, Long> splitTimes;
	
	
	public Long getTotalDuration() {
		return totalDuration;
	}
	public void setTotalDuration(Long totalDuration) {
		this.totalDuration = totalDuration;
	}
	public Map<Integer, Long> getSplitTimes() {
		return splitTimes;
	}
	public void setSplitTimes(Map<Integer, Long> splitTimes) {
		this.splitTimes = splitTimes;
	}
}
