<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore';
import { tUI } from "@/i18n";
import CountdownBar from "./CountdownBar.vue"
import { computed, onMounted, ref, watch, type VNodeRef } from 'vue';
import type { Player } from '@/types/lobbyupdate';

const props = defineProps<{
    duel: any // Duel-Objekt { duelId, firstMeeple, secondMeeple, targetField, miniGameId, miniGameName, miniGameType, timeout, state }
}>()

const emit = defineEmits<{
    (e: 'close'): void // Wird aufgerufen wenn das Spiel beendet ist
}>()

const inputRef = ref(); // referenz des input-felds

const inputValue = ref<number>(); // wert des input-felds

const isInputSend = ref<boolean>(false); // ob Eingabe-Wert abgesendet wurde

const termRepresentation = computed(() => props.duel.state?.termRepresentation) // String Representation des Rechenterms

const store = useMilefizStore()

/**
 * Gibt Array von {@link Player}, bei denen Player[0] immer der aktuelle Spieler ist
 */
const players = computed<(Player | undefined)[]>(() => {
    let players: (Player | undefined)[] = [];
    players.push(store.getOwnPlayer());
    store.gamedata.lobby?.players.forEach((player: Player) => {
        if (isPlayer1()) {
            if (player.id === props.duel.state?.player2) {
                players.push(player);
                return players;
            }
        } else {
            if (player.id === props.duel.state?.player1) {
                players.push(player);
                return players;
            }
        }
    })
    return players;
})

onMounted(() => {
    // initialisierung und abfrage des terms
    store.sendLobbyMessage(
        `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/math/term`,
        { id: store.gamedata.playerId }
    );

    doInputFocus(); // fokus
})

const preventNonNumeric = (e: KeyboardEvent) => {
  const allowedKeys = [ '0','1','2','3','4','5','6','7','8','9' ];

  if (allowedKeys.includes(e.key)) {
    return;
  }
  e.preventDefault();
}

const sendOnEnter = (e: KeyboardEvent) => {
      if (e.key === "Enter") {
        sendInput();
        return;
    }
    preventNonNumeric(e);
}

const doInputFocus = () => {
    inputRef.value?.focus();
};

/**
 * Prüft ob aktueller Spieler gewonnen hat
 */
function isWinner() {
    return props.duel.state?.winner === store.gamedata.playerId
}


/**
 * Prüft ob aktueller Spieler player1 ist.
 * Wichtig für konsistente Anzeige in der UI
 */
const isPlayer1 = () => {
    return props.duel.state?.player1 === store.gamedata.playerId
}


/**
 * Sendet Input-Wert zur Verarbeitung an Backend
 */
const sendInput = () => {
    isInputSend.value = true;
    store.sendLobbyMessage(
        `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/math/input`,
        { playerId: store.gamedata.playerId, input: inputValue.value }
    )
}

/**
 * Gibt den aktuellen Eingabe-Wert des Spielers aus dem Duel-Objekt aus
 */
const playerInput = () => (isPlayer1() ? props.duel.state?.p1Value : props.duel.state?.p2Value);


/**
 * Gibt den aktuellen Eingabe-Wert des Gegners aus dem Duell-Objekt aus
 */
const rivalInput = () => (isPlayer1() ? props.duel.state?.p2Value : props.duel.state?.p1Value);


/**
 * Validiert den Eingabe-Wert des Spielers gegen die richtige Lösung und gibt entsprechend einen String 'right'/'wrong' zurück.
 * WIchtig für Anzeige in UI.
 */
const validatePlayerInput = computed(() => {
    if (!props.duel.state?.finished) return '';
    if (playerInput() == null) return 'wrong';
    return (playerInput() == props.duel.state?.termValue) ? 'right' : 'wrong';
});


/**
 * Validiert den Eingabe-Wert des Gegners gegen die richtige Lösung und gibt entsprechend einen String 'right'/'wrong' zurück.
 * WIchtig für Anzeige in UI.
 */
const validateRivalInput = computed(() => {
    if (!props.duel.state?.finished) return '';
    if (rivalInput() == null) return 'wrong';
    return (rivalInput() == props.duel.state?.termValue) ? 'right' : 'wrong';
});


/**
 * Überprüft in Duel-Objekt ob Duel beendet
 */
const isFinished = computed(() => {
    return props.duel?.state?.finished ?? false
})

watch(isFinished, (finished) => {
    isInputSend.value = true;
    inputValue.value = playerInput();
    if (finished) {
        setTimeout(() => {
            emit('close')
        }, 2000) // schließt nach beenden des Minispiels das Fenster nach 2 Sekunden
    }
})

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
            <!-- Anzeige des Terms -->
            <div class="math-item-container">
                <div class="math-term-value align-right">
                    <div class="value-box">
                        {{ termRepresentation ?? "1 + 1" }}
                    </div>
                </div>
                <div class="math-term-value align-center"> = </div>
                <div class="math-term-value align-left">
                    <div class="value-box">
                        {{ duel.state?.termValue ?? "???" }}
                    </div>
                </div>
            </div>
            <!-- Spieler Eingabe und Wert -->
            <div class="math-item-container">
                <div class="math-user-name align-right" :style="{ color: players[0]?.color }">{{ players[0]?.playerName ?? 'Player1' }} ({{ tUI('MINIGAME_MATH_YOURSELF') }})</div>
                <div class="math-term-value align-center"> = </div>
                <div class="math-user-value">
                    <input class="value-box math-user-input" :class="validatePlayerInput" :disabled="isInputSend"
                        name="math-value" type="number" step="1" ref="inputRef" v-model="inputValue"  @keypress.stop="sendOnEnter" @keydown.stop="preventNonNumeric"/>
                </div>
            </div>
            <!-- Gegner Wert -->
            <div v-if="duel.state?.finished" class="math-item-container">
                <div class="math-user-name align-right"  :style="{ color: players[1]?.color }">{{ players[1]?.playerName ?? 'Player2' }}</div>
                <div class="math-term-value align-center"> = </div>
                <div class="math-user-value">
                    <div class="value-box" :class="validateRivalInput">{{ rivalInput() ?? '&nbsp;' }}</div>
                </div>
            </div>
            <!-- Button zum Validieren -->
            <div v-if="!duel.state?.finished" class="math-item-container">
                <div>
                    <button class="math-button" :disabled="isInputSend" @click="sendInput()">{{
                        tUI('MINIGAME_MATH_CHECK') }}</button>
                </div>
            </div>
            <!-- Anzeige des Gewinners -->
            <div v-if="duel.state?.finished" class="winner-big">
                <span v-if="isWinner()" class="winner-text">
                    {{ tUI('DUEL_WON') }}
                </span>

                <span v-else class="loser-text">
                    {{ tUI('DUEL_LOST') }}
                </span>
            </div>
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
    width: 100%;
    flex-direction: row;
    flex: 1;
    align-items: center;
}

.math-item-container>* {
    flex: 1;
    padding: 1rem;
    font-size: 1.1rem;
}

.math-item-container>.align-right {
    text-align: right;
}

.math-item-container>.align-left {
    text-align: left;
}

.math-item-container>.align-center {
    flex: 0;
    text-align: center;
}

.math-term-value-container,
.math-term-input-container {
    width: 100%;
}

.math-term-value {
    display: flex;
    flex-direction: row;
}

.value-box {
    padding: 10px 14px;
    border-radius: 8px;
    background-color: #a5a5a5;
    color: black;
    width: 100%;
    text-align: center;
}

.value-box.right {
    background-color: rgb(149, 204, 149) !important;
}

.value-box.wrong {
    background-color: rgb(214, 158, 158) !important;
}

.math-user-input {
    outline: none;
    border: 1px solid black;
    background-image: none;
    background-color: transparent;
    -webkit-box-shadow: none;
    -moz-box-shadow: none;
    box-shadow: none;
    width: 100%;
    background-color: aliceblue;
    color: black;
    font-size: 1.1rem;
}

.math-user-input:disabled {
    background-color: #a5a5a5;
}

.math-user-name {
        font-size: 1.4rem;
    font-weight: 900;
    margin: 6px 0 0 0;
    font-family: "Acme", sans-serif;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    text-shadow:
        0 0 1px rgba(0, 0, 0, .95),
        1px 0 1px rgba(0, 0, 0, .9),
        -1px 0 1px rgba(0, 0, 0, .9),
        0 1px 1px rgba(0, 0, 0, .9),
        0 -1px 1px rgba(0, 0, 0, .9);
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

.winner-big {
    margin-top: 18px;
    text-align: center;
    font-size: 1.8rem;
    font-weight: 900;
    font-family: "Acme", sans-serif;
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

input[type="number"] {
    -moz-appearance: textfield;
}
</style>