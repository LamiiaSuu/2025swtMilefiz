<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useMilefizStore } from "@/stores/milefizstore"
import CountdownBar from "../CountdownBar.vue"
import { tUI } from "@/i18n"
import type { UIKey } from '@/i18n/uiKeys'

const props = defineProps<{
  duel: any
}>()

const emit = defineEmits<{
  (e: "close"): void
}>()

const store = useMilefizStore()
const waiting = ref<string | null>(null)

const colorToUIKey: Record<string, UIKey> = {
  RED: 'MINIGAME_COLORBRAIN_RED',
  BLUE: 'MINIGAME_COLORBRAIN_BLUE',
  GREEN: 'MINIGAME_COLORBRAIN_GREEN',
  YELLOW: 'MINIGAME_COLORBRAIN_YELLOW',
  ORANGE: 'MINIGAME_COLORBRAIN_ORANGE',
  PINK: 'MINIGAME_COLORBRAIN_PINK',
  PURPLE: 'MINIGAME_COLORBRAIN_PURPLE',
  BLACK: 'MINIGAME_COLORBRAIN_BLACK',
}


const displayWord = computed(() => {
  const word = props.duel?.selectedColors?.[0]?.toUpperCase() ?? ""
  const key = colorToUIKey[word]
  return key ? tUI(key) : word
})



const displayTextColor = computed(() => (props.duel?.selectedColors?.[1] ?? "WHITE").toLowerCase())

const shuffledColors = ref<string[]>([])
const hasShuffled = ref(false)

const hasClicked = ref(false)

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
 * Findet anhand der eindeutingen Id des Meeples, der am Duell teilnimmt, den Namen des Spielers, dem dieser Meeple ist.
 * 
 * @param meepleId die eindeutige Id des Meeples, der am Duell teilnimmt
 */
function getPlayerNameByMeeple(meepleId: string) {
  const lobby = store.gamedata.lobby
  if (!lobby) return "?"

  for (const player of lobby.players) {
    if (player.meeples?.some(m => m.id === meepleId)) {
      return player.playerName ?? "?"
    }
  }
  return "?"
}

/**
 * Findet anhand der eindeutingen Id des Meeples, der am Duell teilnimmt, die Farbe des Spielers, dem dieser Meeple ist.
 * 
 * @param meepleId die eindeutige Id des Meeples, der am Duell teilnimmt
 */
function getPlayerColorByMeeple(meepleId: string) {
  const lobby = store.gamedata.lobby
  if (!lobby) return "#ffffff"

  for (const player of lobby.players) {
    if (player.meeples?.some(m => m.id === meepleId)) {
      return player.color || "#ffffff"
    }
  }

  return "#ffffff"
}

// auto close nachdem es fertig is
watch(
  () => props.duel.state?.finished,
  finished => {
    if (finished) setTimeout(() => emit("close"), 1500)
  }
)

/**
 * Mischt die Reihenfolge der Farben zufaellig.
 */
watch(
  () => props.duel?.selectedColors,
  (colors) => {
    if (!colors) return
    if (hasShuffled.value) return

    const copy = [...colors]
    for (let i = copy.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
        ;[copy[i], copy[j]] = [copy[j], copy[i]]
    }

    shuffledColors.value = copy
    hasShuffled.value = true
  },
  { immediate: true }
)

/**
 * Verarbeitet einen Klick auf eine Farbe.
 * Sendet eine WebSocket-Message an das Backend (/colorbrain/click).
 */
const handleColorClick = (color: string) => {
  if (isFinished.value) return

  hasClicked.value = true

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/colorbrain/click`,
    color
  )
}

/**
 * Findet welcher Spieler welche Farbe geklickt hat.
 * 
 * @param color die Farbe die geklickt wurde
 */
function getPlayerNameByColor(color: string) {
  if (!isFinished.value) return ""

  const clickedBy: string[] = []

  if (props.duel?.state?.player1Pick === color) {
    clickedBy.push(String(getPlayerNameByMeeple(props.duel.firstMeeple)))
  }

  if (props.duel?.state?.player2Pick === color) {
    clickedBy.push(String(getPlayerNameByMeeple(props.duel.secondMeeple)))
  }

  return clickedBy.join("& ") // falls beide dasselbe geklickt haben
}



</script>

<template>
  <div class="minigame-card no-select">

    <!-- Titel -->
    <h2 class="minigame-title">
      Colorbrain
    </h2>

    <!-- COUNTDOWN -->
    <CountdownBar :seconds="duel.timeOut" />

    <!-- ANLEITUNGSTEXT -->
    <h3 class="minigame-instructions" v-if="!isFinished">
      {{ tUI('MINIGAME_COLORBRAIN_INSTRUCTION') }}
    </h3>

    <h2 class="colorbrain-word" :class="displayTextColor">
      {{ displayWord }}
    </h2>

    <!-- Color Buttons -->
    <div class="button-container">
      <button v-for="color in shuffledColors" :key="color" :disabled="hasClicked || isFinished"
        :class="['color-button', color.toLowerCase()]" @click="handleColorClick(color)">
        <span v-if="isFinished">
          {{ getPlayerNameByColor(color) }}
        </span>
      </button>
    </div>

    <!-- Spieler -->
    <div class="players">
      <!-- Spieler 1 -->
      <div class="player">
        <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">
          {{ getPlayerNameByMeeple(duel.firstMeeple) }}
        </h3>
      </div>

      <div class="vs">
        <h3>vs.</h3>
      </div>

      <!-- Spieler 2 -->
      <div class="player">
        <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
          {{ getPlayerNameByMeeple(duel.secondMeeple) }}
        </h3>
      </div>
    </div>

    <!-- GEWINNER/VERLIERER Anzeige -->
    <div v-if="isFinished" class="winner-big">
      <span v-if="isWinner" class="winner-text">
        {{ tUI('DUEL_WON') }}
      </span>

      <span v-else class="loser-text">
        {{ tUI('DUEL_LOST') }}
      </span>
    </div>

    <!-- Platzhalter wenn Timer noch nicht läuft (verhindert Layout-Shift) -->
    <div v-else class="countdown-placeholder"></div>

  </div>
</template>

<style scoped>
.big-countdown {
  font-family: "Acme", sans-serif;
  text-align: center;
  font-size: 3.4rem;
  font-weight: 800;
  margin: 6px 0 6px;
  opacity: 0.9;
}

.players {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  margin: 0 0 15px;
  gap: 12px;
}

.vs {
  text-align: center;
  font-family: "Acme", sans-serif;
  font-size: 1.4rem;
  font-weight: 900;
  margin: 0px 0 6px 0;
}

.player {
  text-align: center;
}

.player h3 {
  font-size: 1.8rem;
  font-weight: 900;
  margin: 0px 0 6px 0;

  text-shadow:
    0 0 1px rgba(0, 0, 0, .95),
    1px 0 1px rgba(0, 0, 0, .9),
    -1px 0 1px rgba(0, 0, 0, .9),
    0 1px 1px rgba(0, 0, 0, .9),
    0 -1px 1px rgba(0, 0, 0, .9);

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: "Acme", sans-serif;
}

.winner-big {
  margin-top: 18px;
  text-align: center;
  font-size: 1.8rem;
  font-weight: 900;
  font-family: "Acme", sans-serif;
}

.winner-text,
.loser-text {
  font-family: "Acme", sans-serif;
}

.no-select {
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;

  -webkit-user-drag: none;
}

.countdown-placeholder {
  height: 20px;
  margin-bottom: 10px;
}


.minigame-title {
  font-family: "Acme", sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
}

.minigame-instructions {
  font-family: "Acme", sans-serif;
  text-align: center;
}

.colorbrain-word {
  font-family: "Acme", sans-serif;
  font-size: 2.4rem;
  font-weight: 900;
  text-align: center;
  margin: 10px 0 14px 0;
  text-transform: uppercase;
}

.button-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5vh;
  justify-items: center;
  margin: 0 auto;
  padding-bottom: 2vh;
}

.color-button {
  height: 6vh;
  width: 90%;
  border-radius: 18px;
  border: none;
  cursor: pointer;
}

.color-button:disabled {
  opacity: 0.5;
  pointer-events: none;
}

.color-button span {
  color: white;
  font-family: "Acme", sans-serif;
  font-weight: 900;
  text-shadow:
    0 0 3px rgba(0, 0, 0, 0.8),
    1px 1px 2px rgba(0, 0, 0, 0.8);
}

/* Textfarben-Klassen */
.red {
  color: red;
}

.blue {
  color: blue;
}

.green {
  color: green;
}

.yellow {
  color: yellow;
}

.orange {
  color: orange;
}

.pink {
  color: hotpink;
}

.purple {
  color: purple;
}

.black {
  color: black;
}

.color-button.red {
  background-color: red;
}

.color-button.blue {
  background-color: blue;
}

.color-button.green {
  background-color: green;
}

.color-button.yellow {
  background-color: yellow;
}

.color-button.orange {
  background-color: orange;
}

.color-button.pink {
  background-color: hotpink;
}

.color-button.purple {
  background-color: purple;
}

.color-button.black {
  background-color: black;
}
</style>