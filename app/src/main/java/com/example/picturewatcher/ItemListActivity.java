package com.example.picturewatcher;

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
import android.widget.TextView;

import com.example.picturewatcher.Utils.Triple;
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

    private boolean mLoading = true;
    private int mPastVisiblesItems, mVisibleItemCount, mTotalItemCount;
    private LinearLayoutManager mLayoutManager;

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


        createAndAddNewItems(Constants.ITEMS_PER_PAGE, 1);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, com.example.picturewatcher.Content.ITEMS, mTwoPane));
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

                    if (mLoading) {
                        if ((mVisibleItemCount + mPastVisiblesItems) >= mTotalItemCount) {
                            mLoading = false;
                            createAndAddNewItems(Constants.ITEMS_PER_PAGE, 1);
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

    private abstract class AddItemsRunnable implements Runnable {
        public AddItemsRunnable(List<ImageInformation> items) {
            this.items = items;
        }

        public List<ImageInformation> items;
    }

    private void createAndAddNewItems(Integer itemsPerPage, Integer pageNumber) {

        final String apiLink = Constants.UNSPLASH_API_URL + "/photos/" +
                "?client_id=" + Constants.ACCESS_KEY +
                "&page=" + pageNumber.toString() +
                "&per_page=" + itemsPerPage.toString();

        new Thread(new UriLoadRunnable(apiLink) {
            @Override
            public void run() {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<ImageInformation> items = mapper.readValue(new URL(link), new TypeReference<List<ImageInformation>>(){});
                    runOnUiThread(new AddItemsRunnable(items) {
                        @Override
                        public void run() {
                            for(ImageInformation item : items) {
                                Content.addItem(Content.createItem(Content.ITEMS.size(), item));
                            }
                        }
                    });
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

            holder.mContentTextView.setText(item.content);
            holder.mContentTextView.setBackgroundColor(Color.parseColor(item.imageInformation.color));

            if(item.image != null) {
                holder.mContentImageView.setImageBitmap(item.image);
            } else {
                holder.mContentImageView.setImageBitmap(Bitmap.createBitmap(
                    Constants.MEDIUM_IMAGE_W,
                    Constants.MEDIUM_IMAGE_H,
                    Bitmap.Config.valueOf(item.imageInformation.color)));

                new ImageDownloadTask().execute(new Triple<>(
                        item.imageInformation.urls.raw,
                        item.image,
                        holder.mContentImageView
                ));
            }

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

            ViewHolder(View view) {
                super(view);
                mContentTextView = (TextView) view.findViewById(R.id.content);
                mContentImageView = (ImageView) view.findViewById(R.id.contentImage);
            }
        }
    }
}
