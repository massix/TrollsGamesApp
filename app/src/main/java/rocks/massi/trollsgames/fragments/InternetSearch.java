package rocks.massi.trollsgames.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.adapter.GamesServicesAdapter;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.data.GameSearchService;
import rocks.massi.trollsgames.data.ThirdPartyServices;
import rocks.massi.trollsgames.events.GameFoundOnPhilibertEvent;
import rocks.massi.trollsgames.events.GameFoundOnTricTracEvent;
import rocks.massi.trollsgames.events.MissingConnectionEvent;

import java.util.LinkedList;
import java.util.List;

public class InternetSearch extends Fragment {
    private GamesServicesAdapter adapter;
    private List<GameSearchService> gameSearchServices;
    private ProgressBar progressBar;
    private Game shownGame;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final GameSearchService event) {
        Log.i(getClass().toString(), "Open on external service");
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(event.getUrl())));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final GameFoundOnPhilibertEvent event) {
        gameSearchServices.add(new GameSearchService(ThirdPartyServices.PHILIBERT,
                shownGame,
                event.getPhilibertSearchResponse().getProduct_link(),
                event.getPhilibertSearchResponse().getPname()));
        progressBar.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged();
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final MissingConnectionEvent event) {
        progressBar.setVisibility(View.INVISIBLE);
    }


    public InternetSearch() {
        // Required empty public constructor
    }

    public static InternetSearch newInstance(Game shownGame) {
        InternetSearch fragment = new InternetSearch();
        fragment.shownGame = shownGame;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_internet_search, container, false);

        gameSearchServices = new LinkedList<>();
        adapter = new GamesServicesAdapter(getContext(), 0, gameSearchServices);
        progressBar = ret.findViewById(R.id.search_progress_bar);

        ListView searchResults = ret.findViewById(R.id.search_results_view);
        searchResults.setAdapter(adapter);

        return ret;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
