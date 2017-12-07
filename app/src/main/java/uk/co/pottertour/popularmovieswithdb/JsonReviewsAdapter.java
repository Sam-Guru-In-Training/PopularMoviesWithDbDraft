package uk.co.pottertour.popularmovieswithdb;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static uk.co.pottertour.popularmovieswithdb.utilities.MoviesDBJsonUtils.MDB_REVIEW_AUTHOR_KEY;
import static uk.co.pottertour.popularmovieswithdb.utilities.MoviesDBJsonUtils.MDB_REVIEW_TEXT_KEY;

/**
 * Created by Sam on 13/08/2017.
 * Converts data into views for MainActivity to draw
 */
// TODO 1 save everything to a DB
// include columns for reviews object and trailer Uris

public class JsonReviewsAdapter extends RecyclerView.Adapter<JsonReviewsAdapter.DetailsAdapterViewHolder> {

    private final String TAG = JsonReviewsAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_TOP = 0;
    private static final int VIEW_TYPE_LIST_ITEM = 1;
    private int expandedPosition = -1; // holds the position of the present expanded list item

    private Cursor mCursor;
    //private List<String> mReviewsList;
    private JSONArray mReviewJsonArray;
    /* The context we use to utility methods, app resources and layout inflaters */
    private Context mContext; // final
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final DetailsAdapterOnClickHandler mClickHandler;

    // used for databinding, avoids findViewById repetition
    //private ActivityDetailBinding mDetailBinding;

    /**
     * The interface that receives onClick messages.
     */
    public interface DetailsAdapterOnClickHandler {
        void onClick(String trailerUrl);
    }

    /**
     * Creates a MoviesAdapter.
     *
     * @param context      Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public JsonReviewsAdapter(@NonNull Context context, DetailsAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;

    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class DetailsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mAuthorTV;
        TextView mReviewTV;
        ImageView mExpandBtnIV;
        ImageView mMinimiseBtnIV;

        public DetailsAdapterViewHolder(View view) {
            super(view);

            mAuthorTV = (TextView) view.findViewById(R.id.tv_reviewer_name);
            mReviewTV = (TextView) view.findViewById(R.id.tv_review_text);
            mExpandBtnIV = (ImageView) view.findViewById(R.id.expand_button);
            mMinimiseBtnIV = (ImageView) view.findViewById(R.id.minimise_button);

//            mFavourite = (ToggleButton) view.findViewById(R.id.button_favourite);
            //mFavourite.setOnClickListener(this);
            // ClickListener for entire list item
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param view The View that was clicked
         */
        @Override
        public void onClick(View view) {
            Log.i(TAG + " onClick", "button clicked");

            int adapterPosition = getAdapterPosition();

            RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) view.getTag();

            // TODO check if view is already expanded if so minimise IS THIS WORKING?
            if (adapterPosition == expandedPosition) {
                Log.i(TAG + " onClick", "trying to minimise the box");
                expandedPosition = -10;
                notifyItemChanged(adapterPosition);
                return;
            }

            // Check for an expanded item view elsewhere, collapse if you find one
            if (expandedPosition >= 0) {
                int previousExpandedReview = expandedPosition;
                notifyItemChanged(previousExpandedReview);
            }
            // Set the current position to "expanded"
            expandedPosition = adapterPosition;
            notifyItemChanged(expandedPosition);
            notifyDataSetChanged();

            //TODO load expanded view
            //mCursor.moveToPosition(adapterPosition);

            //long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);


            //Log.v(TAG, "sending URL str to DetailActivity: " + reviewUrlStr);
            //mClickHandler.onClick(reviewUrlStr);

/*
            Log.v(TAG, "adapter pos = " + adapterPosition);
            //TODO get the trailers array, index to the write one, launch an internet inte


            //https://stackoverflow.com/questions/36759744/onclicklistener-on-the-specific-item-of-the-recyclerview-in-android
            if (view.getId() == mFavourite.getId()) {
                Log.v(TAG, "just clicked FAVOURITE!!!!!!!!!!");
                Boolean faveBool = !(MainActivity.mFave); //mCursor.getString(DetailActivity.INDEX_MOVIE_FAVOURITE);
                MainActivity.mFave = faveBool;
                // now set the button state to Stick to pressed or depressed
                ((ToggleButton) view).setChecked(faveBool);
                Log.v(TAG, "Button value = " + faveBool);
                Log.v(TAG, "Reversed Button value = " + faveBool);

                //TODO saving to db, is this best practice, should I use a loader instead?
                ContentResolver moviesContentResolver = mContext.getContentResolver();
                ContentValues favouriteContentValue = new ContentValues();
                favouriteContentValue.put(MoviesContract.MoviesEntry.COLUMN_FAVOURITE, faveBool.toString());

                String selection = MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?";
                // selectionArgs
                //String movieIdStr = mCursor.getString(DetailActivity.INDEX_MOVIE_ID);
                String movieIdStr = String.valueOf(DetailActivity.INDEX_MOVIE_ID);
                String[] selectionArgs = { "" + movieIdStr};
                *//* update our contentProvider with trailers for the appropriate row *//*
                int rowUpdated = moviesContentResolver.update(
                        MoviesContract.MoviesEntry.INSERT_DETAILS_URI, //CONTENT_URI,
                        favouriteContentValue,
                        selection,
                        selectionArgs);

                if (rowUpdated == 0) {
                    Log.wtf(TAG, "toggled favourite but db hasn't been updated WTH?!");
                    // TODO work out how to make method throw an exception without try catch
                    //throw new Exception("Favourite toggled but db not updated, weird?");
                }
                else {
                    Log.wtf(TAG, "updated db with new favourite value, 1 row? " + rowUpdated);
                    // ToggleButton state again
                }
                return;
                //Toast.makeText(mContext, "favourite btn pressed", Toast.LENGTH_SHORT).show();
            }
            else if (adapterPosition == 0) {
                // header otherwise not a clickable field
                return;
            }
            // TODO else we have a list item, and need to launch the url
            String trailerUrlStr = mReviewJsonArray.get(adapterPosition - 1); // subtract one to account for header

            //String trailersStr = mCursor.getString(DetailActivity.INDEX_MOVIE_TRAILERS);
            //String reviewsStr = mCursor.getString(DetailActivity.INDEX_MOVIE_REVIEWS);
            //MovieObject movieData = mMoviesData[adapterPosition];
            //mCursor.moveToPosition(adapterPosition - 1);
            //if (view.getTag() == mContext.getResources().getString(R.string.trailersTag)) {
            Log.v("MoviesAdapter", "A trailer has been clicked");
                // NEED TO SORT OUT THE PROJECTION TO GET THIS
//                trailersStr = mCursor.getString(DetailActivity.INDEX_MOVIE_TRAILERS);
//                Log.wtf(TAG, "adapter opening trailer url: " + trailersStr);
                //TODO decode them back from a ArrayList<String>
                // https://stackoverflow.com/questions/12276205/java-convert-arraylist-to-string-and-back-to-arraylist

//            }
//            else if (view.getTag() == mContext.getResources().getString(R.string.reviewsTag)) {
//                Log.v("MoviesAdapter", "A review has been clicked");
//                // String reviewsStr;
//                trailersStr = mCursor.getString(DetailActivity.INDEX_MOVIE_REVIEWS);
//                //TODO decode them back from a ArrayList<String>
//                //Log.wtf(TAG, "adapter opening reviews: " + reviewsStr);
//                //ArrayList<String> reviews = new ArrayList<String>(reviewsStr);
//            }
            //ArrayList<String> trailerUrlArray = (ArrayList<String>) Arrays.asList(trailersStr.split(","));
            //String trailerUrl = trailerUrlArray.get(adapterPosition - 1);
            //double movieId = mCursor.getDouble(MainActivity.INDEX_MOVIE_ID);
            Log.wtf("MoviesAdapter", "A view has been clicked: " + view.getId());
            Log.v("MoviesAdapter", "Do we have Trailers/reviews?: " + trailerUrlStr);
            //Log.v("MoviesAdapter", "Do we have Trailers/reviews?: " + reviewsStr);
            //Log.v("MoviesAdapter", "Trailer ID: " + R.id.tv_reviews_label);
            //mClickHandler.onClick(movieData);
            mClickHandler.onClick(trailerUrlStr);*/
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public DetailsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId;
        String layoutTag;

//        switch (viewType) {
//            case VIEW_TYPE_TOP:
//                layoutId = R.layout.primary_detail_info;
//                break;
//            case VIEW_TYPE_LIST_ITEM:
                //Log.wtf(TAG, "Setting Layout for a review");
                // USED for trailer and review rows
                layoutId = R.layout.review_list_item;
//                break;
//            default:
//                throw new IllegalArgumentException("" + viewType);
//        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);

        view.setFocusable(true);

        return new DetailsAdapterViewHolder(view);


//        Context context = viewGroup.getContext();
//        int layoutIdForListItem = R.layout.movie_list_item;
//        LayoutInflater inflater = LayoutInflater.from(context);
//        boolean shouldAttachToParentImmediately = false;
//        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
//
//        view.setFocusable(true);
//        // TODO IS THIS BEST PRACTICE?  IS THERE A BETTER WAY TO GET CONTEXT FOR onBindViewHolder??
//        mContext = context;
//
//        return new DetailsAdapterViewHolder(view);

    }
    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param detailsAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(DetailsAdapterViewHolder detailsAdapterViewHolder, int position) {

        /* https://stackoverflow.com/questions/27203817/recyclerview-expand-collapse-items */
        // check for an expanded view, collapse if you find one, SO COLLAPSES ON SCROLL?



        //mCursor.moveToPosition(position);
        //MovieObject movieDetails = mMoviesData[position];
        int viewType = getItemViewType(position);

        /*switch (viewType) {
              case VIEW_TYPE_TOP:
                // attach the movie details such as title etc.
                String title = MainActivity.mTitle;//mCursor.getString(DetailActivity.INDEX_MOVIE_TITLE);
                detailsAdapterViewHolder.mTitle.setText(title);

                String dateText = MainActivity.mReleaseDate; //mCursor.getString(DetailActivity.INDEX_MOVIE_RELEASE_DATE);
                detailsAdapterViewHolder.mReleaseDate.setText(dateText);

                String voteAverage = MainActivity.mRating; // mCursor.getString(DetailActivity.INDEX_MOVIE_RATING)  + "/10";
                detailsAdapterViewHolder.mVoteAvg.setText(voteAverage);

                String overview = MainActivity.mSynopsis; // mCursor.getString(DetailActivity.INDEX_MOVIE_OVERVIEW);
                detailsAdapterViewHolder.mSynopsis.setText(overview);

                // Boolean.valueOf(mCursor.getString(DetailActivity.INDEX_MOVIE_FAVOURITE));
                detailsAdapterViewHolder.mFavourite.setPressed(MainActivity.mFave);

                String posterUrlString = MainActivity.mPosterPath; // mCursor.getString(DetailActivity.INDEX_MOVIE_POSTER);
                //ImageView mPosterIV = (ImageView) findViewById(R.id.iv_detail_poster_thumbnail);
                // TODO does picasso magically cache it to prevent re-requests?  How should I do it different?
                Picasso.with(mContext)
                        .load(posterUrlString).fit()
                        .into(detailsAdapterViewHolder.mPosterImageView);

        *//* Store a summary for sharing with friends *//*
                //mMovieSummary = String.format("%s - %s", title, voteAverage);

                break;
            case VIEW_TYPE_LIST_ITEM:*/
                Log.i(TAG + "onBindViewHolder", "laying out review " + position);
                //detailsAdapterViewHolder.mLinkTV.setText("Review " + (position + 1));

        String authorName = "Review by ";
        String reviewText = "Error loading review";
        try {
            JSONObject newJsonObject = mReviewJsonArray.getJSONObject(position);
            authorName = authorName + newJsonObject.getString(MDB_REVIEW_AUTHOR_KEY);
            reviewText = newJsonObject.getString(MDB_REVIEW_TEXT_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        detailsAdapterViewHolder.mAuthorTV.setText(authorName);
        detailsAdapterViewHolder.mReviewTV.setText(reviewText);

        if (position == expandedPosition) {
            Log.e(TAG + " onBindViewHolder", "expanded position show review");
            detailsAdapterViewHolder.mReviewTV.setVisibility(View.VISIBLE);
            detailsAdapterViewHolder.mMinimiseBtnIV.setVisibility(View.VISIBLE);
            detailsAdapterViewHolder.mExpandBtnIV.setVisibility(View.GONE);
        } else {
            Log.i(TAG + " onBindViewHolder", "hiding review text");
            detailsAdapterViewHolder.mReviewTV.setVisibility(View.GONE);
            detailsAdapterViewHolder.mMinimiseBtnIV.setVisibility(View.GONE);
            detailsAdapterViewHolder.mExpandBtnIV.setVisibility(View.VISIBLE);
        }

                //detailsAdapterViewHolder.mReviewLink.setText("Review " + position);

          /*      break;
            default:
                throw new IllegalArgumentException("" + viewType);
        }*/

//        String moviePoster = mCursor.getString(MainActivity.INDEX_POSTER_PATH);
//
//        Picasso.with(mContext)
//                .load(moviePoster).fit()
//                .into(detailsAdapterViewHolder.mMoviesImageView);
//        Picasso.with(mContext) // TODO IS GETTING CONTEXT THIS WAY BEST PRACTICE?
//                .load(movieDetails.getPoster()).fit()
//                .into(moviesAdapterViewHolder.mMoviesImageView);

    }
    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position index within our RecyclerView and Cursor
     * @return the view type (today or future day)
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_TOP;
        } else {
            return VIEW_TYPE_LIST_ITEM;
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (mReviewJsonArray == null) return 0;
        //Log.i(TAG, "getItemCount: we have " + (mReviewJsonArray.length()) + " reviews\n" + mReviewJsonArray);
        return mReviewJsonArray.length(); //size();
        //if (mCursor == null) return 0;
        //return mCursor.getCount();
    }

    /**
     * Swaps the cursor used by the MovieAdapter for its movie data. This method is called by
     * DetailActivity after a load has finished, as well as when the Loader responsible for loading
     * the movie data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * This method is used to set the movie details on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param newReviewsArray The new authors & reviews to be displayed.
     */
    public void setMoviesData(JSONArray newReviewsArray) {//List<String> trailersList) {
        //Log.v(TAG, "setMoviesData, db list = " + newReviewsArray);
        mReviewJsonArray = newReviewsArray;
        notifyDataSetChanged();
    }

    /*
    * Returns an ArrayList of all MovieObjects for putting into a parceable onSaveInstanceState
     */
//    public ArrayList<MovieObject> getItems() {
//        ArrayList<MovieObject> mMoviesDataArrayList = new ArrayList<>(Arrays.asList(mMoviesData));
//        return mMoviesDataArrayList;
//    }

    /**
     * This method is used to set the movie details on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param moviesData The new movies data to be displayed.
     */
//    public void setMoviesData(MovieObject[] moviesData) {
//        mMoviesData = moviesData;
//        notifyDataSetChanged();
//    }


}
