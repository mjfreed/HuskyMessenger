package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tcss450.uw.edu.messengerapp.model.PullService;
import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeatherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherFragment extends Fragment {

    private static final String TAG = "MainActivity";

    private Button mResultsButton;
    private Button mStartButton;
    private Button mStopButton;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    //Fragment constructor
    public WeatherFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WeatherFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherFragment newInstance(String param1, String param2) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //Creates fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    //Loads saved weather from database.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_weather, container, false);
        Button currentLocation = (Button) v.findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(this::loadCurrent);
        Button searchButton = (Button) v.findViewById(R.id.searchButton);
        //searchButton.setOnClickListener(this::loadSearch);
        loadSavedWeather();
        return v;
    }

    //Loads saved weather for user.
    private void loadSavedWeather() {
        JSONObject messageJson = new JSONObject();
        try {

            SharedPreferences sharedPreferences =
                    getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);

            messageJson.put("username", sharedPreferences.getString("username",""));
            Log.e("long",messageJson.toString());
            new SendPostAsyncTask.Builder("http://group3-messenger-backend.herokuapp.com/getWeatherLocations", messageJson).onCancelled(this::handleError).onPostExecute(this::loadSavedWeather).build().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Add buttons for each saved location.
    private void loadSavedWeather(String s) {
        try {
            JSONObject messageJson = new JSONObject(s);
            JSONArray msgs = messageJson.getJSONArray("data");
            for (int i = 0; i < msgs.length(); i++) {
                JSONObject msg = msgs.getJSONObject(i);
                String city = msg.getString("city");
                if(!city.equals("undefined") ) {
                    String long1 = msg.getString("long");
                    String lat1 = msg.getString("lat");
                    String out = city + long1 + "  " + lat1;
                    Log.e("long",out);
                    Button b = new Button(getActivity());
                    b.setTextColor(Color.parseColor("#ffffff"));
                    b.setText(city); //Get chat name here!
                    Drawable mDrawable = getContext().getResources().getDrawable(R.drawable.start_chat_box,null);
                    b.setBackgroundResource(R.drawable.start_chat_box);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(5,5,5,5);
                    b.setLayoutParams(params);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences sharedPreferences =
                                    getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                                            Context.MODE_PRIVATE);
                            final SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("CurrentLocation", false);
                            editor.putString("LatInfo", lat1);
                            editor.putString("LongInfo", long1);
                            editor.apply();
                            Intent intent = new Intent(getActivity(), MyLocationsActivity.class);
                            startActivity(intent);
                        }
                    });
                    LinearLayout  scrollView = (LinearLayout ) getView().findViewById(R.id.saveWeather);
                    scrollView.addView(b);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    //Loads the zip code search.
    private void loadSearch(View view) {
        JSONObject messageJson = new JSONObject();
        TextView zipSearch = (TextView) getView().findViewById(R.id.zipSearch);
        Log.e("ZIP",zipSearch.getText().toString());
         try {
             messageJson.put("zipcode", zipSearch.getText().toString());
             String mSendUrl = new Uri.Builder()
                     .scheme("https")
                     .appendPath(getString(R.string.ep_base_url))
                     .appendPath("getWeatherZip")
                     .build()
                     .toString();
             new SendPostAsyncTask.Builder("http://group3-messenger-backend.herokuapp.com/", messageJson).onCancelled(this::handleError).onPostExecute(this::loadSearchHelper).build().execute();

         } catch (JSONException e) {
             e.printStackTrace();
         }
    }


    //Handles api error.
    private void handleError(final String msg) {
        Log.e("CHAT ERROR!!!", msg.toString());
    }

    //Helper to load zip search.
    private void loadSearchHelper(String s) {
        try {
            JSONObject res = new JSONObject(s);
            String longInfo = res.getString("long").toString();
            String latInfo = res.getString("lat").toString();
            SharedPreferences sharedPreferences =
                    getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("CurrentLocation", false);
            editor.putString("LatInfo", latInfo);
            editor.putString("LongInfo", longInfo);
            editor.apply();
            Intent intent = new Intent(getActivity(), MyLocationsActivity.class);
            startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Loads current location data.
    private void loadCurrent(View view) {
        SharedPreferences sharedPreferences =
                getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("CurrentLocation", true);
        //editor.putString("chatid", "1"); //defeault chatid
        editor.apply();

        //Start the service to wait for messages from database.
        Intent intent = new Intent(getActivity(), MyLocationsActivity.class);
        startActivity(intent);

    }


    //Starts listening.
    private void startListener(final View theButton) {
        //PullService.startServiceAlarm(getContext(), true);
        mStartButton.setEnabled(false);
    }

    //Stops listening.
    private void stopListener(final View theButton) {
        //PullService.stopServiceAlarm(getContext());
        mStartButton.setEnabled(true);
    }

}
