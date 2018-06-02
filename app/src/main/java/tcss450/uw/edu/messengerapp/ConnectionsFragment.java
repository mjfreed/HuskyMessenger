package tcss450.uw.edu.messengerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tcss450.uw.edu.messengerapp.model.MyRecyclerViewAdapter;
import tcss450.uw.edu.messengerapp.utils.ListenManager;

/**
 * Fragment inside of the app that acts as a page to interact with connections.
 *
 * Page gives the ability to search for users of the app based off of their first
 * and last name, email address or username. Also gives the ability for a user to add
 * and delete contacts and start a chat with them.
 *
 * @author Marshall Freed
 * @version 5/12/2018
 */
public class ConnectionsFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        MyRecyclerViewAdapter.ItemClickListener {

    //fields used when searching for users
    private String mUsername;
    private String mSearchBy;

    //listen manager for contact request updates
    private ListenManager mListenerManager;

    //Data structures to store users that aren't you
    private ArrayList<String> mRequests;
    private ArrayList<String> mVerified;
    private ArrayList<String> mPending;

    //Views for all three contact lists
    private RecyclerView mRequestList;
    private RecyclerView mVerifiedList;
    private RecyclerView mPendingList;

    //Adapters needed for recycler views
    private MyRecyclerViewAdapter mRecyclerAdapter;
    private MyRecyclerViewAdapter mVerifiedRecyclerAdapter;
    private MyRecyclerViewAdapter mPendingAdapter;

    //Listener for the interface
    private OnConnectionsInteractionListener mInteractionListener;

    public ConnectionsFragment() {
        // Required empty public constructor
    }

    /**
     * Method called when this fragment is created. Sets up UI elements
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connections, container, false);

        Spinner spinner = (Spinner) v.findViewById(R.id.connectionsSearchSpinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.connections_search_filter,
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        ImageButton b = (ImageButton) v.findViewById(R.id.connectionsSearchButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchButtonClick();
            }
        });


        return v;
    }

    /**
     * Method called when this fragment comes into view.
     * Creates the list for friend requests as well as sets up the listener to listen
     * for new friend requests.
     */
    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        mRequests = new ArrayList<>();
        mVerified = new ArrayList<>();
        mPending = new ArrayList<>();

        getContacts();
        getPending();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mRequestList = getView().findViewById(R.id.connectionsRequestsRecycler);
        mRequestList.setLayoutManager(layoutManager);

        mRecyclerAdapter = new MyRecyclerViewAdapter(getActivity(), mRequests);
        mRecyclerAdapter.setClickListener(this::onItemClickRequests);
        mRequestList.setAdapter(mRecyclerAdapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRequestList.getContext(),
                        layoutManager.getOrientation());
        mRequestList.addItemDecoration(dividerItemDecoration);

        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_requests))
                .appendQueryParameter("username", mUsername)
                .build();

        mListenerManager = new ListenManager.Builder(retrieveRequests.toString(), this::publishRequests)
                .setExceptionHandler(this::handleError)
                .setDelay(5000)
                .build();

    }

    /**
     * Method called when fragment is brought back into view.
     * Makes the listener for new contact requests start listening again.
     */
    @Override
    public void onResume() {
        super.onResume();
        mListenerManager.startListening();
    }

    /**
     * Method called when fragment is no longer visible.
     * Stops the new contact request listener.
     */
    @Override
    public void onStop() {
        super.onStop();
        String latestMessage = mListenerManager.stopListening();
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);

        prefs.edit().putString(getString(R.string.keys_prefs_time_stamp), latestMessage).apply();
    }

    /**
     * Method called when the fragment has been associated with the activity.
     * @param context the activity that the fragment is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConnectionsInteractionListener) {
            mInteractionListener = (OnConnectionsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionsInteractionListener");
        }
    }

    /**
     * Callback method to be invoked when an item in this view has been selected.
     * This callback is invoked only when the newly selected position is different from
     * the previously selected position or if there was no selected item.
     * @param adapterView The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param i The position of the view in the adapter
     * @param l The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String choice = (String) adapterView.getAdapter().getItem(i);
        if (choice.equals("Username")) {
            mSearchBy = "username";
        } else if (choice.equals("First Name")) {
            mSearchBy = "firstname";
        } else if (choice.equals("Last Name")) {
            mSearchBy = "lastname";
        } else {
            mSearchBy = "email";
        }

    }

    /**
     * Callback method to be invoked when the selection disappears from this view.
     * The selection can disappear for instance when touch is activated or
     * when the adapter becomes empty.
     * @param adapterView The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        mSearchBy = "username";
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     * @param  v The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     */
    @Override
    public void onItemClick(View v, int position) {
        String str = mVerifiedRecyclerAdapter.getItem(position);
        str = str.substring(0, str.indexOf(" "));

    }

    /**
     * Method called when an item in the Contacts adapter view has been clicked.
     * Displays an alert dialog specific to Contacts
     * @param v The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter
     */
    public void onItemClickContacts(View v, int position) {
        String connections = "connections";
        String str = mVerifiedRecyclerAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "What do you want to do with " + username + "?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "What do you want to do with Haylee Ryan?";
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Pick an Action").setMessage(msg)
                .setNegativeButton(getString(R.string.connections_delete_connection),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                builder.setTitle("Confirm Deletion")
                                        .setMessage("Are you sure you want to delete your connection with " +
                                                username + "?")
                                        .setPositiveButton(getString(R.string.searchConnections_Yes),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        final String frag = "connections";
                                                        mInteractionListener.onConnectionsDeleteInteractionListener(username, frag);
                                                    }
                                                })
                                        .setNegativeButton(getString(R.string.searchConnections_Nah),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                        .setIcon(R.drawable.alert)
                                        .show();
                            }
                        })
                .setPositiveButton(getString(R.string.connections_start_chat_dialog_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mInteractionListener.onConnectionsStartChatListener(username);
                            }
                        })
                .setNeutralButton("Nevermind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        if (ryan) {
            builder.setIcon(R.drawable.ryan);
        } else {
            builder.setIcon(R.drawable.contactrequest);
        }
        builder.show();

    }

    /**
     * Method called when an item in the Requests adapter view has been clicked.
     * Displays an alert dialog specific to Requests
     * @param v The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter
     */
    public void onItemClickRequests(View v, int position) {
        String connections = "connections";
        String str = mRecyclerAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "Would you like to accept " + username
                + "'s connection request?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "Would you like to accept Haylee Ryan's connection request?";
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Resolve Request").setMessage(msg)
                .setPositiveButton(getString(R.string.connections_decline_request_diaglog_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean accept = false;
                        mInteractionListener.onRequestInteractionListener(username, accept, connections);
                    }
                })
                .setNegativeButton(getString(R.string.connections_accept_request_dialog_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                boolean accept = true;
                                mInteractionListener.onRequestInteractionListener(username, accept, connections);
                            }
                        });
        if (ryan) {
            builder.setIcon(R.drawable.ryan);
        } else {
            builder.setIcon(R.drawable.contactrequest);
        }
        builder.show();

    }

    /**
     * Method called when an item in the Contacts Pending adapter view has been clicked.
     * Displays an alert dialog specific to Pending contacts
     * @param v The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter
     */
    public void onItemClickPending(View v, int position) {
        String str = mPendingAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "Would you like to cancel your connection request to " +
                username + "?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "Would you like to canel your connection request to Haylee Ryan?";
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog
                .Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Remove Pending Request")
                .setMessage(msg)
                .setNegativeButton(getString(R.string.searchConnections_remove_pending),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                builder.setTitle("Confirm Cancellation")
                                        .setMessage("Are you sure you want to cancel your connection request to " +
                                                username + "?")
                                        .setPositiveButton(getString(R.string.searchConnections_Yes),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        boolean accept = false;
                                                        final String frag = "connectionsPending";
                                                        mInteractionListener.onRequestInteractionListener(username, accept, frag);
                                                    }
                                                })
                                        .setNegativeButton(getString(R.string.searchConnections_Nah),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                        .setIcon(R.drawable.alert)
                                        .show();
                            }
                        })
                .setPositiveButton(getString(R.string.searchConnections_nevermind),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
        if (ryan) {
            builder.setIcon(R.drawable.ryan);
        } else {
            builder.setIcon(R.drawable.delete);
        }
        builder.show();
    }

    /**
     * Method to called when the search button is clicked.
     * Checks constraints before a search for a username is allowed.
     */
    public void onSearchButtonClick() {
        EditText search = (EditText) getView().findViewById(R.id.connectionsSearchEditText);
        String searchString = search.getText().toString();

        if (searchString.trim().length() == 0) {
            search.setError("Field cannot be empty");
        } else {
            mInteractionListener
                    .onSearchInteractionListener(mSearchBy, searchString,
                            mVerified, mRequests, mPending);
        }

    }

    /**
     * Method that is called by onStart to send a PostAsyncTask to an endpoint in the
     * webservice to retrieve currently connected contacts.
     */
    public void getContacts() {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        String username = prefs.getString(getString(R.string.keys_prefs_username), "");

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_contacts))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", username);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleRequestOnPre)
                .onPostExecute(this::handleContactsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Method that is called by onStart to send a PostAsyncTask to an endpoint in the
     * webservice to retrieve pending contact requests.
     */
    public void getPending() {
        SharedPreferences prefs = getActivity().
                getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        String username = prefs.getString(getString(R.string.keys_prefs_username), "");

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_pending_requests))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", username);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleRequestOnPre)
                .onPostExecute(this::handlePendingOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    /**
     * Method to be called from activity after PostAsyncTask has finished. Method parses
     * through JSON that is returned and loads it correctly into the contacts recycler view.
     * @param result String representation of JSON object that contains contact information
     */
    public void handleContactsOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_connections_a))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_connections_a));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String username = req.get(getString(R.string.keys_json_username))
                                    .toString();
                            String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                                    .toString();
                            String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                                    .toString();
                            String str = username + " (" + lastName +", " + firstName + ")";
                            mVerified.add(str);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                if (resultsJSON.has(getString(R.string.keys_json_connections_b))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_connections_b));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String username = req.get(getString(R.string.keys_json_username))
                                    .toString();
                            String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                                    .toString();
                            String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                                    .toString();
                            String str = username + " (" + lastName +", " + firstName + ")";

                            if (!mVerified.contains(str)) {
                                mVerified.add(str);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mVerified.sort(String::compareToIgnoreCase);

                LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());

                mVerifiedList = getView().findViewById(R.id.connectionsVerifiedRecycler);
                mVerifiedList.setLayoutManager(layoutManager2);

                mVerifiedRecyclerAdapter = new MyRecyclerViewAdapter(getActivity(), mVerified);
                mVerifiedRecyclerAdapter.setClickListener(this::onItemClickContacts);
                mVerifiedList.setAdapter(mVerifiedRecyclerAdapter);

                DividerItemDecoration dividerItemDecoration2 =
                        new DividerItemDecoration(mVerifiedList.getContext(),
                                layoutManager2.getOrientation());
                mVerifiedList.addItemDecoration(dividerItemDecoration2);

                ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

                enableDisableViewGroup(vg, true);

            } else {
                Log.e("IT DOESN'T WORK", "WHY NOT");
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    /**
     * Method to be called from activity after PostAsyncTask has finished. Method parses
     * through JSON that is returned and loads it correctly into the pending requests recycler view.
     * @param result String representation of JSON object that contains pending contact information
     */
    public void handlePendingOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_requests))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_requests));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String username = req.get(getString(R.string.keys_json_username))
                                    .toString();
                            String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                                    .toString();
                            String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                                    .toString();
                            String str = username + " (" + lastName + ", " + firstName + ")";

                            if (!mPending.contains(str)) {
                                mPending.add(str);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                mPending.sort(String::compareToIgnoreCase);

                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

                mPendingList = getView().findViewById(R.id.connectionsPendingRecycler);
                mPendingList.setLayoutManager(layoutManager);

                mPendingAdapter = new MyRecyclerViewAdapter(getActivity(), mPending);
                mPendingAdapter.setClickListener(this::onItemClickPending);
                mPendingList.setAdapter(mPendingAdapter);

                DividerItemDecoration dividerItemDecoration =
                        new DividerItemDecoration(mPendingList.getContext(),
                                layoutManager.getOrientation());
                mVerifiedList.addItemDecoration(dividerItemDecoration);

                if (!mPending.isEmpty()) {
                    TextView tv = getView().findViewById(R.id.connectionsPendingHeaderTextView);
                    tv.setVisibility(TextView.VISIBLE);

                    View v = getView().findViewById(R.id.connectionsDividerThree);
                    v.setVisibility(View.VISIBLE);
                }

                ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

                enableDisableViewGroup(vg, true);

            } else {
                Log.wtf("IT'S NOT WORKING (PENDING ON POST)", "WHY NOT");
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    /**
     * Method to be called from activity if there was an error. Re-enables the views that were
     * disabled during the execution of the task.
     * @param result String representation of JSON object that contains contact information
     */
    public void handleErrorsInTask(String result) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);

        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Method to be called when there is an error during the listening for new contact requests.
     * @param e Exception given by the listener
     */
    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    /**
     * Method called when listener obtains new information to put into the Requests recycler view.
     * Parses the JSON and assigns to the Requests recycler view in proper format.
     * @param requests JSON object returned by listener containing new request info
     */
    private void publishRequests(JSONObject requests) {
        final String[] reqs;
        if (requests.has(getString(R.string.keys_json_requests))) {
            try {
                JSONArray jReqs = requests.getJSONArray(getString(R.string.keys_json_requests));
                reqs = new String[jReqs.length()];
                for (int i = 0; i < jReqs.length(); i++) {
                    JSONObject req = jReqs.getJSONObject(i);
                    String username = req.get(getString(R.string.keys_json_username))
                            .toString();
                    String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                            .toString();
                    String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                            .toString();
                    String str = username + " (" + lastName + ", " +
                            firstName + ") has requested you as a connection!";

                    if (!mRequests.contains(str)) {
                        mRequests.add(str);

                        TextView tv = getView().findViewById(R.id.connectionsRequestsHeaderTextView);

                        mRequests.sort(String::compareToIgnoreCase);
                        getActivity().runOnUiThread(() -> {
                            mRecyclerAdapter.notifyDataSetChanged();
                            if (tv.getVisibility() == View.GONE) {
                                tv.setVisibility(TextView.VISIBLE);

                                View v = getView().findViewById(R.id.connectionsDividerTwo);
                                v.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * Method to be called when the user either accepts or declines a contact request.
     * Disables all children of the main layout.
     */
    public void handleRequestOnPre() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, false);

    }

    /**
     * Method to be called after the user makes a decision about a connection request. Depending on
     * whether they accepted or declined, requesting user gets placed into current contacts. Request
     * gets deleted from request list. All views become enabled again.
     * @param success success of the AsyncTask
     * @param username username of request user accepted or declined
     * @param accept whether user accepted the request or not
     */
    public void handleRequestOnPost(boolean success, String username, boolean accept) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

        if (success) {
            for (int i = 0; i < mRequests.size(); i++) {
                String str = mRequests.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    String[] arr = str.split(" ");
                    String verified = arr[0] + " " + arr[1] + " " + arr[2];

                    if (accept) {
                        mVerified.add(verified);
                        mVerified.sort(String::compareToIgnoreCase);
                        mVerifiedRecyclerAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(),
                                "You added " + username + " as a contact!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(),
                                "You declined " + username + "'s connection request",
                                Toast.LENGTH_LONG).show();
                    }
                    mRequests.remove(i);
                    mRecyclerAdapter.notifyDataSetChanged();
                    break;
                }
            }

            if (mRequests.isEmpty()) {
                TextView tv = getView().findViewById(R.id.connectionsRequestsHeaderTextView);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.connectionsDividerTwo);
                v.setVisibility(View.GONE);
            }

        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);

    }

    /**
     * Method to be called after the user revokes a pending request. Request
     * gets deleted from pending list. All views become enabled again.
     * @param success success of the AsyncTask
     * @param username username of pending contact request
     */
    public void handlePendingOnPost(boolean success, String username) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

        if (success) {
            for (int i = 0; i < mPending.size(); i++) {
                String str = mPending.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    mPending.remove(i);
                    mPendingAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(),
                            "You rescinded your connection request to " + username,
                            Toast.LENGTH_LONG).show();
                    break;
                }
            }

            if (mPending.isEmpty()) {
                TextView tv = getView().findViewById(R.id.connectionsPendingHeaderTextView);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.connectionsDividerThree);
                v.setVisibility(View.GONE);
            }
        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);
    }

    /**
     * Method to be called after the user deletes a contact. Contact will be
     * deleted from the contact list. All views become enabled again.
     * @param success success of the AsyncTask
     * @param username username of pending contact request
     */
    public void handleContactDeletedOnPost(boolean success, String username) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

        if (success) {
            for (int i = 0; i < mVerified.size(); i++) {
                String str = mVerified.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    mVerified.remove(i);
                    mVerifiedRecyclerAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "You deleted " + username + " as a contact",
                            Toast.LENGTH_LONG).show();
                    break;
                }
            }
        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);
    }

    /**
     * Method to be called before the searching AsyncTask is executed.
     * Disables all children views of the parent layout.
     */
    public void handleSearchOnPost() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);
    }

    /**
     * Method to be called when the search AsyncTask returns no results.
     * Creates a toast telling you just that.
     */
    public void handleEmptySearch() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);

        Toast.makeText(getActivity(), "Search yielded no results", Toast.LENGTH_LONG).show();
    }

    /**
     * Method to be called when the search AsyncTask returns yourself.
     * Creates a toast telling you just that.
     */
    public void handleSearchForSelf() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);

        Toast.makeText(getActivity(), "I think you just searched for yourself...",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Method disables all children of the viewgroup that is passed in.
     * @param vg viewgroup to enable or disable
     * @param enabled boolean to decide whether to enable or disable
     */
    private void enableDisableViewGroup(ViewGroup vg, boolean enabled) {
        int children = vg.getChildCount();
        for (int i = 0; i < children; i++) {
            View v = vg.getChildAt(i);
            v.setEnabled(enabled);
            if (v instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) v, enabled);
            }
        }

        if (enabled) {
            ProgressBar pg = getView().findViewById(R.id.connectionsProgressBar);
            pg.setVisibility(ProgressBar.GONE);
        } else {
            ProgressBar pg = getView().findViewById(R.id.connectionsProgressBar);
            pg.setVisibility(ProgressBar.VISIBLE);
        }
    }

    /**
     * Method called if there is an error within an AsyncTask
     * @param err error that occurred
     */
    public void setError(String err) {
        Toast.makeText(getActivity(), "Request unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Method called if there is an error that occurs within an AsyncTask.
     * Re-Enables the viewgroup
     * @param e error that occurred
     */
    public void handleOnError(String e) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

        Toast.makeText(getActivity(), "Request unsuccessful for reason: " + e,
                Toast.LENGTH_SHORT).show();

        enableDisableViewGroup(vg, true);
    }

    /**
     * Interface that contains methods that pertain to button clicks.
     */
    public interface OnConnectionsInteractionListener {
        void onConnectionsDeleteInteractionListener(String username, String fragment);
        void onConnectionsStartChatListener(String username);
        void onRequestInteractionListener(String username, boolean accept, String fragment);
        void onSearchInteractionListener(String searchBy, String searchString,
                                         ArrayList<String> contacts, ArrayList<String> requests,
                                         ArrayList<String> pending);
    }

}
