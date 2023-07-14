package com.vipulog.ebookreader

class UniqueList<T> : ArrayList<T>() {
    override fun add(element: T): Boolean {
        if (!contains(element)) {
            return super.add(element)
        }
        return false
    }

    override fun add(index: Int, element: T) {
        if (!contains(element)) {
            super.add(index, element)
        }
    }
}
