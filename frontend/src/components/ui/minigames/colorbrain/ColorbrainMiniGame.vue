<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useMilefizStore } from "@/stores/milefizstore"
import CountdownBar from "../CountdownBar.vue"
import { tUI } from "@/i18n"

const props = defineProps<{
  duel: any
}>()

const emit = defineEmits<{
  (e: "close"): void
}>()

const store = useMilefizStore()
const waiting = ref<string | null>(null)

const displayWord = computed(() => props.duel?.selectedColors?.[0] ?? "")
const displayTextColor = computed(() => (props.duel?.selectedColors?.[1] ?? "WHITE").toLowerCase())

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

const shuffledColors = ref<string[]>([])
const hasShuffled = ref(false)


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




const hasClicked = ref(false)

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


</script>

<template>
  <div class="minigame-card no-select">

    <!-- Titel -->
    <h2 class="minigame-title">
      Colorbrain
    </h2>

    <!-- ANLEITUNGSTEXT -->
    <h3 v-if="!isFinished">
      {{ tUI('MINIGAME_COLORBRAIN_INSTRUCTION') }}
    </h3>
    <h2 class="colorbrain-word" :class="displayTextColor">
      {{ displayWord }}
    </h2>

    <!-- COUNTDOWN -->
    <CountdownBar :seconds="duel.timeOut" />

    <!-- Spieler -->
    <div class="players">
      <!-- Spieler 1 -->
      <div class="player">
        <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">
          {{ getPlayerNameByMeeple(duel.firstMeeple) }}
        </h3>
      </div>

      <!-- Spieler 2 -->
      <div class="player">
        <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
          {{ getPlayerNameByMeeple(duel.secondMeeple) }}
        </h3>
      </div>
    </div>

    <!-- Color Buttons -->
    <div class="button-container">
      <button v-for="color in shuffledColors" :key="color" :disabled="hasClicked || isFinished"
        :class="['color-button', color.toLowerCase()]" @click="handleColorClick(color)">
        {{ color }}
      </button>
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
  grid-template-columns: 1fr 1fr;
  justify-content: space-between;
  margin: 0px 0 15px;
  gap: 12px;
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

.dice-wrapper {
  position: relative;
  width: 125px;
  height: 125px;
  margin: 10px auto 0;
}

.winner-big {
  margin-top: 18px;
  text-align: center;
  font-size: 1.8rem;
  font-weight: 900;
  font-family: "Acme", sans-serif;
}

button {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
}

.minigame-title {
  font-family: "Acme", sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
}

.dice-roll-button {
  font-family: "Acme", sans-serif;
  font-weight: 900;
  font-size: 1.6rem;

  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
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

.colorbrain-word {
  font-family: "Acme", sans-serif;
  font-size: 2.4rem;
  font-weight: 900;
  text-align: center;
  margin: 10px 0 14px 0;
  text-transform: uppercase;
}

/* Textfarben-Klassen */
.red { color: red; }
.blue { color: blue; }
.green { color: green; }
.yellow { color: yellow; }
.orange { color: orange; }
.pink { color: hotpink; }
.purple { color: purple; }
.black { color: black; }

</style>