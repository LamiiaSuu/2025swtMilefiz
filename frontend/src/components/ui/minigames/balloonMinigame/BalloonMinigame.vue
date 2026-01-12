<script setup lang="ts">

import { tUI } from '@/i18n'
import { useMilefizStore } from '@/stores/milefizstore'
import { computed, onMounted, ref, watch } from 'vue'
import CountdownBar from '../CountdownBar.vue'

const props = defineProps<{
  duel: any // Duel-Objekt mit state, firstMeeple, secondMeeple, timeOut, etc.
}>()

const emit = defineEmits<{
  (e: 'close'): void // Wird aufgerufen wenn das Spiel beendet ist
}>()

const store = useMilefizStore()
const myLocalClicks = ref(0) // Lokaler Click-Counter (optional für UI)
const showInstructions = ref(true) // Zeigt Instructions-Overlay
const timerStarted = ref(false) // Flag ob Timer sichtbar sein soll

/**
 * Startet das Spiel nach 2 Sekunden (nach Instructions).
 * Blendet Instructions aus und startet den Countdown-Timer.
 */
onMounted(() => {
  setTimeout(() => {
    showInstructions.value = false
    timerStarted.value = true
  }, 2000)
})

/**
 * Findet die PlayerId zu einer gegebenen MeepleId.
 * Durchsucht alle Spieler in der Lobby nach der MeepleId.
 * 
 * @param meepleId - Die ID des Meeples
 * @returns PlayerId oder null falls nicht gefunden
 */
function getPlayerIdByMeeple(meepleId: string): string | null {
  const lobby = store.gamedata.lobby
  if (!lobby) return null

  for (const player of lobby.players) {
    if (player.meeples?.some((m) => m.id === meepleId)) {
      return player.id
    }
  }
  return null
}

/**
 * Prüft ob der aktuelle Spieler der erste Duellant ist.
 * Wichtig für die korrekte Anzeige von myPhase/rivalPhase.
 */
const isPlayer1 = computed(() => {
  const firstPlayerId = getPlayerIdByMeeple(props.duel?.firstMeeple)
  return firstPlayerId === store.gamedata.playerId
})

/**
 * Aktuelle Phase des eigenen Ballons (0-4).
 */
const myPhase = computed(() => {
  const state = props.duel?.state
  if (!state) return 0
  
  const phase = isPlayer1.value ? state.phasePlayer1 : state.phasePlayer2
  return phase ?? 0  // Fallback auf 0 falls undefined
})

/**
 * Aktuelle Phase des gegnerischen Ballons (0-4).
 */
const rivalPhase = computed(() => {
  const state = props.duel?.state
  if (!state) return 0

  const phase = isPlayer1.value ? state.phasePlayer2 : state.phasePlayer1
  return phase ?? 0  // Fallback auf 0 falls undefined
})

/**
 * Prüft ob der aktuelle Spieler gewonnen hat.
 * Vergleicht duel.state.winner mit der eigenen PlayerId.
 */
const isWinner = computed(() => {
  return props.duel?.state?.winner === store.gamedata.playerId
})

/**
 * Prüft ob das Spiel beendet ist (Gewinner oder Timeout).
 */
const isFinished = computed(() => {
  return props.duel?.state?.finished ?? false
})

/**
 * Holt den Spielernamen zur gegebenen MeepleId.
 * 
 * @param meepleId - Die ID des Meeples
 * @returns Spielername oder '?' falls nicht gefunden
 */
function getPlayerNameByMeeple(meepleId: string) {
  const lobby = store.gamedata.lobby
  if (!lobby) return '?'

  for (const player of lobby.players) {
    if (player.meeples?.some((m) => m.id === meepleId)) {
      return player.playerName ?? '?'
    }
  }
  return '?'
}

/**
 * Holt die Spielerfarbe zur gegebenen MeepleId.
 * 
 * @param meepleId - Die ID des Meeples
 * @returns Farbe (z.B. 'RED', 'BLUE') oder 'RED' als Fallback
 */
function getPlayerColorByMeeple(meepleId: string) {
  const lobby = store.gamedata.lobby
  if (!lobby) return 'RED'

  for (const player of lobby.players) {
    if (player.meeples?.some((m) => m.id === meepleId)) {
      return player.color || 'RED'
    }
  }
  return 'RED'
}

/**
 * Generiert den Bildpfad für den Ballon basierend auf Farbe und Phase.
 * Beispiel: /balloonMinigame/RED_LVL2.jpg
 * 
 * @param meepleId - Die ID des Meeples
 * @param phase - Die aktuelle Phase (0-4)
 * @returns Bildpfad
 */
function getBalloonImage(meepleId: string, phase: number) {
  const color = getPlayerColorByMeeple(meepleId)
  return `/balloonMinigame/${color}_LVL${phase}.jpg`
}

/**
 * Verarbeitet einen Klick auf den Button.
 * Sendet eine WebSocket-Message an das Backend (/balloon/click).
 * Erhöht den lokalen Click-Counter (optional für UI-Feedback).
 */
const handleClick = () => {
  if (isFinished.value || showInstructions.value) return

  myLocalClicks.value++ // Optional: Lokaler Counter

  // WebSocket-Message an Backend senden
  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/balloon/click`,
    { id: store.gamedata.playerId }
  )
}

/**
 * Schließt das Minigame-Popup automatisch 2 Sekunden nach Spielende.
 */
watch(isFinished, (finished) => {
  if (finished) {
    setTimeout(() => {
      emit('close')
    }, 2000)
  }
})
</script>

<template>
  <div class="dice-card no-select">
    <!-- Titel -->
    <h2 class="dice-title">
      {{ tUI('MINIGAME_BALLOON_TITLE') }}
    </h2>

    <!-- COUNTDOWN - nur anzeigen wenn Timer gestartet -->
    <CountdownBar v-if="timerStarted" :seconds="duel.timeOut" />

    <!-- Platzhalter wenn Timer noch nicht läuft (verhindert Layout-Shift) -->
    <div v-else class="countdown-placeholder"></div>

    <!-- Spieler & Ballons -->
    <div class="players">
      <!-- Spieler 1 (firstMeeple) -->
      <div class="player">
        <div class="balloon-wrapper">
          <img
            :src="getBalloonImage(duel.firstMeeple, isPlayer1 ? myPhase : rivalPhase)"
            :alt="`Balloon Phase ${isPlayer1 ? myPhase : rivalPhase}`"
            class="balloon-image"
          />
        </div>
        <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">
          {{ getPlayerNameByMeeple(duel.firstMeeple) }}
        </h3>
      </div>

      <!-- Spieler 2 (secondMeeple) -->
      <div class="player">
        <div class="balloon-wrapper">
          <img
            :src="getBalloonImage(duel.secondMeeple, isPlayer1 ? rivalPhase : myPhase)"
            :alt="`Balloon Phase ${isPlayer1 ? rivalPhase : myPhase}`"
            class="balloon-image"
          />
        </div>
        <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
          {{ getPlayerNameByMeeple(duel.secondMeeple) }}
        </h3>
      </div>
    </div>

    <!-- Klick-Button (nur sichtbar während des Spiels) -->
    <button
      v-if="!isFinished"
      class="dice-roll-button"
      :disabled="showInstructions"
      @click="handleClick"
    >
      {{ tUI('MINIGAME_BALLOON_CLICK') }}
    </button>

    <!-- GEWINNER/VERLIERER Anzeige -->
    <div v-if="isFinished" class="winner-big">
      <span v-if="isWinner" class="winner-text">
        {{ tUI('DUEL_WON') }}
      </span>

      <span v-else class="loser-text">
        {{ tUI('DUEL_LOST') }}
      </span>
    </div>

    <!-- INSTRUCTIONS OVERLAY (erste 2 Sekunden) -->
    <div v-if="showInstructions" class="instructions-overlay">
      <div class="instructions-popup">
        <p class="instruction-text">{{ tUI('MINIGAME_BALLOON_INSTRUCTION') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Instructions Overlay - Vollbild-Popup */
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
}

.instructions-popup {
  background: rgba(20, 20, 20, 0.95);
  border: 3px solid #ffcc00;
  border-radius: 15px;
  padding: 30px 40px;
  box-shadow: 0 0 30px rgba(255, 204, 0, 0.6);
}

.instruction-text {
  font-family: 'Acme', sans-serif;
  font-size: 2rem;
  font-weight: 900;
  color: #ffcc00;
  text-shadow:
    0 0 5px rgba(0, 0, 0, 0.95),
    2px 2px 5px rgba(0, 0, 0, 0.95);
  margin: 0;
}

/* Spieler-Grid: Zwei Spalten nebeneinander */
.players {
  display: grid;
  grid-template-columns: 1fr 1fr;
  justify-content: space-between;
  margin: 0px 0 15px;
  gap: 12px;
}

/* Einzelner Spieler: Zentriert Ballon + Name */
.player {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.player h3 {
  font-size: 1.8rem;
  font-weight: 900;
  margin: 8px 0 0 0;

  text-shadow:
    0 0 1px rgba(0, 0, 0, 0.95),
    1px 0 1px rgba(0, 0, 0, 0.9),
    -1px 0 1px rgba(0, 0, 0, 0.9),
    0 1px 1px rgba(0, 0, 0, 0.9),
    0 -1px 1px rgba(0, 0, 0, 0.9);

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'Acme', sans-serif;
  max-width: 100%;
}

/* Ballon-Container mit weißem Rahmen */
.balloon-wrapper {
  position: relative;
  width: 125px;
  height: 150px;
  border: 3px solid white;
  background: white;
  border-radius: 8px;
  padding: 5px;
}

.balloon-image {
  width: 100%;
  height: 100%;
  object-fit: contain; /* Bild proportional skalieren */
}

/* Platzhalter für CountdownBar (verhindert Layout-Shift) */
.countdown-placeholder {
  height: 30px;
  margin-bottom: 10px;
}

/* Gewinner/Verlierer Text */
.winner-big {
  margin-top: 18px;
  text-align: center;
  font-size: 1.8rem;
  font-weight: 900;
  font-family: 'Acme', sans-serif;
}

.dice-title {
  font-family: 'Acme', sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
}

/* Klick-Button Styling */
.dice-roll-button {
  font-family: 'Acme', sans-serif;
  font-weight: 900;
  font-size: 1.6rem;

  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

.dice-roll-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.winner-text,
.loser-text {
  font-family: 'Acme', sans-serif;
}

/* Verhindert Text-Selektion und Bild-Drag */
.no-select {
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;

  -webkit-user-drag: none;
}

.dice-card {
  position: relative;
}
</style>