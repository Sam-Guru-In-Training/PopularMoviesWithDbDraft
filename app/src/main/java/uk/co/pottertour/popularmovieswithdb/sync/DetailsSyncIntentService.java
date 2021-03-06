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

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DetailsSyncIntentService extends IntentService {

    private final static String TAG = DetailsSyncIntentService.class.getSimpleName();

    public DetailsSyncIntentService() {
        super("DetailsSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.wtf(TAG, "launching MoviesSyncTask.syncDetails");
        int movieId = intent.getExtras().getInt("movieId");
        //MoviesSyncTask.syncDetails(this, movieId);
        Log.v(TAG, "movieId: " + movieId);
        MoviesSyncTask.syncDetails(this, movieId);
        Log.wtf(TAG, "exiting IntentService after network request processing");
    }
}