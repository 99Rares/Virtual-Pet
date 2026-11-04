package com.rares.virtualpetpompi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rares.virtualpetpompi.data.BuyResult
import com.rares.virtualpetpompi.data.GameRepository
import com.rares.virtualpetpompi.data.GameState
import com.rares.virtualpetpompi.data.StepStats
import com.rares.virtualpetpompi.domain.Difficulty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(private val repo: GameRepository) : ViewModel() {
    /** Null until the first emission, so the UI can hold back instead of flashing onboarding. */
    val state: StateFlow<GameState?> =
        repo.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val stats: StateFlow<StepStats?> =
        repo.stats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The wake-up animation only plays the first time the pet screen opens per process. */
    var wakePlayed = false

    fun onboard(difficulty: Difficulty) = viewModelScope.launch { repo.onboard(difficulty) }
    fun restart() = viewModelScope.launch { repo.restart() }
    fun setDailyGoal(goal: Int) = viewModelScope.launch { repo.setDailyGoal(goal) }
    fun selectRoom(id: String) = viewModelScope.launch { repo.selectRoom(id) }

    suspend fun buyFood(id: String): BuyResult = repo.buyFood(id)
    suspend fun buyRoom(id: String): BuyResult = repo.buyRoom(id)
    suspend fun feed(id: String): Boolean = repo.feed(id)

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { PetViewModel(container.repository) }
        }
    }
}
