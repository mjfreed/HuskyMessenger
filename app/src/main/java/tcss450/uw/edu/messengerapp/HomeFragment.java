package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author Jon Anderson
 * @version 5/31/2018
 *
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    /**Default fields added by Android Studio**/
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    /**Default string for home page buttons if user is not part of 5+ chats**/
    private final static String BUTTON_EMPTY = "Click to create a chat.";

    /**Listener for get requests in this fragment**/
    private OnFragmentInteractionListener mListener;

    /**Storage of this user's username**/
    private String mUsername;

    /**Collections for home page buttons, recent chats, and what those chats say**/
    private ArrayList<Message> mRecentMessageInfo = new ArrayList<>();
    private Button[] mButtons = new Button[5];
    private Message[] mMesseges = new Message[5];

    public HomeFragment() {
        // Required empty public constructor
    }

    /*
     * Default factory method
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Default factory method
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
    * Open the new fragment and initializes the buttons with recent chat information
    */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences prefs = getActivity().
                getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");

        TextView tv = v.findViewById(R.id.homeWelcome);

        Button b = (Button) v.findViewById(R.id.homeFragmentSearchButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSearchInteractionListener();
            }
        });

        tv.setText("Welcome, " + mUsername + "!"); //sets text for welcome message

        //Update recent chats for this user
        initButtons(v);
        getRecentChats();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Sets on click listeners for recent chat buttons
     * @param v the home fragment
     */
    private void initButtons(View v) {
        mButtons[0] = v.findViewById(R.id.chat0);
        mButtons[0].setOnClickListener(this);
        mButtons[1] = v.findViewById(R.id.chat1);
        mButtons[1].setOnClickListener(this);
        mButtons[2] = v.findViewById(R.id.chat2);
        mButtons[2].setOnClickListener(this);
        mButtons[3] = v.findViewById(R.id.chat3);
        mButtons[3].setOnClickListener(this);
        mButtons[4] = v.findViewById(R.id.chat4);
        mButtons[4].setOnClickListener(this);
    }

    /**
     * Reads through our list of chats and orders them such that
     * the most recent can be put onto the buttons
     */
    private void initRecentMessages() {
        Collections.sort(mRecentMessageInfo);
        for (int i = 0; i < Math.min(mMesseges.length, mRecentMessageInfo.size()); i++) {
            if (!mRecentMessageInfo.isEmpty()) {
                mMesseges[i] = mRecentMessageInfo.remove(0);
                mButtons[i].setText(mMesseges[i].mAuthor + ": " + mMesseges[i].mContent);
                mRecentMessageInfo.add(mMesseges[i]);
            } else {
                mMesseges[i] = null;
                mButtons[i].setText(BUTTON_EMPTY);
            }
        }

    }

    /**
     * DB call to get all the chats this user is a part of
     */
    private void getRecentChats() {

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
                .onPreExecute(this::getRecentChatsOnPre)
                .onPostExecute(this::getRecentChatsOnPost)
                .onCancelled(this::handleError)
                .build().execute();
    }

    private void getRecentChatsOnPre() {
    }

    /**
     * Takes the chats this user is a part of and makes a DB call to get the
     * most recent messages from those chats
     * @param result chats
     */
    private void getRecentChatsOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");


            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_chats))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_chats));
                        for (int i = 0; i < jReqs.length(); i++) {

                            String time = "1970-01-01 00:00:00";

                            int chatid = jReqs.getJSONObject(i).getInt("chatid");

                            //build the web service URL
                            Uri uri = new Uri.Builder()
                                    .scheme("https")
                                    .appendPath(getString(R.string.ep_base_url))
                                    .appendPath(getString(R.string.ep_post_get_messages))
                                    .build();

                            JSONObject msg = new JSONObject();
                            try {
                                msg.put("chatId", chatid);
                                msg.put("after", time);
                            } catch (JSONException e) {
                                Log.wtf("JSON EXCEPTION", e.toString());
                            }

                            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                                    .onPreExecute(this::handleGetMessagesOnPre)
                                    .onPostExecute(this::handleGetMessagesOnPost)
                                    .onCancelled(this::handleError)
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

    private void handleGetMessagesOnPre() {
    }

    /**
     * Read through the list of chats and see if the most recent is from
     * somebody else and came recently.
     * @param result of finding chats
     */
    public void handleGetMessagesOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_messages))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_messages));
                        String messageTime = jReqs.getJSONObject(jReqs.length()-1).getString("timestamp");
                        String chatid = jReqs.getJSONObject(jReqs.length()-1).getString("chatid");
                        String message = jReqs.getJSONObject(jReqs.length()-1).getString("message");
                        String username = jReqs.getJSONObject(jReqs.length()-1).getString("username");
                        mRecentMessageInfo.add(new Message(messageTime, chatid, message, username));
                        initRecentMessages();
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
     * Gracefully handle any errors on AsyncTask
     * @param e the error message
     */
    private void handleError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

    /**
     * Attached my listener to this fragment
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Detaches my listener from this fragment.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Helper method for attaching chat opening to buttons
     * @param buttonNum
     */
    private void setButtonListener(int buttonNum) {
        if (mMesseges[buttonNum] != null) {
            mListener.onOpenChat(Integer.valueOf(mMesseges[buttonNum].mId));
        } else {
            mListener.onOpenChat(-1);
        }
    }

    /**
     * Sets the on click listener for whichever button is sent to it
     * @param theButton to set
     */
    @Override
    public void onClick(View theButton) {
        if (mListener != null) {
            switch (theButton.getId()) {
                case R.id.chat0:
                    setButtonListener(0);
                    break;
                case R.id.chat1:
                    setButtonListener(1);
                    break;
                case R.id.chat2:
                    setButtonListener(2);
                    break;
                case R.id.chat3:
                    setButtonListener(3);
                    break;
                case R.id.chat4:
                    setButtonListener(4);
                    break;
                default:
                    Log.wtf("", "Didn't expect to see me...");
            }
        }
    }

    /**
     * Inner class to store messages with the ability to sort by timestamp
     */
    private class Message implements Comparable<Message> {

        GregorianCalendar mMessageTime;
        String mId;
        String mContent;
        String mAuthor;

        public Message(String theTimestamp, String theID, String theContent, String theAuthor) {
            int year = Integer.valueOf(theTimestamp.substring(0,4));
            int month = Integer.valueOf(theTimestamp.substring(5, 7));
            int day = Integer.valueOf(theTimestamp.substring(8, 10));
            int hour = Integer.valueOf(theTimestamp.substring(14,16));
            int minute = Integer.valueOf(theTimestamp.substring(17,19));
            int second = Integer.valueOf(theTimestamp.substring(20, 22));
            mMessageTime = new GregorianCalendar(year, month, day, hour, minute, second);
            mId = theID;
            mContent = theContent;
            mAuthor = theAuthor;
        }


        @Override
        public int compareTo(@NonNull Message o) {
            return (this.mMessageTime.compareTo(o.mMessageTime) * -1);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onOpenChat(int theChatId);
        void onSearchInteractionListener();
    }


}
