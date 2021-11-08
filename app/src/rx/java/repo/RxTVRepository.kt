package repo

import TVLocalAPICall
import TVRemoteAPICall
import com.example.tvguide.mapping.ModelMap
import com.example.tvguide.model.TVScheduleModel
import com.example.tvguide.model.TVUIModel
import io.reactivex.rxjava3.core.Single

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
class RxTVRepository(private val call : TVRemoteAPICall) : IRxTVRepository {

    override fun fetchTVListBySchedule(): Single<Map<String, List<TVScheduleModel>>> {
        return call.getTVScheduleList()
            .map { model ->
                val hashMap = hashMapOf<String, List<TVScheduleModel>>()
                model.forEach { resp ->
                    hashMap[resp.channel] = resp.programs.toSet().map { ModelMap.toTVScheduleModel(it) }
                }

                hashMap
            }
    }
    //TESTME: remote data
    override fun fetchTVListByChannel(chanName: String): Single<List<TVUIModel>> {
        return call.getTVListByChannelName(chanName)
            .map{ resp -> resp.map {ModelMap.toTVUIModel(it) } }
    }
    //TESTME: remote data
    override fun fetchProgramDetailById(chanName: String, id: String): Single<TVUIModel> {
        return call.getProgramById(chanName, id)
            .map { ModelMap.toTVUIModel(it) }
    }
}