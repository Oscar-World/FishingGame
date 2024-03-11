package abled.kkont.fishinggame.network

import abled.kkont.fishinggame.data.Rank
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {

    @FormUrlEncoded
    @POST("fishingGame/rankProcess.php")
    fun readRankArrayList(

        @Field("mode") mode: String

    ): Call<ArrayList<Rank>>

    @FormUrlEncoded
    @POST("fishingGame/rankProcess.php")
    fun updateRankArrayList(

        @Field("mode") mode: String,
        @Field("nickname") nickname: String,
        @Field("type") type: String,
        @Field("content") content: String

    ): Call<ArrayList<Rank>>

    @FormUrlEncoded
    @POST("fishingGame/userProcess.php")
    fun duplicateNickname(

        @Field("nickname") nickname: String

    ) : Call<String>



}