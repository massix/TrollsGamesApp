package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.RetryableException;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.MissingConnectionEvent;
import rocks.massi.trollsgames.events.UserFetchEvent;
import rocks.massi.trollsgames.events.UsersFetchedEvent;
import rocks.massi.trollsgames.services.TrollsServer;

import java.util.List;

public class UsersAsyncConnector extends AsyncTask<Void, User, List<User>> {
    private TrollsServer connector;
    private List<Game> games;
    private List<User> users;

    public UsersAsyncConnector() {
        connector = Feign.builder()
                .decoder(new GsonDecoder())
                .target(TrollsServer.class, "https://trolls-server.herokuapp.com");
    }

    private Game extractGameForId(int gameId) {
        for (Game g : games) {
            if (g.getId() == gameId)
                return g;
        }

        return null;
    }

    @Override
    protected List<User> doInBackground(Void... voids) {
        try {
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
            EventBus.getDefault().post(new UsersFetchedEvent(users));
    }
}
