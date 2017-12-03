package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.RequiredArgsConstructor;
import rocks.massi.trollsgames.data.User;
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
        Log.i(getClass().getName(), "Registering user " + user.toString());
        User ret = connector.register(user);
        Log.i(getClass().getName(), "Received user " + ret.toString());
        return null;
    }
}
