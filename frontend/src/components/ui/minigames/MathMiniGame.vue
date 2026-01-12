<script setup lang="ts">
import { useMilefizStore } from '@/stores/milefizstore';
import { tUI } from "@/i18n";
import CountdownBar from "./CountdownBar.vue"

const props = defineProps<{
    duel: any // Duel-Objekt { duelId, firstMeeple, secondMeeple, targetField, miniGameId, miniGameName, miniGameType, timeout, state }
}>()

const emit = defineEmits<{
    (e: 'close'): void // Wird aufgerufen wenn das Spiel beendet ist
}>()

const store = useMilefizStore()

</script>
<template>
    <div class="dice-card no-select">
        <!-- Titel -->
        <h2 class="dice-title">
            {{ tUI('MINIGAME_MATH_TITLE') }}
        </h2>

        <!-- COUNTDOWN -->
        <CountdownBar :seconds="duel.timeOut" />
    </div>
</template>
<style scoped></style>