package com.example.hopperrestaurant.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hopperrestaurant.DBHelper
import com.example.hopperrestaurant.EmpAvailability
import com.example.hopperrestaurant.Employee
import com.example.hopperrestaurant.R
import com.example.hopperrestaurant.fragments.EmployeeFragment
import com.example.hopperrestaurant.fragments.ScheduleFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    private lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DBHelper(this)

        supportActionBar?.title = "Hopper's Restaurant"
        if (dbHelper.getAllEmployees().size == 0) {
            addDummyData()
        }

        loadFragment(ScheduleFragment())
        // Bottom navigation bar(schedule and employee)
        bottomNav = findViewById(R.id.bottomNavBar)

        loadFragment(ScheduleFragment())
        // Listen to click of user and take you to calendar or employee fragment
        bottomNav.setOnItemSelectedListener { iconClicked ->
            when (iconClicked.itemId) {
                R.id.schedulesItemBtn -> {
                    loadFragment(ScheduleFragment())
                    true
                }
                R.id.employeeItemBtn -> {
                    loadFragment(EmployeeFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    /*Replaces the current fragment with the fragment that is passed:
      ie. if we are in schedule fragment and the employee fragment is passed,
      it will take us to employee fragment*/
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    // For testing, delete later
    private fun addDummyData() {
        val emp1 = dbHelper.addEmployee(
            Employee(
                -1L, "John", "Smith", "Johnny", "johnsmith@bobby.com",
                "(202) 698-4178", canOpen = true, canClose = false, status = true
            )
        )
        val emp2 = dbHelper.addEmployee(
            Employee(
                -1L, "Jane", "Doe", "Janet",
                "jannythegoat@people.com", "(400) 123-9011", canOpen = false,
                canClose = true, status = true
            )
        )
        val emp3 = dbHelper.addEmployee(
            Employee(
                -1L, "Benjamin", "Tennyson", "Benjamin",
                "ben10@earth.com", "(900) 689-6969", canOpen = false,
                canClose = true, status = true
            )
        )
        val emp4 = dbHelper.addEmployee(
            Employee(
                -1L, "Kevin", "Levin", "Kevin", "kevinlevin@dogs.com",
                "(100) 453-9021", canOpen = false, canClose = true, status = false
            )
        )
        val emp5 = dbHelper.addEmployee(
            Employee(
                -1L, "Toph", "Beifong", "Blindie",
                "beifongthethird@dogs.com", "(100) 823-9021", canOpen = false,
                canClose = true, status = true
            )
        )
        val emp6 = dbHelper.addEmployee(
            Employee(
                -1L, "Dora", "Explorer", "Explorer",
                "doraexplora@mars.com", "(346) 821-8320", canOpen = false,
                canClose = true, status = true
            )
        )
        val emp7 = dbHelper.addEmployee(
            Employee(
                -1L, "Danny", "Phantom", "Danny",
                "dannyphantom@ghosts.com", "(472) 730-7321", canOpen = false,
                canClose = true, status = true
            )
        )
        val emp8 = dbHelper.addEmployee(
            Employee(
                -1L, "Michael", "Myers", "Slasher",
                "slasher@myers.com", "(202) 902-6598", canOpen = true,
                canClose = true, status = true
            )
        )
        addFakeAvailability(emp1)
        addFakeAvailability(emp2)
        addFakeAvailability(emp3)
        addFakeAvailability(emp4)
        addFakeAvailability(emp5)
        addFakeAvailability(emp6)
        addFakeAvailability(emp7)
        addFakeAvailability(emp8)
    }

    private fun addFakeAvailability(empId: Long) {
        val availability = EmpAvailability(
            -1L,
            empId,
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean(),
            randomBoolean()
        )
        dbHelper.addAvailability(availability)
    }

    private fun randomBoolean(): Boolean {
        return (0..1).random() == 1
    }
}

