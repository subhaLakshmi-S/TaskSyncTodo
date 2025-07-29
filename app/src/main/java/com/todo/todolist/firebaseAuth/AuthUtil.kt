package com.todo.todolist.firebaseAuth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

class AuthUtil @Inject constructor(){

    fun getCurrentUserId(): String = FirebaseAuth.getInstance().currentUser?.uid ?: "default"

    fun isLoggedIn(): Boolean = FirebaseAuth.getInstance().currentUser != null

    fun signOut() = FirebaseAuth.getInstance().signOut()

    fun signInWithEmail(email: String, password: String, onComplete: (Boolean, String?) -> Unit){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(it.isSuccessful) onComplete(true, null)
                else onComplete(false, it.exception?.message)
        }
    }
    fun registerUser(email: String, password: String, username: String, onComplete: (Boolean, Exception?) -> Unit){
        val auth= FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(it.isSuccessful) {
                    val userId = auth.currentUser?.uid?: return@addOnCompleteListener
                    val userMap = hashMapOf("username" to username)
                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener{onComplete(false, it) }
                }
                else onComplete(false, it.exception)
            }
    }

}