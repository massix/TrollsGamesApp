package rocks.massi.trollsgames.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InternetSearch.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InternetSearch#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InternetSearch extends Fragment {
    private OnFragmentInteractionListener mListener;

    private GamesServicesAdapter adapter;
    private List<GameSearchService> gameSearchServices;
    private ProgressBar progressBar;
    private Game shownGame;

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final GameSearchService event) {
        Log.i(getClass().toString(), "Open on external service");
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(event.getUrl())));
    }


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
    public void onEvent(final MissingConnectionEvent event) {
        TextView header = getView().findViewById(R.id.philibert_header);
        header.setText(R.string.missing_network);
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

        gameSearchServices = new LinkedList<>();
        adapter = new GamesServicesAdapter(getContext(), 0, gameSearchServices);
        progressBar = getView().findViewById(R.id.search_progress_bar);

        ListView searchResults = getView().findViewById(R.id.search_results_view);
        searchResults.setAdapter(adapter);

        TextView header = getView().findViewById(R.id.philibert_header);
        header.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "font/Raleway-Regular.ttf"));

        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_internet_search, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
