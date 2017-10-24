package uk.co.pottertour.popularmovieswithdb;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ProgressBar;

import java.util.Arrays;
import java.util.List;

import uk.co.pottertour.popularmovieswithdb.data.MoviesContract;
import uk.co.pottertour.popularmovieswithdb.databinding.ActivityDetailBinding;
import uk.co.pottertour.popularmovieswithdb.sync.DetailsSyncUtils;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,DetailsAdapter.DetailsAdapterOnClickHandler {

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

    private static final int ID_DETAIL_LOADER = 666;

    private Boolean mFavouriteStatus = false;

    private DetailsAdapter mDetailsAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private ProgressBar mLoadingIndicator;


    /* A summary of the film that can be shared by clicking the share button in the actionbar */
    private String mMovieSummary;
    private static final String MOVIE_SHARE_HASHTAG = " #SamsMovieApp";

    private Uri mUri;

    private int mMovieId;

    // used for databinding, avoids findViewById repetition
    private ActivityDetailBinding mDetailBinding;

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
            Bundle rowSelection = new Bundle();
            rowSelection.putInt("MovieId", mMovieId);

            Log.e(TAG, "inside, movieID api movieId? "  + mMovieId);

         /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movie_details);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
            mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

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
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
            mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
            mRecyclerView.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
            mDetailsAdapter = new DetailsAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
            mRecyclerView.setAdapter(mDetailsAdapter);

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
            //getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, rowSelection, this);
            //getSupportLoaderManager().restartLoader(ID_DETAIL_LOADER, rowSelection, this);

            // this begins an immediate Network request pulling JSON from the server
            DetailsSyncUtils.initialize(this, mMovieId);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // get the DB row_ID we're interested in
        int movieId = args.getInt("MovieId");
        switch (loaderId) {

            case ID_DETAIL_LOADER:
                Log.wtf(TAG, "attempting to read db with a new CursorLoader using movie: " + movieId);
                String selection = MoviesContract.MoviesEntry._ID + " = ?"; //COLUMN_MOVIE
                String[] selectionArgs = {String.valueOf(movieId)}; // HOPEFULLY this selects just 1 row
                Uri detailsQueryUri = MoviesContract.MoviesEntry.INSERT_DETAILS_URI;//CONTENT_URI;
                String sortOrder = null;
                // TODO this is not returning anything, it's faulty
                return new CursorLoader(this,
                        detailsQueryUri,
                        DETAILS_PROJECTION, // all columns
                        selection, // where Movie_ID column =
                        selectionArgs, // movieId
                        sortOrder);

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
            Log.wtf(TAG, "cursor doesn\'t have valid data");
            return;
        }

        /* Swap out the data on the adapter*/
        //mDetailsAdapter.swapCursor(data);
        //get the trailer
        String trailersStr = data.getString(DetailActivity.INDEX_MOVIE_TRAILERS);
        // recreate ArrayList
        List<String> trailersList = Arrays.asList(trailersStr.split(","));
        Log.v(TAG, "trailersList: " + trailersList);
        mDetailsAdapter.setMoviesData(trailersList);
        // TODO TESTING THIS STUFF
//        data.close();
//        mDetailsAdapter.notifyDataSetChanged();


        //TextView mTitleTop = (TextView) findViewById(R.id.tv_title);
        //mTitleTop.setText(data.getString(INDEX_MOVIE_TITLE));
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);

        mFavouriteStatus = Boolean.valueOf(data.getString(INDEX_MOVIE_FAVOURITE));
        //if (data.getCount() != 0) showWeatherDataView();

        // THIS CLOSES THE CURSOR PREVENTING CHANGES IF DATASET CHANGES
        //data.close();
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
        mDetailsAdapter = null;
        //mDetailsAdapter.swapCursor(null);
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

        Intent mIntent = new Intent(Intent.ACTION_VIEW);
        mIntent.setData(Uri.parse(url));
        startActivity(mIntent);
    }
    /* toggles favourite state */
    public void onclick_favourite_button(View view) {

        mFavouriteStatus = !mFavouriteStatus;
        // TODO save to database
    }
}
