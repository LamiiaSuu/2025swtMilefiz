<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/ui/pages/Header.vue'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import BackButton from '../pages/BackButton.vue'

const router = useRouter()
const audio = useAudioStore()

function onHover() {
  audio.playSfx('hover')
}

type Tab = 'winning' | 'jump' | 'duel' | 'dice' | 'controls'

const active = ref<Tab>('winning')

const order: Tab[] = ['winning', 'jump', 'duel', 'dice', 'controls']

function next() {
  const i = order.indexOf(active.value)
  active.value = order[(i + 1) % order.length]!
}

function prev() {
  const i = order.indexOf(active.value)
  active.value =
    order[(i - 1 + order.length) % order.length]!
}


</script>

<template>

  <!--MI'lefiz Header -->
  <Header overlay>{{ tUI('TUTORIAL') }}</Header>

  <div class="popup">

    <div class="menu-bar">
      <button class="menu-item" :class="{ active: active === 'winning' }" @mouseenter="onHover"
        @click="active = 'winning'">
        Gewinnen
      </button>

      <button class="menu-item" :class="{ active: active === 'jump' }" @mouseenter="onHover" @click="active = 'jump'">
        Hüpfen
      </button>

      <button class="menu-item" :class="{ active: active === 'duel' }" @mouseenter="onHover" @click="active = 'duel'">
        Duellieren
      </button>

      <button class="menu-item" :class="{ active: active === 'dice' }" @mouseenter="onHover" @click="active = 'dice'">
        Würfeln
      </button>

      <button class="menu-item" :class="{ active: active === 'controls' }" @mouseenter="onHover"
        @click="active = 'controls'">
        Steuerung
      </button>
    </div>


    <div class="content">
      <transition name="fade" mode="out-in">
        <div :key="active">
          <div v-if="active === 'winning'">Winning content</div>
          <div v-else-if="active === 'jump'">Jump content</div>
          <div v-else-if="active === 'duel'">Duel content</div>
          <div v-else-if="active === 'dice'">Würfeln content</div>
          <div v-else-if="active === 'controls'">Controls content</div>
        </div>
      </transition>
    </div>

    <div class="button-container">
      <BackButton @mouseenter="onHover" :to="{ name: 'Homepage' }" />
    </div>
  </div>

</template>

<style scoped>
.popup {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 80vw;
  text-align: center;
  position: relative;
  justify-content: flex-start;
  align-items: center;

  animation: fadeIn 0.3s ease;

  background:
    linear-gradient(rgba(40, 60, 35, 0.92),
      rgba(25, 40, 25, 0.92));

  box-shadow:
    inset 0 0 0 2px rgba(255, 255, 255, 0.06),
    0 12px 30px rgba(0, 0, 0, 0.45);

  border: 3px solid rgba(120, 160, 110, 0.25);
  border-radius: 18px;

  margin-top: -10vh;
  z-index: 2;
}

.menu-bar {
  display: flex;
  width: 90%;
  margin: 2vh;

  background: var(--button-gradient-green);
  border: 3px solid rgba(120, 160, 110, 0.5);
  border-radius: 18px;
}

.menu-item {
  flex: 1;
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;

  padding: 0.4vw;
  font-size: 3vh;
}

.menu-item.active {
  background: rgba(40, 60, 35, 0.92);
}

.menu-item.active:first-child {
  border-radius: 18px 0 0 18px;
}

.menu-item.active:last-child {
  border-radius: 0 18px 18px 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.content {
  flex: 1;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}



.button-container {
  display: flex;
  flex-direction: column;
  padding-bottom: 1vh;
}
</style>
