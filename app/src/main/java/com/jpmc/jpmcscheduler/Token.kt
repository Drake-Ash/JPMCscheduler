package com.jpmc.jpmcscheduler

import java.util.*

/**
 * Created by ashwin on 4/4/2018.
 */
class Token(
        var date: String,
        var timeslot: String,
        var token: String,
        var ba: String,
        var number: Long
){
    constructor():this("","","", "", 0L){}
}