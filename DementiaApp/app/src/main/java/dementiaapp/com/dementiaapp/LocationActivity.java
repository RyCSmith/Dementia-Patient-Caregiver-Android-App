package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LocationActivity extends Activity implements OnMapReadyCallback {

    List<ParseObject> locations = new ArrayList<ParseObject>();

    boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        final ParseQuery<ParseObject> locationsQuery = ParseQuery.getQuery("Location");
        locationsQuery.whereEqualTo("email", ParseUser.getCurrentUser().getEmail());
        locationsQuery.setLimit(10);
        locationsQuery.addDescendingOrder("createdAt");
        locationsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                Collections.reverse(list);

                locations = list;

                List<Marker> markers = new ArrayList<Marker>();

                for (ParseObject location : locations) {
                    ParseGeoPoint point = location.getParseGeoPoint("geopoint");
                    String currentUserName = (String) ParseUser.getCurrentUser().get("patientName");
                    String locationString = location.getCreatedAt().toString();

                    markers.add(map.addMarker(new MarkerOptions()
                            .position(new LatLng(point.getLatitude(), point.getLongitude()))
                            .title("Location tracked")
                            .snippet(currentUserName + " was here at " + locationString)));
                }
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(37.7750, 122.4183))
                        .title("San Francisco")
                        .snippet("Population: 776733")));
                markers.add(map.addMarker(new MarkerOptions()
                        .position(new LatLng(37.8044, 122.2708))
                        .title("Oakland")
                        .snippet("Population: 10000")));

                if (list.size() > 1) {
                    Marker startingPoint = markers.get(0);
                    Marker endingPoint = markers.get(markers.size() - 1);

                    startingPoint.setTitle("Started here");
                    endingPoint.setTitle("Ended here");

                    for (int i = 0; i < list.size() - 1; i++) {
                        ParseGeoPoint src = list.get(i).getParseGeoPoint("geopoint");
                        ParseGeoPoint dest = list.get(i + 1).getParseGeoPoint("geopoint");
                        LatLng point1 = new LatLng(src.getLatitude(), src.getLongitude());
                        LatLng point2 = new LatLng(dest.getLatitude(), dest.getLongitude());
                        Polyline line = map.addPolyline(new PolylineOptions()
                                .add(point1, point2)
                                .width(4)
                                .color(Color.BLUE).geodesic(true));
                    }
                }

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                if (markers.size() == 1) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 15F));
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
