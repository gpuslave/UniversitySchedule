package ru.vafeen.universityschedule.presentation.components.viewModels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.vafeen.universityschedule.domain.GSheetsServiceRequestStatus
import ru.vafeen.universityschedule.domain.models.Settings
import ru.vafeen.universityschedule.domain.usecase.db.GetAsFlowLessonsUseCase
import ru.vafeen.universityschedule.domain.usecase.network.GetSheetDataAndUpdateDBUseCase
import ru.vafeen.universityschedule.domain.utils.getSettingsOrCreateIfNull
import ru.vafeen.universityschedule.domain.utils.save

internal class SettingsScreenViewModel(
    private val getAsFlowLessonsUseCase: GetAsFlowLessonsUseCase,
    private val getSheetDataAndUpdateDBUseCase: GetSheetDataAndUpdateDBUseCase,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {
    private val _settings =
        MutableStateFlow(sharedPreferences.getSettingsOrCreateIfNull())
    private var lastLink: String? = null
    val settings = _settings.asStateFlow()
    private val spListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            viewModelScope.launch(Dispatchers.IO) {
                _settings.emit(sharedPreferences.getSettingsOrCreateIfNull())
            }
        }

    fun saveSettingsToSharedPreferences(settings: Settings) {
        sharedPreferences.save(settings = settings)
    }

    private val _gSheetsServiceRequestStatusFlow =
        MutableStateFlow<GSheetsServiceRequestStatus>(GSheetsServiceRequestStatus.Waiting)
    val gSheetsServiceRequestStatusFlow = _gSheetsServiceRequestStatusFlow.asStateFlow()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(spListener)
        viewModelScope.launch(Dispatchers.IO) {
            settings.collect {
                val link = it.link
                when {
                    link.isNullOrEmpty() -> {
                        _gSheetsServiceRequestStatusFlow.emit(GSheetsServiceRequestStatus.NoLink)
                    }

                    link.isNotEmpty() && link != lastLink -> {
                        lastLink = link
                        getSheetDataAndUpdateDBUseCase.use(link = link) { status ->
                            Log.d("status", "status = $status")
                            _gSheetsServiceRequestStatusFlow.emit(status)
                        }
                    }
                }
            }
        }
    }

    private val _subgroupFlow = MutableStateFlow<List<String>>(listOf())
    val subgroupFlow = _subgroupFlow.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getAsFlowLessonsUseCase.use().map { it.mapNotNull { lesson -> lesson.subGroup } }
                .collect { subGroups ->
                    _subgroupFlow.emit(subGroups.distinct())
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(spListener)
    }
}
