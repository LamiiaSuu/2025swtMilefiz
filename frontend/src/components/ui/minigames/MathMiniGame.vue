<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore';
import { tUI } from "@/i18n";
import CountdownBar from "./CountdownBar.vue"
import { ref, type VNodeRef } from 'vue';

const props = defineProps<{
    duel: any // Duel-Objekt { duelId, firstMeeple, secondMeeple, targetField, miniGameId, miniGameName, miniGameType, timeout, state }
}>()

const emit = defineEmits<{
    (e: 'close'): void // Wird aufgerufen wenn das Spiel beendet ist
}>()

const inputRef = ref();

const inputValue = ref<string>('')

const store = useMilefizStore()

const doInputFocus = () => {
  inputRef.value?.focus();
};

</script>
<template>
    <div class="no-select" tabindex="0" @focus="doInputFocus">
        <!-- Titel -->
        <h2 class="math-title">
            {{ tUI('MINIGAME_MATH_TITLE') }}
        </h2>

        <!-- COUNTDOWN -->
        <CountdownBar :seconds="duel.timeOut" />

        <div class="math-container">
            <div class="math-term-container">
                <div class="math-term-value-container">
                    <div class="math-term-value">
                        
                    </div>
                </div>
                <div class="math-term-input-container">
                    <input class="math-term-input" name="math-value" type="number" ref="inputRef" :value="inputValue"/>
                </div>
            </div>
        </div>
    </div>
</template>
<style scoped>

.math-container {
    display: flex;
    flex-direction: column;
}

.math-term-container {
    display: flex;
    flex-direction: row;
    flex: 1;
}

.math-term-value-container, .math-term-input-container {
    width: 100%;
}

.math-term-input {
    all: unset;
    padding: 1rem;
    background-color: aliceblue;
    color: black;
}

    .math-title {
  font-family: "Acme", sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
}

.math-button {
  font-family: "Acme", sans-serif;
  font-weight: 900;
  font-size: 1.6rem;

  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

.winner-text,
.loser-text {
  font-family: "Acme", sans-serif;
}

.no-select {
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;

  -webkit-user-drag: none;
}

        input::-webkit-outer-spin-button,
        input::-webkit-inner-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }

        /* Hide spin buttons in Firefox */
        input[type="number"] {
            -moz-appearance: textfield;
        }
</style>