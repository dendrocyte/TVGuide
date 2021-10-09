import com.example.tvguide.ITVScheduleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

import repo.IRxTVRepository
import repo.RxTVRepository
import repo.TestRxTVRepository
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
    single<IRxTVRepository>(named("remote")) { RxTVRepository(get()) }
    single<IRxTVRepository>(named("local")) { TestRxTVRepository(get()) }

    //usecase
    factory { RxTVScheduleUsecase(get(named("local"))) }
    //vm
    viewModel<ITVScheduleViewModel> { RxTVScheduleViewModel(get()) }

}