package com.example.hopperrestaurant.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.Employee
import com.example.hopperrestaurant.R
import com.example.hopperrestaurant.Schedule
import com.example.hopperrestaurant.adapters.AvailEmployeesAdapter
import com.example.hopperrestaurant.adapters.WeekendShiftAdapter
import com.example.hopperrestaurant.databinding.ActivityScheduleWeekendBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleWeekendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleWeekendBinding
    private lateinit var chosenDate: LocalDate
    private lateinit var dbHelper: DBHelper

    // Weekend
    private lateinit var weekendShiftAdapter: WeekendShiftAdapter
    private lateinit var weekendShiftRecyclerView: RecyclerView

    // all available employees
    private lateinit var availEmployeesAdapter: AvailEmployeesAdapter
    private lateinit var availEmployeesRecyclerView: RecyclerView

    private lateinit var dialog: Dialog

    // old values, used to know when to enable the save changes button
    private var oldEmployees  = ArrayList<Employee>()
    private var oldBusyDay : Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleWeekendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DBHelper(this)
        dialog = Dialog(this)

        // converting the date string to LocalDate so it's easier to work with
        chosenDate = LocalDate.parse(
            intent.getStringExtra("date"),
            DateTimeFormatter.ofPattern("d MMMM yyyy")
        )

        // set title of action bar
        supportActionBar?.title =
            chosenDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
        // show back button. On back button click, onOptionsItemSelected() will be called
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initDialog()
        initRecyclerViews() // needs dialog to be initialized first
        binding.busyDayCheckBox.isChecked = dbHelper.isBusyDay(chosenDate)

        binding.saveChangesBtn.isEnabled = false // will be enabled anytime old values != current values
        // set old values
        oldEmployees.addAll(weekendShiftAdapter.getEmployees())
        oldBusyDay = dbHelper.isBusyDay(chosenDate)

        // if the day is busy, and there are at least 3 employees in the recycler view, disable btn
        if (binding.busyDayCheckBox.isChecked && weekendShiftAdapter.itemCount >= 3){
                binding.addFullShiftButton.isEnabled = false
        }
        else if (!binding.busyDayCheckBox.isChecked && weekendShiftAdapter.itemCount >= 2){
            binding.addFullShiftButton.isEnabled = false
        }
        // On click listener for Add FullShift button
        binding.addFullShiftButton.setOnClickListener {
            initAvailRecyclerView()
            dialog.findViewById<TextView>(R.id.textView).text = "Full Day Availabilities"
            dialog.show()
        }

        // disable the add button when the recycler view has more than 2 or 3 items

        weekendShiftAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { // for when item is added
                super.onItemRangeInserted(positionStart, itemCount)
                val busyDay = binding.busyDayCheckBox.isChecked
                if (busyDay){
                    // enable when item count is less than 3. If >=, disable
                    binding.addFullShiftButton.isEnabled = weekendShiftAdapter.itemCount < 3
                }
                else {
                    binding.addFullShiftButton.isEnabled = weekendShiftAdapter.itemCount < 2
                }
                enableSaveBtnIfChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {// for when item is removed
                super.onItemRangeRemoved(positionStart, itemCount)
                val busyDay = binding.busyDayCheckBox.isChecked
                if (busyDay){
                    binding.addFullShiftButton.isEnabled = weekendShiftAdapter.itemCount < 3
                }
                else{
                    binding.addFullShiftButton.isEnabled = weekendShiftAdapter.itemCount < 2
                }
                enableSaveBtnIfChanged()
            }
        })

        var saved = false

        // On click listener for Save changes button
        binding.saveChangesBtn.setOnClickListener {
            val date = chosenDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            // first, delete all shifts for the chosen date, then add whatever is in the recycler views
            // this makes updating easier because we don't have to check if the employee is already
            // scheduled
            dbHelper.deleteAllShifts(date)

            // for each employee in the recycler view adapter, create a schedule for them
            // and add it to the database
            weekendShiftAdapter.getEmployees().forEach {
                val schedule = Schedule(-1L, date, "Full", it.empId, binding.busyDayCheckBox.isChecked)
                val check = dbHelper.checkDupShift(chosenDate, "Full", it.empId)
                if (check == true) {
                    dbHelper.addShift(schedule)
                    saved = true
                }
            }
            if (saved){
                Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // On click listener for Busy day checkbox
        binding.busyDayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val numWeekendEmp = weekendShiftAdapter.itemCount
            // the user is trying to un-mark the day as busy. There are 3 employees scheduled.
            // The user needs to remove at least one employee before un-marking the day as busy
            if (!isChecked && numWeekendEmp >= 3) {
                showDialog("You need to remove at least one employee before un-marking the day as busy.")
                binding.busyDayCheckBox.isChecked = true
            }
            enableSaveBtnIfChanged()
            val busyDay = binding.busyDayCheckBox.isChecked
            var openerFound = false
            var closerFound = false
            for (employee in weekendShiftAdapter.getEmployees()){
                if (employee.canOpen) openerFound = true
            }
            for (employee in weekendShiftAdapter.getEmployees()){
                if (employee.canClose) closerFound = true
            }
            if (!isChecked && (!closerFound || !openerFound) && numWeekendEmp >= 2){
                showDialog("Switching busy day toggle will create invalid squad.")
                binding.busyDayCheckBox.isChecked = true
                return@setOnCheckedChangeListener
            }
            if (busyDay && numWeekendEmp < 3)
                binding.addFullShiftButton.isEnabled = true
            if (!busyDay && numWeekendEmp >= 2){
                binding.addFullShiftButton.isEnabled = false
            }
        }
    }

    // Call this method anytime the recycler view changed, or the busy day checkbox is checked
    private fun enableSaveBtnIfChanged() {
        // basically, if oldEmployees != newEmployees OR oldBusyDay != newBusyDay, enable the save button
        binding.saveChangesBtn.isEnabled = oldEmployees != weekendShiftAdapter.getEmployees() ||
                oldBusyDay != binding.busyDayCheckBox.isChecked
    }

    private fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
            .setTitle("Information")
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setNeutralButton("Got it") { _, _ -> }
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This method takes in either "Am" or "Pm" and returns the type of day it is using
     * the chosenDate.
     *
     * E.g MonAm, or Sat or TuePm
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getShiftDay(): String {
        // convert day of the week to string, extract first 3 letters e.g. MON, TUE, etc.
        // lowercase it, then uppercase the first letter
        val day = chosenDate.dayOfWeek.toString()
            .substring(0, 3).lowercase()
            .replaceFirstChar { it.uppercase() }

        if (day == "Sun" || day == "Sat") { // weekend, just return the day of the week since there's no AM OR PM
            return day
        }
        // else add the AM or PM to the day of the week to get something like MonAm, ThuPm, etc.
        return day
    }

    /**
     * Method to initialize the dialog containing the list of available employees
     * */
    private fun initDialog() {
        dialog.setContentView(R.layout.available_employees)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.window?.attributes?.windowAnimations = R.style.popupAnimation

        val confirmBtn = dialog.findViewById<Button>(R.id.confirmBtn)
        val cancelBtn = dialog.findViewById<Button>(R.id.cancelBtn)

        /*
        * When confirm button is clicked, get the selected employees.
        * For each of them/"it":
        *   If the dayAndTime of the availability that was shown contains "Am", add it to the A.M.
        *   recyclerview, else add it to the P.M. recyclerview
        * */
        confirmBtn.setOnClickListener {
            availEmployeesAdapter.selectedEmployees.forEach {
                if (availEmployeesAdapter.dayAndTime.contains("")) {
//                    removed check as it was unnecessary with new checks in AvailEmployeesAdapter
//                    if (!squadIsValid()) {
//                        availEmployeesAdapter.showDialog(
//                            "Your current selection is missing an employee that can open/close",
//                            this
//                        )
//                        return@setOnClickListener
//                    }
                    weekendShiftAdapter.addEmployee(it)
                }
            }
            Toast.makeText(this, "Confirmed", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    /*
    * calling it squadIsValid because I don't know what to call it
    *
    * A squad is valid if it fulfils the requirements in bullet point 4 of the project specs
    * */
    private fun squadIsValid(): Boolean {
        // check both the employee in the recycler view and the employee(s) that was/were selected
        val temp = weekendShiftAdapter.getEmployees() + availEmployeesAdapter.selectedEmployees
        val size = temp.size
        for (i in 0 until size) {
            val emp = temp[i]
            if (emp.canOpen && emp.canClose) {// found an employee that can open and close, so return true
                return true
            }
            if (emp.canOpen) { // found an employee that can open
                for (j in i + 1 until size) { // check the rest of the employees
                    if (temp[j].canClose) { // found an employee that can close, so return true
                        return true
                    }
                }
            }
            if (emp.canClose) { // found an employee that can close
                for (j in i + 1 until size) { // check the rest of the employees
                    if (temp[j].canOpen) { // found an employee that can open, so return true
                        return true
                    }
                }
            }
        }
        return false
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initRecyclerViews() {
        val scheduledW = dbHelper.getScheduledEmployees(chosenDate, "")
        // A.M.
        weekendShiftRecyclerView = binding.WeekendRecyclerView
        weekendShiftAdapter = WeekendShiftAdapter(scheduledW)
        weekendShiftRecyclerView.adapter = weekendShiftAdapter
        weekendShiftRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAvailRecyclerView() {
        availEmployeesRecyclerView = dialog.findViewById(R.id.availEmpRecyclerView)
        availEmployeesAdapter = AvailEmployeesAdapter(isBusyDay = binding.busyDayCheckBox.isChecked)
        availEmployeesRecyclerView.adapter = availEmployeesAdapter
        availEmployeesRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)

        val dayAndTime = getShiftDay()
        availEmployeesAdapter.dayAndTime = dayAndTime
        // this holds the employees that are available to work on the chosen day and time
        // Not to be confused with actualAvailEmployees below
        val availEmployees: MutableList<Employee>
        availEmployees = dbHelper.getAvailableEmployees(chosenDate, "")
        availEmployeesAdapter.empInRecyclerView.addAll(weekendShiftAdapter.getEmployees())


        // making sure the employees shown in the pop up are not in the recycler view
        // plan: get the available employees, and remove the employees that are in the recycler view

        // Explanation for the code below:
        //      from availableEmployees, start filtering. Recall: "it" is the current element in the for each
        //      if empInRecyclerView does not contain "it", add "it" to availableEmployees
        val actualAvailEmployees = availEmployees.filter {
            !availEmployeesAdapter.empInRecyclerView.contains(it)
        }
        // actualAvailEmployees should now contain the employees that are available to work, and not
        // already in the recycler view i.e. not already scheduled
        // Add each of them available employee to the adapter (pop-up)
        actualAvailEmployees.forEach {
            availEmployeesAdapter.addEmployee(it)
        }
    }
}