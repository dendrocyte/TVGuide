package repo

import TVLocalAPICall
import com.example.tvguide.mapping.ModelMap
import com.example.tvguide.model.TVScheduleModel
import com.example.tvguide.model.TVUIModel
import io.reactivex.rxjava3.core.Single

/**
 * Created by luyiling on 2020/12/28
 * Modified by
 *
 * TODO:
 * Description: 定義出實際回傳的格式
 * 以方便做mock 此類做測試
 * @params
 * @params
 */
class TestRxTVRepository(private val localCall : TVLocalAPICall) : IRxTVRepository{

    /*tv list by schedule*/
    override fun fetchTVListBySchedule(): Single<Map<String, List<TVScheduleModel>>> {
        return localCall.getTVScheduleList()
            .map { model ->
                val hashMap = hashMapOf<String, List<TVScheduleModel>>()
                model.forEach { resp ->
                    //排除重複的節目時段
                    hashMap[resp.channel] = resp.programs.toSet().map { ModelMap.toTVScheduleModel(it) }
                }

                hashMap
            }
    }

    //TESTME: remote data
    /*tv list by channel*/
    override fun fetchTVListByChannel(chanName: String): Single<List<TVUIModel>> {
        return localCall.getTVListByChannelName(chanName)
            .map{ resp -> resp.map { ModelMap.toTVUIModel(it) } }
    }

    //TESTME: remote data
    /*program detail*/
    override fun fetchProgramDetailById(chanName: String, id: String): Single<TVUIModel> {
        return localCall.getProgramById(chanName, id)
            .map { ModelMap.toTVUIModel(it) }
    }

}