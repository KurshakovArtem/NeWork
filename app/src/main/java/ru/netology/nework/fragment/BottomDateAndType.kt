package ru.netology.nework.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import ru.netology.nework.R
import ru.netology.nework.databinding.BottomViewDateBinding
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.supportingFunctions.convertResponseToCardPost
import ru.netology.nework.supportingFunctions.toDate
import ru.netology.nework.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.getValue

class BottomDateAndType : BottomSheetDialogFragment() {

    private var _binding: BottomViewDateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomViewDateBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.onlineRadio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setEventType(EventType.ONLINE)
            }
        }

        binding.offlineRadio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setEventType(EventType.OFFLINE)
            }
        }

        binding.dateInputLayout.setEndIconOnClickListener {
            showDateTimePicker()
        }

        val savedDate = viewModel.eventEdited.value?.datetime
        if (!savedDate.isNullOrBlank()) {
            val dateText = convertResponseToCardPost(savedDate)
            binding.dateInput.setText(dateText)
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val savedDate = viewModel.eventEdited.value?.datetime?.toDate()

        if (savedDate != null) {
            calendar.time = savedDate
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_event_date))
            .setSelection(calendar.timeInMillis)
            .setPositiveButtonText(getString(R.string.next))
            .setNegativeButtonText(R.string.cancel)
            .build()

        datePicker.addOnPositiveButtonClickListener { selectionMillis ->
            calendar.timeInMillis = selectionMillis

            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .setTitleText(R.string.select_time)
                .setPositiveButtonText(getString(R.string.select))
                .setNegativeButtonText(R.string.cancel)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)

                val format = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                val formatted = format.format(calendar.time)

                binding.dateInput.setText(formatted)

                val formatIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                formatIso.timeZone = TimeZone.getTimeZone("UTC")
                viewModel.setEventDateTime(formatIso.format(calendar.time))
            }
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}