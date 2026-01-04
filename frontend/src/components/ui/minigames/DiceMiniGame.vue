<template>
  <div>
    <h2>{{ duel.miniGameName }}</h2>

    <p v-if="countdown !== null">
      ⏳ {{ countdown }}s
    </p>

    <div class="players">
      <div class="player">
        <h3>{{ getPlayerNameByMeeple(duel.firstMeeple) }}</h3>

        <div class="dice">{{ duel.state?.rollP1 ?? "-" }}</div>
      </div>

      <div class="player">
        <h3>{{ getPlayerNameByMeeple(duel.secondMeeple) }}</h3>

        <div class="dice">{{ duel.state?.rollP2 ?? "-" }}</div>
      </div>
    </div>

    <button
      v-if="!duel.state?.finished"
      :disabled="waiting === duel.duelId"
      @click="roll()"
    >
      Würfeln
    </button>

    <div v-if="duel.state?.finished" class="winner">
      <span v-if="isWinner()">🎉 Du hast gewonnen!</span>
      <span v-else>😵 Du hast verloren…</span>
    </div>
  </div>
</template>


<script setup lang="ts">
import { ref, watch, onMounted } from "vue"
import { useMilefizStore } from "@/stores/milefizstore"

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

    countdown.value = timeOut-1

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

<style scoped>
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

button {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
}

.winner {
  margin-top: 10px;
  text-align: center;
  font-weight: bold;
}
</style>
