package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.CacheFoundEvent;
import rocks.massi.trollsgames.events.CacheInvalidEvent;
import rocks.massi.trollsgames.events.UserFetchEvent;
import rocks.massi.trollsgames.events.UsersFetchedEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

@RequiredArgsConstructor
public class CacheAsyncLoader extends AsyncTask<Void, Void, User[]> {

    private final File cacheLocation;

    @Override
    protected User[] doInBackground(Void... voids) {
        File cache = new File(cacheLocation + "/users.json");
        Log.i(getClass().getName(), "Cache location " + cache.toString());
        if (cache.exists()) {
            EventBus.getDefault().post(new CacheFoundEvent());

            try {
                Gson gson = new GsonBuilder().create();
                User[] usersFromCache = gson.fromJson(new JsonReader(new FileReader(cache)), User[].class);
                Log.i(getClass().getName(), "Loaded cache from disk " + usersFromCache.length);
                return usersFromCache;
            } catch (FileNotFoundException exception) {
                EventBus.getDefault().post(new CacheInvalidEvent(exception.getMessage()));
                Log.w(getClass().getName(), "Cache not found");
            } catch (JsonSyntaxException exception) {
                EventBus.getDefault().post(new CacheInvalidEvent(exception.getMessage()));
                Log.w(getClass().getName(), "Invalid cache " + exception.getMessage());
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(User[] users) {
        super.onPostExecute(users);
        if (users != null) {
            for (User user : users) {
                EventBus.getDefault().post(new UserFetchEvent(true, user, users.length));
            }

            EventBus.getDefault().post(new UsersFetchedEvent(null, true));
        }
    }
}
