package ly.mens.pivotjot;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;


/**
 * Allow user to authenticate with pivotal tracker
 * Created by mensly on 3/05/2015.
 */
public class LoginFragment extends Fragment implements TextView.OnEditorActionListener {
    private static final String URI_FORGOT_PW = "https://www.pivotaltracker.com/signin/passwords/new?username=";

    @InjectView(R.id.username)
    EditText username;
    @InjectView(R.id.password)
    EditText password;
    @InjectViews({R.id.username, R.id.password, R.id.btn_forgot_pw, R.id.btn_login})
    List<View> actionViews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        password.setOnEditorActionListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        username.requestFocus();
        username.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (username != null) {
                    username.requestFocus();
                    InputMethodManager input = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    input.showSoftInput(username, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 300);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PivotalService.BROADCAST_AUTH_SUCCESS);
        intentFilter.addAction(PivotalService.BROADCAST_AUTH_ERROR);
        intentFilter.addAction(PivotalService.BROADCAST_NETWORK_ERROR);
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(receiver, new IntentFilter(PivotalService.BROADCAST_AUTH_SUCCESS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(receiver);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        login();
        return true;
    }

    @OnClick(R.id.btn_forgot_pw)
    public void forgotPassword() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URI_FORGOT_PW + username.getText().toString())));
    }

    @OnClick(R.id.btn_login)
    public void login() {
        String username = this.username.getText().toString();
        if (username.length() == 0) {
            Toast.makeText(getActivity(), R.string.error_username, Toast.LENGTH_SHORT).show();
            this.username.requestFocus();
        }
        String password = this.password.getText().toString();
        if (password.length() == 0) {
            Toast.makeText(getActivity(), R.string.error_username, Toast.LENGTH_SHORT).show();
            this.password.requestFocus();
        }
        setEnabled(false);
        PivotalService.authenticate(getActivity(), username, password);
    }

    private void setEnabled(boolean enabled) {
        View view = getView();
        if (view != null) {
            view.setAlpha(enabled ? 1 : 0.8f);
            for (View actionView : actionViews) {
                actionView.setEnabled(enabled);
            }
        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                // Handle invalid data
                return;
            }
            switch (action) {
                case PivotalService.BROADCAST_AUTH_ERROR:
                    Toast.makeText(getActivity(), R.string.error_login, Toast.LENGTH_SHORT).show();
                    break;
                case PivotalService.BROADCAST_NETWORK_ERROR:
                    Toast.makeText(getActivity(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    break;
                case PivotalService.BROADCAST_AUTH_SUCCESS:
                    Fragment main = getFragmentManager().findFragmentById(R.id.fragment_content);
                    if (main instanceof JotFragment) {
                        ((JotFragment) main).onLoggedIn();
                    }
                    Activity activity = getActivity();
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).clearOverlay();
                    }
                    break;
            }
            setEnabled(true);
        }
    };
}
