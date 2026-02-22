package edu.gvsu.cis.worder

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Letter(
    val text: Char = '$',
    val point: Int = 0,
    val letterMultiplier: Int = 1,
    val wordMultiplier: Int = 1
)

enum class Origin {
    Stock, CenterBox
}

class AppViewModel: ViewModel() {
    private val letterPoint = mapOf(
        'A' to 1, 'B' to 3, 'C' to 3, 'D' to 2, 'E' to 1,
        'F' to 4, 'G' to 2, 'H' to 4, 'I' to 1, 'J' to 8,
        'K' to 5, 'L' to 1, 'M' to 3, 'N' to 1, 'O' to 1,
        'P' to 3, 'Q' to 10,'R' to 1, 'S' to 1, 'T' to 1,
        'U' to 1, 'V' to 4, 'W' to 4, 'X' to 8, 'Y' to 4,
        'Z' to 10
    )
    private val dictionary = setOf(
        "HONEY", "HOUSE", "MOUSE", "NOSE", "RUN", "TONE", "SOME", "BOOK",
        "LINE", "ROCK", "MINE", "CROOK", "NINE", "SHINE",
        "SUN", "MOON", "STAR", "HOOK", "COOK",
        "READ", "FOOD", "WRITE", "DOCK",
        "CODE", "MOVE", "KOTLIN", "JAVA"
    )
    private val _sourceLetters = MutableStateFlow<List<Letter?>>(emptyList())
    val sourceLetters = _sourceLetters.asStateFlow()

    private val _targetLetters = MutableStateFlow<List<Letter?>>(emptyList())
    val targetLetters = _targetLetters.asStateFlow()
    private val _currentWordScore = MutableStateFlow(0)
    val currentWordScore = _currentWordScore.asStateFlow()

    private val _totalScore = MutableStateFlow(0)
    val totalScore = _totalScore.asStateFlow()

    private val _wordsBuilt = MutableStateFlow(0)
    val wordsBuilt = _wordsBuilt.asStateFlow()

    init {
        selectRandomLetters()
    }

    fun selectRandomLetters() {
        _sourceLetters.update {
            // 60% vowels, 40% consonants
            val vowels = (1..6).map {
                "AEIOU".random()
            }
            val consontants = (1..4).map {
                "BCFGHJKLMNPQRSTVWXYZ".random()
            }
            (vowels + consontants)
                .map { ch -> makeTile(ch) }
                .shuffled()
        }
        _targetLetters.update { emptyList() }
        _currentWordScore.update { 0 }
        _totalScore.update { 0 }
        _wordsBuilt.update { 0 }
    }

    fun reshuffleStock() {
        _sourceLetters.update { it.shuffled() }
    }

    fun rearrangeLetters(group: Origin, arr: List<Letter?>) {
        when (group) {
            Origin.Stock -> _sourceLetters.update { arr }
            Origin.CenterBox -> {
                _targetLetters.update { arr }
                updateCurrentWordScore(arr)
            }
        }
    }
    fun recordCurrentWord() {
        val score = _currentWordScore.value
        if (score <= 0) return

        _totalScore.update { it + score }
        _wordsBuilt.update { it + 1 }

        // After recording, clear arranged letters and reset current score
        _targetLetters.update { emptyList() }
        _currentWordScore.update { 0 }
    }

    private fun updateCurrentWordScore(arranged: List<Letter?>) {
        val word = arranged.filterNotNull().joinToString("") { it.text.toString() }
        if (word.isBlank() || word !in dictionary) {
            _currentWordScore.update { 0 }
            return
        }
        _currentWordScore.update { computeScore(arranged.filterNotNull()) }
    }

    private fun computeScore(tiles: List<Letter>): Int {
        val sum = tiles.sumOf { it.point * it.letterMultiplier }
        val wordMultProduct = tiles.fold(1) { acc, t -> acc * t.wordMultiplier }
        return sum * wordMultProduct
    }
    private fun makeTile(ch: Char): Letter {
        val base = letterPoint[ch] ?: 0
        val assignMultiplier = (1..10).random() == 1
        var lm = 1
        var wm = 1
        if (assignMultiplier) {
            val factor = if ((1..100).random() <= 70) 2 else 3 // mostly 2x
            val isLetterMultiplier = listOf(true, false).random()
            if (isLetterMultiplier) lm = factor else wm = factor
        }

        return Letter(
            text = ch,
            point = base,
            letterMultiplier = lm,
            wordMultiplier = wm
        )
    }
}