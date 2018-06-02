package tcss450.uw.edu.messengerapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment that acts as a Reset Password page for the app
 *
 * Mostly handles the UI for resetting password
 *
 * @author Marshall Freed
 * @version 5/31/2018
 */
public class ResetPassword extends Fragment {

    private OnResetPasswordFragmentInteractionListener mListener;


    public ResetPassword() {
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
        View v = inflater.inflate(R.layout.fragment_reset_password, container, false);
        Button b = (Button) v.findViewById(R.id.verifyChangePasswordButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText code = (EditText) v.findViewById(R.id.verifyChangePasswordCode);
                String input = code.getText().toString();
                if (input.trim().length() == 0) {
                    code.setError("Field cannot be empty");
                } else {
                    int inputCode = Integer.parseInt(input);
                    mListener.onVerifyResetButtonInteraction(inputCode);
                }
            }
        });

        return v;
    }

    /**
     * Called when a fragment is first attached to its context.
     * @param context Activity fragment is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ResetPassword.OnResetPasswordFragmentInteractionListener) {
            mListener = (ResetPassword.OnResetPasswordFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnResetPasswordFragmentInteractionListener");
        }
    }

    /**
     * Called before the app launches an AsyncTask when the user hits the
     * "verify" button to disable the views that the user can
     * interact with and show a progress bar.
     */
    public void handleVerifyOnPre() {
        Button b = getView().findViewById(R.id.verifyChangePasswordButton);
        b.setEnabled(false);

        EditText et = getView().findViewById(R.id.verifyChangePasswordCode);
        et.setEnabled(false);

        ImageView redx = getView().findViewById(R.id.redx);
        ImageView checkmark = getView().findViewById(R.id.checkmark);

        if (redx.getVisibility() == View.VISIBLE) {
            redx.setVisibility(ImageView.GONE);
        }

        if (checkmark.getVisibility() == View.VISIBLE) {
            checkmark.setVisibility(ImageView.GONE);
        }


        ProgressBar progBar = getView().findViewById(R.id.verifyResetProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);
    }

    /**
     * Called before the app launches an AsyncTask when the user hits the
     * "I'm Done" button to disable the views that the user can
     * interact with and show a progress bar.
     */
    public void handleResetOnPre() {
        EditText enter = getView().findViewById(R.id.changePasswordEditText);
        enter.setEnabled(false);

        EditText conf = getView().findViewById(R.id.confirmChangePasswordEditText);
        conf.setEnabled(false);

        Button b = getView().findViewById(R.id.resetPasswordButton);
        b.setEnabled(false);

        ProgressBar progBar = getView().findViewById(R.id.resetPasswordProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);
    }

    /**
     * Called after the AsyncTask is finished when the user's code has been verified.
     * This method applies/shows all of the correct views to the user based on whether
     * the code they entered was valid or not
     * @param success whether the code they entered was valid or not
     */
    public void handleVerifyOnPost(boolean success) {
        ProgressBar progBar = getView().findViewById(R.id.verifyResetProgressBar);
        progBar.setVisibility(ProgressBar.GONE);

        ImageView redx = getView().findViewById(R.id.redx);

        if (success) {
            if (redx.getVisibility() == View.VISIBLE) {
                redx.setVisibility(ImageView.GONE);
            }

            ImageView checkmark = getView().findViewById(R.id.checkmark);
            checkmark.setVisibility(ImageView.VISIBLE);

            TextView tv = getView().findViewById(R.id.resetAndConfirmTextView);
            tv.setVisibility(TextView.VISIBLE);

            EditText enter = getView().findViewById(R.id.changePasswordEditText);
            enter.setVisibility(EditText.VISIBLE);

            TextView instr = getView().findViewById(R.id.changePassInstructionsText);
            instr.setVisibility(TextView.VISIBLE);

            EditText confirm = getView().findViewById(R.id.confirmChangePasswordEditText);
            confirm.setVisibility(EditText.VISIBLE);

            Button b = getView().findViewById(R.id.resetPasswordButton);
            b.setVisibility(Button.VISIBLE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onResetButtonClick();
                }
            });
        } else {
            redx.setVisibility(ImageView.VISIBLE);

            EditText et = getView().findViewById(R.id.verifyChangePasswordCode);
            et.setEnabled(true);
            et.setError("Code was not matching");

            Button b = getView().findViewById(R.id.verifyChangePasswordButton);
            b.setEnabled(true);
        }
    }

    /**
     * Called after the AsyncTask has finished when the user has successfully reset their
     * password. The method shows an alert dialog that the user cannot click outside of telling
     * them that their password changed was a success.
     */
    public void handleResetOnPost() {
        ProgressBar progBar = getView().findViewById(R.id.resetPasswordProgressBar);
        progBar.setVisibility(ProgressBar.GONE);

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Password Changed")
                .setMessage("Your password has successfully been changed")
                .setPositiveButton("Great!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onGreatButtonInteraction();
                    }
                })
                .setIcon(R.drawable.checkicon);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Called after the app launches an AsyncTask and encounters an error to re-enable
     * the views that the user can interact with and hide the progress bar.
     */
    public void handleOnError() {
        EditText enter = getView().findViewById(R.id.changePasswordEditText);
        enter.setEnabled(true);

        EditText conf = getView().findViewById(R.id.confirmChangePasswordEditText);
        conf.setEnabled(true);

        Button b = getView().findViewById(R.id.resetPasswordButton);
        b.setEnabled(true);

        ProgressBar progBar = getView().findViewById(R.id.resetPasswordProgressBar);
        progBar.setVisibility(ProgressBar.GONE);

        Toast.makeText(getActivity(), "Something went wrong in the backend",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the user clicks the "I'm Done" button. Method calls other helper methods
     * to check client-side constraints before the app launches an AsyncTask from the activity
     */
    private void onResetButtonClick() {
        boolean meetsConstraints = passMeetsConstraints();
        boolean isNotEmpty = checkIsEmpty();
        boolean passwordsMatch = passwordsMatch();

        if (isNotEmpty && meetsConstraints && passwordsMatch) {
            EditText password = getView().findViewById(R.id.changePasswordEditText);
            Editable passwordString = password.getEditableText();
            mListener.onResetButtonInteraction(passwordString);
        }
    }

    /**
     * Helper method to check if at least one EditText between
     * the two password EditTexts are empty.
     * @return if at least one of them is empty
     */
    boolean checkIsEmpty() {
        boolean fieldOneIsEmpty = true;
        boolean fieldTwoIsEmpty = true;

        EditText password = getView().findViewById(R.id.changePasswordEditText);
        String passwordString = password.getText().toString();

        EditText confPassword = getView().findViewById(R.id.confirmChangePasswordEditText);
        String confPasswordString = password.getText().toString();

        if (passwordString.trim().length() == 0) {
            password.setError("Field cannot be empty");
        } else {
            fieldOneIsEmpty = false;
        }

        if (confPasswordString.trim().length() == 0) {
            confPassword.setError("Field cannot be empty");
        } else {
            fieldTwoIsEmpty = false;
        }

        if (!(fieldOneIsEmpty && fieldTwoIsEmpty)) {
            return true;
        }

        return false;
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

        EditText password = getView().findViewById(R.id.changePasswordEditText);
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
     * Helper method to check if the both passwords that the user entered are exactly matching
     * or not.
     * @return whether they are matching
     */
    public boolean passwordsMatch() {
        boolean isMatching;

        EditText passwordOne = getView().findViewById(R.id.changePasswordEditText);
        EditText passwordTwo = getView().findViewById(R.id.confirmChangePasswordEditText);
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
     * Interface that contains callback methods for the Activity to do some heavy lifting
     * when buttons on this fragment are pressed.
     */
    public interface OnResetPasswordFragmentInteractionListener {
        void onVerifyResetButtonInteraction(int code);
        void onResetButtonInteraction(Editable pass);
        void onGreatButtonInteraction();
    }

}
