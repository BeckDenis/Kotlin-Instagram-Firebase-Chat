package com.example.kotlininstagramfirebasechat.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Check if no view has focus
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun View.showView() {
    visibility = View.VISIBLE
    animate().alpha(1.0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
            Log.d("Utils", "start animate(1)")
        }

        override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
            super.onAnimationEnd(animation, isReverse)
            Log.d("Utils", "end animate(1)")
        }
    })
}

fun View.hideView() {
    animate().alpha(0.0f).setListener(object : AnimatorListenerAdapter() {

        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
            Log.d("Utils", "start animate(2)")
        }

        override fun onAnimationEnd(animation: Animator?) {
            visibility = View.GONE
            Log.d("Utils", "end animate(2)")
        }

    })
}

fun showToast(context: Context?, message: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun coordinateBtnAndInputs(btn: Button, vararg inputs: EditText) {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled = inputs.all { it.text.isNotEmpty() }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.isNotEmpty() }
}

fun coordinateImgBtnAndInputs(btn: ImageButton, vararg inputs: EditText) {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled = inputs.all { it.text.isNotEmpty() }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.isNotEmpty() }
}