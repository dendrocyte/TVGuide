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
 * /*for mock data use; if remote data is not built ready*/
 * @params
 * @params
 */
interface TVLocalAPICall {
    companion object{
        //TODO: every time to build, have to command adb reverse tcp:3000 tcp:3000
        const val LOCAL_BASE_URL = "http://localhost:3000/"
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