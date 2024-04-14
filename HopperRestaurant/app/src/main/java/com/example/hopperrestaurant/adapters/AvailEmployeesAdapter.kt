package com.example.hopperrestaurant.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.Employee
import com.example.hopperrestaurant.R

/**
 * This adapter handles showing the list of available employees in a pop up
 *
 * We need the dayAndTime parameter so we can check if the selected employee
 * can work a full day shift. It will be updated anytime the Add button is clicked
 * */
class AvailEmployeesAdapter(
    private val employees: ArrayList<Employee> = ArrayList(),
    var dayAndTime: String = "",
    val selectedEmployees: ArrayList<Employee> = ArrayList(),
    var isBusyDay: Boolean,
    val empInRecyclerView: ArrayList<Employee> = ArrayList()
) :
    RecyclerView.Adapter<AvailEmployeesAdapter.EmployeeViewHolder>() {
    private lateinit var dbHelper: DBHelper

    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var empInfo: CheckedTextView = itemView.findViewById(R.id.empInfo)
    }

    fun addEmployee(employee: Employee) {
        employees.add(employee)
        notifyItemInserted(employees.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        dbHelper = DBHelper(parent.context)
        return EmployeeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.available_employee_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val curEmployee = employees[position]
        // E.g Johnny S. (Opener)
        var empInfo = "${position + 1}. ${curEmployee.getCustomName()} (${curEmployee.getRoles()})"

        holder.itemView.apply {
            holder.empInfo.text = empInfo
            var openerFound = false
            var closerFound = false
            // on info click, set the background to checked_emp
            // if it's already checked, uncheck it
            var maxEmployee = 0
            if (isBusyDay) {
                maxEmployee = 3
            }
            else {
                maxEmployee = 2
            }
            holder.empInfo.setOnClickListener {
                if (holder.empInfo.isChecked) {
                    selectedEmployees.remove(curEmployee)
                    holder.empInfo.isChecked = false
                    holder.empInfo.background = null
                } else {
                    // Check to make sure last employee selected makes valid squad
                    if (dayAndTime.lowercase().contains("am")){
                        if ((selectedEmployees.size + empInRecyclerView.size) >= maxEmployee - 1) {
                            for (employee in selectedEmployees) {
                                if (employee.canOpen) openerFound = true
                            }
                            for (employee in empInRecyclerView) {
                                if (employee.canOpen) openerFound = true
                            }
                            if (curEmployee.canOpen) openerFound = true
                            if (!openerFound) {
                                showDialog(
                                    "You must have at least one opener in a morning shift",
                                    context
                                )
                                return@setOnClickListener
                            }
                        }
                    }
                    if (dayAndTime.lowercase().contains("pm")){
                        if ((selectedEmployees.size + empInRecyclerView.size) >= maxEmployee - 1) {
                            for (employee in selectedEmployees) {
                                if (employee.canClose) closerFound = true
                            }
                            for (employee in empInRecyclerView) {
                                if (employee.canClose) closerFound = true
                            }
                            if (curEmployee.canClose) closerFound = true
                            if (!closerFound) {
                                showDialog(
                                    "You must have at least one closer in an afternoon shift",
                                    context
                                )
                                return@setOnClickListener
                            }
                        }
                    }
                    if (dayAndTime == "Sat" || dayAndTime == "Sun") {
                        if ((selectedEmployees.size + empInRecyclerView.size) >= maxEmployee - 1) {
                            for (employee in selectedEmployees) {
                                if (employee.canClose) closerFound = true
                                if (employee.canOpen) openerFound = true
                            }
                            for (employee in empInRecyclerView) {
                                if (employee.canClose) closerFound = true
                                if (employee.canOpen) openerFound = true
                            }
                            if (curEmployee.canClose) closerFound = true
                            if (curEmployee.canOpen) openerFound = true
                            if (!closerFound || !openerFound) {
                                showDialog(
                                    "You must have at least one opener and closer in a weekend shift",
                                    context
                                )
                                return@setOnClickListener
                            }
                        }
                    }
                    if (!isBusyDay) {
                        val howManyLeft = maxEmployee - (empInRecyclerView.size + selectedEmployees.size)
                        // day is not busy and 2 employees are already in the recycler view
                        if (empInRecyclerView.size >= maxEmployee) {
                            showDialog("You have already scheduled $maxEmployee employees", context)
                            return@setOnClickListener
                        }
                        // day is not busy and user selected 2 employees
                        if (selectedEmployees.size >= maxEmployee|| howManyLeft == 0) {
                            showDialog(
                                "You can only select/schedule $maxEmployee employees.\nTo add one more, mark the day as busy",
                                context
                            )
                            return@setOnClickListener
                        }
                    } else {
                        val howManyLeft = (maxEmployee) - (empInRecyclerView.size + selectedEmployees.size)
                        // day is busy and 3 employees are already in the recycler view
                        if (empInRecyclerView.size >= (maxEmployee)) {
                            showDialog("You have already scheduled ${maxEmployee + 1} employees", context)
                            return@setOnClickListener
                        }
                        // day is busy, and user has selected 3 employees
                        if (selectedEmployees.size >= (maxEmployee) || howManyLeft == 0) {
                            showDialog("You can only select/schedule ${maxEmployee + 1} employees.", context)
                            return@setOnClickListener
                        }
                    }
                    // everything good
                    selectedEmployees.add(curEmployee)
                    holder.empInfo.isChecked = true
                    holder.empInfo.setBackgroundResource(R.drawable.checked_emp)
                }
            }
        }
    }

    fun showDialog(message: String, context: Context) {
        val builder = AlertDialog.Builder(context)
            .setTitle("Information")
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setNeutralButton("Got it") { _, _ -> }
        builder.show()
    }

    override fun getItemCount(): Int {
        return employees.size
    }
}