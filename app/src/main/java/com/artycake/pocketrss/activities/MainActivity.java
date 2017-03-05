package com.artycake.pocketrss.activities;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.adapters.FeedAdapter;
import com.artycake.pocketrss.adapters.CategoriesAdapter;
import com.artycake.pocketrss.dialogs.SourceDialog;
import com.artycake.pocketrss.models.Article;
import com.artycake.pocketrss.models.Category;
import com.artycake.pocketrss.models.Source;
import com.artycake.pocketrss.services.FeedService;
import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.UserPrefs;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FEED_STATE = "feed_state";
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.feed_list)
    RecyclerView feedList;
    @BindView(R.id.categories_list)
    ExpandableListView categoriesList;
    @BindView(R.id.nav_all)
    View navAll;
    @BindView(R.id.nav_favorite)
    View navFavorite;

    private UserPrefs userPrefs;
    private RealmController controller;
    private Realm realm;
    private boolean bound = false;
    private FeedService feedService;
    private ServiceConnection feedServiceConnection;
    private List<Article> articles = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private FeedAdapter feedAdapter;
    private CategoriesAdapter categoriesAdapter;
    private Source selectedSource;
    public boolean isFavorite = false;
    private Parcelable feedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userPrefs = UserPrefs.getInstance(this);
        if (userPrefs.getBoolPref(UserPrefs.USE_DARK_THEME, false)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            feedState = savedInstanceState.getParcelable(FEED_STATE);
        }
        connectService();
        setSupportActionBar(toolbar);
        controller = RealmController.getInstance(this);
        realm = controller.getRealm();

        MobileAds.initialize(this, getResources().getString(R.string.ad_key));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddSourceActivity.class));
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                feedService.updateNews(new FeedService.OnUpdate() {
                    @Override
                    public void onUpdate() {
                        swipeRefreshLayout.setRefreshing(false);
                        refreshViews();
                    }
                });
            }
        });
        feedAdapter = new FeedAdapter(articles);
        feedAdapter.setOnItemClick(new FeedAdapter.OnItemClick() {
            @Override
            public void onClick(Article article) {
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra(ArticleActivity.GUID, article.getGuid());
                startActivity(intent);
            }
        });
        feedList.setAdapter(feedAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        feedList.setLayoutManager(layoutManager);

        categoriesAdapter = new CategoriesAdapter(categories);
        categoriesAdapter.setOnSettingsClickListener(new CategoriesAdapter.OnSettingsClickListener() {
            @Override
            public void onClick(Source source) {
                drawer.closeDrawer(GravityCompat.START);
                new SourceDialog(MainActivity.this, source)
                        .setOnSourceSaved(new SourceDialog.OnSourceSaved() {
                            @Override
                            public void onSave() {
                                refreshViews();
                                refreshCategories();
                            }
                        }).show();
            }
        });
        categoriesList.setAdapter(categoriesAdapter);
        categoriesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                isFavorite = false;
                selectedSource = categories.get(groupPosition).getSources().get(childPosition);
                refreshViews();
                feedList.scrollToPosition(0);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        refreshViews();
        refreshCategories();
        if (categoriesAdapter.getGroupCount() > 0) {
            categoriesList.expandGroup(0);
        }
        navAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = false;
                selectedSource = null;
                refreshViews();
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        navFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = true;
                selectedSource = null;
                refreshViews();
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void refreshViews() {
        RealmResults<Article> articleRealmResults;
        if (isFavorite) {
            articleRealmResults = controller.getFavoriteArticles();
            getSupportActionBar().setTitle(getResources().getString(R.string.nav_favorite));
        } else if (selectedSource != null) {
            articleRealmResults = controller.getArticles(selectedSource);
            getSupportActionBar().setTitle(selectedSource.getName());
        } else {
            articleRealmResults = controller.getArticles();
            getSupportActionBar().setTitle(getResources().getString(R.string.nav_all));
        }
        articles.clear();
        articles.addAll(articleRealmResults);
        feedAdapter.notifyDataSetChanged();
    }

    private void refreshCategories() {
        RealmResults<Category> categoryRealmResults = controller.getNotEmptyCategories();
        categories.clear();
        categories.addAll(categoryRealmResults);
        categoriesAdapter.notifyDataSetChanged();
    }

    private void connectService() {
        feedServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                bound = true;
                feedService = ((FeedService.FeedBinder) binder).getService();
                if (UserPrefs.getInstance(MainActivity.this).getBoolPref(UserPrefs.SHOULD_UPLOAD_NEWS, false)) {
                    UserPrefs.getInstance(MainActivity.this).putPreferences(UserPrefs.SHOULD_UPLOAD_NEWS, false);
                    swipeRefreshLayout.setRefreshing(true);
                    feedService.updateNews(new FeedService.OnUpdate() {
                        @Override
                        public void onUpdate() {
                            refreshViews();
                            refreshCategories();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        Intent serviceIntent = new Intent(this, FeedService.class);
        startService(serviceIntent);
        bindService(serviceIntent, feedServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            bound = false;
            unbindService(feedServiceConnection);
        }
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (feedState != null) {
            feedList.getLayoutManager().onRestoreInstanceState(feedState);
        }
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.SHOULD_UPLOAD_NEWS, false)) {
            if (bound) {
                UserPrefs.getInstance(this).putPreferences(UserPrefs.SHOULD_UPLOAD_NEWS, false);
                swipeRefreshLayout.setRefreshing(true);
                feedService.updateNews(new FeedService.OnUpdate() {
                    @Override
                    public void onUpdate() {
                        refreshViews();
                        refreshCategories();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.ASK_FOR_RATE, false)) {
            askForRate();
        }

        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.THEME_CHANGED, false)) {
            UserPrefs.getInstance(this).putPreferences(UserPrefs.THEME_CHANGED, false);
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    private void askForRate() {
        final UserPrefs userPrefs = UserPrefs.getInstance(this);
        if (!userPrefs.getBoolPref(UserPrefs.ASK_FOR_RATE, true)) {
            return;
        }
        if (RealmController.getInstance(this).getSourcesCount() <= UserPrefs.SOURCES_UNTIL_RATE) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.rate_dialog_title, getResources().getString(R.string.app_name)));
        builder.setMessage(R.string.rate_dialog_message);
        builder.setPositiveButton(R.string.rate_dialog_rate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.rate_dialog_never, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userPrefs.putPreferences(UserPrefs.ASK_FOR_RATE, false);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.rate_dialog_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        feedState = feedList.getLayoutManager().onSaveInstanceState();
        state.putParcelable(FEED_STATE, feedState);
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        if (state != null) {
            feedState = state.getParcelable(FEED_STATE);
        }
    }
}
