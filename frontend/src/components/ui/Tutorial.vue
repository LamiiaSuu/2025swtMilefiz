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
  <div class="popup">

    <!-- Linke Spalte -->
    <button class="menu-cycle-button left" @click="prev()" @mouseenter="onHover">&lt;</button>

    <!-- Mitte -->
    <div class="center">

      <!-- Menu Bar -->
      <div class="menu-bar">
        <button class="menu-item" :class="{ active: active === 'winning' }" @mouseenter="onHover"
          @click="active = 'winning'">
          {{ tUI('WINNING_TAB') }}
        </button>

        <button class="menu-item" :class="{ active: active === 'jump' }" @mouseenter="onHover" @click="active = 'jump'">
          {{ tUI('JUMPING_TAB') }}
        </button>

        <button class="menu-item" :class="{ active: active === 'duel' }" @mouseenter="onHover" @click="active = 'duel'">
          {{ tUI('DUEL_TAB') }}
        </button>

        <button class="menu-item" :class="{ active: active === 'dice' }" @mouseenter="onHover" @click="active = 'dice'">
          {{ tUI('ROLL_DICE_TAB') }}
        </button>

        <button class="menu-item" :class="{ active: active === 'controls' }" @mouseenter="onHover"
          @click="active = 'controls'">
          {{ tUI('CONTROLS') }}
        </button>
      </div>

      <!-- Menu Inhalt -->
      <div class="tutorial-content">
        <transition name="fade" mode="out-in">
          <div :key="active" class="content-columns" :class="{ 'controls-active': active === 'controls' }">

            <div class="column left">
              <div class="content-text left-img" v-if="active === 'winning'">

                <div class="img-row">
                  <img class="winning-img" src="/tutorial/winning-tutorial.png"
                    alt="Meeple standing infront of the goal." />
                </div>

              </div>
              <div class="content-text left-img" v-else-if="active === 'jump'">

                <div class="img-row">
                  <img class="energy-bar-img" src="/tutorial/energy-bar-empty-tutorial.png" alt="Empty energy bar" />
                  <img class="energy-button-img" src="/tutorial/buttons-energy-active-tutorial.png"
                    alt="Energy button active - Jump inactive" />
                </div>

                <div class="img-row">
                  <img class="energy-bar-img" src="/tutorial/energy-bar-full-tutorial.png" alt="Full energy bar" />
                  <img class="energy-button-img" src="/tutorial/buttons-jump-active-tutorial.png"
                    alt="Jump button active - Energy inactive" />
                </div>

              </div>

              <div class="content-text left-img" v-else-if="active === 'duel'">
                
                <div class="img-row">
                  <img class="duel-img" src="/tutorial/meeple-duel-tutorial.png"
                    alt="2 Meeple in a duel." />
                </div>

              </div>

              <div class="content-text left-img" v-else-if="active === 'dice'">

                <div class="img-row">
                  <img class="dice-img" src="/tutorial/dice-button-tutorial.png"
                    alt="Button/UI for rolling the dice." />
                </div>


                <div class="img-row">
                  <img class="dice-img" src="/tutorial/dice-button-cooldown-tutorial.png"
                    alt="Button/UI for rolling the dice on cooldown." />

                </div>
              </div>

              <div class="content-text" v-else-if="active === 'controls'">

                <div class="controls-columns">

                  <!-- Reihe 1 -->
                  <div class="controls-row">
                    <div class="keyboard-layout-move">
                      <div class="key key-w">W</div>
                      <div class="key key-a">A</div>
                      <div class="key key-s">S</div>
                      <div class="key key-d">D</div>
                    </div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_MOVE_BUTTONS') }}</p>
                  </div>

                  <!-- Reihe 2 -->
                  <div class="controls-row">
                    <div class="key key-space">{{ tUI('SPACE') }}</div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_JUMP_BUTTON') }}</p>
                  </div>

                  <!-- Reihe 3 -->
                  <div class="controls-row">
                    <div class="key">R</div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_ROLL_DICE_BUTTON') }}</p>
                  </div>

                  <!-- Reihe 4 -->
                  <div class="controls-row">
                    <div class="key">E</div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_SAVE_ENERGY_BUTTON') }}</p>
                  </div>

                </div>

              </div>
            </div>

            <div class="column right">
              <div class="content-text" v-if="active === 'winning'">
                <p>{{ tUI('WINNING_TUTORIAL') }}</p>
              </div>
              <div class="content-text" v-else-if="active === 'jump'">
                <p>{{ tUI('JUMPING_TUTORIAL_1') }}</p>
                <p>{{ tUI('JUMPING_TUTORIAL_2') }}</p>
                <p>{{ tUI('JUMPING_TUTORIAL_3') }}</p>
              </div>
              <div class="content-text" v-else-if="active === 'duel'">
                <p>{{ tUI('DUEL_TUTORIAL_1') }}</p>
                <p>{{ tUI('DUEL_TUTORIAL_2') }}</p>
                <p>{{ tUI('DUEL_TUTORIAL_3') }}</p>
                <p>{{ tUI('DUEL_TUTORIAL_4') }}</p>
              </div>
              <div class="content-text" v-else-if="active === 'dice'">
                <p>{{ tUI('ROLL_DICE_TUTORIAL_1') }}</p>
                <p>{{ tUI('ROLL_DICE_TUTORIAL_2') }}</p>
                <p>{{ tUI('ROLL_DICE_TUTORIAL_3') }}</p>
              </div>
              <div class="content-text" v-else-if="active === 'controls'">

                <div class="controls-columns">

                  <!-- Reihe 1 -->
                  <div class="controls-row right">
                    <div class="keyboard-layout">
                      <div class="key">1</div>
                      <div class="key">2</div>
                      <div class="key">3</div>
                      <div class="key">4</div>
                      <div class="key">5</div>
                    </div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_MEEPLE_NUMBER_BUTTONS') }}</p>
                  </div>

                  <!-- Reihe 2 -->
                  <div class="controls-row right">
                    <div class="key key-tab">TAB</div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_MEEPLE_TAB') }}</p>
                  </div>

                  <!-- Reihe 3 -->
                  <div class="controls-row right">

                    <div class="keyboard-layout">
                      <div class="key key-shift">SHIFT</div>
                      <div class="key key-tab">TAB</div>
                    </div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_MEEPLE_SHIFT_TAB') }}</p>
                  </div>

                  <!-- Reihe 4 -->
                  <div class="controls-row right">

                    <div class="keyboard-layout">
                      <div class="key">2 x</div>
                      <div class="key">ESC</div>
                    </div>
                    <p>{{ tUI('CONTROLS_TUTORIAL_MENU') }}</p>
                  </div>
                </div>

              </div>
            </div>

          </div>
        </transition>
      </div>

    </div>

    <!-- Rechte Spalte -->
    <button class="menu-cycle-button right" @click="next()" @mouseenter="onHover">&gt;</button>

  </div>


</template>

<style scoped>
.popup {
  display: flex;
  flex-direction: row;
  height: 60vh;
  width: 80vw;
  text-align: center;
  position: relative;
  justify-content: flex-start;
  align-items: stretch;

  animation: fadeIn 0.3s ease;

  background:
    linear-gradient(rgba(40, 60, 35, 0.92),
      rgba(25, 40, 25, 0.92));

  box-shadow:
    inset 0 0 0 2px rgba(255, 255, 255, 0.06),
    0 12px 30px rgba(162, 133, 133, 0.45);

  border: 3px solid rgba(120, 160, 110, 0.25);
  border-radius: 18px;

  z-index: 2;
}

.menu-cycle-button {
  background: none;
  border: none;
  color: white;

  width: 4vw;
  min-width: 40px;

  font-size: 5vh;
  font-weight: bold;
  cursor: pointer;

  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-cycle-button:hover {
  background: rgba(255, 255, 255, 0.05);
}

.center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.menu-bar {
  display: flex;
  width: 100%;
  margin: 2vh;

  background: #3D7739;
  border: none;
  border-radius: 18px;
}

.menu-item {
  flex: 1;
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;

  padding: 0.4vw;
  font-size: 3.5vh;
}

.menu-item.active {
  background: #57AA51;
}

.menu-item:not(.active):hover {
  background: rgba(255, 255, 255, 0.05);
}

.menu-item.active:first-child {
  border-radius: 18px 0 0 18px;
}

.menu-item.active:last-child {
  border-radius: 0 18px 18px 0;
}

.tutorial-content {
  width: 98%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.content-columns {
  width: 100%;
  height: 100%;

  display: flex;
  gap: 3vw;
  align-items: center;
}

.content-columns .column {
  height: 100%;
  display: flex;
  justify-content: center;

  padding-top: 3vh;
}

.content-columns .left {
  flex: 1;
}

.content-columns .right {
  flex: 1.75;
}

.content-columns.controls-active {
  gap: 0vw;
}

.content-columns.controls-active .left,
.content-columns.controls-active .right {
  flex: 1;
  gap: 1vw;
}

.content-text p {
  font-family: "AcmeFont", sans-serif;
  color: white;

  font-size: 3vh;
  line-height: 1.4;
  text-align: left;

  padding-bottom: 2vh;
}

.content-text.left-img {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.content-text.left-img .img-row {
  justify-content: center;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

img {
  border-radius: 12px;
}

.img-row {
  display: flex;
  flex-direction: row;
  padding-bottom: 5vh;
  gap: 1vw;
}

.img-row .energy-bar-img,
.img-row .energy-button-img {
  width: auto;
}

.energy-bar-img {
  height: 80%;
  max-width: 65%;
  border-radius: 15px;
}

.energy-button-img {
  height: 100%;
  max-width: 30%;
  
  border-radius: 6px;
}

.img-row .dice-img {
  width: 40%;
  height: auto;
}

.img-row .winning-img {
  width: 100%;
  height: auto;
}

.img-row .duel-img {
  width: 100%;
  height: auto;
}

.controls-columns {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.controls-row {
  display: grid;
  grid-template-columns: minmax(14vw, auto) 1fr;
  align-items: start;
  gap: 2vw;
  padding-bottom: 2vh;
}

.controls-row.right {
  grid-template-columns: 18vw 1fr; 
}

.controls-row p {
  margin: 0;
  display: flex;
  align-items: center;
  height: 100%;
  text-align: left;
  padding-top: 1vh;
}

.controls-row > :first-child {
  align-self: start;
  justify-self: end;
}

.keyboard-layout,
.keyboard-layout-move {
  justify-content: start;
  margin-bottom: 0;
}

.keyboard-layout {
  display: flex;
  flex-direction: row;
  justify-content: start;
  gap: 1vh;
}

.keyboard-layout-move {
  display: grid;
  grid-template-areas:
    ". w ."
    "a s d";
  grid-gap: 1vh;
  justify-content: start;
}

.key {
  width: 3vw;
  height: 3vw;
  border: 3px solid #ccc;
  border-radius: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  font-family: "AcmeFont", sans-serif;
  font-size: 3vh;
  background-color: #f0f0f0;
}

.key-space {
  width: 10vw;
}

.key-shift {
  width: 6vw;
}

.key-tab {
  width: 4vw;
}

.key-w {
  grid-area: w;
}

.key-a {
  grid-area: a;
}

.key-s {
  grid-area: s;
}

.key-d {
  grid-area: d;
}
</style>
