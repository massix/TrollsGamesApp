package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.events.GamesFetchEvent;
import rocks.massi.trollsgames.services.TrollsServer;

import java.util.List;

public class GamesAsyncConnector extends AsyncTask<Void, Game, List<Game>> {
    TrollsServer connector;

    public GamesAsyncConnector() {
        connector = Feign.builder()
                .decoder(new GsonDecoder())
                .target(TrollsServer.class, "http://massi.rocks:8180");
    }

    @Override
    protected List<Game> doInBackground(Void... voids) {
        EventBus.getDefault().post(new GamesFetchEvent(false, null
        ));
        List<Game> ret = connector.getGames();
        return ret;
    }

    @Override
    protected void onPostExecute(List<Game> games) {
        EventBus.getDefault().post(new GamesFetchEvent(true, games));
    }
}
