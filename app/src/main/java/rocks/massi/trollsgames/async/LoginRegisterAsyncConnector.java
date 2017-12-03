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
import rocks.massi.trollsgames.events.UserRegisteredEvent;
import rocks.massi.trollsgames.events.UserRegistrationFailedEvent;
import rocks.massi.trollsgames.services.TrollsServer;

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
                    Log.i(getClass().getName(), ret.toString());
                } catch (FeignException e) {
                    Log.e(getClass().getName(), e.getMessage());
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
