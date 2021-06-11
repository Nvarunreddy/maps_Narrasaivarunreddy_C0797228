package com.app.maps_narrasaivarunreddy_c0797228;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends LocationActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private List<Marker> markerOptionsList = new ArrayList<>();
    private PolygonOptions polygonOptions;
    private Polygon polygon;
    private LatLng currentLatLng;
    List<Polyline> polylines = new ArrayList<>();
    Marker centerOneMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        polygonOptions = new PolygonOptions();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestLocation(new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Location mLocation = location;
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (mMap != null) {
                    LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Current Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17.0f));

                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnMapClickListener(this);
        // Add a marker in Sydney and move the camera
        setLongClickListener(mMap);
    }

    private void setLongClickListener(GoogleMap mMap) {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if (markerOptionsList.size() < 4) {
                    LatLng sydney = new LatLng(latLng.latitude, latLng.longitude);
                    int height = 150;
                    int width = 150;
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    MarkerOptions markerOptions = new MarkerOptions().position(sydney).snippet(calculationByDistance(sydney, currentLatLng) + " meters").title("Distance").icon(smallMarkerIcon);

                    Marker marker = mMap.addMarker(markerOptions.draggable(true));
                    markerOptionsList.add(marker);
                    polygonOptions.add(latLng);
                    if (markerOptionsList.size() == 4) {
                        polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(0).getPosition(), markerOptionsList.get(1).getPosition()).width(7).color(Color.RED).clickable(true)));
                        polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(1).getPosition(), markerOptionsList.get(2).getPosition()).width(7).color(Color.RED).clickable(true)));
                        polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(2).getPosition(), markerOptionsList.get(3).getPosition()).width(7).color(Color.RED).clickable(true)));
                        polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(3).getPosition(), markerOptionsList.get(0).getPosition()).width(7).color(Color.RED).clickable(true)));
                        polygonOptions.strokeColor(Color.RED);
                        polygonOptions.strokeWidth((float) 0.30);
                        polygonOptions.fillColor(ContextCompat.getColor(MapsActivity.this, R.color.green_alpha));
                        polygon = mMap.addPolygon(polygonOptions);
                        polygon.setClickable(true);
                    }
                } else {
                    for (Marker m : markerOptionsList) {
                        m.remove();
                    }

                    for (Polyline polyline : polylines) {
                        polyline.remove();
                    }
                    polygon.remove();
                    polylines.clear();
                    markerOptionsList.clear();
                    if(centerOneMarker !=null && centerOneMarker.isVisible()){
                        centerOneMarker.remove();
                    }
                    polygonOptions = new PolygonOptions();
                }

            }
        });
    }


    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        float[] distance1 = new float[1];
        Location.distanceBetween(StartP.latitude, StartP.longitude, EndP.latitude, EndP.longitude, distance1);
        return distance1[0];

    }

    private void showAddress(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5


            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            Toast.makeText(MapsActivity.this, city + ", " + state + ", " + postalCode, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        showAddress(marker.getPosition());
        return false;
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {
        double totalDistance = calculationByDistance(markerOptionsList.get(0).getPosition(), markerOptionsList.get(1).getPosition())
                + calculationByDistance(markerOptionsList.get(1).getPosition(), markerOptionsList.get(2).getPosition())
                + calculationByDistance(markerOptionsList.get(2).getPosition(), markerOptionsList.get(3).getPosition());
        Toast.makeText(this, "Total distance: " + totalDistance + " meters", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        if (markerOptionsList.size() == 4) {

            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polygon.remove();
            polylines.clear();
            polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(0).getPosition(), markerOptionsList.get(1).getPosition()).width(7).color(Color.RED).clickable(true)));
            polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(1).getPosition(), markerOptionsList.get(2).getPosition()).width(7).color(Color.RED).clickable(true)));
            polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(2).getPosition(), markerOptionsList.get(3).getPosition()).width(7).color(Color.RED).clickable(true)));
            polylines.add(mMap.addPolyline(new PolylineOptions().add(markerOptionsList.get(3).getPosition(), markerOptionsList.get(0).getPosition()).width(7).color(Color.RED).clickable(true)));

            polygonOptions = new PolygonOptions();
            for (Marker m : markerOptionsList) {
                polygonOptions.add(m.getPosition());
            }
            polygonOptions.strokeColor(Color.RED);
            polygonOptions.strokeWidth((float) 0.30);
            polygonOptions.fillColor(ContextCompat.getColor(MapsActivity.this, R.color.green_alpha));
            polygon = mMap.addPolygon(polygonOptions);
            polygon.setClickable(true);
        }
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        float[] distance1 = new float[1];
        Location.distanceBetween(polyline.getPoints().get(0).latitude, polyline.getPoints().get(0).longitude, polyline.getPoints().get(1).latitude, polyline.getPoints().get(1).longitude, distance1);
        LinearLayout distanceMarkerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.distance_marker_layout, null);

        distanceMarkerLayout.setDrawingCacheEnabled(true);
        distanceMarkerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        distanceMarkerLayout.layout(0, 0, distanceMarkerLayout.getMeasuredWidth(), distanceMarkerLayout.getMeasuredHeight());
        distanceMarkerLayout.buildDrawingCache(true);

        TextView positionDistance = (TextView) distanceMarkerLayout.findViewById(R.id.positionDistance);

        positionDistance.setText(distance1[0]+" meters");

        Bitmap b = Bitmap.createBitmap(positionDistance.getWidth(), positionDistance.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        distanceMarkerLayout.layout(positionDistance.getLeft(), positionDistance.getTop(), positionDistance.getRight(), positionDistance.getBottom());
        distanceMarkerLayout.draw(c);
        BitmapDescriptor flagBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(b);

        double dLon = Math.toRadians(polyline.getPoints().get(1).longitude - polyline.getPoints().get(0).longitude);

        double lat1 = Math.toRadians(polyline.getPoints().get(0).latitude);
        double lat2 = Math.toRadians(polyline.getPoints().get(1).latitude);
        double lon1 = Math.toRadians(polyline.getPoints().get(0).longitude);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        lat3 = Math.toDegrees(lat3);
        lon3 = Math.toDegrees(lon3);

        if(centerOneMarker != null){
            centerOneMarker.remove();
        }
        centerOneMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat3, lon3))
                .title("Distance")
                .snippet(""+distance1[0])
                .icon(flagBitmapDescriptor));
    }


    @Override
    public void onMapClick(@NonNull LatLng latLng) {
    }


}