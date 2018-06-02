package tcss450.uw.edu.messengerapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tcss450.uw.edu.messengerapp.model.ListenManager;
import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment newInstance} factory method to
 * create an instance of this fragment.
 * Fragment that allows the user to send and display messages.
 *
 * @author Mahad Fahiye
 * @version 5/31/2018
 */
@SuppressLint("ValidFragment")
public class ChatFragment extends Fragment {
    //Fields for User information
    private String mUsername;
    private String mUserchatID = "1";
    private String chatID;

    //Field for the path to sendMessages endpoint
    private String mSendUrl;

    //Field for output screen for messages
    private TextView mOutputTextView;

    //listen manager for message updates
    private ListenManager mListenManager;

    //Field for the current ChatFragment
    private LinearLayout mFragment;

    //Field for storing the number of current messages the user has before
    //a new message gets sent or recieved
    private int currentMessages;

    //Data structures to store message bubble color, all the usernames in the chat and what
    //color their text bubble should be
    private static final int[] MESSAGE_COLORS = {R.color.colorPrimary3, R.color.colorAccent4,
            R.color.colorPrimary4, R.color.colorAccent5, R.color.colorPrimary, R.color.colorPrimaryDark2};
    public ArrayList<String> allUsernames = new ArrayList<>();
    public HashMap<String, Integer> userColors = new HashMap<>();


    @SuppressLint("ValidFragment")
    public ChatFragment(String chatid) {
        chatID = chatid;
        currentMessages = 0;
        // Required empty public constructor
    }

    /*
     * Opens the new fragment and retrieves chatID from ChatActivity
     * also initializes the send button and attaches a sendMessage method as a listener
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mUserchatID = getArguments().getString("CHAT_ID");
        }
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        v.findViewById(R.id.chatSendButton).setOnClickListener(this::sendMessage);
        mOutputTextView = v.findViewById(R.id.chatOutput);
        mOutputTextView.setMovementMethod(new ScrollingMovementMethod());


        return v;
    }

    /*
     * Maps mFragment field to the current ChatFragment
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragment = (LinearLayout) view.findViewById(R.id.chatLayout);
    }

    /**
     * Method called when this fragment comes into view.
     * Sets the username and builds the path to the sendMessages endpoint
     */
    @Override
    public void onStart() {

        super.onStart();

        currentMessages = 0;

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");

        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_send_message))
                .build()
                .toString();


        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_message))
                .appendQueryParameter("chatId", mUserchatID)
                .build();
        if (prefs.contains(getString(R.string.keys_prefs_time_stamp))) {
            //ignore all of the seen messages. You may want to store these messages locally
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setTimeStamp(prefs.getString(getString(R.string.keys_prefs_time_stamp), "0"))
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        } else {
            //no record of a saved timestamp. must be a first time login
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        }


    }

    /**
     * Method called when fragment is brought back into view.
     * Sets the current messages to 0 and makes the listener for new messages start listening again.
     */
    @Override
    public void onResume() {
        super.onResume();
        currentMessages = 0;

        mListenManager.startListening();
    }

    /**
     * Method called when fragment is no longer visible.
     * Stops the new messages listener and records the most recent message timestamp.
     */
    @Override
    public void onStop() {
        super.onStop();
        currentMessages = 0;

        String latestMessage = mListenManager.stopListening();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        // Save the most recent message timestamp
        prefs.edit().putString(
                getString(R.string.keys_prefs_time_stamp),
                latestMessage)
                .apply();
    }


    /**
     * listener method for the send button in the chat fragment
     *
     * @param theButton the button that this listener method is attached to
     */
    private void sendMessage(final View theButton) {
        JSONObject messageJson = new JSONObject();
        String msg = ((EditText) getView().findViewById(R.id.chatInput))
                .getText().toString();

        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);
            messageJson.put(getString(R.string.keys_json_message), msg);
            messageJson.put(getString(R.string.keys_json_chat_id), chatID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(this::handleError)
                .build().execute();
    }

    /**
     * Method to handle errors that occur when sending a message
     *
     * @param msg the string signifying the error message
     */
    private void handleError(final String msg) {
        Log.e("CHAT ERROR!!!", msg.toString());
    }

    /**
     * Method that parses the string returned from the backend and clears the input box
     * once the user hits the send button
     *
     * @param result the string retrieved once the backend call was made
     */
    private void endOfSendMsgTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);

            if (res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {

                ((EditText) getView().findViewById(R.id.chatInput))
                        .setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to handle errors that occur when removing a user
     *
     * @param e the string signifying the error message
     */
    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    /**
     * Method that parses through the JSON object retrieved from the call to the backend
     * and goes through the list of messages in the particular chat, it then determines who sent
     * the message and assigns a unique text bubble color to that user and displays it on the screen
     *
     * @param messages the JSON object retrieved from the call to the backend
     */
    private void publishProgress(JSONObject messages) {
        final String[] msgs;

        if (messages.has(getString(R.string.keys_json_messages))) {
            try {

                JSONArray jMessages = messages.getJSONArray(getString(R.string.keys_json_messages));

                msgs = new String[jMessages.length()];

                for (int i = 0; i < jMessages.length(); i++) {
                    JSONObject msg = jMessages.getJSONObject(i);
                    String username = msg.get(getString(R.string.keys_json_username)).toString();
                    //chatUsername = username;
                    String userMessage = msg.get(getString(R.string.keys_json_message)).toString();
                    msgs[i] = username + ":" + userMessage;
                    allUsernames.add(username);
                    if (!(userColors.containsKey(username))) {
                        Random r = new Random();
                        userColors.put(username, MESSAGE_COLORS[r.nextInt(MESSAGE_COLORS.length)]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            getActivity().runOnUiThread(() -> {
                mOutputTextView = getView().findViewById(R.id.chatOutput);

                if (msgs.length != 0 && currentMessages != msgs.length) {
                    mFragment.removeAllViews();
                    currentMessages = msgs.length;
                    for (int i = 0; i < msgs.length; i++) {
                        String[] sendUsername = msgs[i].split(":");

                        //check to see if the user that's logged in was the person who sent
                        //the message, if so assign a default green color to their text bubble and
                        //display it on the right side else, give the text bubble a random color
                        //and display it on the left side of the screen
                        if (mUsername.equals(sendUsername[0])) {
                            Button b = new Button(getActivity());
                            b.setText(msgs[i]);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(10, 10, 10, 10);
                            params.weight = 1.0f;
                            params.gravity = Gravity.RIGHT;
                            b.setLayoutParams(params);
                            b.setTextColor(Color.parseColor("#ffffff"));
                            b.setBackgroundResource(R.drawable.sent_messagee_box);
                            b.setPadding(8, 8, 8, 8);
                            mFragment.addView(b);
                        } else {
                            Button b = new Button(getActivity());
                            b.setText(msgs[i]);
                            Drawable mDrawable = getContext().getResources().getDrawable(R.drawable.message_box, null);
                            mDrawable.setColorFilter(getResources().getColor(userColors.get(sendUsername[0]), null), PorterDuff.Mode.MULTIPLY);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(10, 10, 10, 10);
                            b.setLayoutParams(params);
                            b.setTextColor(Color.parseColor("#ffffff"));
                            b.setBackground(mDrawable);
                            b.setPadding(8, 8, 8, 8);
                            mFragment.addView(b);

                        }

                    }
                }
            });
        }
    }
}
