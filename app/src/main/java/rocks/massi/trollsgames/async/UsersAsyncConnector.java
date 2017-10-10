package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.GameFetchedEvent;
import rocks.massi.trollsgames.events.UserFetchEvent;
import rocks.massi.trollsgames.events.UsersFetchedEvent;
import rocks.massi.trollsgames.services.TrollsServer;

import java.util.LinkedList;
import java.util.List;

public class UsersAsyncConnector extends AsyncTask<Void, User, List<User>> {
    private TrollsServer connector;
    private List<Game> games;
    private List<User> users;

    public UsersAsyncConnector() {
        connector = Feign.builder()
                .decoder(new GsonDecoder())
                .target(TrollsServer.class, "http://massi.rocks:8180");
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
        users = connector.getUsers();
        games = connector.getGames();

        for (User u : users) {
            u.buildCollection();
            u.setGamesCollection(new LinkedList<Game>());
            EventBus.getDefault().post(new UserFetchEvent(false, u, users.size()));

            for (Integer gameId : u.getCollection()) {
                Game g = extractGameForId(gameId);
                if (g != null) u.getGamesCollection().add(g);
                EventBus.getDefault().post(new GameFetchedEvent(g));
            }

            publishProgress(u);
        }

        return users;
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
        EventBus.getDefault().post(new UsersFetchedEvent(users));
    }
}
