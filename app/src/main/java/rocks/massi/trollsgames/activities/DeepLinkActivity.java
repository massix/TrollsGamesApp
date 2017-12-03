package rocks.massi.trollsgames.activities;

import android.app.Activity;
import android.os.Bundle;
import com.airbnb.deeplinkdispatch.DeepLinkHandler;
import rocks.massi.trollsgames.AppDeepLinkModule;
import rocks.massi.trollsgames.AppDeepLinkModuleLoader;

@DeepLinkHandler({AppDeepLinkModule.class})
public class DeepLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeepLinkDelegate deepLinkDelegate = new DeepLinkDelegate(new AppDeepLinkModuleLoader());

        deepLinkDelegate.dispatchFrom(this);

        finish();
    }
}
