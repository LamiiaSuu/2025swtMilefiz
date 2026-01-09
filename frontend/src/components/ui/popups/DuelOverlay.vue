<template>
  <div class="overlay no-select">
    <div class="duel-container">

      <div
        v-for="duel in duels"
        :key="duel.duelId"
        class="duel-card"
      >
        <component
          :is="resolveComponent(duel)"
          :duel="duel"
          @close="() => emit('close', duel.duelId)"
        />
      </div>

    </div>
  </div>
</template>

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

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.55);
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
  background: #1b1e25;
  padding: 18px 22px;
  border-radius: 14px;
  color: white;
  min-width: 340px;
  box-shadow: 0 10px 32px rgba(0,0,0,.35);
}

.no-select {
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;

  -webkit-user-drag: none;
}
</style>
