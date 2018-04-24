package com.jpmc.jpmcscheduler

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_token_generator.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import kotlinx.android.synthetic.main.activity_main.*


class TokenGenerator : AppCompatActivity() {
    var select_slot = 0

    lateinit var con: Context
    lateinit var BankActivity: String
    lateinit var timeSlots: MutableList<String>
    var arriveTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token_generator)

        BankActivity = intent.getStringExtra("BankActivity")
        arriveTime = intent.getIntExtra("arrivalTime", 0)

        var databaseref = FirebaseDatabase.getInstance().getReference()

        val cal = Calendar.getInstance()
        cal.time = Date()
        val df = SimpleDateFormat("MMMM d, yyyy")
        var dat = df.format(cal.time)

        databaseref = databaseref.child(dat).child(BankActivity)

        timeSlots = mutableListOf()

        con = this

        timeslot.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                arrayListOf<String>("Wait")) as SpinnerAdapter

        databaseref.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    timeSlots.clear()
                    timeSlots.add("Select the Time Slot")
                    for(p in p0.children){
                        var token = p.getValue(String::class.java)
                        if(token.equals("0")){
                            var temp = p.key
                            var time = Date()
                            time.hours = temp?.substring(0,2)!!.toInt()
                            time.minutes = temp?.substring(3,5)!!.toInt()
                            time.seconds = 0
                            val cal = Calendar.getInstance()
                            cal.time = Date()
                            cal.add(Calendar.MINUTE, arriveTime)

                            if(time.compareTo(cal.time)>0){
                                timeSlots.add(temp)
                            }
                        }
                    }
                    timeslot.adapter = ArrayAdapter<String>(con, android.R.layout.simple_list_item_1,
                            ArrayList(timeSlots))
                }
            }

        })

        timeslot.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                select_slot = 0
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                select_slot = position
            }

        }



        gen_token.setOnClickListener {
            var count = 0
            if(select_slot == 0){
                count++
                var temp = AlertDialog.Builder(this)
                temp.setTitle("Enter valid time")
                temp.setPositiveButton("Okay", { dialogInterface: DialogInterface, i: Int -> })
                temp.show()
            }

            if(count==0){
                transactionOp()
            }
        }

    }

    private fun transactionOp() {
        var databaseref = FirebaseDatabase.getInstance().getReference()

        val cal = Calendar.getInstance()
        cal.time = Date()
        val df = SimpleDateFormat("MMMM d, yyyy")
        var dat = df.format(cal.time)

        databaseref = databaseref.child(dat).child(BankActivity)//.child(timeSlots[select_slot])

        var key = databaseref.child(timeSlots[select_slot]).push().key

        var c1 = false
        var c2 = false

        databaseref.child(timeSlots[select_slot]).setValue(key).addOnSuccessListener {
            c1 = true
        }


        var User = FirebaseAuth.getInstance().currentUser
        var utot = FirebaseDatabase.getInstance().getReference()
        var count: Long = 0

        utot.child(dat).child("Tokens").child("count").addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    count = p0.value as Long
                }
            }

        })

        count++

        utot.child(dat).child("Tokens").child("count").setValue(count).addOnSuccessListener {
            c2= true
        }

        utot.child("Users").child(User?.email).child("Tokens").setValue(
                Token(
                        date = dat,
                        timeslot = timeSlots[select_slot],
                        token = key,
                        ba = BankActivity,
                        number = count
                )
        ).addOnSuccessListener {
            if (c1 and c2) {
                var temp = AlertDialog.Builder(con)
                temp.setTitle("Slot Booked!")
                temp.setPositiveButton("Okay", { dialogInterface: DialogInterface, i: Int ->
                    view_token_action.callOnClick()
                })
                temp.show()
            }
        }

    }
}
