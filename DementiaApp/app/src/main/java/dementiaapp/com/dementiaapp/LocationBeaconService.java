package dementiaapp.com.dementiaapp;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.orhanobut.logger.Logger;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Timer;
import java.util.TimerTask;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class LocationBeaconService extends IntentService {

    public LocationBeaconService() {
        super("LocationBeaconService");
    }

    private int numRetries = 0;

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.e("Entered onHandleIntent in LocationService");

        SmartLocation.with(getApplicationContext())
                .location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(final Location lastKnownLocation) {
                        if (MemAidUtils.hasDebugLocationSettingTurnedOn(getApplicationContext())
                                && MemAidUtils.hasUserAllowedLocationTracking(getApplicationContext())) {
                            Logger.e("Entered onConnected in LocationService");
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {

                                    Logger.e("About to try and send location");
                                    if (lastKnownLocation != null) {
                                        ParseObject location = new ParseObject("Location");
                                        location.put("email", ParseUser.getCurrentUser().getEmail());
                                        location.put("geopoint", new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                                        location.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    Logger.e("Location NOT SENT successfully");
                                                    Toast.makeText(getApplicationContext(), "Could not send location because " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    if (numRetries > 2) {
                                                        SmartLocation.with(getApplicationContext()).location().stop();
                                                        timer.cancel();
                                                    } else {
                                                        numRetries++;
                                                    }
                                                } else {
                                                    Logger.e("Location sent successfully");
                                                    Toast.makeText(getApplicationContext(), "Location sent successfully!", Toast.LENGTH_LONG).show();
                                                    SmartLocation.with(getApplicationContext()).location().stop();
                                                    timer.cancel();
                                                }
                                            }
                                        });
                                    } else {
                                        Logger.e("LastKnownLocation was null in LocationBeaconService");
                                        if (numRetries > 2) {
                                            SmartLocation.with(getApplicationContext()).location().stop();
                                            timer.cancel();
                                        } else {
                                            numRetries++;
                                        }
                                    }
                                }
                            }, 0L, 90 * 1000);
                        } else {
                            SmartLocation.with(getApplicationContext()).location().stop();
                            Logger.e("Either user hasn't allowed location tracking or debug setting is off");
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SmartLocation.with(getApplicationContext()).location().stop();
    }
}
