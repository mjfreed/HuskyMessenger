package tcss450.uw.edu.messengerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import tcss450.uw.edu.messengerapp.model.Credentials;

/**
 * Fragment that acts as a Login Page for the app.
 *
 * Mostly handles the UI for login.
 *
 * @author Marshall Freed
 * @version 5/31/2018
 */
public class LoginFragment extends Fragment {

    private OnLoginFragmentInteractionListener myListener;
    private boolean mArgumentsRead;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Sets onClickListeners to all buttons in view
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
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mArgumentsRead = false;

        Button b = (Button) v.findViewById(R.id.loginButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginButtonClicked(view);
            }
        });

        b = (Button) v.findViewById(R.id.registerButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegisterButtonClicked(view);
            }
        });

        b = (Button) v.findViewById(R.id.loginForgotPasswordButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetDialog();
            }
        });


        return v;
    }

    /**
     * When the "Forgot Password" button is clicked, this method gets called to show an alert
     * dialog that allows the user to enter their email address and launch the forgotten password
     * process
     */
    public void showResetDialog() {
        EditText et = new EditText(getActivity());
        et.setHint("Enter email address");
        et.setTextColor(getResources().getColor(android.R.color.white));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(lp);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                android.R.style.Theme_Material_Dialog_Alert);
        builder.setView(et);
        builder.setTitle("Change Password")
                .setMessage("To change your password, we need to verify your email address")
                .setNegativeButton("Done", null)
                .setPositiveButton("Nevermind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isNotEmail = checkFieldIsEmail(et);
                        boolean isEmpty = checkFieldIsEmpty(et);

                        if (!(isEmpty || isNotEmail)) {
                            SharedPreferences prefs =
                                    getActivity().getSharedPreferences
                                            (getString(R.string.keys_shared_prefs),
                                                    Context.MODE_PRIVATE);
                            prefs.edit().putString("changePassEmail",
                                    et.getText().toString()).apply();
                            dialog.dismiss();
                            myListener.onChangePasswordInteraction();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * Helper method to check if an EditText field is empty
     * @param et the EditText to be checked
     * @return whether it is or is not empty
     */
    public boolean checkFieldIsEmpty(EditText et) {
        boolean isEmpty;
        String email = et.getText().toString();
        if (email.trim().length() > 0) {
            isEmpty = false;
        } else {
            isEmpty = true;
            et.setError("Field cannot be empty");
        }

        return isEmpty;
    }

    /**
     * Helper method to check if what is entered into the EditText is an email address or not
     * @param et the EditText to be checked
     * @return if the text is an email or not
     */
    public boolean checkFieldIsEmail(EditText et) {
        boolean isNotEmail;
        String email = et.getText().toString();
        if (email.contains("@")) {
            isNotEmail = false;
        } else {
            isNotEmail = true;
            et.setError("Must be an email address");
        }

        return isNotEmail;
    }

    /**
     * Helper method to check if the EditText for the username specifically is empty.
     * @return if the field is empty or not
     */
    public boolean usernameIsEmpty() {
        boolean whatever;
        EditText username = getView().findViewById(R.id.usernameEditText);
        String usernameString = username.getText().toString();
        if (usernameString.trim().length() > 0) {
            whatever = false;
        } else {
            whatever = true;
            username.setError("Username cannot be empty");
        }
        return whatever;
    }

    /**
     * Helper method to check if the EditText for the password specifically is empty.
     * @return if the field is empty or not
     */
    public boolean passwordIsEmpty() {
        boolean fine;
        EditText password = getView().findViewById(R.id.passwordEditText);
        String passwordString = password.getText().toString();
        if (passwordString.trim().length() > 0) {
            fine = false;
        } else {
            fine = true;
            password.setError("Password cannot be empty");
        }

        return fine;
    }

    /**
     * Returns the password that the user has typed in.
     * @return the password
     */
    public Editable getPassword() {
        EditText password = getView().findViewById(R.id.passwordEditText);
        Editable passwordString = password.getEditableText();
        return passwordString;
    }

    /**
     * Returns the username that the user has typed in.
     * @return the username
     */
    public String getUsername() {
        EditText username = getView().findViewById(R.id.usernameEditText);
        String usernameString = username.getText().toString();
        return usernameString;
    }

    /**
     * Called when the "login" button in the fragment is clicked. Method will check for
     * client side constraints before any AsyncTasks get launched.
     * @param view the Button that was clicked
     */
    public void onLoginButtonClicked(View view) {
        boolean userIsEmpty = usernameIsEmpty();
        boolean passIsEmpty = passwordIsEmpty();
        String username = getUsername();
        Editable password = getPassword();

        if (!(userIsEmpty || passIsEmpty)) {
            tcss450.uw.edu.messengerapp.model.Credentials credentials =
                    new Credentials.Builder(username, password).build();
            myListener.onLoginButtonInteraction(credentials);
        }
    }

    /**
     * Method responsible for disabling any views that can be clicked by the user during
     * the execution of an AsyncTask.
     */
    public void handleOnPre() {
        Button b = getView().findViewById(R.id.loginButton);
        b.setEnabled(false);

        b = getView().findViewById(R.id.registerButton);
        b.setEnabled(false);

        b = getView().findViewById(R.id.loginForgotPasswordButton);
        b.setEnabled(false);

        ProgressBar progBar = getView().findViewById(R.id.loginProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);
    }

    /**
     * Method responsible for enabling all the views that can be clicked by the user after an
     * error has occurred in the AsyncTask. Sometimes used to enable again when the AsyncTask is
     * done and executed successfully.
     */
    public void handleOnError() {
        ProgressBar progBar = getView().findViewById(R.id.loginProgressBar);
        progBar.setVisibility(ProgressBar.GONE);

        Button b = getView().findViewById(R.id.loginButton);
        b.setEnabled(true);

        b = getView().findViewById(R.id.registerButton);
        b.setEnabled(true);

        b = getView().findViewById(R.id.loginForgotPasswordButton);
        b.setEnabled(true);
    }

    /**
     * Displays a toast if the AsyncTask that attempted to verify the user's credentials returns
     * false. Toast contains details as to why the login did not work. Also sets an error on the
     * EditText field.
     * @param err message about why login was unsuccessful
     */
    public void setError(String err) {
        //Log in unsuccessful for reason: err. Try again.
        Toast.makeText(getActivity(), "Log in unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();

        ((TextView) getView().findViewById(R.id.usernameEditText))
                .setError("Login Unsuccessful");
    }

    /**
     * Method called when the email that the user typed into the EditText when they have forgotten
     * their password is not associated with an account. (Says it's not used but it is)
     */
    public void showEmailAlert() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert)
        .setTitle("Error")
        .setMessage("Email is not associated with an account")
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        })
        .setIcon(R.drawable.alert);
        builder.show();
    }

    /**
     * Called when the register button is clicked. Calls back to the activity to launch the
     * register fragment.
     * @param view the button that was clicked
     */
    public void onRegisterButtonClicked(View view) {
        myListener.onRegisterButtonInteraction();
    }

    /**
     * Called when a fragment is first attached to its context.
     * @param context Activity fragment is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            myListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    /**
     * Interface that contains callback methods for the Activity to do some heavy lifting
     * when buttons on this fragment are pressed.
     */
    public interface OnLoginFragmentInteractionListener {
        void onRegisterButtonInteraction();
        void onLoginButtonInteraction(tcss450.uw.edu.messengerapp.model.Credentials credentials);
        void onChangePasswordInteraction();
    }

}
