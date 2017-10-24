package uk.co.pottertour.popularmovieswithdb;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Sam on 13/08/2017.
 * Converts data into views for MainActivity to draw
 */
// TODO 1 save everything to a DB
// include columns for reviews object and trailer Uris

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private final String TAG = MoviesAdapter.class.getSimpleName();
    //private MovieObject[] mMoviesData;
    private Cursor mCursor;
    /* The context we use to utility methods, app resources and layout inflaters */
    private Context mContext; // final
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MoviesAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MoviesAdapterOnClickHandler {
        void onClick(int movieId, String mPosterPath, String mSynopsis, String mRating,
                     String mReleaseDate, String mTitle, String fave);
    }

    /**
     * Creates a MoviesAdapter.
     *
     * @param context      Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MoviesAdapter(@NonNull Context context, MoviesAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mMoviesImageView;
        //public final TextView mMoviesTextView;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            mMoviesImageView = (ImageView) view.findViewById(R.id.iv_detail_poster_thumbnail);
            view.setOnClickListener(this);

        }

        /**
         * This gets called by the child views during a click.
         *
         * @param view The View that was clicked
         */
        @Override
        public void onClick(View view) {
            // TODO shunt across all fields to Detail, so 1 less db access
            int adapterPosition = getAdapterPosition();
            //MovieObject movieData = mMoviesData[adapterPosition];
            mCursor.moveToPosition(adapterPosition);
            int movieId = mCursor.getInt(MainActivity.INDEX_MOVIE_ID);
            String mPosterPath = mCursor.getString(MainActivity.INDEX_POSTER_PATH);
            String mSynopsis = mCursor.getString(MainActivity.INDEX_MOVIE_OVERVIEW);
            String mRating = mCursor.getString(MainActivity.INDEX_MOVIE_RATING);
            String mReleaseDate = mCursor.getString(MainActivity.INDEX_MOVIE_RELEASE_DATE);
            String mTitle = mCursor.getString(MainActivity.INDEX_MOVIE_TITLE);
            String fave = mCursor.getString(MainActivity.INDEX_MOVIE_FAVOURITE);

            Log.v("MoviesAdapter", "A view has been clicked");
            //mClickHandler.onClick(movieData);
            mClickHandler.onClick(movieId, mPosterPath, mSynopsis, mRating, mReleaseDate, mTitle,
                    fave);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        view.setFocusable(true);
        // TODO IS THIS BEST PRACTICE?  IS THERE A BETTER WAY TO GET CONTEXT FOR onBindViewHolder??
        mContext = context;

        return new MoviesAdapterViewHolder(view);

    }
    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param moviesAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder moviesAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        //MovieObject movieDetails = mMoviesData[position];

        String moviePoster = mCursor.getString(MainActivity.INDEX_POSTER_PATH);

        Picasso.with(mContext)
                .load(moviePoster).fit()
                .into(moviesAdapterViewHolder.mMoviesImageView);
//        Picasso.with(mContext) // TODO IS GETTING CONTEXT THIS WAY BEST PRACTICE?
//                .load(movieDetails.getPoster()).fit()
//                .into(moviesAdapterViewHolder.mMoviesImageView);
    }
    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        //if (mMoviesData == null) return 0;
        //return mMoviesData.length;
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
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
     * This method is used to set the movie details on a ForecastAdapter if we've already
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
