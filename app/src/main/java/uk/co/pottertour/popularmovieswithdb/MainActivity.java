package uk.co.pottertour.popularmovieswithdb;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.pottertour.popularmovieswithdb.data.MoviesContract;
import uk.co.pottertour.popularmovieswithdb.sync.MoviesSyncUtils;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MoviesAdapter.MoviesAdapterOnClickHandler {

    // TODO Need to save sort_by to shared prefs, and then retrieve it when making network request
    private static final String TAG = MainActivity.class.getSimpleName();

    //The columns of data that we are interested in displaying within our MainActivity's list
    public static final String[] MAIN_MOVIES_PROJECTION = {
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

    /* We'll save to global variable during onClick so we can access them in
    *  onBindView in DetailsAdapter */
    public static String mPosterPath;
    public static String mSynopsis;
    public static String mRating;
    public static String mReleaseDate;
    public static String mTitle;
    public static Boolean mFave;


    private String posterPath;
    /*
     * This ID will be used to identify the Loader responsible for loading our weather forecast. In
     * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
     * We will still use this ID to initialize the loader and create the loader for best practice.
     * Please note that 44 was chosen arbitrarily. You can use whatever number you like, so long as
     * it is unique and consistent.
     */
    private static final int ID_POPULAR_MOVIES_LOADER = 44;
    private static final int ID_HIGH_RATED_MOVIES_LOADER = 45;
    private static final int ID_FAVOURITE_MOVIES_LOADER = 46;

    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private int mPosition = RecyclerView.NO_POSITION;


    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_posters);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        Log.e(TAG, "creating adapter");

        mMoviesAdapter = new MoviesAdapter(this, this);
        mRecyclerView.setAdapter(mMoviesAdapter);
        showLoading();

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(ID_POPULAR_MOVIES_LOADER, null, this);

        // this begins an immediate Network request pulling JSON from the server
        MoviesSyncUtils.initialize(this);


//        if (savedInstanceState != null && savedInstanceState.containsKey("moviesData")) {
//            // if we have saved moviesData restore it
//            ArrayList<MovieObject> tempMoviesData = savedInstanceState.getParcelableArrayList("moviesData");
//            mMoviesData = (MovieObject[]) tempMoviesData.toArray();
//            showMoviesDataView();
//            Log.e("after rotation", "setting data on Adapter");
//            // TODO IS THIS RIGHT?  DOES THE ADAPTER STILL EXIST AT THIS POINT?
//            mMoviesAdapter.setMoviesData(mMoviesData);
//        }
//
//        else {
//            // if we haven't check online and fetch from server
//            if (isOnline()) {
//                mMoviesAdapter = new MoviesAdapter(this);
//                mRecyclerView.setAdapter(mMoviesAdapter);
//
//                mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
//                loadMoviesData("popular");
//            } else {
//                // TODO not online: try to load from database?  Would mean saving pictures as a BLOB binary
//                showErrorMessage();
//            }
//        }
    }
    /**
     * This method will make the loading indicator visible and hide the weather View and error
     * message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        /* Then, hide the weather data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /*
    * Checks if we have a network connection, returns false if don't
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void loadMoviesData(String sort_by) {
        showMoviesDataView();

        // sort method selectable via options menu
        Log.e(TAG, "creating Fetch Movies Task");
        //new FetchMoviesTask().execute(sort_by);
        getSupportLoaderManager().initLoader(ID_POPULAR_MOVIES_LOADER, null, this);

        // this begins an immediate Network request pulling JSON from the server
        MoviesSyncUtils.initialize(this);
    }

    /*
    * saves moviesData if we've got it back from network
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if(mMoviesData != null) {
//            outState.putParcelableArrayList("mMoviesAdapter", mMoviesAdapter.getItems());
//        }
    }

    private void showMoviesDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     * TODO make it launch, use putParceable to send objects through
     * @param movieId The weather for the day that was clicked
     * @param posterPath
     * @param synopsis
     * @param rating
     * @param releaseDate
     * @param title
     */

    @Override
    public void onClick(int movieId, String posterPath, String synopsis, String rating,
                        String releaseDate, String title, String fave) {
        Context context = this;
        Class destinationClass = DetailActivity.class;

        mPosterPath = posterPath;
        mSynopsis = synopsis;
        mRating = rating;
        mReleaseDate = releaseDate;
        mTitle = title;
        mFave = Boolean.parseBoolean(fave);

        Log.e(TAG, "A view has been clicked");
        //Toast.makeText(context, "item clicked!", Toast.LENGTH_SHORT).show();

        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("MovieId", movieId);
        Log.v(TAG, "leaving MainActivity with movieId: " + movieId);
        //intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, movieParticulars);
        startActivity(intentToStartDetailActivity);
    }
    // TODO we need to store popularity and rating fields in the database for displaying

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Log.wtf(TAG, "in loader id = " + loaderId);
        Uri queryUri;
        String sortOrder;
        String selection = null; // selects all rows?

        switch (loaderId) {
            case ID_POPULAR_MOVIES_LOADER:
                // URI for all rows of movies data in our movies table
                queryUri = MoviesContract.MoviesEntry.CONTENT_URI;
                sortOrder = MoviesContract.MoviesEntry.COLUMN_POPULARITY + " DESC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                return new CursorLoader(this,
                        queryUri,
                        MAIN_MOVIES_PROJECTION,
                        selection,
                        null,
                        sortOrder);
            case ID_HIGH_RATED_MOVIES_LOADER:
                // URI for all rows of movies data in our movies table
                queryUri = MoviesContract.MoviesEntry.CONTENT_URI;
                sortOrder = MoviesContract.MoviesEntry.COLUMN_RATING + " DESC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                return new CursorLoader(this,
                        queryUri,
                        MAIN_MOVIES_PROJECTION,
                        selection,
                        null,
                        sortOrder);
            case ID_FAVOURITE_MOVIES_LOADER:
                // URI for all rows of movies data in our movies table
                queryUri =  MoviesContract.MoviesEntry.CONTENT_URI;
                selection = MoviesContract.MoviesEntry.COLUMN_FAVOURITE + " = ?";
                String[] selectionArgs = {"true"}; // this is stored as TEXT in db, so fine as String
                sortOrder = null;

                return new CursorLoader(this,
                        queryUri,
                        MAIN_MOVIES_PROJECTION,
                        selection,
                        selectionArgs,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) {
            showMoviesDataView();
            Log.wtf(TAG, "onLoadFinished we have db rows = " + data.getCount());
        }
        else Log.wtf(TAG, "onLoadFinished we have NOOOO data :(");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }



//    public class FetchMoviesTask extends AsyncTask<String, Void, MovieObject[]> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mLoadingIndicator.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected MovieObject[] doInBackground(String... params) {
//
//            final String DIB = "doInBackground";
//            // if no sorting method exit
//            if (params.length == 0) {
//                Log.e(DIB, "no params exiting!");
//                return null;
//            }
//            Log.e(DIB, "Trying to get params[0]");
//            String sort_movies_by = params[0];
//            URL moviesRequestUrl = NetworkUtils.buildUrl(sort_movies_by);
//            Log.e(DIB, "Trying to get JSON in doInBackground");
//            try {
//                String jsonMovieResponse = NetworkUtils
//                        .getResponseFromHttpUrl(moviesRequestUrl);
//
//                MovieObject[] simpleJsonMovieData = MoviesDBJsonUtils
//                        .getMovieObjectsFromJson(MainActivity.this, jsonMovieResponse);
//                Log.e(TAG, "Got! JSON in doInBackground");
//                return simpleJsonMovieData;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        }

//        @Override
//        protected void onPostExecute(MovieObject[] moviesData) {
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//            if (moviesData != null) {
//                mMoviesData = moviesData;
//                showMoviesDataView();
//                Log.e("onPostExecute", "setting data on Adapter");
//                mMoviesAdapter.setMoviesData(moviesData);
//            } else {
//                showErrorMessage();
//            }
//        }
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sortby, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (isOnline()) {
            if (id == R.id.popular) {
                // TODO change the data set ORDER mMoviesAdapter is attached to
                getSupportLoaderManager().initLoader(ID_POPULAR_MOVIES_LOADER, null, this);
                // TODO mMoviesAdapter.swapCursor()

                //mMoviesAdapter. //setMoviesData(null);
                //loadMoviesData("popular");
                return true;
            }
            if (id == R.id.top_rated) {

                // TODO change the data set ORDER mMoviesAdapter is attached to
                // TODO mMoviesAdapter.swapCursor()
                getSupportLoaderManager().initLoader(ID_HIGH_RATED_MOVIES_LOADER, null, this);
                //mMoviesAdapter.setMoviesData(null);
                //loadMoviesData("top_rated");
                return true;
            }
            if (id == R.id.favourites) {
                // TODO change the data set ORDER mMoviesAdapter is attached to
                // TODO mMoviesAdapter.swapCursor()
                getSupportLoaderManager().initLoader(ID_FAVOURITE_MOVIES_LOADER, null, this);
                //mMoviesAdapter.setMoviesData(null);
                //loadMoviesData("top_rated");
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
