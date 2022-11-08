@file:Suppress("DEPRECATION")

package rf.vacation35.extension

import android.app.Fragment
import android.app.FragmentManager

val FragmentManager.topFragment: Fragment?
    get() = findFragmentByTag((backStackEntryCount - 1).toString())

fun FragmentManager.showFragment(id: Int) {
    findFragmentById(id)?.let {
        beginTransaction()
            .show(it)
            .commitAllowingStateLoss()
        executePendingTransactions()
    }
}

fun FragmentManager.hideFragment(id: Int) {
    findFragmentById(id)?.let {
        beginTransaction()
            .hide(it)
            .commitAllowingStateLoss()
        executePendingTransactions()
    }
}

fun FragmentManager.addFragment(id: Int, fragment: Fragment) {
    beginTransaction()
        .add(id, fragment, backStackEntryCount.toString())
        .addToBackStack(fragment.javaClass.name)
        .commitAllowingStateLoss()
    executePendingTransactions()
}

fun FragmentManager.putFragment(id: Int, fragment: Fragment) {
    beginTransaction()
        .replace(id, fragment, backStackEntryCount.toString())
        .addToBackStack(fragment.javaClass.name)
        .commitAllowingStateLoss()
    executePendingTransactions()
}

fun FragmentManager.popFragment(name: String?, immediate: Boolean): Boolean {
    return if (name != null) {
        if (immediate) {
            popBackStackImmediate(name, 0)
        } else {
            popBackStack(name, 0)
            true
        }
    } else {
        if (immediate) {
            popBackStackImmediate()
        } else {
            popBackStack()
            true
        }
    }
}
