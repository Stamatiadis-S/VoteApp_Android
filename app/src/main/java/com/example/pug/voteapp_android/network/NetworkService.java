package com.example.pug.voteapp_android.network;

import android.content.SharedPreferences;

import com.example.pug.voteapp_android.models.Participation;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.models.Token;
import com.example.pug.voteapp_android.models.User;
import com.example.pug.voteapp_android.models.Vote;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.reactivex.Observable;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.List;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class NetworkService {

    private ResourceConverter resourceConverter;
    private ObjectMapper objectMapper;
    private NetworkApi networkApi;

    public NetworkService(String url) {

        String BASE_API_URL = url;

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        resourceConverter = new ResourceConverter(objectMapper, BASE_API_URL, Poll.class, User.class, Participation.class, Vote.class);
        resourceConverter
                .enableSerializationOption(com.github.jasminb.jsonapi.SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(new JSONAPIConverterFactory(resourceConverter))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(BASE_API_URL)
                .build();
        networkApi = retrofit.create(NetworkApi.class);
    }
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public NetworkApi getNetworkApi() {
        return networkApi;
    }

    public interface NetworkApi {

        //User Resource

        @FormUrlEncoded
        @POST("users/token") //This call is form encoded in accordance with the OAuth2 spec.
        Observable<Response<Token>> submitLoginObservable(
                @Field("grant_type") String grantType,
                @Field("username") String username,
                @Field("password") String password);

        @Headers({"Content-Type: application/json"})
        @POST("users")
        Observable<Response<JSONAPIDocument<User>>> submitSignupObservable(
                @Body JSONAPIDocument<User> requestBody);

        @Headers({"Content-Type: application/json"})
        @GET("users/{id}")
        Observable<Response<JSONAPIDocument<User>>> fetchMyUserProfile(
                @Path("id") String id,
                @Body JSONAPIDocument<User> requestBody);

        //Poll Resource

        @Headers({"Content-Type: application/json"})
        @POST("polls")
        Observable<Response<JSONAPIDocument<Poll>>> createPollObservable(
                @Header("Authorization") String authorizationHeader,
                @Body JSONAPIDocument<Poll> requestBody);

        @Headers({"Content-Type: application/json"})
        @GET("polls/{id}")
        Observable<Response<JSONAPIDocument<Poll>>> fetchPollObservable(
                @Path("id") String id,
                @Header("Authorization") String authorizationHeader);

        @Headers({"Content-Type: application/json"})
        @GET("polls/public")
        Observable<Response<JSONAPIDocument<List<Poll>>>> fetchPublicPollsObservable(
                @Query("page") int page,
                @Query("size") int size,
                @Header("Authorization") String authorizationHeader);

        @Headers({"Content-Type: application/json"})
        @GET("polls/private")
        Observable<Response<JSONAPIDocument<List<Poll>>>> fetchPrivatePollsObservable(
                @Query("page") int page,
                @Query("size") int size,
                @Header("Authorization") String authorizationHeader);

        @Headers({"Content-Type: application/json"})
        @GET("polls/created")
        Observable<Response<JSONAPIDocument<List<Poll>>>> fetchCreatedPollsObservable(
                @Query("page") int page,
                @Query("size") int size,
                @Header("Authorization") String authorizationHeader);

        //Participation Resource

        @Headers({"Content-Type: application/json"})
        @POST("participations")
        Observable<Response<JSONAPIDocument<Participation>>> createParticipationObservable(
                @Header("Authorization") String authorizationHeader,
                @Body JSONAPIDocument<Participation> requestBody);

        //Vote Resource

        @Headers({"Content-Type: application/json"})
        @POST("votes")
        Observable<Response<JSONAPIDocument<Vote>>> createVoteObservable(
                @Header("Authorization") String authorizationHeader,
                @Body JSONAPIDocument<Vote> requestBody);
    }
}