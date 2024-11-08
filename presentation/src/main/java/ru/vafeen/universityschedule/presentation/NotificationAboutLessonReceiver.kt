package ru.vafeen.universityschedule.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import ru.vafeen.universityschedule.data.database.DatabaseRepository
import ru.vafeen.universityschedule.data.database.ReminderType
import ru.vafeen.universityschedule.domain.notifications.NotificationService
import ru.vafeen.universityschedule.domain.planner.SchedulerExtra

class NotificationAboutLessonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationService: NotificationService by inject(
            clazz = NotificationService::class.java
        )
        val databaseRepository: DatabaseRepository by inject(
            clazz = DatabaseRepository::class.java
        )
        val idOfReminder = intent.getIntExtra(
            SchedulerExtra.ID_OF_REMINDER,
            -1
        )
        CoroutineScope(Dispatchers.IO).launch {
            val reminder = databaseRepository.getReminderByIdOfReminder(
                idOfReminder = idOfReminder
            )
            reminder?.let {
                when (reminder.type) {
                    ReminderType.BEFORE_LESSON -> {
                        notificationService.showNotification(
                            NotificationService.createNotificationAbout15MinutesBeforeLesson(
                                title = it.title,
                                text = it.text,
                                Intent(context, MainActivity::class.java)
                            )
                        )
                    }

                    ReminderType.AFTER_BEGINNING_LESSON -> {
                        notificationService.showNotification(
                            NotificationService.createNotificationAfterBeginningLessonForBeCheckedAtThisLesson(
                                title = it.title,
                                text = it.text,
                                Intent(context, MainActivity::class.java)
                            )
                        )
                    }
                }
            }
        }
    }
}