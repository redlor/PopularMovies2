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
    @GET("popular")
    Observable<Root> getRepository(@Query("api_key") String apiKey);

    @GET("top_rated")
    Observable<Root> getTopRatedRepo(@Query("api_key") String apiKey);

    // Url requests for trailers and reviews
    @GET("{id}/videos")
    Observable<VideosRoot> getVideos(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("{id}/reviews")
    Observable<ReviewsRoot> getReviews(@Path("id") int movieId, @Query("api_key") String apiKey);
}
