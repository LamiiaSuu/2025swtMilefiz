<script setup lang="ts">
import { tUI } from '@/i18n'
import { useMilefizStore } from '@/stores/milefizstore'
import { computed } from 'vue'

const milefizStore = useMilefizStore()

//schaut, ob schon gewürfelt wurde
const roll = computed(() => {
  return milefizStore.gamedata.currentDiceRoll !== undefined
})

//zieht sich den gewürfelten wert
const rollValue = computed(() => {
  return milefizStore.gamedata.currentDiceRoll || 0
})

</script>

<template>
  <div class="dice-counter">
    <div v-if="roll" class="counter-display">
      <div class="dice-number">{{ rollValue }}</div>
    </div>
    <div v-else class="no-roll">
      <div class="dice-icon-inactive">
        <img src="@/assets/hud/dice.png" class="action-icon" alt="dice"/>
      </div>
      <div class="label">{{ tUI('ROLL_DICE') }}!</div>
    </div>
  </div>
</template>

<style scoped>
.dice-counter {
  background: #23442057;
  border-radius: 8px;
  padding: 1rem;
  color: white;
  text-align: center;
  width: 5rem;
  height: 5rem;       /* Quadratisch mit rem */
  border: 2px solid #00aa5a57;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  
  font-family: "AcmeFont", sans-serif;
}

.counter-display, .no-roll {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  width: 100%;
  height: 100%;
}

.dice-number {
  font-size: 4rem;    /* Responsive font-size */
  font-weight: bold;
  color: #ffffff;
  text-shadow: 0 0 10px rgba(223, 223, 223, 0.71);
  
  font-family: "AcmeFont", sans-serif;
  
}

.dice-icon-inactive {
  width: 2rem;        /* 32px equivalent */
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.label {
  font-size: 1rem;  /* Responsive font-size */
  font-weight: bold;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
  line-height: 1;
  font-family: "AcmeFont", sans-serif;
}

.action-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  image-rendering: crisp-edges;
}

.no-roll {
  opacity: 0.7;
}
</style>
