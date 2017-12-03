package rocks.massi.trollsgames.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import com.airbnb.deeplinkdispatch.DeepLink;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.fragments.Login;
import rocks.massi.trollsgames.fragments.Register;

@DeepLink("tdj://massi.rocks/login")
public class LoginRegisterActivity extends AppCompatActivity {
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        viewPager = findViewById(R.id.lr_pager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new Login();
                    case 1:
                        return new Register();
                    default:
                        return null;
                }
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });

        // Configure toolbar
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        final ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Login").setTabListener(tabListener));
        getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Register").setTabListener(tabListener));
    }
}
