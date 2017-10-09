package rocks.massi.trollsgames.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImagesCache extends LruCache<String, Bitmap> {
    private static ImagesCache instance;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    private ImagesCache(int maxSize) {
        super(maxSize);
    }

    public static ImagesCache getInstance() {
        if (instance == null) {
            instance = new ImagesCache((int) ((Runtime.getRuntime().maxMemory() / 1024) / 2)) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };
        }

        return instance;
    }
}
