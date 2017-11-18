package rocks.massi.trollsgames.async;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.CacheStoredEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@RequiredArgsConstructor
public class CacheAsyncWriter extends AsyncTask<Void, Void, Void> {
    private final File cacheDir;
    private final List<User> users;

    @Override
    protected Void doInBackground(Void... voids) {

        // Store in cache
        Gson gson = new GsonBuilder().create();
        File usersCache = new File(cacheDir, "/users.json");

        if (usersCache.exists()) {
            usersCache.delete();
        }

        try (Writer writer = new FileWriter(usersCache)) {
            gson.toJson(users, writer);
            Log.i(getClass().getName(), "Stored cache to disk");
        } catch (IOException e) {
            Log.e(getClass().getName(), "Impossible to write cache.");
        }

        EventBus.getDefault().post(new CacheStoredEvent());
        return null;
    }
}
