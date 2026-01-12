<script setup lang="ts">
import { computed } from 'vue'
import { useMilefizStore } from '@/stores/milefizstore'

const { gamedata } = useMilefizStore()
// see NewGameView.vue

// Lobby Wrapper mit allen Lobby-Daten
const lobby = computed({
    get: () => gamedata?.lobby,
    set: (value) => {
        // Lobby wird üblicherweise nicht direkt gesetzt, aber für Vollständigkeit
        if (gamedata && value) {
            gamedata.lobby = value
        }
    }
})
</script>

<template>
    <!-- see NewGameView.vue -->
    <div class="players-list">
        <div v-for="(player, index) in lobby?.players" :key="index" class="player-item">
            <span class="player-color-dot" :style="{ backgroundColor: player.color }"></span>
            <span style="pointer-events: none">{{ player.playerName }}</span>
        </div>
    </div>
</template>

<style scoped>
.players-list {
    display: flex;
    flex-direction: column;
    gap: 5px;
    padding-top: 25px;
    padding-left: 25px;
    box-sizing: border-box;
}

.player-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 5px 0;
    font-size: 1.2vw;
    color: white;
    outline: none;
    -webkit-text-stroke: 6px black;
    text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.8), 0 0 10px rgba(0, 0, 0, 0.5);
    paint-order: stroke fill;
}

.player-color-dot {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    border: 2px solid rgba(0, 0, 0, 0.3);
}
</style>