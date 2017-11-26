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
package uk.co.pottertour.popularmovieswithdb.sync;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.net.URL;

import uk.co.pottertour.popularmovieswithdb.data.MoviesContract;
import uk.co.pottertour.popularmovieswithdb.utilities.MoviesDBJsonUtils;
import uk.co.pottertour.popularmovieswithdb.utilities.NetworkUtils;

//import com.example.android.sunshine.data.SunshinePreferences;
//import uk.co.pottertour.popularmovieswithdb.utilities.NotificationUtils;

public class MoviesSyncTask {

    private static final String TAG = MoviesSyncTask.class.getSimpleName();
    /**
     * Performs the network request for movies, parses the JSON from that request, and
     * inserts the new movies information into our ContentProvider.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncMovies(Context context) {

        try {
            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * weather. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            // TODO need to retrieve sort_by from shared prefs
            Log.wtf(TAG, "getting movies Json from server");
            URL moviesRequestUrl = NetworkUtils.buildUrl("popular");

            /* Use the URL to retrieve the JSON */
            String moviesJsonResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);

            /* Parse the JSON into a list of weather values */
            ContentValues[] moviesValues = MoviesDBJsonUtils.getFullMoviesDataFromJson(context, moviesJsonResponse);
            //OpenWeatherJsonUtils
                    //.getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (moviesValues != null && moviesValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver moviesContentResolver = context.getContentResolver();

                /* Delete old weather data because we don't need to keep multiple days' data */
//                moviesContentResolver.delete(
//                        MoviesContract.MoviesEntry.CONTENT_URI,
//                        null,
//                        null);

                // TODO check about collisions, we want to keep the favourite field not overwrite
                /* Insert all our downloaded movies data into the app's ContentProvider */
                moviesContentResolver.bulkInsert(
                        MoviesContract.MoviesEntry.CONTENT_URI,
                        moviesValues);
                Log.wtf(TAG, "movies inserted into database");

            /* If the code reaches this point, we have successfully performed our sync */

            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }

    synchronized public static void syncDetails(Context context, int movieId) {

        try {
            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * weather. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            Log.v(TAG, "about to build urls with movieId: " + movieId);

            URL[] returnedURLs = NetworkUtils.buildDetailsUrls(movieId);
            URL trailersRequestUrl = returnedURLs[0];
            URL reviewsRequestUrl = returnedURLs[1];

            /* Use the URL to retrieve the JSON */

            // TODO https://api.themoviedb.org/3/movie/550/videos?api_key=bf8804126f6854cbd4d8c4d1ea8a4f31
            // TODO https://api.themoviedb.org/3/movie/550/reviews?api_key=bf8804126f6854cbd4d8c4d1ea8a4f31
            String trailersJsonResponse = NetworkUtils.getResponseFromHttpUrl(trailersRequestUrl);
            String reviewsJsonResponse = NetworkUtils.getResponseFromHttpUrl(reviewsRequestUrl);

            /* Parse the JSON into a list of weather values */
            // TODO the return values here may need to be separated into reviews and trailers values

            ContentValues trailersContentValues = MoviesDBJsonUtils
                    .getDetailsDataFromJson(context,
                    trailersJsonResponse, reviewsJsonResponse);

            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            Log.wtf(TAG, "checking if we have downloaded any Json");
            if ((trailersContentValues != null)) { // || (reviewsContentValues != null)) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver moviesContentResolver = context.getContentResolver();

                Log.wtf(TAG, "we have Json! & movieId is " + movieId);

//                moviesContentResolver.bulkInsert(
//                        MoviesContract.MoviesEntry.CONTENT_URI,
//                        trailersContentValues);
                String selection = MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?";
                // selectionArgs
                // TODO swap this for the row id?
                String[] selectionArgs = { "" + movieId};
                /* update our contentProvider with trailers for the appropriate row */
                Uri detailsQueryUri = ContentUris.withAppendedId(
                        MoviesContract.MoviesEntry.INSERT_DETAILS_URI, movieId);

                int rowsUpdated = moviesContentResolver.update(
                        detailsQueryUri, //CONTENT_URI,
                        trailersContentValues,
                        selection,
                        selectionArgs);

                if (rowsUpdated == 0) {
                    throw new Exception("No trailers added to db, weird?");
                }

                //Log.wtf(TAG, "trailers inserted into database: " + rowUpdated);

            /* If the code reaches this point, we have successfully performed our sync */

            }
        } catch (Exception e) {
            /* Server probably invalid */
            Log.wtf(TAG, "dunno what this is about, Server probably invalid");
            e.printStackTrace();
        }

    }
}