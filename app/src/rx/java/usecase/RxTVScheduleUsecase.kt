package usecase

import com.example.tvguide.*
import com.example.tvguide.model.TVListBySchedule
import com.example.tvguide.model.TVScheduleModel
import com.example.tvguide.model.TickGroupModel
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Single
import repo.IRxTVRepository
import kotlin.collections.HashMap

/**
 * Created by luyiling on 2020/9/18
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class RxTVScheduleUsecase(private val repo: IRxTVRepository)
    : ITVScheduleUsecase<Single<List<TVListBySchedule>>>() {


    //FIXME: 考慮納入db 否則如何做filter
    //FIXME: 之後要考慮有channel name 但完全沒有該channel的資料
    //FIXME：因為資料不完整，apk 先呈現no schedule page
    override fun getSchedule() : Single<List<TVListBySchedule>> =
        //轉資料 Map<String, List<TVScheduleModel>>, key=channelName
        repo.fetchTVListBySchedule()
            .map { _map -> //sort ascending
                val mutable = _map.toMutableMap()
                for (k in _map.keys){
                    mutable[k] = _map[k]?.sortedBy { it.scheduleStart } ?: emptyList()

                    //FIXME: 只索取今日的行程(
                    // 1.第一個scheduleStart 比currentDateEnd大 => 就直接empty
                    // 2.最後一個scheduleEnd 比currentDateStart小 => 就直接empty
                    // 3.找出scheduleEnd >currentDateEnd || scheduleStart < currentDateStart 條件下 T->F [target,last] F->T[0,target]的瞬間
                    //若scheduleEnd 比 currentDateStart 小(比今日晚)，就改成emptyList()
//                    .filter { currentDateStart < it.scheduleEnd && currentDateEnd}
                }
                mutable
            }
            .map { mutableMap ->
                //針對每個channel name群組裡的資料檢查資料時間段，間隔補ob
                //TODO:考慮是否由insert 進入db 時就先處理

                fillGap(HashMap(mutableMap))

                mutableMap
            }
            .map {//轉資料 HashMap<String, List<LiveScheduleModel>> -> List<LiveListBySchedule>
                it.map { TVListBySchedule(it.value, it.key) }
            }


}