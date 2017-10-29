package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.PhilibertSearchResponse;
import rocks.massi.trollsgames.events.GameFoundOnPhilibertEvent;
import rocks.massi.trollsgames.events.MissingConnectionEvent;
import rocks.massi.trollsgames.services.Philibert;

import java.util.Date;
import java.util.List;

public class PhilibertAsyncConnector extends AsyncTask<Game, Void, PhilibertSearchResponse> {
    private Philibert philibert;

    public PhilibertAsyncConnector() {
        philibert = Feign.builder().decoder(new GsonDecoder()).target(Philibert.class, "https://www.philibertnet.com");
    }

        @Override
    protected void onPreExecute() {

    }

    @Override
    protected PhilibertSearchResponse doInBackground(Game... games) {
        try {
            Game shownGame = games[0];
            List<PhilibertSearchResponse> searchResponses = philibert.search(shownGame.getName(),
                    30,
                    String.valueOf(new Date().getTime()));
            for (PhilibertSearchResponse searchResponse : searchResponses) {
                Log.i(getClass().toString(), String.format("Found %s", searchResponse.getPname()));
                Log.i(getClass().toString(), String.format("Url: %s", searchResponse.getProduct_link()));
                EventBus.getDefault().post(new GameFoundOnPhilibertEvent(searchResponse));
            }
        }
        catch (Exception e) {
            Log.e(getClass().getName(), "Error while querying Philibert");
            EventBus.getDefault().post(new MissingConnectionEvent(e));
        }

        return null;
    }
}
