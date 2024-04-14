package com.example.hopperrestaurant.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hopperrestaurant.*
import com.example.hopperrestaurant.activities.EditEmployeeActivity

class EmployeeAdapter(private val employees: ArrayList<Employee>, private val viewcontext: Context) :
    RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {
    private lateinit var dbHelper: DBHelper


    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fullName: TextView = itemView.findViewById(R.id.empFullName)
        var empRoles: TextView = itemView.findViewById(R.id.empRoles)
        var editIcon: ImageView = itemView.findViewById(R.id.editIcon)
    }

    fun addEmployee(employee: Employee) {
        employees.add(employee)
        notifyItemInserted(employees.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        return EmployeeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.employee_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        dbHelper = DBHelper(viewcontext)
        val curEmployee = employees[position]
        val fullName = curEmployee.getCustomName()

        val canOpen = if (curEmployee.canOpen) "Opener" else ""
        val canClose = if (curEmployee.canClose) "Closer" else ""
        val roles = arrayOf(canOpen, canClose)
            .filter { it != "" }
            .joinToString(", ")
        val empAvails = getStringFromAvails(dbHelper.findAvailabilityById(curEmployee.empId))

        val result = holder.itemView.context
        holder.editIcon.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditEmployeeActivity::class.java)
            intent.putExtra("curEmpId", curEmployee.empId)
            result.startActivity(intent)
        }


        val empContainer = holder.itemView.context
        holder.itemView.setOnClickListener {
            val builder = AlertDialog.Builder(empContainer)

            //dialog title
            builder.setTitle("        EMPLOYEE INFORMATION")

            //dialog message
            builder.setMessage(
                """
                NAME: ${curEmployee.firstName.replaceFirstChar { it.uppercase() }} ${curEmployee.lastName.replaceFirstChar { it.uppercase() }}
                
                EMAIL: ${curEmployee.email}
                
                PHONE: ${curEmployee.phoneNum}
                
                ROLES: $roles
                
                Availabilities:
                
                $empAvails
            """.trimIndent()
            )

            builder.setPositiveButton("OK") { _, _ ->
            }

            //start alert dialog
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        holder.itemView.apply {
            holder.fullName.text = fullName
            holder.empRoles.text = curEmployee.getRoles()
        }
    }

    override fun getItemCount(): Int {
        return employees.size
    }

    fun clear() {
        val size = employees.size
        employees.clear()
        notifyItemRangeRemoved(0, size)
    }

    private fun getStringFromAvails(empAvails: EmpAvailability?): String {
        var monAvails = ""
        var tueAvails = ""
        var wedAvails = ""
        var thuAvails = ""
        var friAvails = ""
        var satAvails = ""
        var sunAvails = ""

        if (empAvails?.monAM == true || empAvails?.monPM == true) monAvails += "Mon: "
        if (empAvails?.monAM == true) monAvails += "AM"
        if (empAvails?.monPM == true) {
            monAvails += if (empAvails?.monAM == true) ", PM"
            else "PM"
        }
        if (empAvails?.monAM == true || empAvails?.monPM == true) monAvails += "\n                "

        if (empAvails?.tueAM == true || empAvails?.tuePM == true) tueAvails += "Tue: "
        if (empAvails?.tueAM == true) tueAvails += "AM"
        if (empAvails?.tuePM == true) {
            tueAvails += if (empAvails?.tueAM == true) ", PM"
            else "PM"
        }
        if (empAvails?.tueAM == true || empAvails?.tuePM == true) tueAvails += "\n                "

        if (empAvails?.wedAM == true || empAvails?.wedPM == true) wedAvails += "Wed: "
        if (empAvails?.wedAM == true) wedAvails += "AM"
        if (empAvails?.wedPM == true) {
            wedAvails += if (empAvails?.wedAM == true) ", PM"
            else "PM"
        }
        if (empAvails?.wedAM == true || empAvails?.wedPM == true) wedAvails += "\n                "

        if (empAvails?.thuAM == true || empAvails?.thuPM == true) thuAvails += "Thu: "
        if (empAvails?.thuAM == true) thuAvails += "AM"
        if (empAvails?.thuPM == true) {
            thuAvails += if (empAvails?.thuAM == true) ", PM"
            else "PM"
        }
        if (empAvails?.thuAM == true || empAvails?.thuPM == true) thuAvails += "\n                "

        if (empAvails?.friAM == true || empAvails?.friPM == true) friAvails += "Fri: "
        if (empAvails?.friAM == true) friAvails += "AM"
        if (empAvails?.friPM == true) {
            friAvails += if (empAvails?.friAM == true) ", PM"
            else "PM"
        }
        if (empAvails?.friAM == true || empAvails?.friPM == true) friAvails += "\n                "

        if (empAvails?.sat == true) satAvails += "Sat: Yes\n                "

        if (empAvails?.sun == true) sunAvails += "Sun: Yes"

        return monAvails + tueAvails + wedAvails + thuAvails + friAvails + satAvails + sunAvails
    }
}
