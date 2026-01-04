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


    <p v-if="countdown !== null" class="countdown">
      <img src="/minigames/hourglass_icon.png" class="countdown-icon"> {{ countdown }}s
    </p>

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

    <button v-if="!duel.state?.finished" :disabled="waiting === duel.duelId" @click="roll()">
      {{ tUI('ROLL_DICE') }}!
    </button>

    <div v-if="duel.state?.finished" class="winner">
      <span v-if="isWinner()">{{ tUI('MINIGAME_WON') }}</span>
      <span v-else>{{ tUI('MINIGAME_LOST') }}</span>
    </div>
  </div>
</template>

<style scoped>
.content {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.text {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin: 3vh 0;
  text-align: center;
  align-items: center;
}

.title {
  font-size: 7vh;
  color: #000000;
  margin-bottom: 1vh;
}

.countdown-icon {
  font-size: 3vh;
}

.countdown-icon {
  width: 5%;
  height: 5%;
}

.players {
  display: flex;
  justify-content: space-between;
  margin: 14px 0 10px;
  gap: 12px;
}

.player {
  flex: 1;
  text-align: center;
}

.dice {
  font-size: 2.4rem;
}

.winner {
  margin-top: 10px;
  text-align: center;
  font-weight: bold;
}

button {
  padding: 15px 30px;
  background-image: var(--button-gradient-green);
  color: white;
  font-size: 3vh;
  cursor: pointer;

  border: 3px solid black;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.8);
  font-family: "AcmeFont", sans-serif;

  -webkit-text-stroke: 0;
  paint-order: fill;
  text-shadow: none;
  font-weight: 400;
}
</style>