package rocks.massi.trollsgames;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
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

        getSupportActionBar().setSubtitle(getResources().getString(R.string.players_count,
                g.getMinPlayers(),
                g.getMaxPlayers() > g.getMinPlayers() ?
                        String.format(Locale.FRANCE, " à %d", g.getMaxPlayers()) : ""));

        ImageView iv = (ImageView) findViewById(R.id.gameDisplayImage);

        GlideApp.with(this)
                .load(g.getNormalizedThumbnail())
                .placeholder(R.drawable.ic_search_black_24dp)
                .fitCenter()
                .into(iv);

        TextView gameDescription = findViewById(R.id.gameDisplayDescription);
        gameDescription.setText(Html.fromHtml(g.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        gameDescription.setMovementMethod(new ScrollingMovementMethod());
        gameDescription.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));

        TextView gameInformation = findViewById(R.id.gameDisplayInfo);
        gameInformation.setText(formatGameInformation(g));
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

    private CharSequence formatGameInformation(Game g) {
        return Html.fromHtml(getResources().getString(R.string.game_information,
                g.getAuthors(),
                g.getPlayingTime(),
                g.getYearPublished(),
                g.getRank()), Html.FROM_HTML_MODE_COMPACT);
    }
}
