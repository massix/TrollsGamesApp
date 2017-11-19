package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.events.SearchFinishedEvent;
import rocks.massi.trollsgames.services.TrollsServer;

import java.util.List;

public class SearchAsyncConnector extends AsyncTask<Void, Void, Void> {
    TrollsServer connector;
    private final String searchQuery;

    public SearchAsyncConnector(String searchQuery, String serverAddress) {
        this.searchQuery = searchQuery;
        connector = Feign.builder().decoder(new GsonDecoder()).target(TrollsServer.class, serverAddress);

    }

    @Override
    protected Void doInBackground(Void... voids) {
        List<Game> games = connector.search(searchQuery);
        Log.i(getClass().getName(), "found " + games.size() + " games");
        EventBus.getDefault().post(new SearchFinishedEvent(games));
        return null;
    }
}
