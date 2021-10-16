package repo

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
interface IRxTVRepository {

    /*tv list by schedule*/
    fun fetchTVListBySchedule() : Single<Map<String, List<TVScheduleModel>>>

    /*tv list by single channel*/
    fun fetchTVListByChannel(chanName : String) : Single<List<TVUIModel>>

    /*program detail*/
    fun fetchProgramDetailById(chanName : String, id: String) : Single<TVUIModel>

}