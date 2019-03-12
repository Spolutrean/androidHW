package com.example.picturewatcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ItemListActivity extends AppCompatActivity {
    private boolean mTwoPane = false;

    private static boolean mLoading = false;
    private int mPastVisiblesItems, mVisibleItemCount, mTotalItemCount;
    private LinearLayoutManager mLayoutManager;
    private static SimpleItemRecyclerViewAdapter mSimpleItemRecyclerViewAdapter;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.item_detail_container_horizontal) != null) {
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        setupRecyclerScrollListener((RecyclerView) recyclerView);


        createAndAddNewItems(Constants.ITEMS_PER_PAGE);
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
                //check for scroll down
                if(dy > 0) {
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

    private abstract class UriLoadRunnable implements Runnable {
        public String link;

        public UriLoadRunnable(String link) {
            this.link = link;
        }
    }

    private abstract  class RunnableWithItem implements  Runnable {
        public ImageInformation item;

        public RunnableWithItem(ImageInformation item) {
            this.item = item;
        }
    }

    private void createAndAddNewItems(Integer count) {

        final String apiLink = Constants.UNSPLASH_API_URL + "/photos/random" +
                "?client_id=" + Constants.ACCESS_KEY +
                "&count=" + count.toString();

        new Thread(new UriLoadRunnable(apiLink) {
            @Override
            public void run() {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<ImageInformation> items = mapper.readValue(
                            new URL(link),
                            new TypeReference<List<ImageInformation>>(){});

                    DownloadImageService.startLoading(getApplicationContext(), items);

                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG, e.toString());
                }
            }
        }).start();
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

            if(Content.lruCache.get(item.imageInformation.id) != null) {
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
