package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.trictrac.BoardgameResult;
import rocks.massi.trollsgames.data.trictrac.SearchResponse;
import rocks.massi.trollsgames.events.GameFoundOnTricTracEvent;
import rocks.massi.trollsgames.events.MissingConnectionEvent;
import rocks.massi.trollsgames.services.TricTrac;


public class TricTracAsyncConnector extends AsyncTask<Game, Void, Void> {
    private TricTrac tricTrac;

    public TricTracAsyncConnector() {
        tricTrac = Feign.builder().decoder(new GsonDecoder()).target(TricTrac.class, "https://www.trictrac.net");
    }

    @Override
    protected Void doInBackground(Game... games) {
        try {
            SearchResponse searchResponse = tricTrac.search(games[0].getName().replace("!", ""), 10);
            for (BoardgameResult boardgameResult : searchResponse.getResults().getBoardgame().getResults()) {
                Log.i(getClass().toString(), "Found on TT " + boardgameResult.getTitle());
                Log.i(getClass().toString(), "Found on TT " + boardgameResult.getUrl());
                EventBus.getDefault().post(new GameFoundOnTricTracEvent(boardgameResult));
            }
        }
        catch (Exception e) {
            Log.e(getClass().toString(), "Caught exception while querying TricTrac! " + e.getMessage());
            EventBus.getDefault().post(new MissingConnectionEvent(e));
        }

        return null;
    }
}
