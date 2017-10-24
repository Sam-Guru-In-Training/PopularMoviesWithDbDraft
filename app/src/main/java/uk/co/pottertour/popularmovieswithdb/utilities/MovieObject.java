package uk.co.pottertour.popularmovieswithdb.utilities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sam on 16/08/2017.
 * Stores details for a movie
 */

public class MovieObject implements Parcelable {
    /* These are the values that will be collected */
    private int mMovieID;
    private double mRating;
    private String mTitle;
    private String mPosterPath;
    private String mOverview;
    private String mReleaseDate;

    public MovieObject(int id, double rating, String title, String poster,
                       String overview, String release) {
        mMovieID = id;
        mRating = rating;
        mTitle = title;
        mPosterPath = poster;
        mOverview = overview;
        mReleaseDate = release;
    }

    protected MovieObject(Parcel in) {
        mMovieID = in.readInt();
        mRating = in.readDouble();
        mTitle = in.readString();
        mPosterPath = in.readString();
        mOverview = in.readString();
        mReleaseDate = in.readString();
    }
    public int getID() { return mMovieID; }
    public double getRating() { return mRating; }

    public String getTitle() { return mTitle; }
    public String getPoster() { return mPosterPath; }
    public String getOverview() { return mOverview; }
    public String getReleaseDate() { return mReleaseDate; }

    public static final Creator<MovieObject> CREATOR = new Creator<MovieObject>() {
        @Override
        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        @Override
        public MovieObject[] newArray(int size) {
            return new MovieObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(mMovieID);
        parcel.writeDouble(mRating);
        parcel.writeString(mTitle);
        parcel.writeString(mPosterPath);
        parcel.writeString(mOverview);
        parcel.writeString(mReleaseDate);
    }
    private void readFromParcel(Parcel in ) {

        mMovieID = in.readInt();
        mRating  = in.readDouble();
        mTitle   = in.readString();
        mPosterPath = in.readString();
        mOverview = in.readString();
        mReleaseDate = in.readString();
    }

}
