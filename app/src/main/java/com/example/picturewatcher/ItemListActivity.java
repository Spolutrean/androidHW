package com.example.picturewatcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private boolean mLoading = false;
    private int mPastVisiblesItems, mVisibleItemCount, mTotalItemCount;
    private LinearLayoutManager mLayoutManager;
    private SimpleItemRecyclerViewAdapter mSimpleItemRecyclerViewAdapter;
    private int pageLoaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
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

    private abstract  class RunnableWithItemAndId implements  Runnable {
        public ImageInformation item;
        public Integer id;

        public RunnableWithItemAndId(Integer id, ImageInformation item) {
            this.item = item;
            this.id = id;
        }
    }

    private void createAndAddNewItems(Integer itemsPerPage) {

        final String apiLink = Constants.UNSPLASH_API_URL + "/photos/" +
                "?client_id=" + Constants.ACCESS_KEY +
                "&page=" + (pageLoaded++) +
                "&per_page=" + itemsPerPage.toString();

        new Thread(new UriLoadRunnable(apiLink) {
            @Override
            public void run() {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<ImageInformation> items = mapper.readValue(
                            new URL(link),
                            new TypeReference<List<ImageInformation>>(){});

                    for(int i = 0; i < items.size(); ++i) {
                        ImageInformation item = items.get(i);
                        new Thread(new RunnableWithItemAndId(i, item) {
                            @Override
                            public void run() {
                                Content.addItem(Content.createItem(id, item));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSimpleItemRecyclerViewAdapter.notifyDataSetChanged();
                                        mLoading = false;
                                    }
                                });
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG, e.toString());
                }
            }
        }).start();
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<Content.Item> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Content.Item item = (Content.Item) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<Content.Item> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Content.Item item = mValues.get(position);

            holder.mItemLayout.setBackgroundColor(Color.parseColor(item.imageInformation.color));

            holder.mContentTextView.setText(item.content);
            holder.mContentImageView.setImageBitmap(item.image);

            holder.itemView.setTag(item);
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mContentTextView;
            final ImageView mContentImageView;
            final LinearLayout mItemLayout;

            ViewHolder(View view) {
                super(view);
                mContentTextView = (TextView) view.findViewById(R.id.content);
                mContentImageView = (ImageView) view.findViewById(R.id.contentImage);
                mItemLayout = (LinearLayout) view.findViewById(R.id.itemLayout);
            }
        }
    }
}
