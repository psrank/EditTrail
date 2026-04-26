package com.psrank.edittrail

import com.intellij.util.messages.Topic

/** Listener notified whenever the EditTrail history changes. */
fun interface EditTrailListener {
    fun historyUpdated()
}

/** Message bus topics for EditTrail. */
object EditTrailTopics {
    val HISTORY_UPDATED: Topic<EditTrailListener> =
        Topic("EditTrail History Updated", EditTrailListener::class.java)
}
