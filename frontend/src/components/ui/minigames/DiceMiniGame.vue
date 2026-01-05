<template>
  <div class="dice-card">
    <h2 class="dice-title">
      {{ tUI('MINIGAME_DICE_TITLE') }}
    </h2>

    <!-- COUNTDOWN -->
    <div v-if="countdown !== null" class="big-countdown">
      {{ countdown }}
    </div>

    <div class="players">
      <!-- Spieler 1 -->
      <div class="player">
        

        <div class="dice-wrapper">
          <img
            src="@/assets/hud/d20.png"
            class="d20"
            :class="{
              active:
                duel.state?.rollP1 === null ||
                duel.state?.rollP1 === undefined ||
                duel.state?.rollP1 === ''
            }"
          />

          <div class="dice-face">
            {{ duel.state?.rollP1 ?? "" }}
          </div>
        </div>
        <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">{{ getPlayerNameByMeeple(duel.firstMeeple) }}</h3>
      </div>

      <!-- Spieler 2 -->
      <div class="player">
        

        <div class="dice-wrapper">
          <img
            src="@/assets/hud/d20.png"
            class="d20"
            :class="{
              active:
                duel.state?.rollP2 === null ||
                duel.state?.rollP2 === undefined ||
                duel.state?.rollP2 === ''
            }"
          />

          <div class="dice-face">
            {{ duel.state?.rollP2 ?? "" }}
          </div>
        </div>
        <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">{{ getPlayerNameByMeeple(duel.secondMeeple) }}</h3>
      </div>
    </div>

    <button
      class="dice-roll-button"
      v-if="!duel.state?.finished"
      :disabled="waiting === duel.duelId"
      @click="roll()"
    >
      {{ tUI('ROLL_DICE') }}
    </button>

    <!-- GEWINNER -->
    <div v-if="duel.state?.finished" class="winner-big">
      <span
        v-if="isWinner()"
        class="winner-text"
      >
        {{ tUI('DUEL_WON') }}
      </span>

      <span
        v-else
        class="loser-text"
      >
        {{ tUI('DUEL_LOST') }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue"
import { useMilefizStore } from "@/stores/milefizstore"
import { tUI } from "@/i18n";

const countdown = ref<number | null>(null)

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

function roll() {
  waiting.value = props.duel.duelId

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/dice/roll`,
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

// auto close nachdem es fertig is
watch(
  () => props.duel.state?.finished,
  finished => {
    if (finished) setTimeout(() => emit("close"), 1500)
  }
)

// countdown FRONTEND
watch(
  () => props.duel?.timeOut,
  timeOut => {
    if (!timeOut) return

    countdown.value = timeOut - 1

    const interval = setInterval(() => {
      if (countdown.value === null) {
        clearInterval(interval)
        return
      }

      countdown.value--

      if (countdown.value <= 0) {
        clearInterval(interval)
      }
    }, 1000)
  },
  { immediate: true }
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

<style scoped>
.big-countdown {
  font-family: "Acme", sans-serif;
  text-align: center;
  font-size: 3.4rem;
  font-weight: 800;
  margin: 6px 0 12px;
  opacity: 0.9;
}

.players {
  display: grid;
  grid-template-columns: 1fr 1fr;
  justify-content: space-between;
  margin: 0px 0 10px;
  gap: 12px;
}

.player {
  text-align: center;
}

.player h3 {
  font-size: 1.8rem;
  font-weight: 900;
  margin: 0 0 6px 0;

  text-shadow:
    0 0 1px rgba(0,0,0,.95),
    1px 0 1px rgba(0,0,0,.9),
    -1px 0 1px rgba(0,0,0,.9),
    0 1px 1px rgba(0,0,0,.9),
    0 -1px 1px rgba(0,0,0,.9);

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

/* Bild */
.d20 {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: filter 0.06s ease;
}

/* solange kein Ergebnis Blur*/
.d20.active {
  filter: blur(4px);
  animation: d20Jump 0.1s steps(4) infinite;
}


/* Drehung */
@keyframes d20Jump {
  0%   { transform: rotate(0deg); }
  25%  { transform: rotate(90deg); }
  50%  { transform: rotate(180deg); }
  75%  { transform: rotate(270deg); }
  100% { transform: rotate(360deg); }
}

/* Zahl oben auf Würfel drauf, fast wie als wäre es die Augenzahl drauf */
.dice-face {
  position: absolute;
  inset: 0;

  font-family: "Acme", sans-serif;

  display: flex;
  align-items: center;
  justify-content: center;

  transform: translateY(-3%);

  font-size: 2.2rem;
  font-weight: 900;
  text-shadow:
    0 0 3px rgba(0,0,0,.95),
    1px 1px 3px rgba(0,0,0,.95),
    -1px -1px 3px rgba(0,0,0,.95),
    2px 0 4px rgba(0,0,0,.9),
    -2px 0 4px rgba(0,0,0,.9),
    0 2px 4px rgba(0,0,0,.9),
    0 -2px 4px rgba(0,0,0,.9);
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

.dice-title {
  font-family: "Acme", sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
}

.dice-roll-button {
  font-family: "Acme", sans-serif;
  font-weight: 900;
  font-size: 1rem;

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
</style>
