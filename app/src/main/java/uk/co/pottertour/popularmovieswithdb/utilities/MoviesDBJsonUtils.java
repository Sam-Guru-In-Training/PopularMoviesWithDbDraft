/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.pottertour.popularmovieswithdb.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import uk.co.pottertour.popularmovieswithdb.data.MoviesContract.MoviesEntry;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class MoviesDBJsonUtils {

    final static private String TAG = MoviesDBJsonUtils.class.getSimpleName();

    /* Movies information. Each movie info or trailer info is an element of the "list" array */
    private static final String MDB_LIST = "results";


    /* UNIQUE MOVIE id */
    private static final String MDB_ID = "id";
    private static final String MDB_USER_RATING = "vote_average";
    private static final String MDB_TITLE = "title";
    private static final String MDB_POSTER = "poster_path";
    private static final String MDB_OVERVIEW = "overview";
    private static final String MDB_RELEASE_DATE = "release_date";
    private static final String MDB_POPULARITY = "popularity";

    private static final String OWM_MESSAGE_CODE = "cod";

    /* for getting trailers url values from JSON */
    public static final String MDB_TRAILERS_URL_KEY = "key";

    // key to get reviews url
    public static final String MDB_REVIEWS_URL_KEY = "url";

    /**
     * Parse the JSON and convert it into ContentValues that can be inserted into our database.
     *
     * @param context         An application context, such as a service or activity context.
     * @param moviesJsonStr The JSON to parse into ContentValues.
     *
     * @return An array of ContentValues parsed from the JSON.
     */
    public static ContentValues[] getFullMoviesDataFromJson(Context context, String moviesJsonStr)
            throws JSONException {

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        if (moviesJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = moviesJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        //parsedMoviesData = new MovieObject[moviesArray.length()];

        JSONArray moviesArray = moviesJson.getJSONArray(MDB_LIST);

        // intialising new data we'll be returning
        // TODO we will be returning, probably, a shorter array than we're declaring, FIX?
        // some movies may already have been saved??!!!
        ContentValues[] allNewMoviesContentValues = new ContentValues[moviesArray.length()];

        for (int i = 0; i < moviesArray.length(); i++) {
            /* These are the values that will be collected */
            int movieID;
            double rating;
            String title;
            String posterPath;
            String overview;
            String releaseDate;
            double popularity;

            /* Get the JSON object representing a particular movie */
            JSONObject movieDetails = moviesArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */

            movieID = movieDetails.getInt(MDB_ID); // TODO Check it will be OK as an int
            rating = movieDetails.getDouble(MDB_USER_RATING);
            title = movieDetails.getString(MDB_TITLE);
            posterPath = "http://image.tmdb.org/t/p/w185//" + movieDetails.getString(MDB_POSTER);
            overview = movieDetails.getString(MDB_OVERVIEW);
            releaseDate = movieDetails.getString(MDB_RELEASE_DATE).substring(0, 4);
            popularity = movieDetails.getDouble(MDB_POPULARITY);

            // TODO check if movieID already present in DB, if so SKIP

            ContentValues newMovieValues = new ContentValues();
            newMovieValues.put(MoviesEntry.COLUMN_MOVIE_ID, movieID);
            newMovieValues.put(MoviesEntry.COLUMN_RATING, rating);
            newMovieValues.put(MoviesEntry.COLUMN_TITLE, title);
            newMovieValues.put(MoviesEntry.COLUMN_POSTER_PATH, posterPath);
            newMovieValues.put(MoviesEntry.COLUMN_OVERVIEW, overview);
            newMovieValues.put(MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
            newMovieValues.put(MoviesEntry.COLUMN_POPULARITY, popularity);
            // new movie isn't favourited yet
            newMovieValues.put(MoviesEntry.COLUMN_FAVOURITE, false);
            // trailer and reviews columns need addition network requests, leave a flag
            newMovieValues.put(MoviesEntry.COLUMN_TRAILERS, "empty");
            newMovieValues.put(MoviesEntry.COLUMN_REVIEWS, "empty");

            allNewMoviesContentValues[i] = newMovieValues;
        }
        Log.wtf(TAG, "all movies ContentValues ready to install in DB");
        return allNewMoviesContentValues;
    }

    /**
     * Parse the JSON and convert it into ContentValues that can be inserted into our database.
     * Get the trailers youtube key and Reviews url Strings from json
     *
     * @param context         An application context, such as a service or activity context.
     * @param trailersJsonStr The JSON String to parse into ContentValues.
     * @param reviewsJsonStr The JSON String to parse into ContentValues.
     *
     * @return An array of ContentValues parsed from the JSON.
     */
//    public static ContentValues getReviewsDataFromJson(Context context,
//                                                               String trailersJsonStr,
//                                                               String reviewsJsonStr) //String[] detailsJsonStr)
//            throws JSONException {
//
//        JSONObject trailersJson = new JSONObject(trailersJsonStr);
//        JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
//
//        boolean trailersError = (has_error(trailersJson));
//        boolean reviewsError = (has_error(reviewsJson));
//        // if no valid json to work on return
//        if (trailersError && reviewsError) return null;
//
//        JSONArray trailersArray = trailersJson.getJSONArray(MDB_LIST);
//        JSONArray reviewsArray = reviewsJson.getJSONArray(MDB_LIST);
//
//        ContentValues trailersContentValues = new ContentValues(); //[trailersArray.length()];
//        ContentValues reviewsContentValues = new ContentValues();//[reviewsArray.length()];
//
//        String youtubePath = "https://www.youtube.com/watch?v=";
//        // TODO CHANGE THIS SO STORING TO DB IN A ARRAY .toString, NOT SEPARATE ContentValues?
//
//        ArrayList<String> reviewsCellEntry = new ArrayList<>();
//        if (reviewsError == false) {
//            // https://api.themoviedb.org/3/movie/500/reviews?api_key=bf8804126f6854cbd4d8c4d1ea8a4f31
//            for (int reviewNum = 0; reviewNum < reviewsArray.length(); reviewNum++) {
//                ContentValues newTrailerEntry = new ContentValues();
//
//                JSONObject newReviewObj = reviewsArray.getJSONObject(reviewNum);
//                String reviewUrlStr = newReviewObj.getString(MDB_URL);
//
//                //newTrailerEntry.put(MoviesEntry.COLUMN_REVIEWS, reviewUrlStr);
//                reviewsCellEntry.add(reviewUrlStr);
//                //reviewsContentValues[reviewNum] = newTrailerEntry;
//            }
//        }
//        // TODO it's converting an array to a string so you need to deconvert ALSO may not need
//        // to encode, *test*, but will always need to cast back to array.
//        reviewsContentValues.put(MoviesEntry.COLUMN_REVIEWS, reviewsCellEntry.toString());
//        // TODO bug check that if one of these is ContentValues arrays is empty it still works
//        // we have created data in the right format, we still need to INSERT into db
//        return reviewsContentValues;
//    }

        /**
         * Parse the JSON and convert it into ContentValues that can be inserted into our database.
         * Get the trailers youtube key and Reviews url Strings from json
         *
         * @param context         An application context, such as a service or activity context.
         * @param trailersJsonStr JSON String to parse into ContentValues.
         * @return An array of ContentValues parsed from the JSON.
         */
    public static ContentValues getDetailsDataFromJson(Context context,
                                                       String trailersJsonStr, String reviewsJsonStr)
            throws JSONException {


        ArrayList<String> detailsCellEntry = splitToJson(trailersJsonStr, MDB_TRAILERS_URL_KEY);
        detailsCellEntry.addAll(splitToJson(reviewsJsonStr, MDB_REVIEWS_URL_KEY));

        if (detailsCellEntry == null) {
            Log.wtf(TAG, "no reviews or trailers returning null");
            return null;
        }
        // TODO it's converting an array to a string so you need to deconvert ALSO may not need
        // to encode, *test*, but will always need to cast back to array.
        ContentValues detailsContentValues = new ContentValues();
        detailsContentValues.put(MoviesEntry.COLUMN_TRAILERS, detailsCellEntry.toString());
        // TODO bug check that if one of these is ContentValues arrays is empty it still works
        // we have created data in the right format, we still need to INSERT into db
        Log.wtf(TAG, "saving urls with: " + String.valueOf(detailsCellEntry));
        Log.wtf(TAG, "exiting JsonUtils with: " + detailsCellEntry.size() + " trailers/reviews");
        return detailsContentValues;
    }
    /* worker method */
    private static ArrayList<String> splitToJson(String dataJsonStr, String MDB_jsonObjKey)
            throws JSONException {
        JSONObject dataJson = new JSONObject(dataJsonStr);

        boolean hasError = (has_error(dataJson));
        // if no valid json to work on return
        if (hasError) {
            Log.wtf(TAG, "Json has error exiting JsonUtils");
            return null;
        }
        Log.wtf(TAG, "attempting to obtain JSON array from: " + dataJsonStr);

        JSONArray detailsArray = dataJson.getJSONArray(MDB_LIST);

        if (detailsArray.length() == 0 || detailsArray == null) {
            // TODO empty JSON arrays have length 1, why?!
            Log.w(TAG, "JSON array empty = " + detailsArray.length());
            Log.w(TAG, "JSONArray = " + detailsArray);
            // return an empty array
            return new ArrayList<>();
        }

        String youtubePath = "https://www.youtube.com/watch?v=";
        ArrayList<String> detailsCellEntry = new ArrayList<>();

        Log.w(TAG, "now iterating through JSON array length = " + detailsArray.length());
        // https://api.themoviedb.org/3/movie/550/reviews?api_key=bf8804126f6854cbd4d8c4d1ea8a4f31
        for (int arrayIndex = 0; arrayIndex < detailsArray.length(); arrayIndex++) {
            JSONObject newDetailObj = detailsArray.getJSONObject(arrayIndex);

            String detailUrlStr;
            if (newDetailObj.has(MDB_jsonObjKey)) {
                if (MDB_jsonObjKey == MDB_TRAILERS_URL_KEY) {
                    detailUrlStr = youtubePath + newDetailObj.getString(MDB_jsonObjKey);
                } // else we have a full review url
                else {
                    detailUrlStr = newDetailObj.getString(MDB_jsonObjKey);
                }

                //newTrailerEntry.put(MoviesEntry.COLUMN_REVIEWS, reviewUrlStr);
                detailsCellEntry.add(detailUrlStr);
                //reviewsContentValues[reviewNum] = newTrailerEntry;
            }
        }
        return detailsCellEntry;
    }

    /*
    * Checks
    * @param downloadedJson for errors
     */
    private static boolean has_error(JSONObject downloadedJson) throws JSONException {
        if (downloadedJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = downloadedJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    return false;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return true;
                default:
                    /* Server probably down */
                    return true;
            }
        }
        return false;
    }
}