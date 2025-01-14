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

fun FragmentManager.addFragment(id: Int, fragment: Fragment, backStack: Boolean = true) {
    try {
        beginTransaction()
            .add(id, fragment, backStackEntryCount.toString())
            .apply {
                if (backStack) {
                    addToBackStack(fragment.javaClass.name)
                }
            }
            .commitAllowingStateLoss()
        executePendingTransactions()
    } catch (ignored: Throwable) {
    }
}

fun FragmentManager.replaceFragment(id: Int, fragment: Fragment, backStack: Boolean = true) {
    beginTransaction()
        .replace(id, fragment, backStackEntryCount.toString())
        .apply {
            if (backStack) {
                addToBackStack(fragment.javaClass.name)
            }
        }
        .commitAllowingStateLoss()
    executePendingTransactions()
}

fun FragmentManager.removeFragment(fragment: Fragment) {
    try {
        beginTransaction()
            .remove(fragment)
            .commitAllowingStateLoss()
        executePendingTransactions()
    } catch (ignored: Throwable) {
    }
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
