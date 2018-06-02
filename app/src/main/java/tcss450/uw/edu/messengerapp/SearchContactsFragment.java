package tcss450.uw.edu.messengerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tcss450.uw.edu.messengerapp.model.MyRecyclerViewAdapter;

/**
 * Fragment that acts as a Search Contacts page for the app
 *
 * Mostly handles the UI for search results
 *
 * @author Marshall Freed
 * @version 5/31/2018
 */
public class SearchContactsFragment extends Fragment {

    private OnSearchFragmentInteractionListener mListener;

    /*Lists to organize users of app in relation to user*/
    private ArrayList<String> mContacts;
    private ArrayList<String> mRequests;
    private ArrayList<String> mPending;
    private ArrayList<String> mNewPeople;

    /*Views for the lists above*/
    private RecyclerView mNewPeopleRecycler;
    private RecyclerView mContactsRecycler;
    private RecyclerView mRequestsRecycler;
    private RecyclerView mPendingRecycler;

    /*Adapters for the views above*/
    private MyRecyclerViewAdapter mNewPeopleAdapter;
    private MyRecyclerViewAdapter mContactsAdapter;
    private MyRecyclerViewAdapter mRequestsAdapter;
    private MyRecyclerViewAdapter mPendingAdapter;

    public SearchContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is starting. Stores multiple lists as fields.
     * @param savedInstanceState contains data most recently supplied if activity reactivated
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mContacts = bundle.getStringArrayList("contacts");
            mRequests = bundle.getStringArrayList("requests");
            mPending = bundle.getStringArrayList("pending");
            mNewPeople = bundle.getStringArrayList("newPeople");
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_contacts, container, false);
    }

    /**
     * Called when the Fragment is visible to the user.
     * Method calls helper methods to set up the Views for all of the search results
     */
    @Override
    public void onStart() {
        super.onStart();

        setUpNewPeople();
        setUpContacts();
        setUpRequests();
        setUpPending();

    }

    /**
     * Called when a fragment is first attached to its context.
     * @param context Activity fragment is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchFragmentInteractionListener) {
            mListener = (OnSearchFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSearchFragmentInteractionListener");
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Callback method to be invoked when an item in the mNewPeopleAdapter has been clicked.
     * @param v The view within the AdapterView that was clicked
     *          (this will be a view provided by the adapter)
     * @param position he position of the view in the adapter.
     */
    private void onItemClickNewPeople(View v, int position) {
        String str = mNewPeopleAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "Would you like to add " + username + " as a connection?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "Would you like to add Haylee Ryan as a connection?";
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Add as Connection").setMessage(msg)
                .setNegativeButton(getString(R.string.searchConnections_Yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mListener.onSearchAddInteraction(username);
                            }
                        })
                .setPositiveButton(getString(R.string.searchConnections_Nah),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
        if (ryan) {
            builder.setIcon(R.drawable.ryan);
        } else {
            builder.setIcon(R.drawable.contact);
        }
        builder.show();
    }

    /**
     * Callback method to be invoked when an item in the mRequestsAdapter has been clicked.
     * @param v The view within the AdapterView that was clicked
     *          (this will be a view provided by the adapter)
     * @param position he position of the view in the adapter.
     */
    private void onItemClickRequests(View v, int position) {
        String str = mRequestsAdapter.getItem(position);
        String fragment = "search";
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
                        mListener.onSearchRequestInteraction(username, accept, fragment);
                    }
                })
                .setNegativeButton(getString(R.string.connections_accept_request_dialog_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                boolean accept = true;
                                mListener.onSearchRequestInteraction(username, accept, fragment);
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
     * Callback method to be invoked when an item in the mPendingAdapter has been clicked.
     * @param v The view within the AdapterView that was clicked
     *          (this will be a view provided by the adapter)
     * @param position he position of the view in the adapter.
     */
    private void onItemClickPending(View v, int position) {
        String str = mPendingAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "Would you like to cancel your connection request to " +
                username + "?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "Would you like to cancel your connection request to Haylee Ryan?";
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
                                .setNegativeButton(getString(R.string.searchConnections_Yes),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        boolean accept = false;
                                        final String frag = "searchPending";
                                        mListener.onSearchRequestInteraction(username, accept, frag);
                                    }
                                })
                                .setPositiveButton(getString(R.string.searchConnections_Nah),
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
     * Callback method to be invoked when an item in the mContactsAdapter has been clicked.
     * @param v The view within the AdapterView that was clicked
     *          (this will be a view provided by the adapter)
     * @param position he position of the view in the adapter.
     */
    private void onItemClickContacts(View v, int position) {
        String connections = "connections";
        String str = mContactsAdapter.getItem(position);
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
                                                        final String frag = "search";
                                                        mListener.onSearchDeleteContactListener(username, frag);
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
                                mListener.onSearchStartChatListener(username);
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
     * Helper method that takes the list of "new people", or users that are not related to the user yet
     * but are using this app, and sets up the recycler view for them
     */
    private void setUpNewPeople() {
        if (mNewPeople.isEmpty()) {
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mNewPeopleRecycler = getView().findViewById(R.id.searchConnectionsNewPeopleRecycler);
        mNewPeopleRecycler.setLayoutManager(layoutManager);

        mNewPeopleAdapter = new MyRecyclerViewAdapter(getActivity(), mNewPeople);
        mNewPeopleAdapter.setClickListener(this::onItemClickNewPeople);
        mNewPeopleRecycler.setAdapter(mNewPeopleAdapter);

    }

    /**
     * Helper method that takes the list of current contacts and sets up the recycler view for them
     */
    private void setUpContacts() {
        if (mContacts.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsCurrentConnectionsHeader);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider2);
            v.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mContactsRecycler = getView().findViewById(R.id.searchConnectionsConnectionsRecycler);
        mContactsRecycler.setLayoutManager(layoutManager);

        mContactsAdapter = new MyRecyclerViewAdapter(getActivity(), mContacts);
        mContactsAdapter.setClickListener(this::onItemClickContacts);
        mContactsRecycler.setAdapter(mContactsAdapter);

    }

    /**
     * Helper method that takes the list of users that have requested the user as a connection
     * and sets up the recycler view for them
     */
    private void setUpRequests() {
        if (mRequests.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsRequestHeaderText);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider3);
            v.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mRequestsRecycler = getView().findViewById(R.id.searchConnectionsRequestsRecycler);
        mRequestsRecycler.setLayoutManager(layoutManager);

        mRequestsAdapter = new MyRecyclerViewAdapter(getActivity(), mRequests);
        mRequestsAdapter.setClickListener(this::onItemClickRequests);
        mRequestsRecycler.setAdapter(mRequestsAdapter);

    }

    /**
     * Helper method that takes the list of users that the user has requested as a connection
     * and sets up the recycler view for them
     */
    private void setUpPending() {
        if (mPending.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsPendingHeaderText);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider4);
            v.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mPendingRecycler = getView().findViewById(R.id.searchConnectionsPendingRecycler);
        mPendingRecycler.setLayoutManager(layoutManager);

        mPendingAdapter = new MyRecyclerViewAdapter(getActivity(), mPending);
        mPendingAdapter.setClickListener(this::onItemClickPending);
        mPendingRecycler.setAdapter(mPendingAdapter);

    }

    /**
     * Called before the app launches an AsyncTask to disable the views that the user can
     * interact with and show a progress bar.
     */
    public void handleRequestOnPre() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);
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
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

        if (success) {
            for (int i = 0; i < mRequests.size(); i++) {
                String str = mRequests.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    String[] arr = str.split(" ");
                    String verified = arr[0] + " " + arr[1] + " " + arr[2];

                    if (accept) {
                        mContacts.add(verified);
                        mContacts.sort(String::compareToIgnoreCase);
                        mContactsAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(),
                                "You added " + username + " as a contact!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        mNewPeople.add(verified);
                        mNewPeople.sort(String::compareToIgnoreCase);
                        mNewPeopleAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(),
                                "You declined " + username + "'s connection request",
                                Toast.LENGTH_LONG).show();
                    }
                    mRequests.remove(i);
                    mRequestsAdapter.notifyDataSetChanged();
                    break;
                }
            }

            if (mRequests.isEmpty()) {
                TextView tv = getView().findViewById(R.id.searchConnectionsRequestHeaderText);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.searchConnectionsDivider3);
                v.setVisibility(View.GONE);
            }
        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);

    }

    /**
     * Method to be called after the user deletes a pending request they've made. Pending request
     * gets deleted from pending list. All views become enabled again.
     * @param success whether the AsyncTask was successful
     * @param username username of request the user wants to revoke
     */
    public void handlePendingOnPost(boolean success, String username) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

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
                TextView tv = getView().findViewById(R.id.searchConnectionsPendingHeaderText);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.searchConnectionsDivider4);
                v.setVisibility(View.GONE);
            }
        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);
    }

    /**
     * Called after the AsyncTask has finished when the user has requested a new connection with a
     * contact from the search result. Method will put the pending request in the appropriate list
     * and remove from the new people list
     * @param username username of contact the user wants to add
     */
    public void handleAddOnPost(String username) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

        for (int i = 0; i < mNewPeople.size(); i++) {
            String str = mNewPeople.get(i);
            String subStr = str.substring(0, str.indexOf(" "));

            if (username.equals(subStr)) {
                String[] arr = str.split(" ");
                String verified = arr[0] + " " + arr[1] + " " + arr[2];

                mPending.add(verified);
                mPending.sort(String::compareToIgnoreCase);

                Toast.makeText(getActivity(),
                        "You requested " + username + " as a contact!",
                        Toast.LENGTH_LONG).show();

                TextView tv = getView().findViewById(R.id.searchConnectionsPendingHeaderText);
                View v = getView().findViewById(R.id.searchConnectionsDivider4);

                if (tv.getVisibility() == View.GONE) {
                    tv.setVisibility(TextView.VISIBLE);
                    v.setVisibility(View.VISIBLE);
                }

                mPendingAdapter.notifyDataSetChanged();

                mNewPeople.remove(i);
                mNewPeopleAdapter.notifyDataSetChanged();
                break;
            }
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
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

        if (success) {
            for (int i = 0; i < mContacts.size(); i++) {
                String str = mContacts.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    String[] arr = str.split(" ");
                    String verified = arr[0] + " " + arr[1] + " " + arr[2];
                    mNewPeople.add(verified);
                    mNewPeople.sort(String::compareToIgnoreCase);
                    mNewPeopleAdapter.notifyDataSetChanged();

                    mContacts.remove(i);
                    mContactsAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(),
                            "You deleted " + username + " as a contact",
                            Toast.LENGTH_LONG).show();

                    if (mContacts.isEmpty()) {
                        TextView tv = getView().findViewById(R.id.searchConnectionsCurrentConnectionsHeader);
                        View v = getView().findViewById(R.id.searchConnectionsDivider2);
                        tv.setVisibility(TextView.GONE);
                        v.setVisibility(View.GONE);
                    }
                    break;
                }
            }

        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);
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
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

        Toast.makeText(getActivity(), "Request unsuccessful for reason: " + e,
                Toast.LENGTH_SHORT).show();

        enableDisableViewGroup(vg, true);
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
            ProgressBar pg = getView().findViewById(R.id.searchConnectionsProgressBar);
            pg.setVisibility(ProgressBar.GONE);
        } else {
            ProgressBar pg = getView().findViewById(R.id.searchConnectionsProgressBar);
            pg.setVisibility(ProgressBar.VISIBLE);
        }
    }

    /**
     * Interface that contains methods that pertain to button clicks.
     */
    public interface OnSearchFragmentInteractionListener {
        void onSearchRequestInteraction(String username, boolean accept, String fragment);
        void onSearchAddInteraction(String username);
        void onSearchStartChatListener(String username);
        void onSearchDeleteContactListener(String username, String fragment);
    }


}
