package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
 * Fragment that acts as a Verification Page for the app.
 *
 * Mostly handles the UI for email verification after registration.
 *
 * @author Marshall Freed
 * @version 5/31/2018
 */
public class VerifyFragment extends Fragment {

    private OnVerifyFragmentInteractionListener mListener;
    private String mEmail;


    public VerifyFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is starting. Stores an email string that is passed to it
     * in a field.
     * @param savedInstanceState contains data most recently supplied if activity reactivated
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEmail = getArguments().getString("args");
        }

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
        View v = inflater.inflate(R.layout.fragment_verify, container, false);

        final TextView TEXT = v.findViewById(R.id.verifyDoWhatTextView);
        final ImageView IMAGE = v.findViewById(R.id.verifyThumbsUp);
        final Button IGETITB = (Button) v.findViewById(R.id.verifyIGetItButton);
        final Button DOWHATB = (Button) v.findViewById(R.id.doWhatButton);

        Button b = (Button) v.findViewById(R.id.verifyButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onVerifyButtonClicked(view);
            }
        });

        DOWHATB.setVisibility(Button.VISIBLE);

        DOWHATB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TEXT.setVisibility(TextView.VISIBLE);
                IMAGE.setVisibility(ImageView.GONE);
                DOWHATB.setVisibility(Button.GONE);
                IGETITB.setVisibility(Button.VISIBLE);
            }
        });

        IGETITB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TEXT.setVisibility(TextView.GONE);
                IMAGE.setVisibility(ImageView.VISIBLE);
                IGETITB.setVisibility(Button.GONE);
                DOWHATB.setVisibility(Button.VISIBLE);
            }
        });

        return v;
    }

    /**
     * Called when the "verify" button is clicked so that it might call back
     * to the activity to launch an AsyncTask
     * @param view
     */
    public void onVerifyButtonClicked(View view) {
        boolean isEmpty = isFieldEmpty();
        if (!isEmpty) {
            String code = getCode();
            mListener.onVerifyButtonInteraction(mEmail, code);
        }
    }

    /**
     * Helper method to check if the EditText field is empty
     * @return whether it is or is not empty
     */
    public boolean isFieldEmpty() {
        boolean isEmpty = false;
        EditText edit = getView().findViewById(R.id.verifyEditText);
        String text = edit.getText().toString();
        if (text.trim().length() == 0) {
            isEmpty = true;
            edit.setError("Field cannot be empty");
        }

        return isEmpty;
    }

    /**
     * Returns the code that the user typed into the EditText
     * @return the code
     */
    public String getCode() {
        EditText edit = getView().findViewById(R.id.verifyEditText);
        String code = edit.getText().toString();

        return code;
    }

    /**
     * Called before the app launches an AsyncTask to disable the views that the user can
     * interact with and show a progress bar.
     */
    public void handleOnPre() {
        Button b = getView().findViewById(R.id.verifyButton);
        b.setEnabled(false);

        ProgressBar progBar = getView().findViewById(R.id.verifyProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);
    }

    /**
     * Called after the app launches an AsyncTask and encounters an error to re-enable
     * the views that the user can interact with and hide the progress bar.
     */
    public void handleOnError() {
        Button b = getView().findViewById(R.id.verifyButton);
        b.setEnabled(true);

        ProgressBar progBar = getView().findViewById(R.id.verifyProgressBar);
        progBar.setVisibility(ProgressBar.GONE);
    }

    /**
     * Displays a toast if the AsyncTask that attempted to verify the user's credentials returns
     * false. Toast contains details as to why the verification did not work. Also sets an error on the
     * EditText field.
     * @param err message about why verification was unsuccessful
     */
    public void setError(String err) {
        //Register unsuccessful for reason: err. Try again.
        Toast.makeText(getActivity(), "Verify unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();

        ((EditText) getView().findViewById(R.id.verifyEditText))
                .setError("Verification Unsuccessful");
    }

    /**
     * Called when a fragment is first attached to its context.
     * @param context Activity fragment is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VerifyFragment.OnVerifyFragmentInteractionListener) {
            mListener = (VerifyFragment.OnVerifyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerifyFragmentInteractionListener");
        }
    }

    /**
     * Interface that contains callback methods for the Activity to do some heavy lifting
     * when buttons on this fragment are pressed.
     */
    public interface OnVerifyFragmentInteractionListener {
        void onVerifyButtonInteraction(String email, String code);
    }

}
