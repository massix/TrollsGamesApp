package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.RetryableException;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.Quote;
import rocks.massi.trollsgames.data.ServerInformation;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.*;
import rocks.massi.trollsgames.services.TrollsServer;

import java.util.List;

public class UsersAsyncConnector extends AsyncTask<Void, User, List<User>> {
    private TrollsServer connector;
    private List<User> users;

    public UsersAsyncConnector(String serverAddress) {
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

            try {
                Quote quote = connector.getQuote();
                EventBus.getDefault().post(new QuoteReceivedEvent(quote));
            } catch (Exception e) {
                Log.w(getClass().getName(), "Connecting to old server, refusing to display random quote");
            }

            for (User u : users) {
                u.setGamesCollection(connector.getCollectionForUser(u.getBggNick()));
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
