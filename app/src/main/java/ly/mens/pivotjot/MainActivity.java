package ly.mens.pivotjot;

import android.animation.Animator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ly.mens.pivotjot.util.SimpleAnimatorListener;


/**
 * Navigation control Activity
 * Created by mensly on 3/05/2015.
 */
public class MainActivity extends AppCompatActivity {
    private static final boolean TEST_LOGIN = false;

    @InjectView(R.id.overlay)
    View overlayContainer;
    @InjectView(R.id.fragment_overlay)
    View overlayFragment;

    MenuItem logoutItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        if (savedInstanceState == null && (TEST_LOGIN || !PivotalService.hasToken(this))) {
            showLogin();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        logoutItem = menu.findItem(R.id.menu_logout);
        if (getFragmentManager().findFragmentById(R.id.fragment_overlay) instanceof LoginFragment) {
            logoutItem.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                PivotalService.logout(this);
                showLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showLogin() {
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentById(R.id.fragment_overlay) != null) {
            // Already showing overlay
            return;
        }
        if (overlayContainer.getAnimation() != null) {
            // Already animating
            return;
        }
        if (logoutItem != null) {
            logoutItem.setEnabled(false);
        }
        overlayContainer.setAlpha(0);
        overlayContainer.setVisibility(View.VISIBLE);
        fm.beginTransaction().add(R.id.fragment_overlay, new LoginFragment()).commit();
        Resources res = getResources();
        overlayFragment.setTranslationY(-res.getDimension(R.dimen.login_height) - res.getDimension(R.dimen.login_margin));
        int animateTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        overlayContainer.animate().alpha(1).setDuration(animateTime).start();
        overlayFragment.animate().translationY(0).setDuration(animateTime).start();
    }

    public void clearOverlay() {
        if (overlayContainer.getAnimation() != null) {
            // Already animating
            return;
        }
        logoutItem.setEnabled(true);
        int animateTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        overlayContainer.animate().alpha(0).setDuration(animateTime).setListener(new SimpleAnimatorListener()
        {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                FragmentManager fm = getFragmentManager();
                Fragment overlay = fm.findFragmentById(R.id.fragment_overlay);
                fm.beginTransaction().remove(overlay).commit();
                overlayContainer.setVisibility(View.GONE);
            }
        }).start();
    }
}
