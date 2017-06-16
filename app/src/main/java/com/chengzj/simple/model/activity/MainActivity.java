package com.chengzj.simple.model.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.chengzj.simple.R;
import com.chengzj.simple.RxApplication;
import com.chengzj.simple.base.BaseActivity;
import com.chengzj.simple.data.source.remote.FailedEvent;
import com.chengzj.simple.data.source.remote.HttpImpl;
import com.chengzj.simple.data.source.remote.MessageType;
import com.chengzj.simple.model.entity.Token;
import com.chengzj.simple.util.PreferenceConstants;
import com.chengzj.simple.util.PreferenceUtils;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            HttpImpl.getInstance().login("Basic dG1qMDAxOjEyMzQ1Ng==");
        } else if (id == R.id.nav_gallery) {
            HttpImpl.getInstance().login("Basic dG1qMDAxOjEyMzQ1Ng==");
        } else if (id == R.id.nav_slideshow) {
            HttpImpl.getInstance().login("Basic dG1qMDAxOjEyMzQ1Ng==");
        } else if (id == R.id.nav_manage) {
            HttpImpl.getInstance().login("Basic dG1qMDAxOjEyMzQ1Ng==");
        } else if (id == R.id.nav_share) {
            HttpImpl.getInstance().login("Basic dG1qMDAxOjEyMzQ1Ng==");
        } else if (id == R.id.nav_send) {
            HttpImpl.getInstance().login("Basic dG1qMDAxOjEyMzQ1Ng==");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onEventMainThread(Object event) {
        super.onEventMainThread(event);
        if (event instanceof Token) {
            Token token = (Token) event;
            String accessToken = token.getAccess_token();
            PreferenceUtils.setPrefString(RxApplication.getInstance(), PreferenceConstants.REFRESH_TOKEN, token.getRefresh_token());
            Toast.makeText(MainActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
        }
        if (event instanceof FailedEvent){
            int type = ((FailedEvent) event).getType();
            switch (type) {
                case MessageType.LOGIN:
                    Toast.makeText(this, "登录失败！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }
}
