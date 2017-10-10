package rocks.massi.trollsgames;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.adapter.GamesAdapter;
import rocks.massi.trollsgames.async.UsersAsyncConnector;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.events.GameSelectedEvent;
import rocks.massi.trollsgames.events.UserFetchEvent;
import rocks.massi.trollsgames.events.UsersFetchedEvent;

import java.util.LinkedList;
import java.util.List;

public class GamesListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<User> users;
    private List<Game> shownGames;

    private GamesAdapter gamesAdapterAdapter;
    private boolean operationPending;

    private ProgressBar loadingUsersPb;
    private TextView loadingUsersTv;

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    protected void onFinishEvent(final UsersFetchedEvent usersFetchedEvent) {
        findViewById(R.id.fab).setEnabled(true);
        operationPending = false;

        loadingUsersPb.setVisibility(View.INVISIBLE);

        loadingUsersTv.setText("C'est fini ! Vous pouvez maintenant cliquer sur un utilisateur dans le menu de gauche !");
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    protected void onUserEvent(final UserFetchEvent userFetchEvent) {
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
    protected void onGameSelectedEvent(final GameSelectedEvent gameSelectedEvent) {
        Intent i = new Intent(this, GameDisplayActivity.class);
        i.putExtra(Extra.POSTED_GAME, gameSelectedEvent.getGame());
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        users = new LinkedList<>();
        shownGames = new LinkedList<>();

        loadingUsersTv = findViewById(R.id.loadingUserInfo);
        loadingUsersPb = findViewById(R.id.gamesLoadingBar);
        loadingUsersTv.setTypeface(Typeface.createFromAsset(getAssets(), "font/Raleway-Regular.ttf"));

        loadingUsersTv.setText("Veuillez cliquer sur le petit bouton en bas à droite pour " +
                "télécharger les informations des utilisateurs !");

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
                gamesAdapterAdapter.notifyDataSetChanged();

                new UsersAsyncConnector().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                loadingUsersPb.setVisibility(View.VISIBLE);
                loadingUsersPb.setProgress(0, true);
                loadingUsersTv.setVisibility(View.VISIBLE);

                loadingUsersTv.setText("Je télécharge les informations des utilisateurs...");

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
        gamesAdapterAdapter = new GamesAdapter(getApplicationContext(), 0, shownGames);
        lv.setAdapter(gamesAdapterAdapter);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (operationPending) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        User user = users.get(id);

        shownGames.clear();

        for (Game g : user.getGamesCollection()) {
            if (! g.isExtension() && ! shownGames.contains(g))
                shownGames.add(g);
        }

        ListView lv = findViewById(R.id.gameslist);
        loadingUsersTv.setVisibility(View.INVISIBLE);

        gamesAdapterAdapter.notifyDataSetChanged();
        lv.setSelectionAfterHeaderView();

        getSupportActionBar().setTitle(user.getForumNick());
        getSupportActionBar().setSubtitle(user.getCollection().size() + " jeux possédés");

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
