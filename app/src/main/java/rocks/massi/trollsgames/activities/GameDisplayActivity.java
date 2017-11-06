package rocks.massi.trollsgames.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import rocks.massi.trollsgames.GlideApp;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.adapter.GamesDisplayFragmentAdapter;
import rocks.massi.trollsgames.async.PhilibertAsyncConnector;
import rocks.massi.trollsgames.async.TricTracAsyncConnector;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;

public class GameDisplayActivity extends AppCompatActivity {
    private Game shownGame;
    private GamesDisplayFragmentAdapter fragmentAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_display);

        shownGame = (Game) getIntent().getSerializableExtra(Extra.POSTED_GAME);
        setTitle(shownGame.getName());

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            Resources resources = getResources();
            if (shownGame.getMaxPlayers() > shownGame.getMinPlayers()) {
                actionBar.setSubtitle(resources.getString(R.string.players_count_diff, shownGame.getMinPlayers(), shownGame.getMaxPlayers()));
            }
            else {
                actionBar.setSubtitle(resources.getString(R.string.players_count_equal, shownGame.getMaxPlayers()));
            }
        }

        ImageView iv = findViewById(R.id.gameDisplayImage);

        GlideApp.with(this)
                .load(shownGame.getNormalizedThumbnail())
                .placeholder(R.drawable.ic_search_black_24dp)
                .fitCenter()
                .into(iv);

        new PhilibertAsyncConnector().execute(shownGame);
        new TricTracAsyncConnector().execute(shownGame);

        TextView gameInformation = findViewById(R.id.gameDisplayInfo);
        gameInformation.setText(formatGameInformation(shownGame));
        gameInformation.setTypeface(Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf"));

        fragmentAdapter = new GamesDisplayFragmentAdapter(getSupportFragmentManager(), shownGame, getApplicationContext());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(fragmentAdapter);

        PagerTitleStrip pagerTitleStrip = findViewById(R.id.pager_title_strip);
        for (int i = 0; i < pagerTitleStrip.getChildCount(); i++) {
            if (pagerTitleStrip.getChildAt(i) instanceof TextView) {
                TextView tv = (TextView) pagerTitleStrip.getChildAt(i);
                tv.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));
            }
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(getResources().getString(R.string.game_information,
                    g.getAuthors(),
                    g.getPlayingTime(),
                    g.getYearPublished(),
                    g.getRank()), Html.FROM_HTML_MODE_COMPACT);
        }
        else return Html.fromHtml(getResources().getString(R.string.game_information,
                    g.getAuthors(),
                    g.getPlayingTime(),
                    g.getYearPublished(),
                    g.getRank()));
    }
}
