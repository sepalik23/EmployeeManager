package com.example.hopperrestaurant.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hopperrestaurant.activities.AddEmployeeActivity
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.adapters.EmployeeAdapter
import com.example.hopperrestaurant.R

class EmployeeFragment : Fragment() {
    private lateinit var employeeAdapter: EmployeeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_employee, container, false)
        // All initializations should go here
        dbHelper = DBHelper(view.context)
        recyclerView = view.findViewById(R.id.employeeRecyclerView)
        employeeAdapter = EmployeeAdapter(ArrayList(), view.context)
        recyclerView.adapter = employeeAdapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        dbHelper.getAllEmployees().forEach { employee ->
            employeeAdapter.addEmployee(employee)
        }
        val goToEditEmployee = view.findViewById<Button>(R.id.buttonToAddPage)
        goToEditEmployee.setOnClickListener {
            requireActivity().run {
                startActivity(Intent(this, AddEmployeeActivity::class.java))
            }
        }
        return view
    }

    /**
     * This method is executed whenever the fragment comes back onto the main screen
     * */
    override fun onResume() {
        employeeAdapter.clear()
        dbHelper.getAllEmployees().forEach { employee ->
            employeeAdapter.addEmployee(employee)
        }
        super.onResume()
    }
}