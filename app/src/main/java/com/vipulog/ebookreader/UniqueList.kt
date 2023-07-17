package com.vipulog.ebookreader

class UniqueList<T> : ArrayList<T>() {
    override fun add(element: T): Boolean {
        val index = indexOfFirst { it == element }
        if (index != -1) {
            set(index, element)
            return true
        }
        return super.add(element)
    }

    override fun add(index: Int, element: T) {
        if (get(index) == element) {
            set(index, element)
        } else {
            super.add(index, element)
        }
    }
}
