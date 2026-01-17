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
const hasFocus = ref(false)

// Instructions nach 2 Sekunden ausblenden
onMounted(() => {
  setTimeout(() => {
    showInstructions.value = false
  }, 2000)
})

const targetWord = computed(() => {
  return props.duel?.state?.targetWord || ''
})

const userInput = computed(() => {
  const playerId = store.gamedata.playerId
  const isPlayer1 = playerId === props.duel?.state?.player1
  return isPlayer1
    ? props.duel?.state?.player1Input || ''
    : props.duel?.state?.player2Input || ''
})

const correctLetters = computed(() => {
  const playerId = store.gamedata.playerId
  const isPlayer1 = playerId === props.duel?.state?.player1
  return isPlayer1
    ? props.duel?.state?.correctLettersPlayer1 || []
    : props.duel?.state?.correctLettersPlayer2 || []
})

const isFinished = computed(() => props.duel?.state?.finished || false)
const isWinner = computed(() => props.duel?.state?.winner === store.gamedata.playerId)
const winner = computed(() => props.duel?.state?.winner)

// Genauigkeit berechnen
const accuracy = computed(() => {
  if (userInput.value.length === 0) return 1
  const correct = correctLetters.value.filter(Boolean).length
  return correct / userInput.value.length
})

// Buchstaben-Klasse basierend auf Eingabe
const getCharClass = (index: number): string => {
  if (index < userInput.value.length) {
    return correctLetters.value[index] ? 'char-correct' : 'char-incorrect'
  }
  return 'char-pending'
}

// Tastatur-Handling
const handleKeyDown = (e: KeyboardEvent) => {
  e.stopPropagation()
  e.preventDefault()
  
  if (isFinished.value || showInstructions.value) return
  if (!targetWord.value || targetWord.value.length === 0) return

  // Ignoriere Modifier-Tasten
  if (e.ctrlKey || e.altKey || e.metaKey) return

  // Handle Space
  if (e.key === ' ') {
    e.preventDefault()
    sendKeyPress(' ')
    return
  }

  // Handle normale Buchstaben
  if (e.key.length === 1 && e.key.match(/[a-zA-ZäöüÄÖÜß\-]/i)) {
    e.preventDefault()
    sendKeyPress(e.key)
  }
}

const sendKeyPress = (key: string) => {
  if (!store.gamedata.lobby?.id || !props.duel?.duelId) return

  const position = userInput.value.length
  if (position >= targetWord.value.length) return

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby.id}/duel/${props.duel.duelId}/monkeyType/input`,
    {
      playerId: store.gamedata.playerId,
      typedChar: key,
      position: position,
      timestamp: new Date().toISOString()
    }
  )
}

// Event Listener
onMounted(() => {
  window.addEventListener('keydown', handleKeyDown, {
    capture: true,
    passive: false
  })
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown, { capture: true })
})

// Wort anfordern wenn nicht geladen
onMounted(() => {
  if (!targetWord.value) {
    requestWord()
  }
})

const requestWord = () => {
  if (!store.gamedata.lobby?.id || !props.duel?.duelId) return

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby.id}/duel/${props.duel.duelId}/monkeyType/getWord`,
    { playerId: store.gamedata.playerId }
  )
}

// Auto-close wenn fertig
watch(isFinished, (finished) => {
  if (finished) {
    setTimeout(() => emit('close'), 1500)
  }
})
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
        <span 
          v-for="(char, index) in targetWord" 
          :key="index" 
          :class="['word-char', getCharClass(Number(index))]"
        >
          {{ char }}
        </span>
      </div>
    </div>

    <!-- Status-Anzeige -->
    <div class="status-bar">
      <div class="progress">
        {{ userInput.length }} / {{ targetWord.length }}
      </div>
      <div class="accuracy" v-if="userInput.length > 0">
        {{ Math.round(accuracy * 100) }}%
      </div>
    </div>

    <!-- Instructions (erste 2 Sekunden) -->
    <div v-if="showInstructions" class="instructions-overlay">
      <div class="instructions-popup">
        <p class="instruction-text">{{ tUI('MINIGAME_MONKEY_TYPE_INSTRUCTION') }}</p>
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
.dice-card {
  position: relative;
  width: 600px;
  max-width: 90vw;
  background: #1a1a2e;
  border-radius: 16px;
  padding: 20px;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: 'Acme', sans-serif;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

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
}

.instruction-text {
  font-family: 'Acme', sans-serif;
  font-size: 1.4rem;
  font-weight: 900;
  color: #ffcc00;
  text-shadow:
    0 0 5px rgba(0, 0, 0, 0.95),
    2px 2px 5px rgba(0, 0, 0, 0.95);
  margin: 0;
  text-align: center;
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
}

.char-pending {
  color: #ffffff;
  opacity: 0.3;
}

.char-correct {
  color: #4dff6e;
}

.char-incorrect {
  color: #ff4d4d;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-top: 15px;
  padding: 10px 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.progress, .accuracy {
  font-size: 1.2rem;
  font-weight: 600;
  font-family: 'Acme', sans-serif;
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

.timeout-text {
  color: #ffd66b;
  text-shadow: 0 0 10px rgba(255, 214, 107, 0.5);
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
  
  .progress, .accuracy {
    font-size: 1rem;
  }
  
  .winner-big {
    font-size: 1.5rem;
  }
  
  .instruction-text {
    font-size: 1.3rem;
    padding: 20px;
  }
}
</style>