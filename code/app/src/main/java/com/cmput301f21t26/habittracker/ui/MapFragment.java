package com.cmput301f21t26.habittracker.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentMapBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnCameraMoveListener {

    private final String TAG = "MapFragment";

    private final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private final int DEFAULT_ZOOM = 15;    // street
    private final LatLng defaultLocation = new LatLng(53.526733732510735, -113.52709359834766);      // Cmput building

    private FragmentMapBinding binding;
    private boolean locationPermissionGranted;
    private Button locConfirmBtn;
    private NavController navController;

    private Habit habit;
    private HabitEvent hEvent;

    private GoogleMap map;
    private Marker marker;
    private LatLng latLng;

    private LocationRequest locationRequest;

    /**
     * Required empty public constructor
     */
    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(inflater, container, false);

        habit = MapFragmentArgs.fromBundle(getArguments()).getHabit();
        hEvent = MapFragmentArgs.fromBundle(getArguments()).getHabitEvent();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        locConfirmBtn = binding.locationConfirmBtn;
        locConfirmBtn.setOnClickListener(locConfirmBtnOnClickListener);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Turn on the my location layer and the related control on the map.
        updateLocationUI();

        // Get the curr loc of the device and set the pos of the map
        getDeviceLocation();

        map.setOnCameraMoveListener(this);
    }

    /**
     * Hides menu items
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MainActivity.hideMenuItems(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.hideBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.showBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e(TAG, e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                LocationServices.getFusedLocationProviderClient(this.getActivity())
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                // We only want to call this once
                                LocationServices.getFusedLocationProviderClient(getActivity())
                                        .removeLocationUpdates(this);

                                latLng = defaultLocation;

                                if (locationResult != null && locationResult.getLocations().size() > 0) {
                                    int index = locationResult.getLocations().size() - 1;
                                    double latitude = locationResult.getLocations().get(index).getLatitude();
                                    double longitude = locationResult.getLocations().get(index).getLongitude();
                                    latLng = new LatLng(latitude, longitude);
                                }

                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        latLng, DEFAULT_ZOOM));
                                marker = map.addMarker(
                                        new MarkerOptions()
                                                .position(latLng)
                                                .draggable(true));
                            }
                        }, Looper.getMainLooper());
            } else {
                latLng = defaultLocation;
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        latLng, DEFAULT_ZOOM));
                marker = map.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .draggable(true));
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onCameraMove() {
        int mWidth = binding.mapConstraintLayout.getWidth() / 2;
        int mHeight = binding.mapConstraintLayout.getHeight() / 2;

        Point center = new Point(mWidth, mHeight);
        Projection projection = map.getProjection();

        latLng = projection.fromScreenLocation(center);
        marker.setPosition(latLng);
    }

    private View.OnClickListener locConfirmBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (latLng == null) {
                NavDirections direction = (NavDirections) MapFragmentDirections.actionMapFragmentToEditHabitEventFragment(hEvent, habit);
                navController.navigate(direction);
                return;
            }

            @SuppressLint("DefaultLocale")
            String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                    latLng.latitude,
                    latLng.longitude,
                    getResources().getString(R.string.google_maps_key));
            Log.d(TAG, url);

            RequestQueue queue = Volley.newRequestQueue(getContext());

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jObj = new JSONObject(response);
                                JSONArray results = jObj.getJSONArray("results");
                                String addr = results.getJSONObject(0).getString("formatted_address");
                                hEvent.setAddress(addr);
                                NavDirections direction = (NavDirections) MapFragmentDirections.actionMapFragmentToEditHabitEventFragment(hEvent, habit);
                                navController.navigate(direction);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "", error);
                }
            });

            queue.add(stringRequest);
        }
    };
}
