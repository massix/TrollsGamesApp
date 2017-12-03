package rocks.massi.trollsgames.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.airbnb.deeplinkdispatch.DeepLink;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.activities.GamesListActivity;
import rocks.massi.trollsgames.async.LoginRegisterAsyncConnector;
import rocks.massi.trollsgames.data.LoginInformation;
import rocks.massi.trollsgames.events.LoginFailEvent;
import rocks.massi.trollsgames.events.LoginSuccessEvent;

import java.io.*;

@NoArgsConstructor
public class Login extends Fragment {

    private String savedEmail;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @SneakyThrows
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load data from cache
        File tokenFile = new File(getActivity().getCacheDir() + "/token.data");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tokenFile))) {
            String token = bufferedReader.readLine();
            // TODO: Verify token
            Log.i(getClass().getName(), "Got token from file " + token);
            bufferedReader.close();
        } catch (FileNotFoundException exception) {
            // Do nothing
        }

        // Load email from cache
        File emailFile = new File(getActivity().getCacheDir() + "/email.data");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(emailFile))) {
            savedEmail = bufferedReader.readLine();
            Log.i(getClass().getName(), "Got email from file " + savedEmail);
        } catch (FileNotFoundException exception) {
            // Do nothing
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @SneakyThrows
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginSuccessEvent event) {
        Log.i(getClass().getName(), "User logged in " + event.getUser() + "\n" + event.getToken());
        Intent intent = new Intent(getContext(), GamesListActivity.class);

        // Store token to disk
        File tokenFile = new File(getActivity().getCacheDir() + "/token.data");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tokenFile))) {
            bufferedWriter.write(event.getToken());
            Log.i(getClass().getName(), "Stored token to disk " + event.getToken());
        }

        // Store email to disk
        File emailFile = new File(getActivity().getCacheDir() + "/email.data");
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(emailFile))) {
            bufferedWriter.write(event.getUser().getEmail());
            Log.i(getClass().getName(), "Stored email to disk");
        }

        // Remove this activity from the backstack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("AUTHENTICATION_TOKEN", event.getToken());
        intent.putExtra("AUTHENTICATION_USER", event.getUser());
        startActivity(intent);
        getActivity().finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoginFailEvent event) {
        if (getView() != null) {
            Snackbar.make(getView(), "Incorrect user or password (or both)", Snackbar.LENGTH_SHORT).show();

            // Reset button
            Button submit = getView().findViewById(R.id.login_submit);
            submit.setEnabled(true);
            submit.setText(R.string.register_submit);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);

        Log.i(getClass().getName(), "onCreateView");

        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
            Log.i(getClass().getName(), "COMING FROM DEEP LINKING YEAHHH!");
            Snackbar.make(view, "Your account has been verified!", Snackbar.LENGTH_SHORT).show();
        }

        // Extract fields
        final TextView loginHeader = view.findViewById(R.id.login_header);
        final EditText loginPassword = view.findViewById(R.id.login_password);
        final EditText loginEmail = view.findViewById(R.id.login_email);
        final Button loginSubmit = view.findViewById(R.id.login_submit);

        if (savedEmail != null && !savedEmail.isEmpty()) {
            loginEmail.setText(savedEmail);
        }

        // Set typeface
        loginHeader.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/Raleway-Regular.ttf"));

        // Set button listener
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginInformation loginInformation = new LoginInformation(
                        loginEmail.getText().toString(),
                        loginPassword.getText().toString());
                Log.i(getClass().getName(), "Sending login " + loginInformation.toString());
                new LoginRegisterAsyncConnector(loginInformation, getString(R.string.server), LoginRegisterAsyncConnector.Action.LOGIN_ACTION)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                loginSubmit.setEnabled(false);
                loginSubmit.setText(R.string.register_submit_wait);
            }
        });

        return view;
    }
}
