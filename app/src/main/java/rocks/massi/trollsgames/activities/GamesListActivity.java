package rocks.massi.trollsgames.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.BuildConfig;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.adapter.GamesAdapter;
import rocks.massi.trollsgames.async.CacheAsyncLoader;
import rocks.massi.trollsgames.async.CacheAsyncWriter;
import rocks.massi.trollsgames.async.SearchAsyncConnector;
import rocks.massi.trollsgames.async.UsersAsyncConnector;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.*;

import java.io.File;
import java.util.*;

public class GamesListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private List<User> users;
    private List<Game> shownGames;
    private User activeUser;

    private GamesAdapter gamesAdapter;
    private boolean operationPending;
    private boolean displayingQuote = false;
    private boolean resultsFromSearch = false;

    private ProgressBar loadingUsersPb;
    private TextView loadingUsersTv;

    private boolean expansionsHidden;
    private SensorManager sensorManager;
    private boolean debugActivated = false;

    private class SensorHandling {
        float lastAcceleration;
        float currentAcceleration;
        float acceleration;
        long lastDetectedEvent;
    }

    private SensorHandling sensorHandling;

    @SuppressWarnings("deprecation")
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        sensorHandling.lastAcceleration = sensorHandling.currentAcceleration;
        sensorHandling.currentAcceleration = (float) Math.sqrt((double) (x*x + y*y + z*z));
        float delta = sensorHandling.currentAcceleration - sensorHandling.lastAcceleration;
        sensorHandling.acceleration = 0.09f + delta;
        long time = new Date().getTime();

        // 2s delay for events.
        if (sensorHandling.acceleration > 12.0f && !shownGames.isEmpty() &&
                (time - sensorHandling.lastDetectedEvent) >= 2000) {
            Log.i("SensorHandling", "Detected shake event");
            sensorHandling.lastDetectedEvent = time;
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(300);
            EventBus.getDefault().post(new GameSelectedEvent(
                    shownGames.get(new Random().nextInt(shownGames.size()))));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private enum CurrentRanking {
        ALPHABETICAL,
        BGG_RANKING
    }

    private CurrentRanking currentRanking;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorHandling = new SensorHandling();
        sensorHandling.currentAcceleration = SensorManager.GRAVITY_EARTH;
        sensorHandling.lastAcceleration = SensorManager.GRAVITY_EARTH;
        sensorHandling.acceleration = 0.00f;
        sensorHandling.lastDetectedEvent = new Date().getTime();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        sensorManager.unregisterListener(this);
    }

    @SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishEvent(final UsersFetchedEvent usersFetchedEvent) {
        findViewById(R.id.fab).setEnabled(true);
        operationPending = false;

        // Sort users alphabetically
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getForumNick().toLowerCase().compareTo(o2.getForumNick().toLowerCase());
            }
        });

        SubMenu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).getSubMenu();
        int index = 0;
        for (User user : users) {
            menu.add(Menu.NONE, index++, index, user.getForumNick());
        }

        loadingUsersPb.setVisibility(View.INVISIBLE);

        if (usersFetchedEvent.isFromCache()) {
            loadingUsersTv.setText(R.string.loading_end_cache);
        } else if (!displayingQuote) {
            loadingUsersTv.setText(R.string.loading_end);
        }

        new CacheAsyncWriter(getCacheDir(), users).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onUserEvent(final UserFetchEvent userFetchEvent) {
        if (userFetchEvent.isFinished()) {
            users.add(userFetchEvent.getUser());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                loadingUsersPb.setProgress(loadingUsersPb.getProgress() + 1, true);
            }
            else {
                loadingUsersPb.setProgress(loadingUsersPb.getProgress() + 1);
            }
        }
        else {
            loadingUsersPb.setIndeterminate(false);
            loadingUsersPb.setMax(userFetchEvent.getTotalUsers());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameSelectedEvent(final GameSelectedEvent gameSelectedEvent) {
        Intent i = new Intent(this, GameDisplayActivity.class);
        i.putExtra(Extra.POSTED_GAME, gameSelectedEvent.getGame());
        startActivity(i);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkErrorEvent(final MissingConnectionEvent event) {
        loadingUsersTv.setText(R.string.missing_network);
        loadingUsersPb.setVisibility(View.INVISIBLE);
        findViewById(R.id.fab).setEnabled(true);
        operationPending = false;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerInformationEvent(final ServerInformationEvent event) {
        if (debugActivated) {
            Snackbar.make(findViewById(R.id.fab), event.getServerInformation().getVersion(), Snackbar.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerOfflineEvent(final ServerOfflineEvent event) {
        loadingUsersTv.setText(R.string.server_offline);
        loadingUsersPb.setVisibility(View.INVISIBLE);
        findViewById(R.id.fab).setEnabled(true);
        operationPending = false;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCacheFoundEvent(final CacheFoundEvent event) {
        loadingUsersTv.setText(R.string.loading_cache);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCacheStoredEvent(final CacheStoredEvent event) {
        if (debugActivated) {
            Snackbar.make(findViewById(R.id.fab), "Cache stored!", BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvalidCacheEvent(final CacheInvalidEvent event) {
        loadingUsersTv.setText(R.string.cache_error);
        if (debugActivated) {
            Snackbar.make(findViewById(R.id.fab), "Invalid cache " + event.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQuoteReceivedEvent(final QuoteReceivedEvent event) {
        Spanned formattedText = Html.fromHtml(
                "<p>" + event.getQuote().getQuote() + "</p>" +
                        "<footer>-- " + event.getQuote().getAuthor() + "</footer>");
        displayingQuote = true;
        loadingUsersTv.setText(formattedText);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchFinishedEvent(final SearchFinishedEvent event) {
        loadingUsersTv.setVisibility(View.INVISIBLE);
        loadingUsersPb.setIndeterminate(false);
        loadingUsersPb.setVisibility(View.INVISIBLE);
        resultsFromSearch = true;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.search_header));
            getSupportActionBar().setSubtitle(getString(R.string.search_result, event.getGames().size()));
        }

        shownGames.clear();
        shownGames.addAll(event.getGames());
        gamesAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        expansionsHidden = true;
        currentRanking = CurrentRanking.ALPHABETICAL;

        users = new LinkedList<>();
        shownGames = new LinkedList<>();

        loadingUsersTv = findViewById(R.id.loadingUserInfo);
        loadingUsersPb = findViewById(R.id.gamesLoadingBar);
        loadingUsersTv.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));

        loadingUsersTv.setText(R.string.intro);
        new CacheAsyncLoader(getCacheDir()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton f = findViewById(R.id.fab);
                f.setEnabled(false);

                // Refresh Submenu
                ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).getSubMenu().clear();
                users.clear();
                shownGames.clear();
                gamesAdapter.notifyDataSetChanged();

                new UsersAsyncConnector(getString(R.string.server)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                loadingUsersPb.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    loadingUsersPb.setProgress(0, true);
                }
                else {
                    loadingUsersPb.setProgress(0);
                }
                loadingUsersTv.setVisibility(View.VISIBLE);

                loadingUsersTv.setText(R.string.loading_users);

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                    getSupportActionBar().setSubtitle("");
                }

                operationPending = true;
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ListView lv = findViewById(R.id.gameslist);
        gamesAdapter = new GamesAdapter(getApplicationContext(), 0, shownGames);
        lv.setAdapter(gamesAdapter);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                    fab.show();
                else
                    fab.hide();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        final SearchView searchView = findViewById(R.id.game_search_tv);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // Clear shown games
                shownGames.clear();
                gamesAdapter.notifyDataSetChanged();

                // Show progress bar
                loadingUsersPb.setVisibility(View.VISIBLE);
                loadingUsersPb.setIndeterminate(true);

                // Remove focus
                searchView.clearFocus();

                // Start asynchronous search
                new SearchAsyncConnector(query, getString(R.string.server)).execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.games_list, menu);
        List<TextView> textViews = new LinkedList<>();

        TextView navHeaderTv = findViewById(R.id.nav_header_tv);
        textViews.add(navHeaderTv);

        TextView navVersionTv = findViewById(R.id.nav_version_nb);
        textViews.add(navVersionTv);

        final ImageView logoView = findViewById(R.id.logo_view);
        logoView.setOnClickListener(new View.OnClickListener() {
            private long last = 0;
            private int counter = 0;

            @Override
            public void onClick(View v) {
                long now = new Date().getTime();

                if ((now - last) <= 300) {
                    counter++;
                } else {
                    counter = 1;
                }

                last = now;

                if (counter >= 7) {
                    String text = String.format("Version %s\nConnecting to server '%s'",
                            BuildConfig.VERSION_NAME, getString(R.string.server));
                    Snackbar.make(logoView, text, Snackbar.LENGTH_LONG).show();
                    counter = 0;
                    debugActivated = true;
                }
            }
        });

        for (TextView tv : textViews) {
            tv.setTypeface(Typeface.createFromAsset(getAssets(), "font/IndieFlower.ttf"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tv.setTextColor(getColor(R.color.gameDefault));
            }
        }

        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        ListView lv = findViewById(R.id.gameslist);

        if (id == R.id.sort_alpha && !shownGames.isEmpty() && !resultsFromSearch) {
            Log.i("GamesListActivity", "Alphabetical order");
            currentRanking = CurrentRanking.ALPHABETICAL;
            rebuildShownGamesList();
        } else if (id == R.id.sort_rank && !shownGames.isEmpty() && !resultsFromSearch) {
            Log.i("GamesListActivity", "Rank order");
            currentRanking = CurrentRanking.BGG_RANKING;
            rebuildShownGamesList();
        }

        else if (id == R.id.game_random && !shownGames.isEmpty()) {
            EventBus.getDefault().post(new GameSelectedEvent(
                    shownGames.get(new Random().nextInt(shownGames.size()))));
        } else if (id == R.id.expansions_toggle && !shownGames.isEmpty() && !resultsFromSearch) {
            Log.i("GamesListActivity", "Toggle expansions");
            if (expansionsHidden) {
                expansionsHidden = false;
                item.setTitle(R.string.hide_expansions);
            }
            else {
                expansionsHidden = true;
                item.setTitle(R.string.show_expansions);
            }

            rebuildShownGamesList();
        } else if (id == R.id.empty_cache) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.empty_cache_title);
            builder.setMessage(R.string.empty_cache_text);
            builder.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(getClass().getName(), "Empty cache action");
                    shownGames.clear();
                    users.clear();
                    SubMenu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).getSubMenu();
                    menu.clear();

                    gamesAdapter.notifyDataSetChanged();
                    loadingUsersTv.setVisibility(View.VISIBLE);
                    loadingUsersTv.setText(R.string.intro);

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.app_name);
                        getSupportActionBar().setSubtitle("");
                    }

                    File cacheFile = new File(getCacheDir(), "users.json");
                    if (cacheFile.exists()) cacheFile.delete();
                }
            });

            builder.setNegativeButton(R.string.confirm_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.create().show();
        } else if (id == R.id.about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.about));
            View inflatedView = getLayoutInflater().inflate(R.layout.about_dialog, null);
            TextView dialogText = inflatedView.findViewById(R.id.dialog_text);
            dialogText.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));
            dialogText.setText(Html.fromHtml(getString(R.string.about_text, BuildConfig.VERSION_NAME)));

            builder.setView(inflatedView);
            builder.create().show();
        }

        gamesAdapter.notifyDataSetChanged();
        lv.setSelectionAfterHeaderView();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (operationPending) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        activeUser = users.get(id);

        rebuildShownGamesList();

        ListView lv = findViewById(R.id.gameslist);
        loadingUsersTv.setVisibility(View.INVISIBLE);

        gamesAdapter.notifyDataSetChanged();
        lv.setSelectionAfterHeaderView();

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(activeUser.getForumNick());

        drawer.closeDrawer(GravityCompat.START);
        resultsFromSearch = false;

        return true;
    }

    private void rebuildShownGamesList() {
        // Build games list
        shownGames.clear();

        for (Game g : activeUser.getGamesCollection()) {

            if (g != null && g.isExtension()) {
                if (! expansionsHidden && ! shownGames.contains(g))
                    shownGames.add(g);
            }

            else if (g != null && ! shownGames.contains(g))
                shownGames.add(g);
        }

        switch (currentRanking) {
            case BGG_RANKING:
                Collections.sort(shownGames, new Comparator<Game>() {
                    @Override
                    public int compare(Game o1, Game o2) {

                        // Force the ranks 0 on bottom of the list.
                        if (o1.getRank() <= 0 && o2.getRank() > 0)
                            return 1;
                        else if (o2.getRank() <= 0 && o1.getRank() > 0)
                            return -1;

                        // Classical result I guess ?
                        return o1.getRank() - o2.getRank();
                    }
                });
                break;
            case ALPHABETICAL:
                Collections.sort(shownGames, new Comparator<Game>() {
                    @Override
                    public int compare(Game o1, Game o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                break;
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(getResources().getString(R.string.user_game_count, shownGames.size()));
    }
}
