package com.avidas.localize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class DevAroundActivity extends FragmentActivity implements LocationListener {

	private Location currentLoc;
	private Location previousLoc;
	
	private double totalDistance = 0;
	private long eventStartTime = 0;
	
	GoogleMap mGoogleMap;
	Spinner mSprPlaceType;

	String[] mPlaceType = null;
	String[] mPlaceTypeName = null;

	double mLatitude = 0;
	double mLongitude = 0;
	
	Marker oMark = null;
	LocationManager locationManager;

	ArrayList<Marker> markers = new ArrayList<Marker>();
	SupportMapFragment fragment;
	
	TextView txtOutput = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		        
		// Array of place types
		mPlaceType = getResources().getStringArray(R.array.dev_type);

		// Array of place type names
		mPlaceTypeName = getResources().getStringArray(R.array.dev_type_name);

		// Creating an array adapter with an array of Place types
		// to populate the spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, mPlaceTypeName);

		// Getting reference to the Spinner
		mSprPlaceType = (Spinner) findViewById(R.id.spr_place_type);

		// Setting adapter on Spinner to set place types
		mSprPlaceType.setAdapter(adapter);

		Button btnFind;

		// Getting reference to Find Button
		btnFind = (Button) findViewById(R.id.btn_find);

		// Getting Google Play availability status
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());
		
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();		
		} else {
			// Getting reference to the SupportMapFragment
			fragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);

			// Getting Google Map
			mGoogleMap = fragment.getMap();

			// Enabling MyLocation in Google Map
			mGoogleMap.setMyLocationEnabled(true);

			// Getting LocationManager object from System Service
			// LOCATION_SERVICE
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			// Creating a criteria object to retrieve provider
			Criteria oGPSSettings = new Criteria();
			oGPSSettings.setAccuracy(Criteria.ACCURACY_FINE);
			oGPSSettings.setSpeedRequired(true);
			oGPSSettings.setAltitudeRequired(true);
			oGPSSettings.setBearingRequired(false);
			oGPSSettings.setCostAllowed(false);
			oGPSSettings.setPowerRequirement(Criteria.POWER_MEDIUM);
			
			txtOutput = (TextView) findViewById(R.id.txtOutput);
			

			// Getting the name of the best provider
			String provider = locationManager.getBestProvider(oGPSSettings, true);

			// Getting Current Location From GPS
			Location location = locationManager.getLastKnownLocation(provider);

			if (location != null) {
				onLocationChanged(location);
			}

			locationManager.requestLocationUpdates(provider, 20000, 0, this);

			// Setting click event lister for the find button
			btnFind.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					int selectedPosition = mSprPlaceType
							.getSelectedItemPosition();
					String type = mPlaceType[selectedPosition];
					
					mLatitude = 40.7566;
					mLongitude = -73.9863;
					
					StringBuilder sb = new StringBuilder(
							"http://young-sea-7700.herokuapp.com/devsnearme/api/v0.1/venues?");
					sb.append("type=" + type);


					// Creating a new non-ui thread task to download data
					PlacesTask placesTask = new PlacesTask();

					// Invokes the "doInBackground()" method of the class
					// PlaceTask
					placesTask.execute(sb.toString());

				}
			});
		}
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			Log.d("URL :", strUrl);
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();
			Log.d("JSON_RESPONSE", data);

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}

		return data;
	}
	
	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadUrl(url[0]);
				Log.d("RESPONSE", data);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {
			ParserTask parserTask = new ParserTask();

			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}

	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;
			DevsJSONParser devsJsonParser = new DevsJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a List construct */
				places = devsJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return places;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {

			// Clears all the existing markers
			mGoogleMap.clear();
			LatLng latLng = null;

			for (int i = 0; i < list.size(); i++) {
			
				// Creating a marker
				MarkerOptions markerOptions = new MarkerOptions();

				// Getting a place from the places list
				HashMap<String, String> hmPlace = list.get(i);

				// Getting latitude of the place
				
				double lat = Double.parseDouble(hmPlace.get("lat"));

				// Getting longitude of the place
				
				double lng = Double.parseDouble(hmPlace.get("lng"));

				// Getting name
				String name = hmPlace.get("venue_name");
				Log.d("VENUE_NAME :", name);

				// Getting vicinity
				String expectedAtt = hmPlace.get("expected_attendance");

				latLng = new LatLng(lat, lng);

				// Setting the position for the marker
				markerOptions.position(latLng);

				// Setting the title for the marker.
				// This will be displayed on taping the marker
				markerOptions.title(name + " : " + expectedAtt);
				markerOptions.snippet(hmPlace.get("Tags"));
				//markerOptions.snippet("Lat : " + hmPlace.get("lat"));
				//markerOptions.snippet("Lon : " + hmPlace.get("lng"));

				// Placing a marker on the touched position
				Marker marker_entry = mGoogleMap.addMarker(markerOptions);
				markers.add(marker_entry);
							
			}
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 12);
			mGoogleMap.moveCamera(update);
			mGoogleMap.setOnMapClickListener(new OnMapClickListener(){

				@Override
				public void onMapClick(LatLng arg0) {
					LatLngBounds.Builder b = new LatLngBounds.Builder();
					for (Marker m : markers) {
					    b.include(m.getPosition());
					    //Toast.makeText(getBaseContext(), m.getPosition().toString(), Toast.LENGTH_LONG).show();
					}
					LatLngBounds bounds = b.build();
					//Change the padding as per needed
					CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
				    mGoogleMap.animateCamera(cu);							
				}		
			});
		}
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLoc = location;
		long currentTime = getRawTime();
		if (previousLoc != null) {
			// Calculates the distance between the last location and the
			// current.
			double dis = getDistance(previousLoc, currentLoc);
			// Adds the distance to the previous total.
			totalDistance += dis; // in KM
			long totalTimeDiff = (currentTime - eventStartTime) / 1000L;
			double CurrentMetersPerSecond = location.getSpeed();
			double kps = totalTimeDiff / totalDistance; // km per second
			// 1 kilometer per second = 3600 kilometers per hour or kps / 60
			// minutes / 60 seconds
			double kPH = 1 / ((kps / 60) / 60);

			String sText = "Distance: "
					+ String.format("%.2f", Double.valueOf(totalDistance))
					+ " km\n" + "Speed: "
					+ String.format("%.2f", (CurrentMetersPerSecond * 3.6))
					+ " kph\n" + "Avg Speed: " + String.format("%.2f", (kPH))
					+ " kph\n" + "Lon: " + currentLoc.getLongitude() + "\n"
					+ "Lat: " + currentLoc.getLatitude() + "\n" + "Alt: "
					+ currentLoc.getAltitude();

			//txtOutput.setText(sText);	FIXME display distance properly
		}
		// sets this location as last location.
		previousLoc = currentLoc;
		
		LatLng oPos = new LatLng(currentLoc.getLatitude(),
				currentLoc.getLongitude());
		
		fragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		if (fragment != null) {
			mGoogleMap.clear();
			oMark = mGoogleMap.addMarker(new MarkerOptions().position(oPos).title(
					"My Location"));
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(oPos));
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
		}
		//mLatitude = location.getLatitude();
		//mLongitude = location.getLongitude();
		//LatLng latLng = new LatLng(mLatitude, mLongitude);

		//mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		//mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
	}

	@Override
	public void onProviderDisabled(String provider) {
		locationManager.removeUpdates(this);
		Toast.makeText(getBaseContext(), provider + " is disabled.",
				Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		locationManager.removeUpdates(this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
		Toast.makeText(getBaseContext(), provider + " is enabled.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		locationManager.removeUpdates(this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.removeUpdates(this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}
	/**
	 * 
	 * Returns the current date/time in milliseconds since epoch.
	 */
	public static long getRawTime() {
		Calendar dt = Calendar.getInstance();
		return dt.getTimeInMillis();
	}

	/**
	 * getDistance is used to return the distance in kilometers between two
	 * location points.
	 */
	public static double getDistance(Location PreviousLocation,
			Location CurrentLocation) { // in KM

		double lat1 = PreviousLocation.getLatitude();
		double lon1 = PreviousLocation.getLongitude();
		double lat2 = CurrentLocation.getLatitude();
		double lon2 = CurrentLocation.getLongitude();

		double latA = Math.toRadians(lat1);
		double lonA = Math.toRadians(lon1);
		double latB = Math.toRadians(lat2);
		double lonB = Math.toRadians(lon2);

		double cosAng = (Math.cos(latA) * Math.cos(latB) * Math
				.cos(lonB - lonA)) + (Math.sin(latA) * Math.sin(latB));
		double ang = Math.acos(cosAng);
		double dist = ang * 6371; // earth's radius!
		return dist;
	}

}
