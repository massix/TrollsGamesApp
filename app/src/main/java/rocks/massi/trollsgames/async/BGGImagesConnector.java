package rocks.massi.trollsgames.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import lombok.AllArgsConstructor;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.cache.ImagesCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@AllArgsConstructor
public class BGGImagesConnector extends AsyncTask<String, Void, Bitmap> {
    private ImageView imageView;

    @Override
    protected Bitmap doInBackground(String... urls) {
        Bitmap icon = null;
        String finalUrl = urls[0];

        if (! urls[0].startsWith("http")) {
            finalUrl = "https:" + urls[0];
        }

        try(InputStream in = new URL(finalUrl).openStream()) {
            icon = BitmapFactory.decodeStream(in);
        }
        catch(MalformedURLException e) {
            Log.e("BGGImagesConnector", String.format("Malformed URL: '%s'", urls[0]));
        } catch (IOException e) {
            Log.e("BGGImagesConnector", String.format("IOException while trying to resolve URL: %s", e.getMessage()));
        }

        if (urls[0] != null && icon != null)
            ImagesCache.getInstance().put(urls[0], icon);

        return icon;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else
            imageView.setImageResource(R.drawable.ic_search_black_24dp);
    }
}
