import android.os.CountDownTimer
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.allFrenchWords
//import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import com.example.unscramble.ui.GameUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private lateinit var currentWord: String
    private var usedWords: MutableSet<String> = mutableSetOf()
    var userGuess by mutableStateOf("")
        private set

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0

    private var currentLanguage = "en"
    private var currentWords: Set<String> = allWords
    private var currentLocale: Locale = Locale.ENGLISH
    var currentStrings: AppStrings = englishStrings
        private set


    private fun pickRandomWordAndShuffle(): String {
        currentWord = currentWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        timeLeftInMillis = 20000
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.update { currentState ->
                    currentState.copy(timeLeft = millisUntilFinished / 1000)
                }
            }

            override fun onFinish() {
                skipWord()
            }
        }.start()
    }

    fun resetGame() {
        countDownTimer?.cancel()
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle(), timeLeft = 20)
        startTimer()
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            countDownTimer?.cancel()
            val timeBonus = _uiState.value.timeLeft.toInt()
            val updatedScore = _uiState.value.score + timeBonus*2
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = currentState.currentWordCount.inc(),
                    score = updatedScore
                )
            }
            startTimer()
        }
    }

    fun skipWord() {
        countDownTimer?.cancel()
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }

    fun setLanguage(lang: String) {
        countDownTimer?.cancel()
        currentLocale = when (lang) {
            "fr" -> Locale.FRENCH
            else -> Locale.ENGLISH
        }
        currentStrings = when (lang) {
            "fr" -> frenchStrings
            else -> englishStrings
        }
        currentWords = when (lang) {
            "fr" -> allFrenchWords
            else -> allWords
        }
        resetGame()
    }

    init {
        resetGame()
    }
}

data class AppStrings(
    val appName: String,
    val instructions: String,
    val submit: String,
    val skip: String,
    val score: String,
    val wordCount: String,
    val enterWord: String,
    val wrongGuess: String,
    val timeLeft: String,
    val congratulations: String,
    val youScored: String,
    val exit: String,
    val playAgain: String
)

val englishStrings = AppStrings(
    appName = "Unscramble",
    instructions = "Unscramble the word using all the letters",
    submit = "Submit",
    skip = "Skip",
    score = "Score: %d",
    wordCount = "%d/10",
    enterWord = "Enter your word",
    wrongGuess = "Wrong Guess!",
    timeLeft = "Time left: %d",
    congratulations = "Congratulations!",
    youScored = "You scored: %d",
    exit = "Exit",
    playAgain = "Play Again"
)

val frenchStrings = AppStrings(
    appName = "Unscramble",
    instructions = "Reconstituez le mot avec toutes les lettres",
    submit = "Valider",
    skip = "Passer",
    score = "Score : %d",
    wordCount = "%d/10",
    enterWord = "Entrez votre mot",
    wrongGuess = "Mauvaise réponse !",
    timeLeft = "Temps restant : %d",
    congratulations = "Félicitations !",
    youScored = "Votre score : %d",
    exit = "Quitter",
    playAgain = "Rejouer"
)
