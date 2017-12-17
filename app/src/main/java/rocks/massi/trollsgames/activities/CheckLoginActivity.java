package rocks.massi.trollsgames.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.async.LoginRegisterAsyncConnector;
import rocks.massi.trollsgames.events.TokenVerificationFailure;
import rocks.massi.trollsgames.events.TokenVerificationSuccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CheckLoginActivity extends Activity {

    private String token;

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private String readStringFromFile(File src) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(src))) {
            return bufferedReader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TokenVerificationFailure event) {
        Log.i(getClass().getName(), "Token verification KO");
        Intent i = new Intent(getApplicationContext(), LoginRegisterActivity.class);
        startActivity(i);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TokenVerificationSuccess event) {
        Log.i(getClass().getName(), "Token verification OK");
        Intent i = new Intent(getApplicationContext(), GamesListActivity.class);
        i.putExtra("AUTHENTICATION_TOKEN", token);
        i.putExtra("AUTHENTICATION_USER", event.getUser());
        startActivity(i);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = readStringFromFile(new File(getCacheDir() + "/token.data"));
        String user = readStringFromFile(new File(getCacheDir() + "/user.data"));
        String email = readStringFromFile(new File(getCacheDir() + "/email.data"));

        if (token != null && user != null && email != null) {
            new LoginRegisterAsyncConnector(new LoginRegisterAsyncConnector.CheckTokenObject(user, token),
                    getString(R.string.server),
                    LoginRegisterAsyncConnector.Action.CHECK_TOKEN_ACTION)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.i(getClass().getName(), "No cache found, starting LoginRegister");
            Intent i = new Intent(getApplicationContext(), LoginRegisterActivity.class);
            startActivity(i);
            finish();
        }
    }
}
