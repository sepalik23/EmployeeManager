package com.example.hopperrestaurant.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.Employee
import com.example.hopperrestaurant.R

class AmShiftAdapter(
    private val employees: ArrayList<Employee>,
    private val dayAndTime: String
) :
    RecyclerView.Adapter<AmShiftAdapter.EmployeeViewHolder>() {
    private lateinit var dbHelper: DBHelper

    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var empInfo: TextView = itemView.findViewById(R.id.empInfo)
        var trashCan: ImageView = itemView.findViewById(R.id.trashCan)
    }

    fun addEmployee(employee: Employee) {
        employees.add(employee)
        notifyItemInserted(employees.size - 1)
    }

    private fun removeEmployee(position: Int) {
        employees.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, employees.size)
    }

    fun getEmployees(): ArrayList<Employee> {
        return employees
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        dbHelper = DBHelper(parent.context)
        return EmployeeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.employee_shift_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val curEmployee = employees[position]
        var empInfo = "${position + 1}. ${curEmployee.getCustomName()} (${curEmployee.getRoles()})"
        val canWorkFullShift = dbHelper.canWorkFullShift(curEmployee.empId, dayAndTime)
        if (canWorkFullShift) {
            empInfo += " - All day"
        }
        holder.itemView.apply {
            holder.empInfo.text = empInfo
        }

        holder.trashCan.setOnClickListener {
            val customName = curEmployee.getCustomName()
            val context = holder.itemView.context

            // check if deleting the curEmployee will make the schedule invalid.
            // A morning schedule is NOT valid if there is NO opener
            var foundOpener = false
            for (employee in employees) {
                // != to skip the current employee that is about to be deleted
                if (employee.empId != curEmployee.empId){
                    if (employee.canOpen){
                        foundOpener = true
                        break
                    }
                }
            }

            getDialog("Confirmation", "Remove $customName from the schedule?", context)
                .setPositiveButton("Yes") { _, _ ->
                    removeEmployee(position)
                    Toast.makeText(context, "Removed $customName", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
            return@setOnClickListener
        }
    }

    private fun getDialog(title: String, message: String, context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setIconAttribute(android.R.attr.alertDialogIcon)
    }

    override fun getItemCount(): Int {
        return employees.size
    }
}