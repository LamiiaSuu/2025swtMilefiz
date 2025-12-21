<script setup lang="ts">
import { computed } from 'vue'
import { useMilefizStore } from '@/stores/milefizstore'
import { useBoardStore } from '@/stores/boardStore'
import MeepleIcon from './MeepleIcon.vue'

const milefizStore = useMilefizStore()
const boardStore = useBoardStore()

// Aktueller Spieler
const currentPlayer = computed(() => {
  const lobby = milefizStore.gamedata.lobby
  const myId = milefizStore.gamedata.playerId
  if (!lobby || !myId) return null
  
  return lobby.players.find(p => p.id === myId)
})

// Meeples mit Metadaten
const playerMeeples = computed(() => {
  if (!currentPlayer.value || !boardStore.board) return []
  
  return currentPlayer.value.meeples.map((meeple, index) => {
    const fieldId = boardStore.meeplePositions[meeple.id]
    const field = boardStore.board?.fields.find(f => f.id === fieldId)
    
    console.log(`🔍 Meeple ${index + 1}:`, {
      id: meeple.id,
      fieldId: fieldId,
      fieldType: field?.type,
      isInBase: field?.type?.startsWith('START_')
    })
    
    const isInBase = field?.type?.startsWith('START_') ?? true
    
    return {
      id: meeple.id,
      color: currentPlayer.value!.color,
      isInBase: isInBase,
      isSelected: meeple.id === currentPlayer.value!.activeMeeple?.id,
      keyNumber: index + 1
    }
  })
})

</script>

<template>
  <div class="meeple-bar">
    <MeepleIcon
      v-for="meeple in playerMeeples"
      :key="meeple.id"
      :meepleId="meeple.id"
      :color="meeple.color"
      :isInBase="meeple.isInBase"
      :isSelected="meeple.isSelected"
      :keyNumber="meeple.keyNumber"
    />
  </div>
</template>

<style scoped>
.meeple-bar {
  display: flex;
  gap: 10px;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-left: 10px;
  padding-right: 25px;
  background: #00552d;
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  right: 0px;
}

.meeple-icons {
  display: flex;
  flex-direction: row; /* ← Horizontal */
  gap: 12px;
  align-items: center;
}
</style>