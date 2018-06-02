package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.messengerapp.model.ListenManager;
import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartChatFragment extends Fragment {


    private String mUsername;
    private String mSendUrl;
    private EditText mOutputTextView;
    private ListenManager mListenManager;
    private int currentMessages = 0;
    private int mUserchatID;
    private int chatId;
    EditText usernames;
    EditText chatName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_start_chat, container, false);
        usernames = (EditText) v.findViewById(R.id.enterUsernames);
        chatName = (EditText) v.findViewById(R.id.chatName);
        v.findViewById(R.id.createChat).setOnClickListener(this::sendMessage);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            usernames.setText(bundle.getString("user"));
            usernames.setEnabled(false);
        }

        return v;
    }

    private void sendMessage(View view) {
        String newChatName = (String) chatName.getText().toString();

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_post_add_new_chat))
                .build();

        JSONObject msg = new JSONObject();
        try {
            if(chatName.equals("")) {
                msg.put("nameOfChat", "Default ChatName");
            }
            else {
                msg.put("nameOfChat", newChatName);
            }
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }
        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
           .onPostExecute(this::addPeopleToChatroom)
           .build().execute();

    }

    private void addPeopleToChatroom(String result) {

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        String usersToAdd = (String) usernames.getText().toString() + "," + mUsername;
        usersToAdd = usersToAdd.replaceAll("\\s","");
        usernames.setText("");
        chatName.setText("");
        JSONObject resultsJSON = null;
        JSONObject resultsJSON1 = null;

        try {
            resultsJSON = new JSONObject(result);
            String chatId=  resultsJSON.getString("messages");
            resultsJSON1 = new JSONObject(chatId);
            String chatId2=  resultsJSON1.getString("chatid");
            addMembersToChat(usersToAdd,chatId2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void addMembersToChat(String usernames, String chatId2) {
        //get usernames from text box

        String users[] = usernames.split(",");
        // add user mUsername;

        for(int i = 0; i < users.length; i++) {
            Log.i("USERNAME STRING",users[i]);

            JSONObject msg = new JSONObject();
            try {
                //Set chat name here
                msg.put("username", users[i]);
                msg.put("chatId", chatId2);
            } catch (JSONException e) {
                Log.wtf("JSON EXCEPTION", e.toString());
            }
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_post_add_user_to_chat))
                    .build();
            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPostExecute(this::temp)
                    .build().execute();
        }


        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("CHAT_ID",chatId2);
        startActivity(intent);

//        SharedPreferences prefs =
//                getActivity().getSharedPreferences(
//                        getString(R.string.keys_shared_prefs),
//                        Context.MODE_PRIVATE);
//        final SharedPreferences.Editor editor = prefs.edit();
//
//        editor.putString("chatid", chatId2);
//        editor.apply();

//        Intent intent = new Intent(getActivity(), ChatActivity.class);
//        intent.putExtra("CHAT_ID",mChatMap.get(b.getText()));



    }

    private void temp(String s) {
        Log.i("Add user to chatid",s);
    }

}
