package tcss450.uw.edu.messengerapp.model;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import tcss450.uw.edu.messengerapp.HomeActivity;
import tcss450.uw.edu.messengerapp.R;

/**
 * Polls database periodically for new chat or connections notifications
 *
 * @author Jon Anderson
 * @author Charles Bryan
 */
public class PullService extends IntentService {

    /**Tags for detecting which notification was received**/
    public static final String UPDATE = "UPDATE!";
    public static final String MESSAGE_UPDATE = "New Message!";
    public static final String CONNECTION_UPDATE = "New Connection Request!";

    //60 seconds - 1 minute is the minimum...
    private static final int POLL_INTERVAL = 60_000;

    private static final String TAG = "PullService";

    //Storage for data from notifications
    private static String mUsername = "";
    private static String mChatName = "";
    private boolean isInForeground;

    //List of new connections requests (filering copies)
    ArrayList<String> connectionRequests = new ArrayList<>();

    public PullService() {
        super("PullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "Performing the service");
            checkWebservice(intent.getBooleanExtra(getString(R.string.keys_is_foreground), false));
        }
    }

    /**
     * Starts one service for chat notifications and one for connection requests
     * @param context of app
     * @param isInForeground for notification type
     */
    public static void startServiceAlarm(Context context, boolean isInForeground) {
        Intent i = new Intent(context, PullService.class);
        i.putExtra(context.getString(R.string.keys_is_foreground), isInForeground);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, i, 0);
        PendingIntent pendingIntent2 = PendingIntent.getService(context, 2, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int startAfter = isInForeground ? POLL_INTERVAL : POLL_INTERVAL * 2;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , startAfter
                , POLL_INTERVAL, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , startAfter
                , POLL_INTERVAL, pendingIntent2);

        Log.e(TAG, "starting service");

    }

    /**
     * Ends the current version of the service.
     * @param context of the app
     */
    public static void stopServiceAlarm(Context context) {
        Intent i = new Intent(context, PullService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, i, 0);
        PendingIntent pendingIntent2 = PendingIntent.getService(context, 2, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
        alarmManager.cancel(pendingIntent2);
        pendingIntent.cancel();
        pendingIntent2.cancel();
        Log.e(TAG, "stopping service");
    }

    /**
     * Makes call to web service and sends foreground notifications
     * @param inForeground
     * @return
     */
    private boolean checkWebservice(boolean inForeground) {
        isInForeground = inForeground;
        checkNewMessages();
        checkNewConnectionRequests();
        return true;
    }

    /**
     * Prepares a notification if the app is in the background.
     * @param notificationFlag what the notification will see
     * @param chatId of the new message
     * @param notificationType for differrentiation
     */
    private void buildNotification(String notificationFlag, String chatId, int notificationType) {
        NotificationCompat.Builder mBuilder = null;
        NotificationCompat.Builder mBuilder2 = null;
        Intent notifyIntent = null;

        //******Chat notification == 0, Connection notification == 1*****//
        if (notificationType == 0) { //new chat

            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_chat)
                    .setContentTitle("New message from " + notificationFlag + "!")
                    .setContentText("Click to open this chat!");
            // Creates an Intent for the Activity
            notifyIntent = new Intent(this, HomeActivity.class);
            notifyIntent.putExtra(getString(R.string.keys_chat_notification), chatId);
            // Sets the Activity to start in a new, empty task
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Creates the PendingIntent
            PendingIntent notifyPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            1,
                            notifyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // Puts the PendingIntent into the notification builder
            mBuilder.setContentIntent(notifyPendingIntent);
            mBuilder.setAutoCancel(true);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());

        } else if (notificationType == 1) { //new connection request

            mBuilder2 = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_person_add)
                    .setContentTitle(notificationFlag + " sent you a new connection request!")
                    .setContentText("Click to view your requests!");
            notifyIntent = new Intent(this, HomeActivity.class);
            notifyIntent.putExtra(getString(R.string.keys_connection_notification), notificationFlag);
            // Sets the Activity to start in a new, empty task
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Creates the PendingIntent
            PendingIntent notifyPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            2,
                            notifyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // Puts the PendingIntent into the notification builder
            mBuilder2.setContentIntent(notifyPendingIntent);
            mBuilder2.setAutoCancel(true);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(2, mBuilder2.build());
        }


    }

    /**
     * Sends a DB call to check if there have been any new connection requests
     * that have not already been stored.
     */
    private void checkNewConnectionRequests() {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_post_get_requests2))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put(getString(R.string.keys_json_username), mUsername);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                //.onPreExecute(this::handleGetConnectionRequestsOnPre)
                .onPostExecute(this::handleGetConnectionRequestsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Read through the list of chats and see if the most recent is from
     * somebody else and came recently.
     * @param result of finding chats
     */
    public void handleGetConnectionRequestsOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_requests))) {
                    try {

                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_requests));
                        ArrayList<String> newRequests = new ArrayList<>();

                        for (int i = 0; i < jReqs.length(); i++) {
                            String request = jReqs.getJSONObject(i).getString("username");
                            if (!connectionRequests.contains(request)) {
                                connectionRequests.add(request);
                                if (isInForeground) {
                                    newRequests.add(request);
                                } else {
                                    buildNotification(request, "1", 1);
                                }
                            }
                        }

                        if (isInForeground) {
                            Intent intent = new Intent(CONNECTION_UPDATE);
                            for (String s : newRequests) {
                                intent.putExtra("newConnect", s);
                            }
                            sendBroadcast(intent);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    /**
     * Sends a request for all the chats this user is a part of
     */
    private void checkNewMessages() {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_all_chats))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                //.onPreExecute(this::handleGetChatsOnPre)
                .onPostExecute(this::handleGetChatsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Iterated through all the chats you are a part of and makes a call to see if
     * any of those chats have new messages.
     * @param result of the getAllChats call
     */
    public void handleGetChatsOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_chats))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_chats));
                        for (int i = 0; i < jReqs.length(); i++) {

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date();
                            date.setMinutes(date.getMinutes()-1);
                            String currentDateTime = dateFormat.format(date);

                            int chatid = jReqs.getJSONObject(i).getInt("chatid");
                            mChatName = jReqs.getJSONObject(i).getString("name");

                            //build the web service URL
                            Uri uri = new Uri.Builder()
                                    .scheme("https")
                                    .appendPath(getString(R.string.ep_base_url))
                                    .appendPath(getString(R.string.ep_post_get_messages))
                                    .build();

                            JSONObject msg = new JSONObject();
                            try {
                                msg.put("chatId", chatid);
                                msg.put("after", currentDateTime);
                            } catch (JSONException e) {
                                Log.wtf("JSON EXCEPTION", e.toString());
                            }

                            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                                    //.onPreExecute(this::handleGetMessagesOnPre)
                                    .onPostExecute(this::handleGetMessagesOnPost)
                                    .onCancelled(this::handleErrorsInTask)
                                    .build().execute();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    /**
     * Read through the list of chats and see if the most recent is from
     * somebody else and came recently.
     * @param result messages of a particular chat
     */
    public void handleGetMessagesOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_messages))) {
                    try {
                        JSONArray jReqs2 = resultsJSON.getJSONArray(getString(R.string.keys_json_messages));

                        String messageFrom = jReqs2.getJSONObject(jReqs2.length()-1).getString("username");
                        String messageTime = jReqs2.getJSONObject(jReqs2.length()-1).getString("timestamp");
                        String chatId = jReqs2.getJSONObject(jReqs2.length()-1).getString("chatid");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date();
                        date.setMinutes(date.getMinutes()-1);
                        String currentDateTime = dateFormat.format(date);

                        if (!messageFrom.equals(mUsername) && inLastMinute(messageTime,currentDateTime)) {
                            if (isInForeground) {
                                Intent intent = new Intent(MESSAGE_UPDATE);
                                intent.putExtra(getString(R.string.keys_extra_results), messageFrom);
                                sendBroadcast(intent);
                            } else {
                                buildNotification(messageFrom, chatId,0);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    /**
     * Gracefully handles errors on AsyncTask
     */
    public void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Helper method to see if any message was received int he last minute.
     * @param time of the message
     * @param currentTime on the phone
     * @return whether or not the essage was in the last minute
     */
    public boolean inLastMinute(String time, String currentTime) {

        String date = time.substring(0,10);
        String hour = time.substring(14,16);
        String minute = time.substring(17,19);

        String date1 = currentTime.substring(0,10);
        String hour1 = currentTime.substring(11,13);
        String minute1 = currentTime.substring(14,16);

        if(date.equals(date1) && hour.equals(hour1)) {
            int currentMinute = Integer.valueOf(minute1);
            int msgMinute = Integer.valueOf(minute);
            if (currentMinute == msgMinute || (currentMinute == (msgMinute + 1))) {
                return true;
            }
        }
        return false;
    }

    public static void setUsername(final String theString) {
        mUsername = theString;
    }


}