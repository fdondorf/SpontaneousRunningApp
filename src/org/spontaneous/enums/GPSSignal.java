package org.spontaneous.enums;

import java.util.LinkedList;
import java.util.List;

public enum GPSSignal {

	NO_GPS_SIGNAL(0),
	
	GPS_SIGNAL_BAD(1),
	
	GPS_SIGNAL_MEDIUM(2),
	
	GPS_SIGNAL_GOOD(3);
	
	int signalStrenght = 0;
	
	GPSSignal (int signalStrenght) {
		this.signalStrenght = signalStrenght;
	}
	
	public static List<GPSSignal> getValues() {
		List<GPSSignal> values = new LinkedList<GPSSignal>();
		for ( GPSSignal signal : GPSSignal.values() ) {
			values.add(signal);
		}
		return values;
	}
	
	public static GPSSignal getGPSSignal(int signalStrenght){
		for(GPSSignal signal : getValues()) {
			if (signal.getSignalStrenght() == signalStrenght)
				return signal;
		}
		return null;
	}

	public int getSignalStrenght() {
		return signalStrenght;
	}
	
	
}
