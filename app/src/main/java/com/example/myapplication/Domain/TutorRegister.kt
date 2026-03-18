package com.example.myapplication.Domain

import com.google.firebase.database.PropertyName

data class TutorRegister(
    @get:PropertyName("Id") @set:PropertyName("Id")
    var Id: String = "",
    @get:PropertyName("Address") @set:PropertyName("Address")
    var Address: String = "",
    @get:PropertyName("Biography") @set:PropertyName("Biography")
    var Biography: String = "",
    @get:PropertyName("Email") @set:PropertyName("Email")
    var Email: String = "",
    @get:PropertyName("Experience") @set:PropertyName("Experience")
    var Experience: Int = 0,
    @get:PropertyName("Location") @set:PropertyName("Location")
    var Location: String = "",
    @get:PropertyName("Mobile") @set:PropertyName("Mobile")
    var Mobile: String = "",
    @get:PropertyName("Name") @set:PropertyName("Name")
    var Name: String = "",
    @get:PropertyName("Password") @set:PropertyName("Password")
    var Password: String = "",
    @get:PropertyName("Picture") @set:PropertyName("Picture")
    var Picture: String = "",
    @get:PropertyName("Site") @set:PropertyName("Site")
    var Site: String = "",
    @get:PropertyName("Special") @set:PropertyName("Special")
    var Special: String = "",
    @get:PropertyName("Username") @set:PropertyName("Username")
    var Username: String = "",
    @get:PropertyName("UserType") @set:PropertyName("UserType")
    var UserType: String = "Tutor", //Add userType to distinguish tutors
    var Rating: Double = 0.0,
)
