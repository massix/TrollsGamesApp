package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.RetryableException;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.ServerInformation;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.*;
import rocks.massi.trollsgames.services.TrollsServer;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class UsersAsyncConnector extends AsyncTask<Void, User, List<User>> {
    private TrollsServer connector;
    private List<User> users;

    public UsersAsyncConnector() {
        String serverAddress = "http://localhost:8180";
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            serverAddress = properties.getProperty("server.url");
        } catch (IOException e) {
            Log.e(getClass().getName(), "Could not load properties");
        }
        connector = Feign.builder()
                .decoder(new GsonDecoder())
                .target(TrollsServer.class, serverAddress);
    }

    @Override
    protected List<User> doInBackground(Void... voids) {
        try {
            ServerInformation serverInformation = connector.getInformation();
            EventBus.getDefault().post(new ServerInformationEvent(serverInformation));
            users = connector.getUsers();

            for (User u : users) {
                u.buildCollection();
                u.setGamesCollection(connector.getCollectionForUser(u.getForumNick()));
                EventBus.getDefault().post(new UserFetchEvent(false, u, users.size()));
                publishProgress(u);
            }

            return users;

        } catch (RetryableException e) {
            Log.e("UsersAsyncConnector", "caught exception");
            EventBus.getDefault().post(new MissingConnectionEvent(e));
        } catch (RuntimeException e) {
            Log.e(getClass().getName(), "Server offline");
            EventBus.getDefault().post(new ServerOfflineEvent());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(User... users) {
        EventBus.getDefault().post(new UserFetchEvent(true, users[0], this.users.size()));
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(List<User> users) {
        if (users != null)
            EventBus.getDefault().post(new UsersFetchedEvent(users, false));
    }
}
