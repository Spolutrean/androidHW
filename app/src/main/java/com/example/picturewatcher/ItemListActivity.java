package com.example.picturewatcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.picturewatcher.Database.LocalDatabaseAPI;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ItemListActivity extends AppCompatActivity {
    private boolean mTwoPane = false;
    private static boolean mInGalery = false, mLoading = false;
    private Integer mPastVisiblesItems, mVisibleItemCount, mTotalItemCount, mPageNumForLoading = 1;
    private String mSearchText = "";
    private LinearLayoutManager mLayoutManager;
    private static SimpleItemRecyclerViewAdapter mSimpleItemRecyclerViewAdapter;
    private static Context context;
    public static LocalDatabaseAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        context = this;
        Constants.PATH_FOR_LOADED_FILES = getFilesDir().getParent() + "/app_loaded_images/";
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        api = new LocalDatabaseAPI(context);

        if (findViewById(R.id.item_detail_container_horizontal) != null) {
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        setupRecyclerScrollListener((RecyclerView) recyclerView);

        setupClickListenerOnToolbarSearchButton();
        setupClickListenerOnToolbarLikesButton();

        createAndAddNewItems(Constants.ITEMS_PER_PAGE);
    }

    private void setupClickListenerOnToolbarLikesButton() {
        findViewById(R.id.likesViewButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInGalery) {
                    mInGalery = false;
                } else {
                    mInGalery = true;
                    mPageNumForLoading = 1;
                }
                Content.ITEMS.clear();
                Content.ITEM_MAP.clear();
                mSimpleItemRecyclerViewAdapter.notifyDataSetChanged();
                createAndAddNewItems(Constants.ITEMS_PER_PAGE);
            }
        });
    }

    private void setupClickListenerOnToolbarSearchButton() {
        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchText = ((EditText) findViewById(R.id.searchText)).getText().toString();
                Content.ITEMS.clear();
                Content.ITEM_MAP.clear();
                mPageNumForLoading = 1;
                mSimpleItemRecyclerViewAdapter.notifyDataSetChanged();
                createAndAddNewItems(Constants.ITEMS_PER_PAGE);
            }
        });
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mSimpleItemRecyclerViewAdapter = new SimpleItemRecyclerViewAdapter(this, com.example.picturewatcher.Content.ITEMS, mTwoPane);
        recyclerView.setAdapter(mSimpleItemRecyclerViewAdapter);
    }

    public static void addNewRecyclerItem(Content.Item item) {
        synchronized (Content.ITEMS) {
            Content.addItem(item);
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSimpleItemRecyclerViewAdapter.notifyItemChanged(Content.ITEMS.size() - 1);
                    mLoading = false;
                }
            });
        }
    }

    private void setupRecyclerScrollListener(@NonNull RecyclerView recyclerView) {
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    mVisibleItemCount = mLayoutManager.getChildCount();
                    mTotalItemCount = mLayoutManager.getItemCount();
                    mPastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!mLoading) {
                        if ((mVisibleItemCount + mPastVisiblesItems) >= mTotalItemCount) {
                            mLoading = true;
                            createAndAddNewItems(Constants.ITEMS_PER_PAGE);
                        }
                    }
                }
            }
        });
    }

    private void createAndAddNewItems(Integer count) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ImageInformation> items;
                    if(mInGalery) {
                        items = api.getFavouritesFromDatabase(mPageNumForLoading);
                        mPageNumForLoading++;
                    } else if (mSearchText.length() != 0) {
                        String apiLink = Constants.UNSPLASH_API_URL + "/search/photos" +
                                "?client_id=" + Constants.ACCESS_KEY +
                                "&per_page=" + count.toString() +
                                "&query=" + mSearchText +
                                "&page=" + mPageNumForLoading.toString();
                        mPageNumForLoading++;
                        items = getSearchedPhotosList(apiLink);
                    } else {
                        String apiLink = Constants.UNSPLASH_API_URL + "/photos/random" +
                                "?client_id=" + Constants.ACCESS_KEY +
                                "&count=" + count.toString();
                        items = getRandomPhotosList(apiLink);
                    }
                    DownloadImageService.startLoading(getApplicationContext(), items);
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG, e.toString());
                }
            }
        }).start();
    }

    private static List<ImageInformation> getRandomPhotosList(String url) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                new URL(url),
                new TypeReference<List<ImageInformation>>() {
                });
    }

    private static List<ImageInformation> getSearchedPhotosList(String url) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                new URL(url),
                SearchImageInformation.class).results;
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final boolean mTwoPane;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Content.Item item = (Content.Item) view.getTag();
                Bundle arguments = new Bundle();
                arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.imageInformation.id);
                ItemDetailFragment fragment = new ItemDetailFragment();
                fragment.setArguments(arguments);
                if (mTwoPane) {
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container_horizontal, fragment)
                            .commit();


                } else {
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .add(R.id.frameLayout, fragment)
                            .addToBackStack(null)
                            .commit();

                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<Content.Item> items,
                                      boolean twoPane) {
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        ThreadPoolExecutor ex = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Content.Item item = Content.ITEMS.get(position);

            holder.mItemLayout.setBackgroundColor(Color.parseColor(item.imageInformation.color));
            holder.mContentImageView.setImageBitmap(Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565));

            if (Content.lruCache.get(item.imageInformation.id) != null) {
                holder.mContentImageView.setImageBitmap(Content.lruCache.get(item.imageInformation.id));
            } else {
                ex.submit(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = Content.checkInternalStorage(item.smallPicturePath);
                        Content.lruCache.put(item.imageInformation.id, bitmap);
                        mParentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.mContentImageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                });
            }

            holder.itemView.setTag(item);
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return Content.ITEMS.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView mContentImageView;
            final LinearLayout mItemLayout;

            ViewHolder(View view) {
                super(view);
                mContentImageView = (ImageView) view.findViewById(R.id.contentImage);
                mItemLayout = (LinearLayout) view.findViewById(R.id.itemLayout);
            }
        }
    }
}
