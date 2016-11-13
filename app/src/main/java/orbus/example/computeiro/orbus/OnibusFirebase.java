package orbus.example.computeiro.orbus;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class OnibusFirebase {
	private double latitude;
	private double longitude;
	private float speed;
	private HashMap<String, Object> timestampCreated;

	public OnibusFirebase() {}

	public OnibusFirebase(double latitude, double longitude, float speed) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed = speed;
		timestampCreated = new HashMap<String,Object>();
		timestampCreated.put("timestamp", ServerValue.TIMESTAMP);
	}


	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setTimestampCreated(HashMap<String, Object> timestampCreated) {
		this.timestampCreated = timestampCreated;
	}

	public HashMap<String, Object> getTimestampCreated() {
		return this.timestampCreated;
	}

	@Exclude
	public long getTimestampCreatedLong() {
		return (long)timestampCreated.get("timestamp");
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
