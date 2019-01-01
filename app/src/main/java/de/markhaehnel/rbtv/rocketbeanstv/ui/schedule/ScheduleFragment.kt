package de.markhaehnel.rbtv.rocketbeanstv.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import de.markhaehnel.rbtv.rocketbeanstv.AppExecutors
import de.markhaehnel.rbtv.rocketbeanstv.R
import de.markhaehnel.rbtv.rocketbeanstv.binding.FragmentDataBindingComponent
import de.markhaehnel.rbtv.rocketbeanstv.databinding.FragmentScheduleBinding
import de.markhaehnel.rbtv.rocketbeanstv.di.Injectable
import de.markhaehnel.rbtv.rocketbeanstv.ui.common.RetryCallback
import de.markhaehnel.rbtv.rocketbeanstv.util.autoCleared
import kotlinx.android.synthetic.main.fragment_schedule.*
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

class ScheduleFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<FragmentScheduleBinding>()

    private lateinit var scheduleViewModel: ScheduleViewModel
    private var adapter by autoCleared<ScheduleItemListAdapter>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentScheduleBinding>(
            inflater,
            R.layout.fragment_schedule,
            container,
            false,
            dataBindingComponent
        )

        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                scheduleViewModel.retry()
            }
        }

        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scheduleViewModel = ViewModelProviders.of(this, viewModelFactory).get(ScheduleViewModel::class.java)
        binding.setLifecycleOwner(viewLifecycleOwner)

        val rvAdapter = ScheduleItemListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors
        ) { clickedShow ->
            //TODO: Show details
        }
        binding.showList.adapter = rvAdapter
        this.adapter = rvAdapter

        binding.schedule = scheduleViewModel.schedule

        initSchedule()
    }

    private fun initSchedule() {
        scheduleViewModel.schedule.observe(viewLifecycleOwner, Observer { schedule ->
            if (schedule.data !== null && schedule.data.days.isNotEmpty()) {
                val items = schedule.data.days[0].items

                adapter.submitList(items)

                val cal = Calendar.getInstance(TimeZone.getTimeZone("gmt"))
                val currentIndex = items.indexOfLast {
                    it.timeEnd.before(cal.time)
                }

                show_list.scrollToPosition(currentIndex)
            }
        })
    }
}