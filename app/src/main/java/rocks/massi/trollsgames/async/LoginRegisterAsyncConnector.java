package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.LoginInformation;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.data.UserInformation;
import rocks.massi.trollsgames.events.*;
import rocks.massi.trollsgames.services.TrollsServer;

import java.io.IOException;
import java.util.HashMap;

@RequiredArgsConstructor
public class LoginRegisterAsyncConnector extends AsyncTask<Void, Void, Void> {

    public enum Action {
        REGISTER_ACTION,
        LOGIN_ACTION,
        CHECK_TOKEN_ACTION
    }

    @RequiredArgsConstructor
    public static class CheckTokenObject {
        private final String nick;
        private final String token;
    }

    private final Object parameter;
    private final String serverAddress;
    private final Action action;
    private TrollsServer connector = null;

    private void createConnector() {
        if (connector == null) {
            Log.i(getClass().getName(), "Creating connector to " + serverAddress);
            connector = Feign.builder().decoder(new GsonDecoder()).encoder(new GsonEncoder()).options(new Request.Options(600, 200)).target(TrollsServer.class, serverAddress);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        createConnector();
        switch (action) {
            case LOGIN_ACTION:
                try {
                    LoginInformation loginInformation = (LoginInformation) parameter;
                    Log.i(getClass().getName(), "Logging in user");
                    Response ret = connector.login(loginInformation);
                    if (ret.status() == 200) {
                        User user = (User) new GsonDecoder().decode(ret, User.class);
                        String token = ret.headers().get("Authorization").iterator().next();
                        EventBus.getDefault().post(new LoginSuccessEvent(user, token.replace("Bearer ", "")));
                        Log.i(getClass().getName(), "Logged in user " + user.toString());
                    } else {
                        Log.e(getClass().getName(), "Failure " + ret.toString());
                        EventBus.getDefault().post(new LoginFailEvent(ret.body().toString()));
                    }
                } catch (FeignException | IOException e) {
                    Log.e(getClass().getName(), e.getMessage());
                    EventBus.getDefault().post(new LoginFailEvent(e.getMessage()));
                }
                break;

            case REGISTER_ACTION:
                try {
                    User user = (User) parameter;
                    Log.i(getClass().getName(), "Registering user " + user.toString());
                    User ret = connector.register(user);
                    Log.i(getClass().getName(), "Received user " + ret.toString());
                    EventBus.getDefault().post(new UserRegisteredEvent(ret));
                } catch (FeignException ex) {
                    EventBus.getDefault().post(new UserRegistrationFailedEvent(ex.getMessage()));
                }
                break;
            case CHECK_TOKEN_ACTION:
                try {
                    CheckTokenObject token = (CheckTokenObject) parameter;
                    Log.i(getClass().getName(), "Checking token " + token.token + " for user " + token.nick);
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token.token);
                    UserInformation userInformation = connector.getUserInformation(headers, token.nick);
                    Log.i(getClass().getName(), "Verified user " + userInformation.getEmail());

                    // Get user
                    User u = connector.getUser(userInformation.getUser());

                    EventBus.getDefault().post(new TokenVerificationSuccess(userInformation, u));
                } catch (FeignException e) {
                    EventBus.getDefault().post(new TokenVerificationFailure());
                }
        }
        return null;
    }
}
