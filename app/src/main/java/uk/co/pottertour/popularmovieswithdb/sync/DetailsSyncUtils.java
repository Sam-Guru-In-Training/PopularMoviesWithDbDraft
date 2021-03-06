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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

import uk.co.pottertour.popularmovieswithdb.data.MoviesContract;

public class DetailsSyncUtils {

    /*
     * Interval at which to sync with the reviews & trailers. Use TimeUnit for convenience, rather
     * than writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */
    private static final int SYNC_INTERVAL_HOURS = 3;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

    private static boolean sInitialized;

    private static final String MOVIES_SYNC_TAG = "DETAILS-sync";
    private static final String TAG = DetailsSyncUtils.class.getSimpleName();

    /**
     * Schedules a repeating sync of Sunshine's weather data using FirebaseJobDispatcher.
     * @param context Context used to create the GooglePlayDriver that powers the
     *                FirebaseJobDispatcher
     */
    static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context) {

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        /* Create the Job to periodically sync Sunshine */
        Job syncMoviesJob = dispatcher.newJobBuilder()
                /* The Service that will be used to sync Sunshine's data */
                .setService(MoviesFirebaseJobService.class)
                /* Set the UNIQUE tag used to identify this Job */
                .setTag(MOVIES_SYNC_TAG)
                /*
                 * Network constraints on which this Job should run. We choose to run on any
                 * network, but you can also choose to run only on un-metered networks or when the
                 * device is charging. It might be a good idea to include a preference for this,
                 * as some users may not want to download any data on their mobile plan. ($$$)
                 */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /*
                 * setLifetime sets how long this job should persist. The options are to keep the
                 * Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want Sunshine's weather data to stay up to date, so we tell this Job to recur.
                 */
                .setRecurring(true)
                /*
                 * We want the weather data to be synced every 3 to 4 hours. The first argument for
                 * Trigger's static executionWindow method is the start of the time frame when the
                 * sync should be performed. The second argument is the latest point in time at
                 * which the data should be synced. Please note that this end time is not
                 * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
                 */
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag with provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();

        /* Schedule the Job with the dispatcher */
        dispatcher.schedule(syncMoviesJob);
    }
    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver
     */
    synchronized public static void initialize(@NonNull final Context context, final int movieId) {

        /*
         * Only perform initialization once per app lifetime. If initialization has already been
         * performed, we have nothing to do in this method.
         */
//        if (sInitialized) {
//            Log.wtf(TAG, "already intialized not getting new json");
//            return;
//        }
        Log.wtf(TAG, "proceeding with initialisation");
        sInitialized = true;

        /*
         * This method call triggers the app to create its task to synchronize weather data
         * periodically.
         */
        scheduleFirebaseJobDispatcherSync(context);

        /*
         * We need to check to see if our ContentProvider has data to display in our forecast
         * list. However, performing a query on the main thread is a bad idea as this may
         * cause our UI to lag. Therefore, we create a thread in which we will run the query
         * to check the contents of our ContentProvider.
         */
        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {

                /* URI for every row of weather data in our weather table*/
                Uri forecastQueryUri = MoviesContract.MoviesEntry.CONTENT_URI;

                /*
                 * Since this query is going to be used only as a check to see if we have any
                 * data (rather than to display data), we just need to PROJECT the ID of each
                 * row. In our queries where we display data, we need to PROJECT more columns
                 * to determine what movie details need to be displayed.
                 */
                String[] projectionColumns = {MoviesContract.MoviesEntry.COLUMN_REVIEWS,
                        MoviesContract.MoviesEntry.COLUMN_TRAILERS};
                // want all rows from the projection
                String selectionStatement = null;
                        //MoviesContract.MoviesEntry.getSqlSelectForTodayOnwards();

                /* Here, we perform the query to check to see if we have any movies data */
//                Cursor cursor = context.getContentResolver().query(
//                        forecastQueryUri,
//                        projectionColumns,
//                        selectionStatement,
//                        null,
//                        null);
//                /*
//                 * A Cursor object can be null for various different reasons. A few are
//                 * listed below.
//                 *
//                 *   1) Invalid URI
//                 *   2) A certain ContentProvider's query method returns null
//                 *   3) A RemoteException was thrown.
//                 *
//                 * Bottom line, it is generally a good idea to check if a Cursor returned
//                 * from a ContentResolver is null.
//                 *
//                 * If the Cursor was null OR if it was empty, we need to sync immediately to
//                 * be able to display data to the user.
//                 */
//                if (null == cursor || cursor.getCount() == 0) {
//                    // database is empty get pictures and movieDetails
//                    Log.wtf(TAG, "No trailers or reviews in DB, starting immediate sync");
//                    startImmediateSync(context, movieId);
//                }
                Log.wtf(TAG, "in thread bit startImmediateSync");
                startImmediateSync(context, movieId);
                Log.wtf(TAG, "passed startImmediateSync");

                /* Make sure to close the Cursor to avoid memory leaks! */
                //cursor.close();
            }
        });

        /* Finally, once the thread is prepared, fire it off to perform our checks. */
        checkForEmpty.start();
    }

    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */
    public static void startImmediateSync(@NonNull final Context context, int movieId) {
        Log.wtf(TAG, "starting Intent Service");
        Intent intentToSyncImmediately = new Intent(context, DetailsSyncIntentService.class);
        intentToSyncImmediately.putExtra("movieId", movieId);
        context.startService(intentToSyncImmediately);
        Log.wtf(TAG, "returning from startImmediateSync");
    }
}