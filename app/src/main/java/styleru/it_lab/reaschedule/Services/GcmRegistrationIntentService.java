package styleru.it_lab.reaschedule.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import styleru.it_lab.reaschedule.R;

public class GcmRegistrationIntentService extends IntentService {

    public static final String TAG = "GcmRegistrationIntentService";
    public static final String DEBUG_TAG = "Registration_DEBUG";

    public GcmRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //TODO implement methods to unsubscribe topics, when user changes group or name

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(DEBUG_TAG, "GCM Registration Token: " + token);

            // Subscribe to topic channels
            subscribeTopics(token);

        } catch (Exception e) {
            Log.d(DEBUG_TAG, "Failed to complete token refresh", e);
        }
//        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void subscribeTopics(String token) throws IOException
    {
        //TODO Implement topic subsribing based on SharedPreferences data
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        pubSub.subscribe(token, "/topics/" + "all", null);
    }
}
