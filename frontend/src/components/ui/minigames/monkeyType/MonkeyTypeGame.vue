<script setup lang="ts">
import { tUI } from '@/i18n'
import { useMilefizStore } from '@/stores/milefizstore'
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import CountdownBar from '../CountdownBar.vue'

const props = defineProps<{
  duel: any
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const store = useMilefizStore()
const showInstructions = ref(true)

// Local Source of Truth
const localProgress = ref(0)
const wrongIndex = ref<number | null>(null)

// Abzutippendes Wort oder Phrase
const targetWord = computed(() => props.duel?.state?.targetWord ?? '')

const isFinished = computed(() => props.duel?.state?.finished ?? false)
const isWinner = computed(() => props.duel?.state?.winner === store.gamedata.playerId)
const winner = computed(() => props.duel?.state?.winner)

const playerId = computed(() => store.gamedata.playerId)


// Reset bei neuem Wort
watch(() => targetWord.value, (newWord) => {
  localProgress.value = 0
  wrongIndex.value = null
})


/**
 * Sendet den Fortschritt eines Spielers ans Backend 
 * @param progress Fortschritt des Spielers
 */
function sendProgressTobackend(progress: number) {
  const lobbyId = store.gamedata.lobby?.id
  const duelId = props.duel?.duelId

  if (!lobbyId || !duelId || !playerId.value) return

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${lobbyId}/duel/${duelId}/monkeyType/progress`, { playerId: playerId.value, progress }
  )
}
/**
 * Visual Feedback bei Fehleingabe -> markiert den aktuell erwarteten Char kurz als falsch an
 * @param index position des Chars in 'targetWord'
 */
function flashWrongAt(index: number) {
  wrongIndex.value = index

  window.setTimeout(() => {
    if (wrongIndex.value === index) wrongIndex.value = null
  }, 160)
}

/**
 * Überprüft, ob eine Tasteneingabe erlaubt ist
 * @param e zu überpüfende Eingabe
 */
function isTypableKey(e: KeyboardEvent): boolean {
  if (e.ctrlKey || e.metaKey || e.altKey) return false

  //handle space
  if (e.key === ' ') return true
  if (e.key.length === 1) return true

  return false
}

/**
 * 
 * @param e 
 */
function handleKeyDown(e: KeyboardEvent) {
  e.stopPropagation()
  e.preventDefault()

  if (isFinished.value || showInstructions.value) return

  const word = targetWord.value
  if (!word || !isTypableKey(e)) return

  // wenn fertig
  if (localProgress.value >= word.length) return

  // input
  const typed = e.key
  const expected = word[localProgress.value]

  
  //  Frontendseitige Validierung: Nur wenn der Fortschritt tatsächlich steigt, d.h wenn der User den nächsten Char korrekt
  //  eingegeben hat, wird ein der Fortschritt ans Backend gesendet und verwaltet ansonsten wird der Char als kurz als falsch markiert. 
  if (typed === expected) {
    localProgress.value += 1
    wrongIndex.value = null
    sendProgressTobackend(localProgress.value)
  } else {
    flashWrongAt(localProgress.value)
  }
}


// Auto-close wenn fertig
watch(isFinished, (finished) => {
  if (finished) {
    setTimeout(() => emit('close'), 1500)
  }
})


// Life Cycle
/**
 * Requested ein Wort, falls keines vorhanden.
 * 
 * Globaler Keydown-Listener in Capture-Phase:
 * - fängt alle Tasteneingaben vor der restlichen UI ab
 * - verhindert Browser-Default (Scrollen, Shortcuts)
 * - sorgt dafür, dass das Minigame exklusiven Fokus hat
 */
onMounted(() => {
  if (!targetWord.value) {
    requestWord()
  }
  window.addEventListener('keydown', handleKeyDown, {
    capture: true, // "Capture-Phase": Exklusiver Fokus --> Minigame fängt Inputs als erstes ab
    passive: false // erlaubt explizit e.preventDefault
  })
  setTimeout(() => {
    showInstructions.value = false
  }, 2000)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown, { capture: true })
})

/**
 * Fordert das Zielwort für das MonkeyType-Minigame vom Backend an.
 *
 * Wird verwendet, wenn beim Öffnen des Minigames noch kein Zielwort
 * im aktuellen Duel-State vorhanden ist (z. B. bei Reconnects
 * oder inkonsistentem Client-State).
 *
 * Sendet eine STOMP-Nachricht an den Server, der anschließend
 * ein `MonkeyTypeGameUpdate` mit `targetWord` broadcastet.
 *
 * Hinweis:
 * Im Normalfall sollte das Zielwort bereits beim Spielstart
 * serverseitig gesetzt und an alle Clients gesendet werden.
 */
const requestWord = () => {
  if (!store.gamedata.lobby?.id || !props.duel?.duelId) return

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby.id}/duel/${props.duel.duelId}/monkeyType/getWord`,
    { playerId: store.gamedata.playerId }
  )
}

// UI helpers
const correctCount = computed(() => localProgress.value)

const progressText = computed(() => `${correctCount.value} / ${targetWord.value.length}`)


// Klassen fürs Rendering
function getCharClass(index: number): string {
  if (index < localProgress.value) return 'char-correct'
  if (index === localProgress.value) {
    return wrongIndex.value === index ? 'char-incorrect' : 'char-current'
  }
  return 'char-pending'
}

</script>

<template>
  <div class="dice-card no-select">
    <h2 class="dice-title">
      {{ tUI('MINIGAME_MONKEY_TYPE_TITLE') }}
    </h2>

    <!-- COUNTDOWN -->
    <CountdownBar :seconds="duel.timeOut" />

    <!-- Wort-Anzeige -->
    <div class="word-container">
      <div class="word-display">
        <span v-for="(char, index) in targetWord" :key="index" :class="['word-char', getCharClass(Number(index))]">
          {{ char }}
        </span>
      </div>
    </div>

    <!-- Status-Anzeige -->
    <div class="status-bar">
      <div class="progress">
        {{ progressText }}
      </div>
    </div>

    <!-- Instructions -->
    <div v-if="showInstructions" class="instructions-overlay">
      <div class="instructions-popup">
        <p class="instruction-text">{{ tUI('MINIGAME_MONKEY_TYPE_INSTRUCTION') }}</p>
        <p class="subinstruction">Bei Fehlern: Tippe solange, bis der Buchstabe richtig ist!</p>
      </div>
    </div>

    <!-- Ergebnis -->
    <div v-if="isFinished" class="winner-big">
      <span v-if="isWinner" class="winner-text">
        {{ tUI('DUEL_WON') }}
      </span>
      <span v-else-if="winner" class="loser-text">
        {{ tUI('DUEL_LOST') }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.dice-title {
  font-family: "Acme", sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
  color: #4dabf7;
}

.instructions-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(3px);
  border-radius: 16px;
}

.instructions-popup {
  background: rgba(20, 20, 20, 0.95);
  border: 3px solid #ffcc00;
  border-radius: 15px;
  padding: 25px 30px;
  box-shadow: 0 0 30px rgba(255, 204, 0, 0.6);
  text-align: center;
}

.instruction-text {
  font-family: 'Acme', sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  color: #ffcc00;
  text-shadow: 0 0 5px rgba(0, 0, 0, 0.95);
  margin: 0 0 10px 0;
}

.subinstruction {
  font-family: 'Acme', sans-serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: #ff6b6b;
  margin: 0;
}

.word-container {
  width: 100%;
  min-height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  margin: 15px 0;
  padding: 20px;
  border: 2px solid rgba(255, 255, 255, 0.1);
}

.word-display {
  font-size: 2rem;
  font-weight: 500;
  letter-spacing: 1px;
  line-height: 1.4;
  text-align: center;
  word-break: break-word;
}

.word-char {
  display: inline-block;
  margin: 0 1px;
  transition: color 0.2s ease;
  min-width: 20px;
  text-align: center;
}

.char-pending {
  color: #ffffff;
  opacity: 0.3;
}

.char-correct {
  color: #4dff6e;
  text-shadow: 0 0 5px rgba(77, 255, 110, 0.5);
}

.char-current {
  color: #ffffff;
  opacity: 0.9;
  text-decoration: underline;
  text-underline-offset: 6px;
}

.char-incorrect {
  color: #ff4d4d;
  text-shadow: 0 0 5px rgba(255, 77, 77, 0.5);
  position: relative;
}

.char-incorrect::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: #ff4d4d;
  border-radius: 1px;
}

.status-bar {
  display: flex;
  flex-direction: column;
  width: 100%;
  margin-top: 15px;
  padding: 10px 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  gap: 8px;
}

.progress {
  color: #88c9ff;
}

.accuracy {
  color: #4dff6e;
}

.winner-big {
  margin-top: 18px;
  text-align: center;
  font-size: 1.8rem;
  font-weight: 900;
  font-family: "Acme", sans-serif;
}

.winner-text {
  color: #4dff6e;
  text-shadow: 0 0 10px rgba(77, 255, 110, 0.5);
}

.loser-text {
  color: #ff4d4d;
  text-shadow: 0 0 10px rgba(255, 77, 77, 0.5);
}

.no-select {
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;
  -webkit-user-drag: none;
}

@media (max-width: 768px) {
  .dice-card {
    width: 95vw;
    padding: 15px;
  }

  .dice-title {
    font-size: 1.4rem;
  }

  .word-display {
    font-size: 1.6rem;
  }

  .word-container {
    min-height: 80px;
    padding: 15px;
  }

  .progress,
  .accuracy {
    font-size: 1rem;
  }

  .winner-big {
    font-size: 1.5rem;
  }

  .instruction-text {
    font-size: 1.3rem;
  }

  .subinstruction {
    font-size: 1rem;
  }
}
</style>