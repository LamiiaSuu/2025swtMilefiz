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

// 🔴 INPUT BUFFER für schnelles Tippen
const inputBuffer = ref<Array<{key: string, timestamp: number}>>([])
const isProcessingBuffer = ref(false)

// 🔴 LOKALER STATE (nur zur Anzeige)
const localInput = ref('')

// Sync mit Backend bei neuem Wort
watch(() => props.duel?.state?.targetWord, (newWord) => {
  if (newWord) {
    localInput.value = ''
    inputBuffer.value = []
  }
})

// Computed Properties
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

// 🔴 FÜR ANZEIGE: Kombiniere Backend-Input mit lokalen, die noch nicht gesynct sind
const displayInput = computed(() => {
  // Zeige Backend-Input an (korrekte + falsche)
  return userInput.value
})

const isFinished = computed(() => props.duel?.state?.finished || false)
const isWinner = computed(() => props.duel?.state?.winner === store.gamedata.playerId)
const winner = computed(() => props.duel?.state?.winner)

// Korrekte Buchstaben zählen
const correctCount = computed(() => {
  return correctLetters.value.filter(Boolean).length
})

// Genauigkeit berechnen
const accuracy = computed(() => {
  if (userInput.value.length === 0) return 1
  return correctCount.value / userInput.value.length
})

// Buchstaben-Klasse basierend auf Backend-Daten
const getCharClass = (index: number): string => {
  if (index < userInput.value.length) {
    if (correctLetters.value[index]) {
      return 'char-correct'
    } else {
      return 'char-incorrect'
    }
  }
  return 'char-pending'
}

// 🔴 TASTATUR-HANDLING MIT BUFFER
const handleKeyDown = (e: KeyboardEvent) => {
  e.stopPropagation()
  e.preventDefault()
  
  if (isFinished.value || showInstructions.value) return
  if (!targetWord.value || targetWord.value.length === 0) return

  // Ignoriere Modifier-Tasten
  if (e.ctrlKey || e.altKey || e.metaKey) return

  // Handle Space
  if (e.key === ' ') {
    addToBuffer(' ')
    return
  }

  // Handle normale Buchstaben
  if (e.key.length === 1 && e.key.match(/[a-zA-ZäöüÄÖÜß\-]/i)) {
    addToBuffer(e.key)
  }
}

// 🔴 ZUM BUFFER HINZUFÜGEN
const addToBuffer = (key: string) => {
  // Zum Buffer hinzufügen
  inputBuffer.value.push({
    key: key,
    timestamp: Date.now()
  })
  
  // Sofort verarbeiten (aber nicht blockieren)
  if (!isProcessingBuffer.value) {
    processBuffer()
  }
}

// 🔴 BUFFER VERARBEITEN
const processBuffer = async () => {
  if (isProcessingBuffer.value || inputBuffer.value.length === 0) return
  
  isProcessingBuffer.value = true
  
  try {
    // 🔴 ALLE Puffer-Events verarbeiten
    while (inputBuffer.value.length > 0) {
      const item = inputBuffer.value.shift()!
      await processSingleKey(item.key)
      
      // 🔴 KURZE PAUSE zwischen Events (nicht blockierend)
      await new Promise(resolve => setTimeout(resolve, 5))
    }
  } finally {
    isProcessingBuffer.value = false
  }
}

// 🔴 EINZELNEN TASTENDRUCK VERARBEITEN
const processSingleKey = async (key: string) => {
  const currentPosition = userInput.value.length
  
  // Prüfe ob Position gültig
  if (currentPosition >= targetWord.value.length) return
  
  const expectedChar = targetWord.value[currentPosition]
  const isCorrect = (key === expectedChar)
  
  // 🔴 NUR KORREKTE EINGABEN WEITERGEBEN (Backend-Logik)
  // (Backend prüft selbst nochmal, aber wir können schon filtern)
  if (isCorrect) {
    await sendToBackend(key, currentPosition)
  }
  // 🔴 FALSCHE EINGABE: IGNORIEREN (keine Aktion)
}

// 🔴 AN BACKEND SENDEN (ASYNCHRON)
const sendToBackend = async (key: string, position: number) => {
  if (!store.gamedata.lobby?.id || !props.duel?.duelId) return
  
  try {
    store.sendLobbyMessage(
      `/app/milefiz/lobby/${store.gamedata.lobby.id}/duel/${props.duel.duelId}/monkeyType/input`,
      {
        playerId: store.gamedata.playerId,
        typedChar: key,
        position: position,
        timestamp: new Date().toISOString()
      }
    )
  } catch (error) {
    console.error('Error sending to backend:', error)
  }
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

// Instructions nach 2 Sekunden ausblenden
onMounted(() => {
  setTimeout(() => {
    showInstructions.value = false
  }, 2000)
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
        {{ correctCount }} / {{ targetWord.length }}
      </div>
      <div class="accuracy" v-if="displayInput.length > 0">
        {{ Math.round(accuracy * 100) }}%
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
  }
  
  .subinstruction {
    font-size: 1rem;
  }
}
</style>