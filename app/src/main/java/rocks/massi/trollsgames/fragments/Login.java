package rocks.massi.trollsgames.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.async.LoginRegisterAsyncConnector;
import rocks.massi.trollsgames.data.LoginInformation;

@NoArgsConstructor
public class Login extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);

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
            }
        });

        return view;
    }
}
