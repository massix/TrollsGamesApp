package rocks.massi.trollsgames.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.fragments.GameDescription;
import rocks.massi.trollsgames.fragments.InternetSearch;

public class GamesDisplayFragmentAdapter extends FragmentPagerAdapter {
    private Game shownGame;

    public GamesDisplayFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public GamesDisplayFragmentAdapter(FragmentManager fm, Game shownGame) {
        super(fm);
        this.shownGame = shownGame;
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
                return "Recherche Internet";
            case 1:
                return "Description du jeu";
        }

        return super.getPageTitle(position);
    }
}
