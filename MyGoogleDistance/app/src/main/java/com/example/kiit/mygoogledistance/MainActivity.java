package com.example.kiit.mygoogledistance;

import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.kiit.mygoogledistance.R.id.map;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener

          {


    public static final String TAG = MainActivity.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final LatLng destPosition=new LatLng(22.6026,88.3659);
 //   private static final LatLng sourcePosition=new LatLng(22.5765,88.4796);

              TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        textView=(TextView)findViewById(R.id.textView);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);

    }
              @Override
              protected void onResume() {
                  super.onResume();
                  setUpMapIfNeeded();
                  mGoogleApiClient.connect();
              }

              @Override
              protected void onPause() {
                  super.onPause();

                  if (mGoogleApiClient.isConnected()) {
                      LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                      mGoogleApiClient.disconnect();
                  }
              }

              private void setUpMapIfNeeded() {
                  // Do a null check to confirm that we have not already instantiated the map.
                  if (mMap == null) {
                      // Try to obtain the map from the SupportMapFragment.
                      mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(map))
                              .getMap();
                      // Check if we were successful in obtaining the map.
                      if (mMap != null) {
                          setUpMap();
                      }
                  }
              }

              private void setUpMap() {

                  mMap.addMarker(new MarkerOptions().position(new LatLng(22.6026,88.3659)).title("Destinationn"));
              }

              private void handleNewLocation(Location location) {
                  Log.d(TAG, location.toString());

                  double currentLatitude = location.getLatitude();
                  double currentLongitude = location.getLongitude();


                  LatLng latLng = new LatLng(currentLatitude, currentLongitude);

                  String url = getDirectionsUrl(latLng, destPosition);

                  DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                  downloadTask.execute(url);

                //  PolylineOptions polylineOptions=new PolylineOptions().add(latLng).add(destPosition).width(5).color(Color.BLUE).
                //          geodesic(true);
                //  mMap.addPolyline(polylineOptions);
                //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));

                  //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
                  MarkerOptions options = new MarkerOptions()
                          .position(latLng)
                          .title("I am here!");

                  options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                  mMap.addMarker(options);
                  mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                 // getDistance(latLng,destPosition);
                  CalculationByDistance(latLng,destPosition);


              }



              public double CalculationByDistance(LatLng StartP, LatLng EndP) {
                  int Radius = 6371;// radius of earth in Km
                  double lat1 = StartP.latitude;
                  double lat2 = EndP.latitude;
                  double lon1 = StartP.longitude;
                  double lon2 = EndP.longitude;
                  double dLat = Math.toRadians(lat2 - lat1);
                  double dLon = Math.toRadians(lon2 - lon1);
                  double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                          + Math.cos(Math.toRadians(lat1))
                          * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                          * Math.sin(dLon / 2);
                  double c = 2 * Math.asin(Math.sqrt(a));
                  double valueResult = Radius * c;
                  double km = valueResult / 1;
                  DecimalFormat newFormat = new DecimalFormat("####");
                  int kmInDec = Integer.valueOf(newFormat.format(km));
                  double meter = valueResult % 1000;
                  int meterInDec = Integer.valueOf(newFormat.format(meter));
                //  Toast.makeText(getApplicationContext(),"Radious Value:  "+valueResult+"  KM: "+kmInDec+" Meter: "+meterInDec,Toast.LENGTH_LONG).show();
                  Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                          + " Meter   " + meterInDec);
                  textView.setText("Radius Value"+ valueResult +", "+kmInDec+"KM,  "+meterInDec+"Meter. ");
                  return Radius * c;
              }



              @Override
              public void onConnected(Bundle bundle) {
                  Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                  if (location == null) {
                      LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                  }
                  else {
                      handleNewLocation(location);
                  }
              }

              @Override
              public void onConnectionSuspended(int i) {

              }

              @Override
              public void onConnectionFailed(ConnectionResult connectionResult) {
                  if (connectionResult.hasResolution()) {
                      try {
                          // Start an Activity that tries to resolve the error
                          connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
                      } catch (IntentSender.SendIntentException e) {
                          // Log the error
                          e.printStackTrace();
                      }
                  } else {
        /*
         * If no resolution is available, display a dialog to the
         * user with the error.
         */
                      Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
                  }
              }

              @Override
              public void onLocationChanged(Location location) {
                  handleNewLocation(location);
              }



              //////////////////////////////////
              private String getDirectionsUrl(LatLng origin,LatLng dest){


                  // Origin of route
                  String str_origin = "origin="+origin.latitude+","+origin.longitude;

                  // Destination of route
                  String str_dest = "destination="+dest.latitude+","+dest.longitude;

                  // Sensor enabled
                  String sensor = "sensor=false";

                  // Building the parameters to the web service
                  String parameters = str_origin+"&"+str_dest+"&"+sensor;

                  // Output format
                  String output = "json";

                  // Building the url to the web service
                  String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

                  return url;
              }



              /** A method to download json data from url */
              private String downloadUrl(String strUrl) throws IOException {
                  String data = "";
                  InputStream iStream = null;
                  HttpURLConnection urlConnection = null;
                  try{
                      URL url = new URL(strUrl);

                      // Creating an http connection to communicate with url
                      urlConnection = (HttpURLConnection) url.openConnection();

                      // Connecting to url
                      urlConnection.connect();

                      // Reading data from url
                      iStream = urlConnection.getInputStream();

                      BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                      StringBuffer sb = new StringBuffer();

                      String line = "";
                      while( ( line = br.readLine()) != null){
                          sb.append(line);
                      }

                      data = sb.toString();

                      br.close();

                  }catch(Exception e){
                      Log.d("Exception while downloading url", e.toString());
                  }finally{
                      iStream.close();
                      urlConnection.disconnect();
                  }
                  return data;
              }

              // Fetches data from url passed
              private class DownloadTask extends AsyncTask<String, Void, String> {

                  // Downloading data in non-ui thread
                  @Override
                  protected String doInBackground(String... url) {

                      // For storing data from web service
                      String data = "";

                      try{
                          // Fetching the data from web service
                          data = downloadUrl(url[0]);
                      }catch(Exception e){
                          Log.d("Background Task",e.toString());
                      }
                      return data;
                  }

                  // Executes in UI thread, after the execution of
                  // doInBackground()
                  @Override
                  protected void onPostExecute(String result) {
                      super.onPostExecute(result);

                      ParserTask parserTask = new ParserTask();

                      // Invokes the thread for parsing the JSON data
                      parserTask.execute(result);
                  }
              }

              /** A class to parse the Google Places in JSON format */
              private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

                  // Parsing the data in non-ui thread
                  @Override
                  protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

                      JSONObject jObject;
                      List<List<HashMap<String, String>>> routes = null;

                      try {
                          jObject = new JSONObject(jsonData[0]);
                          DirectionsJSONParser parser = new DirectionsJSONParser();

                          // Starts parsing data
                          routes = parser.parse(jObject);
                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                      return routes;
                  }

                  // Executes in UI thread, after the parsing process
                  @Override
                  protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                      ArrayList<LatLng> points = null;
                      PolylineOptions lineOptions = null;
                      MarkerOptions markerOptions = new MarkerOptions();

                      // Traversing through all the routes
                      for (int i = 0; i < result.size(); i++) {
                          points = new ArrayList<LatLng>();
                          lineOptions = new PolylineOptions();

                          // Fetching i-th route
                          List<HashMap<String, String>> path = result.get(i);

                          // Fetching all the points in i-th route
                          for (int j = 0; j < path.size(); j++) {
                              HashMap<String, String> point = path.get(j);

                              double lat = Double.parseDouble(point.get("lat"));
                              double lng = Double.parseDouble(point.get("lng"));
                              LatLng position = new LatLng(lat, lng);

                              points.add(position);
                          }

                          // Adding all the points in the route to LineOptions
                          lineOptions.addAll(points);
                          lineOptions.width(12);
                          lineOptions.color(Color.RED);
                      }

                      // Drawing polyline in the Google Map for the i-th route
                      mMap.addPolyline(lineOptions);
                  }
              }
    }


