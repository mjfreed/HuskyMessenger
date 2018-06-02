package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import tcss450.uw.edu.messengerapp.model.Credentials;

/**
 * Fragment that acts as a Registration Page for the app.
 *
 * Mostly handles the UI for registration.
 *
 * @author Marshall Freed
 * @version 5/31/2018
 */
public class RegisterFragment extends Fragment {

    private OnRegisterFragmentInteractionListener myListener;

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
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        Button b = (Button) v.findViewById(R.id.submitButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmitButtonClicked(view);
            }
        });

        return v;
    }

    /**
     * Called when the "submit" button in the fragment is clicked. Method will collect
     * all information entered in the EditTexts and pass them to helper methods to check
     * for client-side constraints before calling back to the Activity to launch an AsyncTask
     * @param view the button that was clicked
     */
    public void onSubmitButtonClicked(View view) {
        EditText edits[] = new EditText[6];

        edits[0] = getView().findViewById(R.id.register_fname);
        edits[1] = getView().findViewById(R.id.register_lname);
        edits[2] = getView().findViewById(R.id.register_nickname);
        edits[3] = getView().findViewById(R.id.register_email);
        edits[4] = getView().findViewById(R.id.register_password);
        edits[5] = getView().findViewById(R.id.register_confirm_password);

        boolean fieldIsEmpty = isFieldEmpty(edits);
        boolean passwordsMatch = passwordsMatch();
        boolean isEmail = isEmail();
        boolean meetsConstraints = passMeetsConstraints();

        if (!fieldIsEmpty && passwordsMatch && isEmail && meetsConstraints) {
            String fname = getFname();
            String lname = getLname();
            String nickname = getNickname();
            String email = getEmail();
            Editable pass = getPassword();

            tcss450.uw.edu.messengerapp.model.Credentials credentials =
                    new Credentials.Builder(nickname, pass)
                            .addFirstName(fname)
                            .addLastName(lname)
                            .addEmail(email)
                            .build();

            myListener.onSubmitButtonInteraction(credentials);
        }
    }

    /**
     * Helper method to check if at least one EditText in an array is empty.
     * @param edits array of EditTexts
     * @return if at least one of them is empty
     */
    public boolean isFieldEmpty(EditText edits[]) {
        boolean fieldIsEmpty = false;

        for (int i = 0; i < edits.length; i++) {
            EditText field = edits[i];
            String text = field.getText().toString();
            if (text.trim().length() == 0) {
                fieldIsEmpty = true;
                field.setError("Field cannot be empty");
            }
        }

        return fieldIsEmpty;
    }

    /**
     * Helper method to check if the both passwords that the user entered are exactly matching
     * or not.
     * @return whether they are matching
     */
    public boolean passwordsMatch() {
        boolean isMatching;

        EditText passwordOne = getView().findViewById(R.id.register_password);
        EditText passwordTwo = getView().findViewById(R.id.register_confirm_password);
        String passwordOneString = passwordOne.getText().toString();
        String passwordTwoString = passwordTwo.getText().toString();
        if (passwordOneString.equals(passwordTwoString)) {
            isMatching = true;
        } else {
            isMatching = false;
            passwordOne.setError("Passwords must be matching");
            passwordTwo.setError("Passwords must be matching");
        }

        return isMatching;
    }

    /**
     * Helper method to check if the email they typed in is actually an email address (in theory).
     * Only checks for an "@" symbol.
     * @return whether it's an email address or not
     */
    public boolean isEmail() {
        boolean isEmail = false;

        EditText email = getView().findViewById(R.id.register_email);
        String emailString = email.getText().toString();

        if (emailString.contains("@")) {
            isEmail = true;
        } else {
            email.setError("Must be an email address");
        }

        return isEmail;
    }

    /**
     * Helper method to check if the password they typed in meets our constraints.
     * Passwords must have at least 5 characters, an uppercase character and at least one digit
     * @return whether the password meets the constraints or not
     */
    public boolean passMeetsConstraints() {
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasLength = false;

        boolean meetsConstraints = false;

        EditText password = getView().findViewById(R.id.register_password);
        String passwordString = password.getText().toString();

        if (passwordString.trim().length() < 5) {
            password.setError("Password must be at least 5 characters");
        } else {
            hasLength = true;
        }

        for (int i = 0; i < passwordString.length(); i++) {
            if (Character.isUpperCase(passwordString.charAt(i))) {
                hasUpper = true;
            }
        }

        if (!hasUpper) {
            password.setError("Password must contain upper case letter");
        }

        for (int i = 0; i < passwordString.length(); i++) {
            if (Character.isDigit(passwordString.charAt(i))) {
                hasDigit = true;
            }
        }

        if (!hasDigit) {
            password.setError("Password must contain one digit");
        }

        if (hasDigit && hasLength && hasUpper) {
            meetsConstraints = true;
        }

        return meetsConstraints;
    }

    /**
     * Returns the "nickname" (username) that the user entered for their account
     * @return the username
     */
    public String getNickname() {
        EditText nickName = getView().findViewById(R.id.register_nickname);

        return nickName.getText().toString();
    }

    /**
     * Returns the password that the user entered for their account
     * @return the password
     */
    public Editable getPassword() {
        EditText password = getView().findViewById(R.id.register_password);

        return password.getEditableText();

    }

    /**
     * Returns the first name of the user that they entered for their account
     * @return the name
     */
    public String getFname() {
        EditText fname = getView().findViewById(R.id.register_fname);

        return fname.getText().toString();
    }

    /**
     * Returns the last name of the user that they entered for their account
     * @return the name
     */
    public String getLname() {
        EditText lname = getView().findViewById(R.id.register_lname);

        return lname.getText().toString();
    }

    /**
     * Returns the email that the user entered for their account
     * @return the email
     */
    public String getEmail() {
        EditText email = getView().findViewById(R.id.register_email);

        return email.getText().toString();
    }

    /**
     * Called when a fragment is first attached to its context.
     * @param context Activity fragment is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterFragmentInteractionListener) {
            myListener = (OnRegisterFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRegisterFragmentInteractionListener");
        }
    }

    /**
     * Called before the app launches an AsyncTask to disable the views that the user can
     * interact with and show a progress bar.
     */
    public void handleOnPre() {
        ProgressBar progBar = getView().findViewById(R.id.registerProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);

        Button b = getView().findViewById(R.id.submitButton);
        b.setEnabled(false);
    }

    /**
     * Called after the app launches an AsyncTask and encounters an error to re-enable
     * the views that the user can interact with and hide the progress bar.
     */
    public void handleOnError() {
        ProgressBar progBar = getView().findViewById(R.id.registerProgressBar);
        progBar.setVisibility(ProgressBar.GONE);

        Button b = getView().findViewById(R.id.submitButton);
        b.setEnabled(true);
    }

    /**
     * Displays a toast if the AsyncTask that attempted to register the user returns
     * false. Toast contains details as to why the registration did not work. Also sets an error on the
     * EditText field.
     * @param err message about why verification was unsuccessful
     */
    public void setError(String err) {
        Log.wtf("REGISTER FRAGMENT", "In set error, string is: " + err);
        //Register unsuccessful for reason: err. Try again.
        Toast.makeText(getActivity(), "Register unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();

        ((TextView) getView().findViewById(R.id.register_fname))
                .setError("Login Unsuccessful");
    }

    /**
     * Interface that contains callback methods for the Activity to do some heavy lifting
     * when buttons on this fragment are pressed.
     */
    public interface OnRegisterFragmentInteractionListener {
        void onSubmitButtonInteraction(tcss450.uw.edu.messengerapp.model.Credentials credentials);
    }

}
