package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.LoginInformation;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.LoginFailEvent;
import rocks.massi.trollsgames.events.LoginSuccessEvent;
import rocks.massi.trollsgames.events.UserRegisteredEvent;
import rocks.massi.trollsgames.events.UserRegistrationFailedEvent;
import rocks.massi.trollsgames.services.TrollsServer;

import java.io.IOException;

@RequiredArgsConstructor
public class LoginRegisterAsyncConnector extends AsyncTask<Void, Void, Void> {

    public static enum Action {
        REGISTER_ACTION,
        LOGIN_ACTION
    }

    private final Object parameter;
    private final String serverAddress;
    private final Action action;
    private TrollsServer connector = null;

    private void createConnector() {
        if (connector == null) {
            Log.i(getClass().getName(), "Creating connector to " + serverAddress);
            connector = Feign.builder().decoder(new GsonDecoder()).encoder(new GsonEncoder()).target(TrollsServer.class, serverAddress);
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
        }
        return null;
    }
}
