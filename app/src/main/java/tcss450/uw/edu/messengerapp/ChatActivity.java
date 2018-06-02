package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.messengerapp.ChatFragment;
import tcss450.uw.edu.messengerapp.ChatManagerFragment;
import tcss450.uw.edu.messengerapp.ConnectionsFragment;
import tcss450.uw.edu.messengerapp.HomeFragment;
import tcss450.uw.edu.messengerapp.R;
import tcss450.uw.edu.messengerapp.WeatherFragment;

/**
 * Activity for the Chat portion of the app
 * <p>
 * Activity displays and loads up the appropriate ChatFragment once a chatroom is entered,
 * This activity also handles adding and removing users from a chat.
 *
 * @author Mahad Fahiye
 * @author Jon Anderson
 * @version 5/31/2018
 */

public class ChatActivity extends AppCompatActivity {

    /**
     * Storage of information needed for chats
     **/
    Bundle extras;
    String mUsername;
    String mChatId;

    /**
     * Called when the activity is starting. Handles loading the required information to start a
     * chat and processes adding/removing users
     *
     * @param savedInstanceState contains data most recently supplied if activity reactivated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        extras = getIntent().getExtras();
        String value = "1";
        if (extras != null) {
            value = extras.getString("CHAT_ID");
            mChatId = value;
            Log.e("VALUE", value);
        } else {
            SharedPreferences prefs = getSharedPreferences(
                    getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);
            if (!prefs.contains("chatid")) {
                throw new IllegalStateException("No chatid in prefs!");
            }
            String chatid;
            chatid = prefs.getString("chatid", "");
            Log.i("THE CHAT", "ID IS " + chatid);
        }

        if (savedInstanceState == null) {
            if (findViewById(R.id.chatContainer) != null) {
                loadFragment(new ChatFragment(value));
            }

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Who do you wan't to remove?");
        alertDialog.setMessage("Enter the Username");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        //alertDialog.setIcon(R.drawable.key);
    }

    /**
     * Gives the user the option to either add a user to this chat or
     * remove a user from this chat.
     *
     * @param item that was selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.addUserToChat:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChatActivity.this);
                alertDialog.setTitle("Who do you wan't to add?");
                alertDialog.setMessage("Enter the Username");

                final EditText input = new EditText(ChatActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                //alertDialog.setIcon(R.drawable.key);

                alertDialog.setPositiveButton("Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mUsername = input.getText().toString();
                                //removeFromChat();
                                addToChat();
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dialog.cancel();
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                break;
            case R.id.removeUserFromChat:
                //remove user menu
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(ChatActivity.this);
                alertDialog2.setTitle("Who do you wan't to remove?");
                alertDialog2.setMessage("Enter the Username");

                final EditText input2 = new EditText(ChatActivity.this);
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input2.setLayoutParams(lp2);
                alertDialog2.setView(input2);

                alertDialog2.setPositiveButton("Remove",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mUsername = input2.getText().toString();
                                removeFromChat();
                            }
                        });

                alertDialog2.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog2.show();
                break;
            default:
                Log.e("TAG", "Something wrong in Chat Menu");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is ran when the our chat toolbar menu is created, it inflates the toolbar menu
     *
     * @param menu the chat toolbar
     * @return a boolean true or false depending on whether it successfully inflated the Menu or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    /**
     * Loads whichever chat fragment you choose to send it
     *
     * @param theFragment to be loaded
     */
    public void loadFragment(Fragment theFragment) {
        theFragment.setArguments(extras);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chatContainer, theFragment);
        // Commit the transaction
        transaction.commit();
    }

    /**
     * Leaves back to home activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /**
     * Method to remove user from chat, this method creates a JSON object containing the username and
     * chatID, builds the link to our backend server and starts up the async task to send a post
     * request containing the username and chatID JSON object
     */
    public void removeFromChat() {
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        //mUsername = "test1";
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
            msg.put("chatId", mChatId);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }
        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("removeUserFromChat")
                .build();

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(retrieveRequests.toString(), msg)
                .onPostExecute(this::publishRequests)
                .onCancelled(this::handleError)
                .build().execute();


    }

    /**
     * Method to handle errors that occur when removing a user
     *
     * @param e the string signifying the error message
     */
    private void handleError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

    /**
     * Method called when user selects the menu option to remove a user from the chat.
     * Parses the JSON and removes the appropriate user if they exist in the chat.
     *
     * @param result JSON object returned by listener containing username and chatID
     */
    public void publishRequests(String result) {
        try {
            JSONObject requests = new JSONObject(result);
            boolean success = requests.getBoolean("success");
            //Upon success, show a toast confirming that the user has been removed from the chat.
            if (success) {
                Toast.makeText(this, "User has been successfully removed from the chat",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "You can only remove people who are in this Chat",
                        Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to add user to chat, this method creates a JSON object containing the username and
     * chatID, builds the link to our backend server and starts up the async task to send a post
     * request containing the username and chatID JSON object
     */
    public void addToChat() {
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        //mUsername = "test1";
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
            msg.put("chatId", mChatId);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }
        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("addUserToChat")
                .build();

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(retrieveRequests.toString(), msg)
                .onPostExecute(this::publishAddingRequests)
                .onCancelled(this::handleAddingError)
                .build().execute();
    }

    /**
     * Method to handle errors when adding a user
     *
     * @param e the string signifying the error message
     */
    private void handleAddingError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

    /**
     * Method called when user selects the menu option to add a user to the chat.
     * Parses the JSON and adds the user accordingly.
     *
     * @param result JSON object returned by listener containing username and chatID
     */
    public void publishAddingRequests(String result) {
        try {
            JSONObject requests = new JSONObject(result);
            boolean success = requests.getBoolean("success");
            if (success) {
                Toast.makeText(this, "User has been successfully added!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Please enter a valid username",
                        Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
