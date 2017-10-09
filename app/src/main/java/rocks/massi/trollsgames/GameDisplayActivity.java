package rocks.massi.trollsgames;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;
import org.w3c.dom.Text;
import rocks.massi.trollsgames.async.BGGImagesConnector;
import rocks.massi.trollsgames.cache.ImagesCache;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;

import java.util.Locale;

public class GameDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_display);

        Game g = (Game) getIntent().getSerializableExtra(Extra.POSTED_GAME);
        setTitle(g.getName());

        getSupportActionBar().setSubtitle(String.format(Locale.FRANCE,
                "Pour %d%s joueurs.",
                g.getMinPlayers(),
                g.getMaxPlayers() > g.getMinPlayers() ?
                        String.format(Locale.FRANCE, " à %d", g.getMaxPlayers()) : ""));

        ImageView iv = (ImageView) findViewById(R.id.gameDisplayImage);
        Bitmap image = ImagesCache.getInstance().get(g.getThumbnail());

        if (image != null) {
            iv.setImageBitmap(image);
        }

        else {
            new BGGImagesConnector(iv).execute(g.getThumbnail());
        }

        TextView gameDescription = (TextView) findViewById(R.id.gameDisplayDescription);
        gameDescription.setText(Html.fromHtml(g.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        gameDescription.setMovementMethod(new ScrollingMovementMethod());
        gameDescription.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));

        TextView gameInformation = (TextView) findViewById(R.id.gameDisplayInfo);
        gameInformation.setText(Html.fromHtml(formatGameInformation(g), Html.FROM_HTML_MODE_COMPACT));
        gameInformation.setTypeface(Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        Log.w("GameDisplayActivity", "Overriding back button!");
        return false;
    }

    private String formatGameInformation(Game g) {
        return String.format(Locale.FRANCE,
                "<p>Créé par <b>%s</b></p>" +
                        "<p>Pour une durée de <b>%d</b> minutes</p>" +
                        "<p>Publié en <b>%d</b>" +
                        "<p>Il occupe la place n. <b>%d</b> sur BoardGameGeek</p>",
                g.getAuthors(),
                g.getPlayingTime(),
                g.getYearPublished(),
                g.getRank());
    }
}
