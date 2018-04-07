package com.jpmc.jpmcscheduler

/**
 * Created by ashwin on 4/4/2018.
 */
class Token(
        var timeslot: String,
        var token: String
){
    constructor():this("",""){}
}