<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useMilefizStore } from '@/stores/milefizstore'
import { TresCanvas } from '@tresjs/core'
import D20_Rolling from './D20_Rolling.vue'

const store = useMilefizStore()
const router = useRouter()

</script>

<template>

  <div class="dice-container">
    <TresCanvas :alpha="true" :clear-color="0x000000" :clear-alpha="0"
      style="width: 100%; height: 100%; background: transparent !important;">
      <TresPerspectiveCamera :position="[0, 2, 7]" />
      <TresAmbientLight :intensity="1" />
      <TresDirectionalLight :position="[3, 5, 2]" :intensity="5" />

      <D20_Rolling :scale="1" :y-position="0" bodyColor="grey" numbersColor="red" :rotation-y="-0.35" />
    </TresCanvas>
  </div>

  <div class="button-container">
    <button>Würfeln</button>
  </div>
</template>

<style scoped>
.dice-container {
  width: 100%;
  height: 50vh;
}

.button-container {
  display: flex;
  justify-content: center;

  margin-top: auto;
  margin-bottom: 3vh;
}

.button-container button {
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