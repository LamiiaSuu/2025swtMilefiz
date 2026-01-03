<template>
  <div class="overlay">
    <div class="duel-container">

      <div
        v-for="duel in duels"
        :key="duel.duelId"
        class="duel-card"
      >
        <h2>{{ duel.miniGameName }}</h2>

        <div class="players">
          <div class="player">
            <h3>Du</h3>
            <div class="dice">{{ duel.state?.rollP1 ?? "-" }}</div>
          </div>

          <div class="player">
            <h3>Gegner</h3>
            <div class="dice">{{ duel.state?.rollP2 ?? "-" }}</div>
          </div>
        </div>

        <button
          v-if="!duel.state?.finished"
          :disabled="waiting === duel.duelId"
          @click="roll(duel)"
        >
          Würfeln
        </button>

        <div v-if="duel.state?.finished" class="winner">
          <span v-if="isWinner(duel)">🎉 Du hast gewonnen!</span>
          <span v-else>😵 Du hast verloren…</span>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue"
import { useMilefizStore } from "@/stores/milefizstore"

const props = defineProps<{
  duels: any[]
}>()

const emit = defineEmits<{
  (e: "close", duelId: string): void
}>()

const store = useMilefizStore()

const waiting = ref<string | null>(null)

function isWinner(duel: any) {
  return duel.state?.winner === store.gamedata.playerId
}

function roll(duel: any) {
  waiting.value = duel.duelId

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${duel.duelId}/dice/roll`,
    { playerId: store.gamedata.playerId }
  )
}

// automatisch schließen, wenn Duel fertig -> 2s
watch(
  () => props.duels.map(d => ({ id: d.duelId, finished: d.state?.finished })),
  (list) => {
    list.forEach(entry => {
      if (entry.finished) {
        setTimeout(() => emit("close", entry.id), 2000)
      }
    })
  },
  { deep: true }
)
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.55);
  display: flex;
  align-items: center;
  justify-content: center;
}

.duel-container {
  display: flex;
  gap: 16px;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding: 12px;
}

.duel-card {
  background: #1b1e25;
  padding: 18px 22px;
  border-radius: 14px;
  color: white;
  min-width: 340px;
  box-shadow: 0 10px 32px rgba(0,0,0,.35);
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
