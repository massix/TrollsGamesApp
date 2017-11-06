package rocks.massi.trollsgames.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.fragments.GameDescription;
import rocks.massi.trollsgames.fragments.InternetSearch;

public class GamesDisplayFragmentAdapter extends FragmentPagerAdapter {
    private Game shownGame;
    private Context context;

    public GamesDisplayFragmentAdapter(FragmentManager fm, Game shownGame, Context context) {
        super(fm);
        this.shownGame = shownGame;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment ret = null;

        switch (position) {
            case 0:
                ret = InternetSearch.newInstance(shownGame);
                break;
            case 1:
                ret = GameDescription.newInstance(shownGame);
                break;
        }

        return ret;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.fragment_search_title);
            case 1:
                return context.getResources().getString(R.string.fragment_description_title);
        }

        return super.getPageTitle(position);
    }
}
