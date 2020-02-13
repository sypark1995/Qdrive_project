package com.giosis.util.qdrive.qdelivery

class CountryResult {

    var resultCode: Int = -1
    lateinit var resultMsg: String
    lateinit var resultObject: ArrayList<Country>

    class Country(var _country: String, var _countryCode: String)
}