package uk.co.pottertour.popularmovieswithdb.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import uk.co.pottertour.popularmovieswithdb.BuildConfig;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String API_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w185//";


    /*
     * NOTE: These values only effect responses from OpenWeatherMap, NOT from the fake weather
     * server. They are simply here to allow us to teach you how to build a URL if you were to use
     * a real API.If you want to connect your app to OpenWeatherMap's API, feel free to! However,
     * we are not going to show you how to do so in this course.
     */

    /* The format we want our API to return */
    private static final String format = "json";

    final static String SORT_BY = "sort_by";
    final static String API = "api_key";
    final static String ADDITIONALS = "append_to_response";
    // in total "&append_to_response" will be appended

    /**
     * Builds the URL used to talk to the movie server ordering by sort_by.
     *
     * @param sort_by The location that will be queried for.
     * @return The URL to use to query the movie server.
     */
    public static URL buildUrl(String sort_by) {
        // TODO need to retrieve sort_by from shared prefs
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(sort_by)
                .appendQueryParameter(API, API_KEY)
                //.appendQueryParameter(ADDITIONALS,"videos, images")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URL, does it have: ?api_key=" + url);

        return url;
    }
    public static URL[] buildDetailsUrls(int movieId) {
        // TODO need poster url and reviews url
        // form http://api.themoviedb.org/3/movie/550/videos?api_key=
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath("videos")
                .appendQueryParameter(API, API_KEY)
                //.appendQueryParameter(ADDITIONALS,"videos, images")
                .build();

        URL urlVideos = null;
        try {
            urlVideos = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Uri builtUriReviews = Uri.parse(BASE_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath("reviews")
                .appendQueryParameter(API, API_KEY)
                //.appendQueryParameter(ADDITIONALS,"videos, images")
                .build();

        URL urlReviews = null;
        try {
            urlReviews = new URL(builtUriReviews.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URLs: " + urlReviews + " " + urlVideos);

        return new URL[]{urlVideos, urlReviews};
    }
    /**
     * @param imageID The image that will be queried for.
     * @return The URL to use to query the movie server.
     */
    public static Uri buildImageUrl(String imageID) {
        Uri builtUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                .appendPath(POSTER_SIZE)
                .appendPath(imageID)
                .build();

        Log.v(TAG, "Built image Uri: " + builtUri.toString());

        return builtUri;
    }

    /**
     * This method returns a Uri for trailers
     */
//    public static Uri buildTrailersUrl(String movieID) {
//        Uri builtUri = Uri.parse()
//    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
