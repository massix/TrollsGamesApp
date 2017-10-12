package rocks.massi.trollsgames;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;

import java.util.Locale;

public class GameDisplayActivity extends AppCompatActivity {
    private Game shownGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_display);

        shownGame = (Game) getIntent().getSerializableExtra(Extra.POSTED_GAME);
        setTitle(shownGame.getName());

        getSupportActionBar().setSubtitle(getResources().getString(R.string.players_count,
                shownGame.getMinPlayers(),
                shownGame.getMaxPlayers() > shownGame.getMinPlayers() ?
                        String.format(Locale.FRANCE, " à %d", shownGame.getMaxPlayers()) : ""));

        ImageView iv = findViewById(R.id.gameDisplayImage);

        GlideApp.with(this)
                .load(shownGame.getNormalizedThumbnail())
                .placeholder(R.drawable.ic_search_black_24dp)
                .fitCenter()
                .into(iv);

        TextView gameDescription = findViewById(R.id.gameDisplayDescription);
        gameDescription.setText(Html.fromHtml(shownGame.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        gameDescription.setMovementMethod(new ScrollingMovementMethod());
        gameDescription.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));

        TextView gameInformation = findViewById(R.id.gameDisplayInfo);
        gameInformation.setText(formatGameInformation(shownGame));
        gameInformation.setTypeface(Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            Log.w("GameDisplayActivity", "Overriding back button!");
            onBackPressed();
            return true;
        }

        else if (item.getItemId() == R.id.open_bgg) {
            Log.i("GameDisplayActivity", "Open on BGG");
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.boardgamegeek.com/boardgame/" + shownGame.getId())));
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_list, menu);
        return true;
    }

    private CharSequence formatGameInformation(Game g) {
        return Html.fromHtml(getResources().getString(R.string.game_information,
                g.getAuthors(),
                g.getPlayingTime(),
                g.getYearPublished(),
                g.getRank()), Html.FROM_HTML_MODE_COMPACT);
    }
}
