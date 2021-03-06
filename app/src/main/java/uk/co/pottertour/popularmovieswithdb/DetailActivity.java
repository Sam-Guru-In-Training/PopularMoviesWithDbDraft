package uk.co.pottertour.popularmovieswithdb;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.pottertour.popularmovieswithdb.data.MoviesContract;
//import uk.co.pottertour.popularmovieswithdb.databinding.ActivityDetailBinding;
import uk.co.pottertour.popularmovieswithdb.sync.DetailsSyncUtils;
import uk.co.pottertour.popularmovieswithdb.utilities.MoviesDBJsonUtils;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,TrailersAdapter.DetailsAdapterOnClickHandler,
        ReviewsAdapter.DetailsAdapterOnClickHandler, JsonReviewsAdapter.DetailsAdapterOnClickHandler {

    //The columns of data that we are interested in within our DetailActivity's list
    public static final String[] DETAILS_PROJECTION = {
            MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.MoviesEntry.COLUMN_FAVOURITE,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_RATING,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_TRAILERS,
            MoviesContract.MoviesEntry.COLUMN_REVIEWS,
    };
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_ROW_ID = 0;
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_POSTER_PATH = 2;
    public static final int INDEX_MOVIE_FAVOURITE = 3;
    public static final int INDEX_MOVIE_OVERVIEW = 4;
    public static final int INDEX_MOVIE_RATING = 5;
    public static final int INDEX_MOVIE_RELEASE_DATE = 6;
    public static final int INDEX_MOVIE_TITLE = 7;
    public static final int INDEX_MOVIE_TRAILERS = 8;
    private static final int INDEX_MOVIE_REVIEWS = 9;

    private static final int ID_DETAIL_LOADER = 666;
    private static final int ID_FAVOURITE_LOADER = 667;
    private static final String FAVOURITE_KEY = "favourite_key";

    private Boolean mFavouriteStatus;

    private TrailersAdapter trailersAdapter;
    //private ReviewsAdapter reviewsAdapter;
    private JsonReviewsAdapter reviewsAdapter;

    private RecyclerView trailersRecyclerView;
    private RecyclerView reviewsRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    // all these views will be dealt with before service is launched
    private ImageView mPosterImageView;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAvg;
    private ToggleButton mFavourite;
    private TextView mSynopsis;

    private ProgressBar mLoadingIndicator;


    /* A summary of the film that can be shared by clicking the share button in the actionbar */
    private String mMovieSummary;
    private static final String MOVIE_SHARE_HASHTAG = " #SamsMovieApp";

    private Uri mUri;

    private int mMovieId;

    // used for databinding, avoids findViewById repetition
    //private ActivityDetailBinding mDetailBinding;

    //private TextView mTitle;

    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

//        mTitle = (TextView) findViewById(R.id.tv_title9);
//        mTitle.setText("Testing the new layout");
        //mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        /* a details screen:
        original title
        movie poster image thumbnail
        A plot synopsis (called overview in the api)
        user rating (called vote_average in the api)
        release date
        */
        //mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Intent mIntent = getIntent();

        Log.wtf(TAG, "inside Detail Activity, unpacking intent");

        if (mIntent != null) {
            mMovieId = getIntent().getIntExtra("MovieId", 550);
            final Bundle rowSelection = new Bundle();
            rowSelection.putInt("MovieId", mMovieId);

            Log.e(TAG, "inside, movieID api movieId? "  + mMovieId);

         /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
            trailersRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movie_trailers);
            reviewsRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movie_reviews);

            mPosterImageView = (ImageView) findViewById(R.id.iv_detail_poster_thumbnail);
            mTitle = (TextView) findViewById(R.id.tv_title);
            mReleaseDate = (TextView) findViewById(R.id.tv_release_date);
            mVoteAvg = (TextView) findViewById(R.id.tv_vote_average);
            mSynopsis = (TextView) findViewById(R.id.tv_overview);
            // TODO hook up logic for making favourite button work
            mFavourite = (ToggleButton) findViewById(R.id.button_favourite);

            //                // attach the movie details such as title etc.
            String title = MainActivity.mTitle;//mCursor.getString(DetailActivity.INDEX_MOVIE_TITLE);
            mTitle.setText(title);
//                detailsAdapterViewHolder.mTitle.setText(title);
//
            String dateText = MainActivity.mReleaseDate; //mCursor.getString(DetailActivity.INDEX_MOVIE_RELEASE_DATE);
            mReleaseDate.setText(dateText);
//                detailsAdapterViewHolder.mReleaseDate.setText(dateText);
//
            String voteAverage = MainActivity.mRating; // mCursor.getString(DetailActivity.INDEX_MOVIE_RATING)  + "/10";
            mVoteAvg.setText(voteAverage);
//                detailsAdapterViewHolder.mVoteAvg.setText(voteAverage);
//
            String overview = MainActivity.mSynopsis; // mCursor.getString(DetailActivity.INDEX_MOVIE_OVERVIEW);
            mSynopsis.setText(overview);
//                detailsAdapterViewHolder.mSynopsis.setText(overview);
//
                // Boolean.valueOf(mCursor.getString(DetailActivity.INDEX_MOVIE_FAVOURITE));
//                detailsAdapterViewHolder.mFavourite.setPressed(MainActivity.mFave);
//
            String posterUrlString = MainActivity.mPosterPath; // mCursor.getString(DetailActivity.INDEX_MOVIE_POSTER);
            Picasso.with(this)
                        .load(posterUrlString).fit()
                        .into(mPosterImageView);
//                //ImageView mPosterIV = (ImageView) findViewById(R.id.iv_detail_poster_thumbnail);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
            mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_bar);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
            LinearLayoutManager trailersLayoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            LinearLayoutManager reviewsLayoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
            trailersRecyclerView.setLayoutManager(trailersLayoutManager);
            reviewsRecyclerView.setLayoutManager(reviewsLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
            trailersRecyclerView.setHasFixedSize(true);
            reviewsRecyclerView.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The Adapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
            trailersAdapter = new TrailersAdapter(this, this);
            //reviewsAdapter = new ReviewsAdapter(this, this);
            reviewsAdapter = new JsonReviewsAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
            trailersRecyclerView.setAdapter(trailersAdapter);
            reviewsRecyclerView.setAdapter(reviewsAdapter);

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
            getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, rowSelection, this);
            //getSupportLoaderManager().initLoader(MainActivity.ID_POPULAR_MOVIES_LOADER, null, this);
            //getSupportLoaderManager().restartLoader(ID_DETAIL_LOADER, rowSelection, this);


            // setup a listener to reload the favourite button each time it's hit.
            // TODO is this right?
//            ToggleButton faveButton = (ToggleButton) findViewById(R.id.button_favourite);
//            faveButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Log.v(TAG + " faveBtn ClickListener", "button clicked restarting FAVOURITE_LOADER");
//                    mFavouriteStatus = !mFavouriteStatus;
//                    Log.v(TAG, "!!!!!!!!!!!!!!!!!!!! changed status of fave btn: " + mFavouriteStatus);
//                    // as well as movieId, add the Favourite button status,
//                    // TODO, favourite button is now being updated before it's intialised, HELP!
//                    rowSelection.putBoolean(FAVOURITE_KEY, mFavouriteStatus);
//                    getSupportLoaderManager().initLoader(ID_FAVOURITE_LOADER, rowSelection, DetailActivity.this);
//                    getSupportLoaderManager().restartLoader(ID_FAVOURITE_LOADER, rowSelection, DetailActivity.this);
//                }
//            });

            // this begins an immediate Network request pulling JSON from the server
            DetailsSyncUtils.initialize(this, mMovieId);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // get the DB row_ID we're interested in
        Log.i(TAG + " onCreateLoader", "attempting to boot loader");
        switch (loaderId) {

            case ID_DETAIL_LOADER:
                int movieId = args.getInt("MovieId");
                Uri detailsQueryUri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.INSERT_DETAILS_URI, movieId);
                String selection = MoviesContract.MoviesEntry._ID + " = ?"; //COLUMN_MOVIE
                String[] selectionArgs = {String.valueOf(movieId)};
                String sortOrder = null;

                return new CursorLoader(this,
                        detailsQueryUri,
                        DETAILS_PROJECTION, // all columns
                        selection, // where Movie_ID column =
                        selectionArgs, // movieId
                        sortOrder);

            case ID_FAVOURITE_LOADER:
                Log.v(TAG + " FAVOURITE_LOADER", "Attempting to update favourite field in db");
                ContentResolver moviesContentResolver = this.getContentResolver();
                ContentValues favouriteContentValue = new ContentValues();
                favouriteContentValue.put(MoviesContract.MoviesEntry.COLUMN_FAVOURITE, mFavouriteStatus.toString());

                String selectionFave = MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?";
                // selectionArgs
                //String movieIdStr = mCursor.getString(DetailActivity.INDEX_MOVIE_ID);
                String movieIdStr = String.valueOf(DetailActivity.INDEX_MOVIE_ID);
                String[] selectionArgsFave = { "" + movieIdStr};
                //* update our contentProvider with trailers for the appropriate row *//*
                int rowUpdated = moviesContentResolver.update(
                        MoviesContract.MoviesEntry.UPDATE_FAVOURITE_URI, //CONTENT_URI,
                        favouriteContentValue,
                        selectionFave,
                        selectionArgsFave);
                if (rowUpdated == -1) {
                    Log.wtf(TAG, "toggled favourite but db hasn't been updated WTH?!");
                    // TODO work out how to make method throw an exception without try catch
                    //throw new Exception("Favourite toggled but db not updated, weird?");
                }
                else {
                    Log.wtf(TAG, "updated db with new favourite value: " + rowUpdated);
                    // ToggleButton state again
                }
                return null;

            default:
                throw new RuntimeException("Loader Not Implemented!!!!: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        // TODO get trailers url ArrayList
        // TODO bump them up by one so have space for loading header
        // .add(0, null);
        // TODO attach it to adapter
        // Reconfigure adapter to want an ArrayList

        Log.v(TAG, "onLoadFinished we need 1 db rows, have: " + data.getCount());
        Log.v(TAG, "row has: " + data.getColumnCount() + " columns");

        //Log.wtf(TAG, "Title should be fight club: " + mTitle);

//          String mTrailers = data.getString(DetailActivity.INDEX_MOVIE_TRAILERS);
//          Log.wtf(TAG, "trailers: " + mTrailers);
////        String mReviews = data.getString(DetailActivity.INDEX_MOVIE_REVIEWS);
//        Log.wtf(TAG, "trailers: " + mReviews);

//        for (int index = 0; index < data.getColumnCount(); index ++ ) {
//            Log.v(TAG, "name of col at " + index + " is " + data.getColumnName(index));
//        }


        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }
        if (data == null) Log.v(TAG, "cursor is null :(");
        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            Log.wtf(TAG, "cursor doesn\'t have valid data or favourite button pressed");
            return;
        }

        // get the saved Favourite status
        mFavouriteStatus = Boolean.valueOf(data.getString(DetailActivity.INDEX_MOVIE_FAVOURITE));

        /* Swap out the data on the adapter*/
        //trailersAdapter.swapCursor(data);
        //get the trailer
        String trailersStr = data.getString(DetailActivity.INDEX_MOVIE_TRAILERS);
        String reviewsStr = data.getString(DetailActivity.INDEX_MOVIE_REVIEWS);

        // TODO convert to json array attach adapter
        Log.i(TAG, "olf: trailersStr: " + trailersStr);
        // TODO this is coming out *ALREADY* processed, why?
        Log.i(TAG, "olf: reviewsStr: " + reviewsStr);

        ArrayList<String> trailersList = extractAdapterList(trailersStr);
        //ArrayList<String> reviewsList = extractAdapterList(reviewsStr);
        JSONArray reviewsJSONarray = extractReviewsAdapterArray(reviewsStr);

        //Log.v(TAG, "detailsCellEntry trailersList: " + trailersList);
        //Log.v(TAG, "REVIEWS list: " + reviewsStr);
        Log.v(TAG, "olf: setting adapters");
        //reviewsAdapter.setMoviesData(reviewsList);

        reviewsAdapter.setMoviesData(reviewsJSONarray);
        trailersAdapter.setMoviesData(trailersList);
        // TODO TESTING THIS STUFF
//        data.close();
         trailersAdapter.notifyDataSetChanged();
         reviewsAdapter.notifyDataSetChanged();

        Log.v(TAG, "olf: adapters set, scrolling");
        //TextView mTitleTop = (TextView) findViewById(R.id.tv_title);
        //mTitleTop.setText(data.getString(INDEX_MOVIE_TITLE));
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        trailersRecyclerView.smoothScrollToPosition(mPosition);
        //reviewsRecyclerView.smoothScrollToPosition(mPosition);

        mFavouriteStatus = Boolean.valueOf(data.getString(INDEX_MOVIE_FAVOURITE));
        //if (data.getCount() != 0) showWeatherDataView();

        // THIS CLOSES THE CURSOR PREVENTING CHANGES IF DATASET CHANGES
        //data.close();
    }

    private JSONArray extractReviewsAdapterArray(String reviewsStr) {
        JSONArray reviewsJsonArray = new JSONArray();

        Log.i(TAG, "attempting to convert reviews DB str to a JSONArray: " + reviewsStr);
        try {
            reviewsJsonArray = new JSONArray(reviewsStr);
            Log.i(TAG, "converted JSONArray: " + reviewsJsonArray);
            return reviewsJsonArray;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "unable to convert str to array for adapter");
        }
        // returning empty JSONArray
        return reviewsJsonArray;
    }

    /*
    * Converts the text jsonStr to a JsonObject,
    * Extracts the urls from a Json Array and returns them
     * Could be extended to include the review text, opening a second activity or offering
     * accordion recyclerview items. https://stackoverflow.com/questions/27203817/recyclerview-expand-collapse-items
     */
    private ArrayList<String> extractAdapterList(String jsonStr) {
        Log.v(TAG, "detailsCellEntry attempt to extract adapter list");
        ArrayList<String> adapterDataList = new ArrayList<>();
        try {
            // splitToJson will not work because it will try to extract JsonObjects not String from the array
            JSONObject jsonObject = new JSONObject(jsonStr);
            //JSONObject reviewsJsonObject = new JSONObject(reviewsStr);
            //JSONObject trailersJsonObject = new JSONObject(trailersStr);
            Log.v(TAG, "detailsCellEntry converted to JsonObj");
            JSONArray jsonArray = jsonObject.optJSONArray(MoviesDBJsonUtils.MDB_JSON_OBJ_KEY);
            Log.v(TAG, "detailsCellEntry extracted jsonArray: " + jsonArray);
            String urlStr;
            for (int index = 0; index < jsonArray.length(); index++) {
                urlStr = jsonArray.get(index).toString().replace("\\", "");
                adapterDataList.add(urlStr);
            }
            //reviewsList = reviewsJsonObject.optJSONArray(MoviesDBJsonUtils.MDB_JSON_OBJ_KEY);
            Log.v(TAG, "detailsCellEntry attempt to convert from JsonArray to ArrayList<String>: " + adapterDataList );
        } catch (JSONException e) {
            e.printStackTrace();
            Log.wtf(TAG, "onLoadFinished: Cannot populate adapter with urls as db fields null");
        }
        Log.v(TAG, "detailsCellEntry returning adapter list");
        return adapterDataList;
    }
//        String title = data.getString(INDEX_MOVIE_TITLE);
//        mDetailBinding.tvTitle.setText(title);
//
//        String dateText = data.getString(INDEX_MOVIE_RELEASE_DATE);
//        mDetailBinding.tvReleaseDate.setText(dateText);
//
//        String voteAverage = data.getString(INDEX_MOVIE_RATING)  + "/10";
//        mDetailBinding.tvVoteAverage.setText(voteAverage);
//
//        String overview = data.getString(INDEX_MOVIE_OVERVIEW);
//        mDetailBinding.tvOverview.setText(overview);
//
//        String posterUrlString = data.getString(INDEX_MOVIE_POSTER);
//        ImageView mPosterIV = (ImageView) findViewById(R.id.iv_detail_poster_thumbnail);
//        // TODO does picasso magically cache it to prevent re-requests?  How should I do it different?
//        Picasso.with(this)
//                .load(posterUrlString).fit()
//                .into(mPosterIV);
//
//        /* Store a summary for sharing with friends */
//        mMovieSummary = String.format("%s - %s", title, voteAverage);
    /**
     * Called when a previously created loader is being reset, thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     * Since we don't store any of this cursor's data, there are no references we need to remove.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG + " onLoaderReset", "attempting to reset loader");
        trailersAdapter = null;
        reviewsAdapter = null;
        //trailersAdapter.swapCursor(null);
    }
    // TODO 2 get movie trailers in format of
    // https://api.themoviedb.org/3/movie/157336?api_key=....&append_to_response=reviews,videos
    // TODO 3 separate the json out
    // TODO 4 Youtube link assembled by appending a key
    // https://www.youtube.com/watch?v=ePbKGoIGAXY
    // TODO 5 save youtube links to database using movie ID as key, array of URIs under reviews column
    // TODO 6 save reviews into the DB using movie ID as key, array of review objects
    // TODO 7 new reviewsActivity, click through to read a review
    // TODO 8 parceable reviews through to reviewsActivity (or use DB save, restore)
    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see android.app.Activity#onPrepareOptionsMenu(Menu)
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Get the ID of the clicked item */
        int id = item.getItemId();

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mMovieSummary + MOVIE_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    @Override
    public void onClick(String url) {

        Log.v(TAG, "launching intent: " + url);
        Intent mIntent = new Intent(Intent.ACTION_VIEW);
        mIntent.setData(Uri.parse(url)); // has leading white spaces
        startActivity(mIntent);
    }
    /* toggles favourite state */
    public void onclick_favourite_button(View view) {
        String ONCLICK = " onClick_fave";
        mFavouriteStatus = !mFavouriteStatus;
        Log.v(TAG + ONCLICK, "!!!!!!!!!!!!!!!!!!!! changed status of fave btn: " + mFavouriteStatus);

        Bundle args = new Bundle();
        args.putBoolean(FAVOURITE_KEY, mFavouriteStatus);

        // simple update, quick operation so just AsyncTask it
        new ChangeFavouriteStatusInDb().execute(mMovieId);
        // TODO could arrange for update to kick off on rotation, but so quick...
        Log.v(TAG + ONCLICK, "successfully updated fave db field");

        //getSupportLoaderManager().initLoader(ID_FAVOURITE_LOADER, args, this);
    }

    private class ChangeFavouriteStatusInDb extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            int movieId = integers[0];
            String ASYNCTASK = " AsyncTask";
            ContentResolver moviesContentResolver = DetailActivity.this.getContentResolver();
            ContentValues favouriteContentValue = new ContentValues();
            favouriteContentValue.put(MoviesContract.MoviesEntry.COLUMN_FAVOURITE, mFavouriteStatus.toString());

            String selection = MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?";
            // selectionArgs
            //String movieIdStr = mCursor.getString(DetailActivity.INDEX_MOVIE_ID);
            String movieIdStr = String.valueOf(mMovieId);//DetailActivity.INDEX_MOVIE_ID);
            String[] selectionArgs = { "" + movieIdStr};

            Uri favouriteUpdateUri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.UPDATE_FAVOURITE_URI,
                    mMovieId);
            //* update our contentProvider with trailers for the appropriate row *//*
            int rowUpdated = moviesContentResolver.update(
                    favouriteUpdateUri, //CONTENT_URI,
                    favouriteContentValue,
                    selection,
                    selectionArgs);
            if (rowUpdated == -1) {
                Log.wtf(TAG + ASYNCTASK, "toggled favourite but db hasn't been updated WTH?!");
                // TODO work out how to make method throw an exception without try catch
                //throw new Exception("Favourite toggled but db not updated, weird?");
            }
            else {
                Log.wtf(TAG + ASYNCTASK, "updated db with new favourite value");
                // ToggleButton state again
            }

            return null;
        }
    }


}
