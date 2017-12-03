package rocks.massi.trollsgames.fragments;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import lombok.NoArgsConstructor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.async.LoginRegisterAsyncConnector;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.UserRegisteredEvent;
import rocks.massi.trollsgames.events.UserRegistrationFailedEvent;

@NoArgsConstructor
public class Register extends Fragment {

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserRegisteredEvent event) {
        Log.i(getClass().getName(), "Registered user " + event.getUser().toString());
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.register_verify_email, event.getUser().getEmail()), Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserRegistrationFailedEvent event) {
        Log.e(getClass().getName(), "Registration failed " + event.getError());
        if (getView() != null) {
            Snackbar.make(getView(), getString(R.string.register_verify_error, event.getError()), Snackbar.LENGTH_LONG).show();
            getView().findViewById(R.id.register_submit).setEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Set Typefaces
        TextView header = view.findViewById(R.id.f_register_header);
        header.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/Raleway-Regular.ttf"));

        // Set listener for Checkbox
        final CheckBox bggHandledCb = view.findViewById(R.id.cb_bggHandled);
        bggHandledCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditText editBggNick = view.findViewById(R.id.edit_bggNick);
                EditText editForumNick = view.findViewById(R.id.edit_forumNick);
                editBggNick.setEnabled(isChecked);

                if (!isChecked) {
                    editBggNick.setText(editForumNick.getText());
                }
            }
        });

        // Changes in forumnick edit should be reflected in bggnick if account is not bgg handled
        final EditText editForumNick = view.findViewById(R.id.edit_forumNick);
        editForumNick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!bggHandledCb.isChecked()) {
                    EditText editBggNick = view.findViewById(R.id.edit_bggNick);
                    editBggNick.setText(s);
                }
            }
        });

        // Create the listener for the button
        final Button submitButton = view.findViewById(R.id.register_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            private final EditText bggNick = view.findViewById(R.id.edit_bggNick);
            private final EditText forumNick = view.findViewById(R.id.edit_forumNick);
            private final EditText email = view.findViewById(R.id.edit_email);
            private final EditText password = view.findViewById(R.id.edit_password);

            private boolean checkFields() {
                // Fields can't be empty
                if (bggNick.getText().toString().isEmpty() ||
                        forumNick.getText().toString().isEmpty() ||
                        email.getText().toString().isEmpty() ||
                        password.getText().toString().isEmpty()) {
                    Snackbar.make(view, R.string.register_error_missing_fields, Snackbar.LENGTH_SHORT).show();
                    return false;
                }

                // Email must be valid
                if (!email.getText().toString().matches("^.+@.+$")) {
                    Snackbar.make(view, R.string.register_error_missing_email, Snackbar.LENGTH_SHORT).show();
                    return false;
                }

                // Passwords must match
                String verifyPassword = ((EditText) view.findViewById(R.id.edit_password_verify)).getText().toString();
                if (!verifyPassword.equals(password.getText().toString())) {
                    Snackbar.make(view, R.string.register_error_passwords_mismatch, Snackbar.LENGTH_SHORT).show();
                    return false;
                }

                // If not bgg handled, bggnick must be equal to forumnick
                if (!bggHandledCb.isChecked() && (!bggNick.getText().toString().equals(forumNick.getText().toString()))) {
                    Snackbar.make(view, R.string.register_error_generic, Snackbar.LENGTH_SHORT).show();
                }

                return true;
            }

            @Override
            public void onClick(View v) {
                if (checkFields()) {
                    User toBeRegistered = new User(bggNick.getText().toString(),
                            forumNick.getText().toString(),
                            email.getText().toString(),
                            password.getText().toString(),
                            bggHandledCb.isChecked());
                    submitButton.setEnabled(false);
                    submitButton.setText(R.string.register_submit_wait);
                    new LoginRegisterAsyncConnector(toBeRegistered, getActivity().getString(R.string.server)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        // View created!
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
