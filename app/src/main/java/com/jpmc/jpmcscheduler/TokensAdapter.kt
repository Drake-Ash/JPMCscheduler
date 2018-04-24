package com.jpmc.jpmcscheduler

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import kotlinx.android.synthetic.main.token_element.view.*

/**
 * Created by ashwin on 4/24/2018.
 */

class TokensAdapter(val list: MutableList<Token>): RecyclerView.Adapter<TokenViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val cellForRow = layoutInflater.inflate(R.layout.token_element, parent, false)
        return TokenViewHolder(cellForRow)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val row = list.get(position)
        holder.view.token_number.text = holder.view.token_number.text.toString() + row.num.toString()
        holder.view.bankActivity.text = row.ba
        holder.view.timeslot.text = holder.view.timeslot.text.toString() + row.timeslot
        if(row.status) {
            holder.view.status.text = holder.view.status.text.toString() + "Active"
            holder.view.ticketView1.visibility = View.VISIBLE
            holder.view.ticketView2.visibility = View.GONE
        }
        else {
            holder.view.status.text = holder.view.status.text.toString() + "Expired"
            holder.view.ticketView1.visibility = View.GONE
            holder.view.ticketView2.visibility = View.VISIBLE
        }
        holder.view.isEnabled = row.status
    }

}

class TokenViewHolder(var view: View): RecyclerView.ViewHolder(view) {

}