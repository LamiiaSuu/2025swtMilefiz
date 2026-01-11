<template>
  <div class="slot-card no-select">
    <h2 class="slot-title">
      {{ tUI('MINIGAME_SLOT_TITLE') }}
    </h2>

    <!-- COUNTDOWN -->
    <CountdownBar :seconds="duel.timeOut" />

    <div class="players">
      <!-- Spieler 1 -->
      <div class="player">
        <div class="reel-display">

          <div class="slot-wrapper">
            <Slot :class="{
              active:
                duel.state?.rollP1 === null ||
                duel.state?.rollP1 === undefined ||
                duel.state?.rollP1 === ''
            }" />

            <div class="slot-face">
              {{ duel.state?.rollP1 ?? "" }}
            </div>
          </div>
          <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">
            {{ getPlayerNameByMeeple(duel.firstMeeple) }}
          </h3>
        </div>
        <button v-if="!duel.state?.finished" :disabled="waiting === duel.duelId || !isOwnMeeple(duel.firstMeeple)"
          class="slot-roll-button" v-on:click="stop(false)">{{ getPlayerNameByMeeple(duel.firstMeeple) }}
          {{ tUI('MINIGAME_SLOT_BUTTON') }}</button>
      </div>

      <!-- Spieler 2 -->
      <div class="player">
        <div class="reel-display">
          <div class="slot-wrapper">
            <Slot :class="{
              active:
                duel.state?.rollP2 === null ||
                duel.state?.rollP2 === undefined ||
                duel.state?.rollP2 === ''
            }" />

            <div class="slot-face">
              {{ duel.state?.rollP2 ?? "" }}
            </div>
          </div>
          <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
            {{ getPlayerNameByMeeple(duel.secondMeeple) }}
          </h3>
        </div>
        <button v-if="!duel.state?.finished" :disabled="waiting === duel.duelId || !isOwnMeeple(duel.secondMeeple)"
          class="slot-roll-button" v-on:click="stop(false)"> {{ getPlayerNameByMeeple(duel.secondMeeple) }}
          {{ tUI('MINIGAME_SLOT_BUTTON') }}</button>
      </div>

      <!-- Computer -->
      <div class="computer">
        <div class="reel-display">
          <div class="slot-wrapper">
            <Slot :class="{
              active:
                duel.state?.rollP1 === null ||
                duel.state?.rollP1 === undefined ||
                duel.state?.rollP1 === '' ||
                duel.state?.rollP2 === null ||
                duel.state?.rollP2 === undefined ||
                duel.state?.rollP2 === ''
            }" />

            <div class="slot-face">
              {{ duel.state?.rollP2 ?? "" }}
            </div>
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

function stop(isComp: boolean) {
  waiting.value = props.duel.duelId
  if (!isComp) {
    store.sendLobbyMessage(
      `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/slot/stop`,
      { playerId: store.gamedata.playerId }
    )
  } else {
    store.sendLobbyMessage(
      `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/slot/stop`,
      { playerId: "COMP" }
    )
  }

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
    if (finished) setTimeout(() => emit("close"), 1500)
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

// Computer zieht automatisch, wenn beide Spieler fertig sind
watch(
  () => [props.duel.state?.rollP1, props.duel.state?.rollP2],
  ([rollP1, rollP2]) => {
    const bothPlayersFinished =
      rollP1 !== null && rollP1 !== undefined && rollP1 !== '' &&
      rollP2 !== null && rollP2 !== undefined && rollP2 !== ''

    if (bothPlayersFinished && !props.duel.state?.finished) {
      // Computer zieht automatisch
      stop(true)
    }
  }
)

</script>

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

/* Zahl oben auf Würfel drauf, fast wie als wäre es die Augenzahl drauf */
.slot-face {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  font-family: "Acme", sans-serif;
  font-size: 2.2rem;
  font-weight: 900;
  text-shadow:
    0 0 3px rgba(0, 0, 0, .95),
    1px 1px 3px rgba(0, 0, 0, .95),
    -1px -1px 3px rgba(0, 0, 0, .95),
    2px 0 4px rgba(0, 0, 0, .9),
    -2px 0 4px rgba(0, 0, 0, .9),
    0 2px 4px rgba(0, 0, 0, .9),
    0 -2px 4px rgba(0, 0, 0, .9);
  pointer-events: none;

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

.slot-roll-button {
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
</style>
