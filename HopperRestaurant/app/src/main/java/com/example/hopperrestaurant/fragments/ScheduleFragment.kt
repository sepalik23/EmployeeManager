package com.example.hopperrestaurant.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.R
import com.example.hopperrestaurant.activities.ScheduleDayActivity
import com.example.hopperrestaurant.activities.ScheduleWeekendActivity
import com.example.hopperrestaurant.adapters.CalendarAdapter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class ScheduleFragment : Fragment() {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDate: LocalDate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets(view)
        selectedDate = LocalDate.now()
        setMonthView()

        view.findViewById<ImageButton>(R.id.previousMonthAction).setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setMonthView()
            }
        }

        view.findViewById<ImageButton>(R.id.nextMonthAction).setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setMonthView()
            }
        }

        //Listening for export button click and calls "onExportClick" if export button is clicked
        view.findViewById<Button>(R.id.ExportBtn).setOnClickListener {
            onExportClick()
        }
    }



    private fun initWidgets(view: View) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        monthYearText = view.findViewById(R.id.monthYear)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMonthView() {
        monthYearText.text = monthYearFromDate(selectedDate)
        val daysInMonth = daysInMonthArray(selectedDate)
        val calendarAdapter = CalendarAdapter(daysInMonth, this)
        val layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecyclerView.layoutManager = layoutManager
        calendarRecyclerView.adapter = calendarAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun daysInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstOfMonth = selectedDate.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value

        // Check if the first day of the month is a Sunday
        val startIndex = if (dayOfWeek == 7) 0 else dayOfWeek

        for (i in 0..36) {
            if (i < startIndex || i >= daysInMonth + startIndex) {
                daysInMonthArray.add("")
            } else {
                daysInMonthArray.add((i - startIndex + 1).toString())
            }
        }
        return daysInMonthArray
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onItemClick(position: Int, dayText: String) {
        if (dayText.isNotEmpty()) {

            val date = "$dayText ${monthYearFromDate(selectedDate)}"
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
            val dayOfWeek = LocalDate.parse(date, formatter).dayOfWeek

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                val intent = Intent(requireContext(), ScheduleWeekendActivity::class.java)
                intent.putExtra("date", date)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), ScheduleDayActivity::class.java)
                intent.putExtra("date", date)
                startActivity(intent)
            }
        }
    }

    fun getMonthYearText(): String {
        return monthYearText.text.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        setMonthView()
        super.onResume()
    }


    // on ExportBtn click, this function calls the dbhelper method, "getMonthlyScheduleData",
    // formats that data and exports it as a txt to the downloads folder of the nexus 4 emulator
    private fun onExportClick() {

        //Gets the current month and year as a string EX. April 2023
        val selectedMonthYear = getMonthYearText()

        //Parse and format "selectedMonthYear" from "April 2023" to "2023-04"
        val selectedDate = SimpleDateFormat("MMMM yyyy", Locale.US).parse(selectedMonthYear)
        val formattedMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(selectedDate)

        //Gets data from MonthlySched table --> takes formattedMonthYear as argument
        val data = context?.let { DBHelper(it).getMonthlyScheduleData(formattedMonthYear) }

        //file name and location
        val fileName = "${getMonthYearText()} Schedule.txt"
        val downloadLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        //File object and string builder
        val file = File(downloadLoc, fileName)
        val stringContent = StringBuilder()

        //initialize column names for txt file and append formatted rows of data into the txt
        stringContent.append("DATE         SHIFT   NAME(First, Nickname, Last)\n\n")
        if (data != null) {
            for (i in data) {
                // Format each row with proper spacing
                val formattedData = String.format(
                    "%-13s%-8s%s \"%s\" %s",
                    i[0], i[1], i[2], i[3].replace("\"", "\\\""), i[4]
                )
                stringContent.append(formattedData).append("\n")
            }
        }
        try {

            //Write to the txt file
            val outputStream = FileOutputStream(file)
            outputStream.write(stringContent.toString().toByteArray())
            outputStream.close()
            Toast.makeText(context, "Exported as '$fileName' to Downloads", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting schedule: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

}