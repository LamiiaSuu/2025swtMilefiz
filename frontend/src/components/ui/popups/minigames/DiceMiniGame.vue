<script setup lang="ts">
import { ref, watch, onMounted } from "vue"
import { useMilefizStore } from "@/stores/milefizstore"
import { tUI } from '@/i18n'

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
  if (waiting.value || props.duel.state?.finished) return


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

// auto-close after finish
watch(
  () => props.duel.state?.finished,
  finished => {
    if (finished) setTimeout(() => emit("close"), 1500)
  }
)

watch(
  () => props.duel?.timeOut,
  (timeOut) => {
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


</script>

<template>
  <div class="content">

    <div class="text">
      <h1 class="title">{{ tUI('DICE_MINIGAME') }}</h1>
    </div>

    <div class="countdown">

      <div v-if="!duel.state?.finished && countdown !== null" class="countdown-row">
        <img src="/minigames/hourglass_icon.png" class="countdown-icon" alt="Hourglass Icon">
        <p>{{ countdown }}s</p>
      </div>

      <div v-else-if="duel.state?.finished">
        <span :class="isWinner() ? 'winner-win' : 'winner-lose'">
          {{ isWinner() ? tUI('MINIGAME_WON') : tUI('MINIGAME_LOST') }}
        </span>
      </div>

    </div>

    <div class="players">
      <div class="player">
        <div class="dice">{{ duel.state?.rollP1 ?? "-" }}</div>
        <h2>{{ getPlayerNameByMeeple(duel.firstMeeple) }}</h2>

      </div>

      <div class="player">
        <div class="dice">{{ duel.state?.rollP2 ?? "-" }}</div>

        <h2>{{ getPlayerNameByMeeple(duel.secondMeeple) }}</h2>

      </div>
    </div>

    <button :disabled="waiting === duel.duelId" @click="roll()">
      {{ tUI('ROLL_DICE') }}!
    </button>

  </div>
</template>

<style scoped>
.content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 6vh 0;
}


.text {
  text-align: center;
  margin-top: 0;
}

.title {
  font-size: 7vh;
  color: #000000;
  margin-bottom: 4vh;
}

.countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4.5vh;
  gap: 1.5vh;
  font-weight: bold;
  font-family: "AcmeFont", sans-serif;
}

.countdown-row {
  display: flex;
  align-items: center;
  gap: 1.5vh;
}

.countdown-icon {
  width: 4.5vh;
  height: 4.5vh;
}

.winner-win {
  color: var(--button-light-green);
}

.winner-lose {
  color: #b52326;
}

.players {
  display: flex;
  justify-content: space-between;
  width: 60%;
  max-width: 600px;
  gap: 5vw;
  margin-top: -2vh;
}

.player {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.dice {
  font-size: 4vh;
  margin-bottom: 1vh;
}


.winner {
  text-align: center;
  font-weight: bold;

  font-size: 5vh;
}

button {
  margin-top: 4vh;
  margin-bottom: 2vh;
  padding: 15px 30px;
  font-size: 3vh;
  cursor: pointer;
  background-image: var(--button-gradient-green);
  color: white;
  border: 3px solid black;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
  font-family: "AcmeFont", sans-serif;
  transition: background-color 0.2s, opacity 0.2s;
}

button.disabled,
button:disabled {
  background-image: none;
  background-color: #999;
  cursor: not-allowed;
  box-shadow: none;
  opacity: 0.8;
}
</style>