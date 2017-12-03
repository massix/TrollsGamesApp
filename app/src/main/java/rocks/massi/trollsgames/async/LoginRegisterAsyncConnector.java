package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.UserRegisteredEvent;
import rocks.massi.trollsgames.events.UserRegistrationFailedEvent;
import rocks.massi.trollsgames.services.TrollsServer;

@RequiredArgsConstructor
public class LoginRegisterAsyncConnector extends AsyncTask<Void, Void, Void> {

    private final User user;
    private final String serverAddress;
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
        try {
            Log.i(getClass().getName(), "Registering user " + user.toString());
            User ret = connector.register(user);
            Log.i(getClass().getName(), "Received user " + ret.toString());
            EventBus.getDefault().post(new UserRegisteredEvent(ret));
        } catch (FeignException ex) {
            EventBus.getDefault().post(new UserRegistrationFailedEvent(ex.getMessage()));
        }
        return null;
    }
}
