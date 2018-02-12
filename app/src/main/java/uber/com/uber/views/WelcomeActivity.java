package uber.com.uber.views;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import uber.com.uber.R;
import uber.com.uber.Retrofit.IGoogleAPI;
import uber.com.uber.Retrofit.RetrofitClient;
import uber.com.uber.pojo.DirectionsPojo;
import uber.com.uber.pojo.OverviewPolyline;
import uber.com.uber.pojo.Route;
import uber.com.uber.utils.AppConstants;
import uber.com.uber.utils.CommonUtils;

public class WelcomeActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int LATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private DatabaseReference mDrivers;
    private GeoFire mGeoFire;

    private Marker mCarMarker;
    private SupportMapFragment mMapFragment;
    private MaterialAnimatedSwitch mLocation_switch;

    private List<LatLng> mPolyLineLists;
    private Marker mPickupLocationMarker;
    private float mV;
    private double mLat, mLng;
    private Handler mHandler;
    private LatLng mStartPosition, mEndPosition, mCurrentPosition;
    private int mIndex, mNext;
    private Button mBtnGo;
    private EditText mEdtPlace;
    private String mDestination, mOrigin, mMode, mKey, mRoutingPrefrence;
    private PolylineOptions mPolylineOptions, mBlackPolylineOptions;
    private Polyline mBlackPolyLine, mGreyPolyLine;

    Runnable drawPathRunnable=new Runnable() {
        @Override
        public void run() {
              if(mIndex<mPolyLineLists.size()-1){
                  mIndex++;
                  mNext=mIndex+1;
              }
              if(mIndex < mPolyLineLists.size()-1){
                  mStartPosition=mPolyLineLists.get(mIndex);
                  mEndPosition=mPolyLineLists.get(mNext);
              }

            ValueAnimator polyLineAnimator=ValueAnimator.ofFloat(0,1);
            polyLineAnimator.setDuration(3000);
            polyLineAnimator.setInterpolator(new LinearInterpolator());
            polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mV=valueAnimator.getAnimatedFraction();
                    mLng=mV * mEndPosition.longitude+(1-mV)*mStartPosition.longitude;
                    mLat=mV * mEndPosition.latitude+(1-mV)*mStartPosition.latitude;
                    LatLng newpos=new LatLng(mLat,mLng);
                    mCarMarker.setPosition(newpos);
                    mCarMarker.setAnchor(0.5f,0.5f);
                  //  mCarMarker.setRotation(getbearing(mStartPosition,newpos));

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newpos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            polyLineAnimator.start();
            mHandler.postDelayed(this,3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        initUI();

        mLocation_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    startLocationUpdates();
                    displayLocation();
                    CommonUtils.showSnackBarNoAction(mMapFragment.getView(), "You are Online");
                } else {
                    stopLocationUpdates();
                    mCarMarker.remove();
                    CommonUtils.showSnackBarNoAction(mMapFragment.getView(), "You are Offline");

                }
            }
        });

        //Geo Fire
        mDrivers = FirebaseDatabase.getInstance().getReference("Drivers");
        mGeoFire = new GeoFire(mDrivers);

        setUpLocation();


    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, AppConstants.PERMISSION_REQUEST
            );
        } else {
            if (checkPlayServices()) {

                buildGoogleApiClient();
                createLocationRequest();
                if (mLocation_switch.isChecked()) {
                    displayLocation();
                }

            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultcode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultcode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultcode))
                GooglePlayServicesUtil.getErrorDialog(resultcode, this, AppConstants.PLAY_SERVICE_REQUEST);
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void initUI() {
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mLocation_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);

        mPolyLineLists = new ArrayList<>();
        mBtnGo = (Button) findViewById(R.id.btnGo);
        mEdtPlace = (EditText) findViewById(R.id.edtPlace);
        mBtnGo.setOnClickListener(onClickListener);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.btnGo:
                    getDestination();
                    break;
            }
        }
    };

    private void getDestination() {
        mDestination = mEdtPlace.getText().toString();
        mDestination = mDestination.replace(" ", "+");
        // getDirection();
        getDirectionJSON();

    }

    private void getDirectionJSON() {
        mCurrentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                +mCurrentPosition.latitude + "," + mCurrentPosition.longitude
                + "&" + "destination="+mDestination
                + "&" + "mode=driving&"
                +"transit_routing_prefrence=less_driving&"+
                "key=AIzaSyCxyQhG3x0QNi7zFPDYjGnxqs0cIkHnAS8";
        GetPolyLines getPolyLinesobj = new GetPolyLines();
        getPolyLinesobj.execute(url);
    }

    private void getDirection() {


        String requestAPI = null;

        try {
            mOrigin = mCurrentPosition.latitude + "," + mCurrentPosition.longitude;
            mRoutingPrefrence = "less_driving";
            mMode = "driving";
            mKey = getResources().getString(R.string.directions_api_key);


            Retrofit retrofitobj = RetrofitClient.retrofitService();
            IGoogleAPI IGoogleAPIobj = retrofitobj.create(IGoogleAPI.class);

            Call<DirectionsPojo> directionsAPICall = IGoogleAPIobj.getPath(mOrigin, mMode, mRoutingPrefrence, mDestination);
            directionsAPICall.enqueue(new Callback<DirectionsPojo>() {
                @Override
                public void onResponse(Call<DirectionsPojo> call, Response<DirectionsPojo> response) {
                    if (response.isSuccessful()) {
                        List<Route> routesList = response.body().getRoutes();
                        for (int i = 0; i < routesList.size(); i++) {
                            OverviewPolyline overviewPolyLineObj = routesList.get(i).getOverviewPolyline();
                            String points = overviewPolyLineObj.getPoints();
                            mPolyLineLists = decodePoly(points);
                        }

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latlng : mPolyLineLists) {
                            builder.include(latlng);
                            LatLngBounds mLatLngBounds = builder.build();
                            CameraUpdate mCameraUpdate = CameraUpdateFactory
                                    .newLatLngBounds(mLatLngBounds, 2);
                            mMap.animateCamera(mCameraUpdate);

                            mPolylineOptions = new PolylineOptions();
                            mPolylineOptions.color(Color.GRAY);
                            mPolylineOptions.width(5);
                            mPolylineOptions.startCap(new SquareCap());
                            mPolylineOptions.endCap(new SquareCap());
                            mPolylineOptions.jointType(JointType.ROUND);
                            mPolylineOptions.addAll(mPolyLineLists);
                            mGreyPolyLine = mMap.addPolyline(mPolylineOptions);

                            mBlackPolylineOptions = new PolylineOptions();
                            mBlackPolylineOptions.color(Color.BLACK);
                            mBlackPolylineOptions.width(5);
                            mBlackPolylineOptions.startCap(new SquareCap());
                            mBlackPolylineOptions.endCap(new SquareCap());
                            mBlackPolylineOptions.jointType(JointType.ROUND);
                            mBlackPolyLine = mMap.addPolyline(mPolylineOptions);

                            mMap.addMarker(new MarkerOptions()
                                    .position(mPolyLineLists.get(mPolyLineLists.size() - 1))
                                    .title("Pickup Location"));


                        }
                    }
                }

                @Override
                public void onFailure(Call<DirectionsPojo> call, Throwable t) {

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstants.PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();
                        if (mLocation_switch.isChecked()) {
                            displayLocation();
                        }

                    }
                }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            if (mLocation_switch.isChecked()) {
                final double latitude = mLastLocation.getLatitude();
                final double logitude = mLastLocation.getLongitude();

                //update to Firebase
                mGeoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, logitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //add marker
                        if (mCarMarker != null)
                            mCarMarker.remove();
                        mCarMarker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                .title("You")
                                .position(new LatLng(latitude, logitude)));

                        //move camera to this posotion
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, logitude), 15.0f));


                    }
                });
            }
        } else {
            Log.d("ERROR", "Cannot get your Location");
        }


    }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                float rot = t * i + (1 - t) * startRotation;
                mCurrent.setRotation(-rot > 180 ? rot / 2 : rot);

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }


            }
        });

    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class GetPolyLines extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                String directionsUrl = strings[0];
                URL url = new URL(directionsUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection)
                        url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();


                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                bufferedReader.close();
                inputStream.close();
                inputStreamReader.close();
                httpURLConnection.disconnect();


                String finalData = stringBuilder.toString();
                return  finalData;


            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject route = jsonArray.getJSONObject(i);
                    JSONObject poly = route.getJSONObject("overview_polyline");
                    String polyline = poly.getString("points");
                    mPolyLineLists = decodePoly(polyline);
                }

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latlng : mPolyLineLists)
                    builder.include(latlng);
                    LatLngBounds mLatLngBounds = builder.build();
                    CameraUpdate mCameraUpdate = CameraUpdateFactory
                            .newLatLngBounds(mLatLngBounds, 2);
                    mMap.animateCamera(mCameraUpdate);

                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.GRAY);
                    mPolylineOptions.width(5);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.endCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolyLineLists);
                    mGreyPolyLine = mMap.addPolyline(mPolylineOptions);

                    mBlackPolylineOptions = new PolylineOptions();
                    mBlackPolylineOptions.color(Color.BLACK);
                    mBlackPolylineOptions.width(5);
                    mBlackPolylineOptions.startCap(new SquareCap());
                    mBlackPolylineOptions.endCap(new SquareCap());
                    mBlackPolylineOptions.jointType(JointType.ROUND);
                    mBlackPolyLine = mMap.addPolyline(mPolylineOptions);

                    mMap.addMarker(new MarkerOptions()
                            .position(mPolyLineLists.get(mPolyLineLists.size() - 1))
                            .title("Pickup Location"));

                ValueAnimator polyLineAnimator=ValueAnimator.ofInt(0,100);
                polyLineAnimator.setDuration(2000);
                polyLineAnimator.setInterpolator(new LinearInterpolator());
                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        List<LatLng>  points=mGreyPolyLine.getPoints();
                        int percentValue=(int) valueAnimator.getAnimatedValue();
                        int size=points.size();
                        int newPoints=(int) (size *(percentValue/100.0f));
                        List<LatLng> p=points.subList(0,newPoints);
                        mBlackPolyLine.setPoints(p);
                    }
                });
               polyLineAnimator.start();

               mCarMarker=mMap.addMarker(new MarkerOptions().position(mCurrentPosition)
                          .flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

               mHandler=new Handler();
               mIndex=-1;
               mNext=1;
               mHandler.post(drawPathRunnable);


            }catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}
