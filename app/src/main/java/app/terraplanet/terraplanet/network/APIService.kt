package app.terraplanet.terraplanet.network

import app.terraplanet.terraplanet.model.*
import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface APIService {

    @GET("/server/status")
    fun status(): Single<ServerStatus>

    @GET("/wallet/create/{network}")
    fun create(@Path("network") network: String): Single<Wallet>

    @POST("/wallet/restore")
    fun restore(@Body params: RequestBody): Single<Address>

    @GET("/wallet/balance/{address}/{network}")
    fun balance(@Path("address") address: String, @Path("network") network: String): Single<Balance>

    @GET("/market/rate/uluna/uusd")
    fun lunaRate(): Single<Rate>

    @POST("/anchor/balance")
    fun earnBalance(@Body params: RequestBody): Single<EarnBalance>

    @POST("/anchor/market")
    fun rate(@Body params: RequestBody): Single<Market>

    @POST("/anchor/deposit")
    fun anchorDeposit(@Body params: RequestBody): Single<Any>

    @POST("/anchor/withdraw")
    fun anchorWithdraw(@Body params: RequestBody): Single<Any>

    @POST("/wallet/swap/preview")
    fun swapPreview(@Body params: RequestBody): Single<String>

    @POST("/wallet/swap")
    fun swap(@Body params: RequestBody): Single<String>

    @POST("/wallet/send/preview")
    fun sendPreview(@Body params: RequestBody): Single<String>

    @POST("/wallet/send")
    fun send(@Body params: RequestBody): Single<String>
}