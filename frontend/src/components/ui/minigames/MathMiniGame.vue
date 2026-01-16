<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore';
import { tUI } from "@/i18n";
import CountdownBar from "./CountdownBar.vue"
import { computed, onMounted, ref, watch, type VNodeRef } from 'vue';

const props = defineProps<{
    duel: any // Duel-Objekt { duelId, firstMeeple, secondMeeple, targetField, miniGameId, miniGameName, miniGameType, timeout, state }
}>()

const emit = defineEmits<{
    (e: 'close'): void // Wird aufgerufen wenn das Spiel beendet ist
}>()

const inputRef = ref();

const inputValue = ref<number>();

const store = useMilefizStore()

onMounted(() => {
    store.sendLobbyMessage(
        `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/math/term`,
        { id: store.gamedata.playerId }
    )
})

const doInputFocus = () => {
  inputRef.value?.focus();
};

const termRepresentation = computed(() => props.duel.state?.termRepresentation)

function isWinner() {
  return props.duel.state?.winner === store.gamedata.playerId
}

const isPlayer1 = () => {
    return props.duel.state?.player1 === store.gamedata.playerId
}

const sendInput = () => {
    console.log("send input")
  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/math/input`,
    { playerId: store.gamedata.playerId,  input: inputValue.value }
  )
}

console.log(props.duel.state?.termRepresentation);

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
            <div class="math-item-container">
                <div class="math-term-value">
                    {{ termRepresentation }}
                </div>
                <div class="math-term-value">
                    <div v-if="duel.state?.finished">
                        {{ duel.state?.termValue }}
                    </div>
                </div>
            </div>
            <div class="math-item-container">
                <div class="math-user-name">Du</div>
                <div class="math-user-value">
                    <input class="math-user-input" name="math-value" type="number" ref="inputRef" v-model="inputValue" />
                    {{ isPlayer1() ? duel.state?.p1Value : duel.state?.p2Value }}
                </div>
            </div>
            <div class="math-item-container">
                <div class="math-user-name">anderer</div>
                <div class="math-user-value">
                    {{ isPlayer1() ? duel.state?.p2Value : duel.state?.p1Value }}
                </div>
            </div>
            <button @click="sendInput()">Send</button>
            <button @click="$emit('close')">close</button>
        </div>
    </div>
</template>
<style scoped>

.math-container {
    display: flex;
    flex-direction: column;
}

.math-item-container {
    display: flex;
    flex-direction: row;
    flex: 1;
}

.math-term-value-container, .math-term-input-container {
    width: 100%;
}

.math-user-input {
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