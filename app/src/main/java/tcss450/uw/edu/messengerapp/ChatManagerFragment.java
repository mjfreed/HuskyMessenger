package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import tcss450.uw.edu.messengerapp.utils.ListenManager;
import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatManagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 * Fragment that allows the user to send and display messages.
 *
 * @author Mahad Fahiye
 * @version 5/31/2018
 */
public class ChatManagerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    //Default fields added by Android Studio
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    //Data structures to store the chatrooms' chatIDs and a map of the chatrooms with their chatIDs
    private ArrayList<String> chatIdList = new ArrayList<String>();
    private HashMap<String, String> mChatMap = new HashMap<>();

    //Field for the layout of the ChatManagerFragment
    private LinearLayout mChatManagerLayout;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ChatManagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatManagerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatManagerFragment newInstance(String param1, String param2) {
        ChatManagerFragment fragment = new ChatManagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the Fragment is created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        //mChatManagerLayout = getView().findViewById(R.id.chatButtons);

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /*
     * Opens the new fragment and inflates it
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_manager, container, false);
        return rootView;
    }

    /**
     * Once the fragment is inflated it binds mChatManagerLayout to the chatButtons layout inside
     * this fragment and then proceeds to call the getAllChats method
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChatManagerLayout = (LinearLayout) view.findViewById(R.id.chatButtons);
        getAllChats();
    }

    /**
     * Method that retrieves all the chatrooms a user is in, it creates a JSON Object
     * containing the username of the person who is currently logged in and sends it to the backend
     */
    private void getAllChats() {
        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", prefs.getString(getString(R.string.keys_prefs_username), ""));
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }
        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_all_chats))
                .build();

        Log.e("CONTENT", retrieveRequests.toString());

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(retrieveRequests.toString(), msg)
                .onPostExecute(this::publishRequests)
                .onCancelled(this::handleError)
                .build().execute();


    }

    /**
     * Method to handle errors that occur when retrieving the chatrooms
     *
     * @param e the string signifying the error message
     */
    private void handleError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

    /**
     * Method that parses through the JSON object retrieved from the call to the backend
     * and goes through the list of chatrooms that the user is in, it then loads up the chatroom
     * names into buttons that are loaded up onto the screen
     *
     * @param result the JSON object retrieved from the call to the backend
     */
    public void publishRequests(String result) {
        try {
            JSONObject requests = new JSONObject(result);
            boolean success = requests.getBoolean("success");
            if (success) {
                final String[] reqs;
                if (requests.has("chats")) {
                    try {
                        JSONArray jReqs = requests.getJSONArray("chats");
                        Log.e("SIZE", "" + jReqs.length());
                        reqs = new String[jReqs.length()];
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String chatname = req.get(getString(R.string.keys_json_chatname))
                                    .toString();
                            String chatid = req.get(getString(R.string.keys_json_chatid))
                                    .toString();
                            Log.e("THE CHAT NAMES", chatname);
                            if (!(chatIdList.contains(chatid))) {
                                chatIdList.add(chatid);
                            }
                            if (!(mChatMap.containsKey(chatid))) {
                                mChatMap.put(chatid, chatname);
                            }
                        }

                        for (int i = 0; i < chatIdList.size(); i++) {
                            Button b = new Button(getActivity());
                            b.setTextColor(Color.parseColor("#ffffff"));
                            b.setText(mChatMap.get(chatIdList.get(i))); //Get chat name here!
                            Drawable mDrawable = getContext().getResources().getDrawable(R.drawable.start_chat_box, null);


                            b.setBackgroundResource(R.drawable.start_chat_box);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(5, 5, 5, 5);
                            b.setLayoutParams(params);
                            int finalI = i;
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("CHAT_ID", chatIdList.get(finalI));
                                    startActivity(intent);
                                }
                            });
                            mChatManagerLayout.addView(b);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
