package dementiaapp.com.dementiaapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.logger.Logger;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.instance.Sms;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;


public class SafezoneCreationActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    GoogleMap googleMap;

    List<Circle> circleList = new ArrayList<Circle>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safezone_creation);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.safezone_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        Toast.makeText(getApplicationContext(), "To add a safe zone, touch and hold for 2 seconds the area on the map where you want to add the safe zone", Toast.LENGTH_LONG).show();
        googleMap.setOnMapLongClickListener(this);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        final ParseQuery<ParseObject> locationsQuery = ParseQuery.getQuery("Safezone");
        locationsQuery.whereEqualTo("email", ParseUser.getCurrentUser().getEmail());
        locationsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    Toast.makeText(SafezoneCreationActivity.this, "Error while downloading safezone from online database: "+e.getMessage(), Toast.LENGTH_LONG).show();
                    Logger.e("Error while downloading safezone from online database: "+e.getMessage());
                } else {
                    for (ParseObject safezone: parseObjects) {
                        circleList.add(googleMap.addCircle(getCircleOptions(new LatLng(
                                safezone.getDouble("latitude"), safezone.getDouble("longitude")), safezone.getInt("radius"))));
                    }
                }
            }
        });
    }
    private void showBasicAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SafezoneCreationActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        final Dialog dialog = new Dialog(SafezoneCreationActivity.this);
        dialog.setContentView(R.layout.safezone_radius_dialog);
        dialog.setTitle("Safezone radius in meters");

        dialog.show();
        final EditText radiusEditText = (EditText) dialog.findViewById(R.id.safezone_radius_field);
        final EditText safezoneName = (EditText) dialog.findViewById(R.id.safezone_name);
        Button create = (Button) dialog.findViewById(R.id.safezone_confirm_button);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radiusEditText.getText().toString().isEmpty() || safezoneName.getText().toString().isEmpty()) {
                    showBasicAlertDialog("Error", "You did not fill up both the fields. Please enter both the name and the radius for the safezone");
                    return;
                }
                final double radius = Double.valueOf(radiusEditText.getText().toString());
                googleMap.addCircle(getCircleOptions(latLng, radius));

                ParseObject safezone = new ParseObject("Safezone");
                safezone.put("email", ParseUser.getCurrentUser().getEmail());
                safezone.put("safezone_name", safezoneName.getText().toString());
                safezone.put("latitude", latLng.latitude);
                safezone.put("radius", radius);
                safezone.put("longitude", latLng.longitude);
                safezone.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(SafezoneCreationActivity.this, "Error while uploading safezone to online database: "+e.getMessage(), Toast.LENGTH_LONG).show();
                            Logger.e("Error while uploading safezone to online database: "+e.getMessage());
                        }
                        GeofenceModel geofenceModel = new GeofenceModel.Builder(safezoneName.getText().toString())
                                .setExpiration(Geofence.NEVER_EXPIRE)
                                .setLatitude(latLng.latitude)
                                .setLongitude(latLng.longitude)
                                .setRadius((float) radius)
                                .setTransition(Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build();
                        SmartLocation.with(getApplicationContext())
                                .geofencing()
                                .add(geofenceModel)
                                .start(new OnGeofencingTransitionListener() {
                                    @Override
                                    public void onGeofenceTransition(Geofence geofence, int i) {
                                        Toast.makeText(getApplicationContext(), "Geofence triggered!", Toast.LENGTH_LONG).show();
                                        if (i == Geofence.GEOFENCE_TRANSITION_EXIT) {
                                            Toast.makeText(getApplicationContext(), "Geofence EXIT triggered!", Toast.LENGTH_LONG).show();
                                            TwilioRestClient client = new TwilioRestClient(Credentials.TWILIO_ACCOUNT_SID, Credentials.TWILIO_AUTH_TOKEN);

                                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                                            params.add(new BasicNameValuePair("Body", ((MemAidApplication) getApplication()).getPatientName() + " has just left "+geofence.getRequestId()+". Please check if they are wandering."));
                                            params.add(new BasicNameValuePair("To", ((MemAidApplication) getApplication()).getCaregiverPhoneNumber()));
                                            params.add(new BasicNameValuePair("From", Credentials.TWILIO_SENDER_PHONE_NUMBER));

                                            try {
                                                SmsFactory messageFactory = client.getAccount().getSmsFactory();
                                                Sms message = messageFactory.create(params);
                                            } catch (TwilioRestException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                    }
                });
                dialog.dismiss();
            }
        });
    }

    private CircleOptions getCircleOptions(LatLng center, double radius) {
        CircleOptions co = new CircleOptions();
        co.center(center);
        co.radius(radius);
        co.fillColor(0x110000FF);
        co.strokeColor(0xFF0000FF);
        return co;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_safezone_creation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_all_safezones) {
            final ParseQuery<ParseObject> locationsQuery = ParseQuery.getQuery("Safezone");
            locationsQuery.whereEqualTo("email", ParseUser.getCurrentUser().getEmail());
            locationsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e != null) {
                        Toast.makeText(SafezoneCreationActivity.this, "Error while finding safezone in online database: "+e.getMessage(), Toast.LENGTH_LONG).show();
                        Logger.e("Error while finding safezone in online database: "+e.getMessage());
                    } else {
                        for (ParseObject safezone: parseObjects) {
                            SmartLocation.with(SafezoneCreationActivity.this)
                                    .geofencing()
                                    .remove(safezone.getString("safezone_name"));
                            safezone.deleteInBackground();
                        }
                        if (googleMap != null) {
                            for(Circle circle : circleList) {
                                circle.remove();
                            }
                        }
                    }
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
