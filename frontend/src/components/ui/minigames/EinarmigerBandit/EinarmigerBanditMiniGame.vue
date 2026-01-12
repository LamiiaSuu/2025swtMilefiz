<script setup lang="ts">
import { ref, watch } from "vue"
import { useMilefizStore } from "@/stores/milefizstore"
import { tUI } from "@/i18n";
import CountdownBar from "../CountdownBar.vue"
import Slot from './Slot.vue'
import type DuelOverlay from "../../popups/DuelOverlay.vue";
import { playerColors } from "@/types/colorsAssets";

const props = defineProps<{
  duel: any
}>()

const emit = defineEmits<{
  (e: "close"): void
}>()

const store = useMilefizStore()
const waiting = ref<string | null>(null)

function isWinner() {
  return props.duel.state?.winner === store.gamedata.playerId
}

function stop() {
  waiting.value = props.duel.duelId
  console.log('Sending playerId:', store.gamedata.playerId)
  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/einarmigerBandit/stop`,
    { playerId: store.gamedata.playerId }
  )
}

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

function isOwnMeeple(meepleId: string): boolean {
  const lobby = store.gamedata.lobby
  if (!lobby) return false

  for (const player of lobby.players) {
    if (player.id === store.gamedata.playerId) {
      return player.meeples?.some(m => m.id === meepleId) ?? false
    }
  }
  return false
}

// auto close nachdem es fertig is
watch(
  () => props.duel.state?.finished,
  finished => {
    if (finished) setTimeout(() => emit("close"), 2000)
  }
)

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

</script>


<template>
  <div class="slot-card no-select">
    <h2 class="slot-title" :class="{ jackpot: duel.state?.jackpot }">
      {{ duel.state?.jackpot ? tUI('MINIGAME_SLOT_JACKPOT') : tUI('MINIGAME_SLOT_TITLE') }}
    </h2>

    <img v-if="duel.state?.jackpot" src="@/assets/winPopUpAssets/confetti_down.gif" class="confetti-gif"
      alt="Confetti" />

    <!-- COUNTDOWN -->
    <CountdownBar :seconds="duel.timeOut" />

    <div class="players">
      <!-- Spieler 1 -->
      <div class="player">
        <div class="reel-display">

          <div class="slot-wrapper">
            <Slot :result="duel.state?.resultP1" />
          </div>
          <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">
            {{ getPlayerNameByMeeple(duel.firstMeeple) }}
          </h3>
        </div>
        <button v-if="!duel.state?.finished" :disabled="waiting === duel.duelId || !isOwnMeeple(duel.firstMeeple)"
          class="slot-stop-button" v-on:click="stop()">{{ getPlayerNameByMeeple(duel.firstMeeple) }}
          {{ tUI('MINIGAME_SLOT_BUTTON') }}</button>
      </div>

      <!-- Spieler 2 -->
      <div class="player">
        <div class="reel-display">
          <div class="slot-wrapper">
            <Slot :result="duel.state?.resultP2" />
          </div>
          <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
            {{ getPlayerNameByMeeple(duel.secondMeeple) }}
          </h3>
        </div>
        <button v-if="!duel.state?.finished" :disabled="waiting === duel.duelId || !isOwnMeeple(duel.secondMeeple)"
          class="slot-stop-button" v-on:click="stop()"> {{ getPlayerNameByMeeple(duel.secondMeeple) }}
          {{ tUI('MINIGAME_SLOT_BUTTON') }}</button>
      </div>

      <!-- Computer -->
      <div class="computer">
        <div class="reel-display">
          <div class="slot-wrapper">
            <Slot :result="duel.state?.resultComp" />
          </div>
          <h3>{{ tUI('MINIGAME_SLOT_COMP') }}</h3>
        </div>
      </div>
    </div>

    <!-- GEWINNER -->
    <div v-if="duel.state?.finished" class="winner-big">
      <span v-if="isWinner()" class="winner-text">
        {{ tUI('DUEL_WON') }}
      </span>

      <span v-else class="loser-text">
        {{ tUI('DUEL_LOST') }}
      </span>
    </div>
  </div>
</template>


<style scoped>
.slot-card {
  position: relative;
}

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
  grid-template-columns: 1fr 1fr 1fr;
  justify-content: space-between;
  margin: 0px 0 15px;
  gap: 12px;
}

.player,
.computer {
  text-align: center;
}

.player h3,
.computer h3 {
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

.computer h3 {
  color: black;
}

.slot-wrapper {
  position: relative;
  width: 125px;
  height: 125px;
  margin: 10px auto 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reel-display {
  background-color: white;
  border-radius: 8px;
  margin-bottom: 15%;
  padding-bottom: 5%;
  padding-top: 30px;
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

.slot-title {
  font-family: "Acme", sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
}

.slot-stop-button {
  font-family: "Acme", sans-serif;
  font-weight: 900;
  font-size: 1.6rem;

  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

.slot-stop-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.winner-text,
.loser-text {
  font-family: "Acme", sans-serif;
}

.slot-title.jackpot {
  background: linear-gradient(90deg,
      #ff0000, #ff7f00, #ffff00, #00ff00, #0000ff, #4b0082, #9400d3);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: rainbow 2s ease infinite;
  font-size: 2.2rem;
}

@keyframes rainbow {
  0% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0% 50%;
  }
}

.confetti-gif {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  object-fit: cover;
  z-index: 10;
}

.no-select {
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;

  -webkit-user-drag: none;
}
</style>
