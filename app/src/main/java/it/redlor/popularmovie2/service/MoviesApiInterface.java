package it.redlor.popularmovie2.service;

import io.reactivex.Observable;
import it.redlor.popularmovie2.pojos.ReviewsRoot;
import it.redlor.popularmovie2.pojos.Root;
import it.redlor.popularmovie2.pojos.VideosRoot;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface to define endpoints
 */

public interface MoviesApiInterface {

    // Customize the Url request
    @GET("movie/popular")
    Observable<Root> getRepository(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Observable<Root> getTopRatedRepo(@Query("api_key") String apiKey);

    // Url requests for trailers and reviews
    @GET("movie/{id}/videos")
    Observable<VideosRoot> getVideos(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Observable<ReviewsRoot> getReviews(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("search/movie")
    Observable<Root> getSearchedMovie(@Query("api_key") String apiKey, @Query("query") String movieName);
}

// https://api.themoviedb.org/3/search/movie?api_key=###&query=tron