package ru.vafeen.universityschedule.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import ru.vafeen.universityschedule.domain.models.model_additions.ReminderType
import ru.vafeen.universityschedule.domain.notifications.NotificationBuilder
import ru.vafeen.universityschedule.domain.notifications.NotificationService
import ru.vafeen.universityschedule.domain.scheduler.SchedulerExtra
import ru.vafeen.universityschedule.domain.usecase.db.GetReminderByIdOfReminderUseCase

class NotificationAboutLessonReceiver : BroadcastReceiver() {
    private val notificationService: NotificationService by inject(
        clazz = NotificationService::class.java
    )
    private val notificationBuilder: NotificationBuilder by inject(clazz = NotificationBuilder::class.java)
    private val getReminderByIdOfReminderUseCase: GetReminderByIdOfReminderUseCase by inject(clazz = GetReminderByIdOfReminderUseCase::class.java)
    override fun onReceive(context: Context, intent: Intent) {


        val idOfReminder = intent.getIntExtra(
            SchedulerExtra.ID_OF_REMINDER,
            -1
        )
        CoroutineScope(Dispatchers.IO).launch {
            val reminder = getReminderByIdOfReminderUseCase.use(
                idOfReminder = idOfReminder
            )
            reminder?.let {
                when (reminder.type) {
                    ReminderType.BEFORE_LESSON -> {
                        notificationService.showNotification(
                            notificationBuilder.createNotificationAbout15MinutesBeforeLesson(
                                title = it.title,
                                text = it.text,
                                Intent(context, MainActivity::class.java)
                            )
                        )
                    }

                    ReminderType.AFTER_BEGINNING_LESSON -> {
                        notificationService.showNotification(
                           notificationBuilder.createNotificationAfterBeginningLessonForBeCheckedAtThisLesson(
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