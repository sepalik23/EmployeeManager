package com.example.hopperrestaurant.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.R
import com.example.hopperrestaurant.fragments.ScheduleFragment
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter

class CalendarAdapter(
    private val daysOfMonth: ArrayList<String>, private val scheduleFragment: ScheduleFragment
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    private lateinit var dbHelper: DBHelper

    class CalendarViewHolder(itemView: View, private val onItemListener: ScheduleFragment) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val dayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)
        val cellDayIcon: ImageView = itemView.findViewById(R.id.cellDayIcon)

        init {
            itemView.setOnClickListener(this)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onClick(view: View) {
            onItemListener.onItemClick(bindingAdapterPosition, dayOfMonth.text as String)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.calendar_day_item, parent, false)
        val layoutParams = view.layoutParams
        dbHelper = DBHelper(parent.context)
        layoutParams.height = (parent.height * 0.166666666).toInt()
        return CalendarViewHolder(view, scheduleFragment)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.dayOfMonth.text = daysOfMonth[position]
        if (daysOfMonth[position] == ""){
            return
        }

        val day = daysOfMonth[position].toInt()
        val monthYear = scheduleFragment.getMonthYearText()

        // first make sure the day is valid for the month
        if (!dayIsValid(monthYear, day)) {
            return
        }

        // now create the actual day, month, and year
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
        val stringDate = "$day $monthYear"
        val dateToCheck = LocalDate.parse(stringDate, formatter) // THIS IS THE ACTUAL DATE TO CHECK

        if (dbHelper.isFullyScheduled(dateToCheck)) {  // If the day is scheduled, display a green checkmark
            holder.dayOfMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_fully_booked_24, 0)
        }
        else if (dbHelper.isPartiallyScheduled(dateToCheck)){
            holder.dayOfMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_partial_schedule_24, 0)
        }
        else{
            holder.cellDayIcon.setImageResource(0)
            holder.dayOfMonth.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dayIsValid(monthYear : String, day : Int ): Boolean {
        if (monthYear.contains("Jan") || monthYear.contains("Mar") || monthYear.contains("May")
            || monthYear.contains("Jul") || monthYear.contains("Aug") || monthYear.contains("Oct")
            || monthYear.contains("Dec")){
            if (day > 31){
                return false
            }
        }
        else if (monthYear.contains("Apr") || monthYear.contains("Jun") ||
            monthYear.contains("Sep") || monthYear.contains("Nov")){
            if (day > 30){
                return false
            }
        }
        else if (monthYear.contains("Feb")){
            val year = monthYear.split(" ")[1].toInt()
            val isLeapYear = Year.isLeap(year.toLong())
            if (isLeapYear && day > 29){
                return false
            }
            else if (!isLeapYear && day > 28){
                return false
            }
        }
        return true
    }

    override fun getItemCount(): Int = daysOfMonth.size

}