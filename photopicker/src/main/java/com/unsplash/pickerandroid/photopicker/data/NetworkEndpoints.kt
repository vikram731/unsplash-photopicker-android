package com.unsplash.pickerandroid.photopicker.data

import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit endpoints definition.
 */
interface NetworkEndpoints {

    @GET("collections/{collection_id}/photos")
    fun loadPhotos(
        @Path("collection_id") collectionId: String,
        @Query("client_id") clientId: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    ): Observable<Response<List<UnsplashPhoto>>>

    @GET("search/photos")
    fun searchPhotos(
        @Query("client_id") clientId: String,
        @Query("query") criteria: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    ): Observable<Response<SearchResponse>>

    @GET
    fun trackDownload(@Url url: String, @Header("Authorization") content_type: String): Completable

    companion object {
        const val BASE_URL = "https://api.unsplash.com/"
    }
}
