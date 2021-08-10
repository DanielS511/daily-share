package com.example.dailyshare.models

import com.google.firebase.firestore.PropertyName

data class User (@get:PropertyName("user_name") @set:PropertyName("user_name") var username : String = "",
                 var age : Int = 0)