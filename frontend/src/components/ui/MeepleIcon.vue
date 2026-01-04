<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  meepleId: string
  color: string
  isInBase: boolean
  isSelected: boolean
  keyNumber: number
}>()

const iconSrc = computed(() => {
  const colorUpper = props.color.toUpperCase()
  const state = props.isInBase ? 'house' : 'default'
  
  return `/meepleIcons/${colorUpper}_${state}.png`
})
</script>

<template>
  <div 
    class="meeple-icon"
    :class="{ 
      'selected': isSelected,
      'in-base': isInBase 
    }"
  >
    <img 
      :src="iconSrc" 
      :alt="`Meeple ${meepleId}`"
      :class="{ 'default-size': !isInBase }"
    />
    <div class="key-badge">{{ keyNumber }}</div>
    <div v-if="isSelected" class="selection-ring"></div>
  </div>
</template>

<style scoped>
.meeple-icon {
  position: relative;
  width: 70px;
  height: 70px;
  padding: 5px;
  border: 2px solid #c8a25d;
  border-radius: 8px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.meeple-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.3));
}

.meeple-icon img.default-size {
  width: 85%;
  height: 85%;
  margin: auto;
}

.key-badge {
  position: absolute;
  bottom: -3px;
  left: 3px;
  font-size: 2vh;
  font-weight: bold;
  color: #ffffff;
  border-radius: 3px;
  font-family: "AcmeFont", sans-serif;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.meeple-icon.selected {
  border-color: #ffd700;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  animation: pulse-border 1.5s ease-in-out infinite;
}

.meeple-icon.selected .key-badge {
  color: #00552d;
  border-color: #ffd700;
  animation: pulse-badge 1.5s ease-in-out infinite;
}


@keyframes pulse-border {
  0%, 100% { 
    border-color: #ffd700;
    box-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  }
  50% { 
    border-color: #ffed4e;
    box-shadow: 0 0 20px rgba(255, 215, 0, 0.9);
  }
}
</style>