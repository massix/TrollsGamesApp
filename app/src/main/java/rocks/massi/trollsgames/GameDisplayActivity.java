package rocks.massi.trollsgames;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.adapter.GamesServicesAdapter;
import rocks.massi.trollsgames.async.PhilibertAsyncConnector;
import rocks.massi.trollsgames.async.TricTracAsyncConnector;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.GameSearchService;
import rocks.massi.trollsgames.data.ThirdPartyServices;
import rocks.massi.trollsgames.events.GameFoundOnPhilibertEvent;
import rocks.massi.trollsgames.events.GameFoundOnTricTracEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class GameDisplayActivity extends AppCompatActivity {
    private Game shownGame;
    private GamesServicesAdapter adapter;
    private List<GameSearchService> gameSearchServices;
    private ProgressBar progressBar;

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final GameFoundOnPhilibertEvent event) {
        gameSearchServices.add(new GameSearchService(ThirdPartyServices.PHILIBERT,
                shownGame,
                event.getPhilibertSearchResponse().getProduct_link(),
                event.getPhilibertSearchResponse().getPname()));
        progressBar.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final GameFoundOnTricTracEvent event) {
        GameSearchService service = new GameSearchService(
                ThirdPartyServices.TRICTRAC,
                shownGame,
                event.getBoardgameResult().getUrl(),
                event.getBoardgameResult().getTitle());

        if (! gameSearchServices.contains(service)) {
            gameSearchServices.add(service);
            progressBar.setVisibility(View.INVISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final GameSearchService event) {
        Log.i(getClass().toString(), "Open on external service");
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(event.getUrl())));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_display);

        gameSearchServices = new LinkedList<>();
        adapter = new GamesServicesAdapter(getApplicationContext(), 0, gameSearchServices);
        progressBar = findViewById(R.id.search_progress_bar);

        shownGame = (Game) getIntent().getSerializableExtra(Extra.POSTED_GAME);
        setTitle(shownGame.getName());

        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(getResources().getString(R.string.players_count,
                shownGame.getMinPlayers(),
                shownGame.getMaxPlayers() > shownGame.getMinPlayers() ?
                        String.format(Locale.FRANCE, " à %d", shownGame.getMaxPlayers()) : ""));

        ImageView iv = findViewById(R.id.gameDisplayImage);
        ListView searchResults = findViewById(R.id.search_results_view);
        searchResults.setAdapter(adapter);

        GlideApp.with(this)
                .load(shownGame.getNormalizedThumbnail())
                .placeholder(R.drawable.ic_search_black_24dp)
                .fitCenter()
                .into(iv);

        new PhilibertAsyncConnector().execute(shownGame);
        new TricTracAsyncConnector().execute(shownGame);

        TextView header = findViewById(R.id.philibert_header);
        header.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));

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
