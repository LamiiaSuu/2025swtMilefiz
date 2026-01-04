<script setup lang="ts">
import DiceMiniGame from "../minigames/DiceMiniGame.vue"

const props = defineProps<{
  duels: any[]
}>()

const emit = defineEmits<{
  (e: "close", duelId: string): void
}>()

function resolveComponent(duel: any) {
  switch (duel.miniGameType) {
    case "DiceGame":
      return DiceMiniGame

    default:
      return DiceMiniGame
  }
}
</script>

<template>
  <div class="overlay">
    <div class="duel-container">

      <div v-for="duel in duels" :key="duel.duelId" class="duel-card">
        <component :is="resolveComponent(duel)" :duel="duel" @close="() => emit('close', duel.duelId)" />
      </div>

    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99990;
}

.duel-container {
  display: flex;
  gap: 16px;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding: 12px;
}

.duel-card {
  display: flex;
  flex-direction: column;
  background-color: var(--background-color-forms);
  border-radius: 15px;
  width: 45vw;
  height: 55vh;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
}
</style>
