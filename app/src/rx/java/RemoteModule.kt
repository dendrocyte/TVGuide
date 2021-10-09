import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Created by luyiling on 2021/8/6
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
var remoteModule = module {
    //okhttp
    single<OkHttpClient>(named("noAuth")) {
        OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()
    }
    //gson
    factory<Gson>(named("time")) {
        GsonBuilder()
            .registerTypeAdapter( //register time format
                ZonedDateTime::class.java,
                JsonDeserializer { json, typeOfT, context ->
                    ZonedDateTime.parse(json.asString)
                })
            .create()
    }
    //api
    //if db.json is > 10k, show error
    single<TVRemoteAPICall>{
        Retrofit.Builder()
            .baseUrl(TVRemoteAPICall.DEV_BASE_URL)//設置baseUrl即要連的網站
            .addConverterFactory(
                GsonConverterFactory.create(//用Gson作為資料處理Converter
                get<Gson>(named("time"))
            ))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) //Rx-call converter
            .client(get<OkHttpClient>(named("noAuth")))
            .build()
            .create(TVRemoteAPICall::class.java)
    }
    //local json-server
    single<TVLocalAPICall>{
        Retrofit.Builder()
            .baseUrl(TVLocalAPICall.LOCAL_BASE_URL)//設置baseUrl即要連的網站
            .addConverterFactory(
                GsonConverterFactory.create(//用Gson作為資料處理Converter
                    get<Gson>(named("time"))
                ))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) //Rx-call converter
            .client(get<OkHttpClient>(named("noAuth")))
            .build()
            .create(TVLocalAPICall::class.java)
    }
}