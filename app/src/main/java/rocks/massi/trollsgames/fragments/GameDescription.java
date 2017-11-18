package rocks.massi.trollsgames.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.data.Game;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameDescription#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameDescription extends Fragment {
    private Game shownGame;

    public GameDescription() {
        // Required empty public constructor
    }

    public static GameDescription newInstance(Game shownGame) {
        GameDescription fragment = new GameDescription();
        fragment.shownGame = shownGame;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_game_description, container, false);
        TextView description = ret.findViewById(R.id.game_description);
        description.setText(Html.fromHtml(shownGame.getDescription()));
        return ret;
    }

}
