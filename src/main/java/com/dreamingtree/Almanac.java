package com.dreamingtree;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Retrofit client for DMBAlmanac.com
 */
interface Almanac {
    /**
     * Returns the raw html response for the homepage.
     *
     * @return a Call to the executed
     */
    @GET("/")
    Call<ResponseBody> homepage();

    @GET("Songs.aspx")
    Call<ResponseBody> songs();

    @GET("SongStats.aspx")
    Call<ResponseBody> song(@Query("sid") int songId);

    /**
     * Returns the raw html response for a specific show's page.
     *
     * @param showUrl a relative url for the show
     * @return a Call to the executed
     */
    @GET
    Call<ResponseBody> show(@Url String showUrl);
}
