package rocks.massi.trollsgames;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Text;
import rocks.massi.trollsgames.adapter.GamesAdapter;
import rocks.massi.trollsgames.constants.Extra;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.User;
import rocks.massi.trollsgames.async.UsersAsyncConnector;

import java.util.LinkedList;
import java.util.List;

public class GamesListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<User> cachedUsers;
    private List<Game> cachedGames;
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

    @Subscribe
    protected void onFinishEvent(List<User> users) {
        Snackbar.make(findViewById(R.id.fab), "Finished", Snackbar.LENGTH_SHORT).show();
        findViewById(R.id.fab).setEnabled(true);
        ((FloatingActionButton) findViewById(R.id.fab)).show();
        operationPending = false;

        loadingUsersPb.setIndeterminate(false);
        loadingUsersPb.setMax(100);
        loadingUsersPb.setProgress(100, true);

        loadingUsersPb.setVisibility(View.INVISIBLE);
        loadingUsersTv.setText("C'est fini ! Vous pouvez maintenant cliquer sur un utilisateur dans le menu de gauche !");
    }

    @Subscribe
    protected void onUserEvent(final Pair<Boolean, User> progress) {
        if (progress.first) {
            SubMenu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).getSubMenu();
            menu.add(Menu.NONE, cachedUsers.size(), cachedUsers.size(), progress.second.getForumNick());
            cachedUsers.add(progress.second);
        }

        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingUsersTv.setText(Html.fromHtml(String.format("Je télécharge les informations pour l'utilisateur <b>%s</b>",
                            progress.second.getForumNick()),
                            Html.FROM_HTML_MODE_COMPACT));
                }
            });

            Snackbar.make(findViewById(R.id.fab), "Fetching user " + progress.second.getForumNick(), Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Subscribe
    protected void onGameSelectedEvent(Game game) {
        Intent i = new Intent(this, GameDisplayActivity.class);
        i.putExtra(Extra.POSTED_GAME, game);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cachedUsers = new LinkedList<>();
        cachedGames = new LinkedList<>();

        loadingUsersTv = (TextView) findViewById(R.id.loadingUserInfo);
        loadingUsersPb = (ProgressBar) findViewById(R.id.loadingUsersBar);

        loadingUsersPb.setVisibility(View.INVISIBLE);
        loadingUsersTv.setVisibility(View.INVISIBLE);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton f = (FloatingActionButton) findViewById(R.id.fab);
                f.setEnabled(false);
                f.hide();

                // Refresh Submenu
                ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).getSubMenu().clear();
                cachedUsers.clear();
                cachedGames.clear();
                gamesAdapterAdapter.notifyDataSetChanged();
                new UsersAsyncConnector().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                loadingUsersPb.setVisibility(View.VISIBLE);
                loadingUsersTv.setVisibility(View.VISIBLE);
                loadingUsersTv.setText("Je télécharge les informations des utilisateurs...");
                operationPending = true;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ListView lv = (ListView) findViewById(R.id.gameslist);
        gamesAdapterAdapter = new GamesAdapter(getApplicationContext(), 0, cachedGames);
        lv.setAdapter(gamesAdapterAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        if (operationPending) return false;

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        User user = cachedUsers.get(id);

        cachedGames.clear();

        for (Game g : user.getGamesCollection()) {
            if (! g.isExtension()) cachedGames.add(g);
        }

        ListView lv = (ListView) findViewById(R.id.gameslist);
        loadingUsersTv.setVisibility(View.INVISIBLE);

        gamesAdapterAdapter.notifyDataSetChanged();
        lv.setSelectionAfterHeaderView();

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitle(user.getForumNick());
        tb.setSubtitle(user.getCollection().size() + " jeux possédés");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
