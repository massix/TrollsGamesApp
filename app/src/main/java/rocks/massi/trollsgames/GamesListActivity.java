package rocks.massi.trollsgames;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.adapter.GamesAdapter;
import rocks.massi.trollsgames.async.UsersAsyncConnector;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.GameSelectedEvent;
import rocks.massi.trollsgames.events.MissingConnectionEvent;
import rocks.massi.trollsgames.events.UserFetchEvent;
import rocks.massi.trollsgames.events.UsersFetchedEvent;

import java.util.*;

public class GamesListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private List<User> users;
    private List<Game> shownGames;
    private User activeUser;

    private GamesAdapter gamesAdapter;
    private boolean operationPending;

    private ProgressBar loadingUsersPb;
    private TextView loadingUsersTv;

    private boolean expansionsHidden;
    private SensorManager sensorManager;

    private class SensorHandling {
        public float lastAcceleration;
        public float currentAcceleration;
        public float acceleration;
        public long lastDetectedEvent;
    };

    SensorHandling sensorHandling;

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Shake event should select a random game if list is not empty
        Toast.makeText(getApplicationContext(), "Sensor change event", Toast.LENGTH_SHORT);

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishEvent(final UsersFetchedEvent usersFetchedEvent) {
        findViewById(R.id.fab).setEnabled(true);
        operationPending = false;

        loadingUsersPb.setVisibility(View.INVISIBLE);

        loadingUsersTv.setText(R.string.loading_end);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onUserEvent(final UserFetchEvent userFetchEvent) {
        if (userFetchEvent.isFinished()) {
            SubMenu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).getSubMenu();
            menu.add(Menu.NONE, users.size(), users.size(), userFetchEvent.getUser().getForumNick());
            users.add(userFetchEvent.getUser());
            loadingUsersPb.setProgress(loadingUsersPb.getProgress() + 1, true);
        }
        else {
            loadingUsersPb.setIndeterminate(false);
            loadingUsersPb.setMax(userFetchEvent.getTotalUsers());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameSelectedEvent(final GameSelectedEvent gameSelectedEvent) {
        Intent i = new Intent(this, GameDisplayActivity.class);
        i.putExtra(Extra.POSTED_GAME, gameSelectedEvent.getGame());
        startActivity(i);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkErrorEvent(final MissingConnectionEvent event) {
        loadingUsersTv.setText(R.string.missing_network);
        loadingUsersPb.setVisibility(View.INVISIBLE);
        findViewById(R.id.fab).setEnabled(true);
        operationPending = false;
    }

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

                new UsersAsyncConnector().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                loadingUsersPb.setVisibility(View.VISIBLE);
                loadingUsersPb.setProgress(0, true);
                loadingUsersTv.setVisibility(View.VISIBLE);

                loadingUsersTv.setText(R.string.loading_users);

                getSupportActionBar().setTitle(R.string.app_name);
                getSupportActionBar().setSubtitle("");

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        ListView lv = findViewById(R.id.gameslist);

        if (id == R.id.sort_alpha && !shownGames.isEmpty()) {
            Log.i("GamesListActivity", "Alphabetical order");
            currentRanking = CurrentRanking.ALPHABETICAL;
            rebuildShownGamesList();
        }

        else if (id == R.id.sort_rank && !shownGames.isEmpty()) {
            Log.i("GamesListActivity", "Rank order");
            currentRanking = CurrentRanking.BGG_RANKING;
            rebuildShownGamesList();
        }

        else if (id == R.id.game_random && !shownGames.isEmpty()) {
            EventBus.getDefault().post(new GameSelectedEvent(
                    shownGames.get(new Random().nextInt(shownGames.size()))));
        }

        else if (id == R.id.expansions_toggle && !shownGames.isEmpty()) {
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

        getSupportActionBar().setTitle(activeUser.getForumNick());
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void rebuildShownGamesList() {
        // Build games list
        shownGames.clear();

        for (Game g : activeUser.getGamesCollection()) {

            if (g.isExtension()) {
                if (! expansionsHidden && ! shownGames.contains(g))
                    shownGames.add(g);
            }

            else if (! shownGames.contains(g))
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

        getSupportActionBar().setSubtitle(getResources().getString(R.string.user_game_count, shownGames.size()));
    }
}
