import com.example.tvguide.ITVScheduleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import repo.IRxTVRepository
import repo.RxTVRepository
import usecase.RxTVScheduleUsecase
import vm.RxTVScheduleViewModel

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
var appModule = module {

    //repo
    single<IRxTVRepository> { RxTVRepository(get()) }
    //usecase
    factory { RxTVScheduleUsecase(get()) }
    //vm
    viewModel<ITVScheduleViewModel> { RxTVScheduleViewModel(get()) }

}