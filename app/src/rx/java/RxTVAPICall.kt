import com.example.tvguide.remote.Program
import com.example.tvguide.remote.ResponseModel
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by luyiling on 2021/8/5
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
interface RxTVAPICall {
    companion object{
        const val DEV_BASE_URL= "https://my-json-server.typicode.com/dendrocyte/mock-restful/"
    }

    @GET("schedule")
    fun getTVScheduleList() : Single<List<ResponseModel>>

    @GET("schedule")
    fun getTVListByChannelName(@Query("channel") chanName : String) : Single<List<Program>>

    //TESTME
    //因為Free Server 的json 內容大小有限制, 只好用schedules 來找
    //deep property 是由Free Server 設計的
    @GET("schedule")
    fun getProgramById(@Query("channel") chanName : String,
                       @Query("programs.pid") id: String
    ) : Single<Program>
}