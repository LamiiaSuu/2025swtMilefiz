<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useMilefizStore } from "@/stores/milefizstore";
import dice from "@/assets/hud/dice.png";

const milefizStore = useMilefizStore()

/* EventListener für Keyboard inputs*/
onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});


/* Würfeln Hotkey Mapping auf Key "R"*/
const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "r") {
        console.log("Würfeln angestoßen")
        rollDice()
    }
}


/* Würfel-Anfrage wird ans Backend geschickt */
function rollDice() {
    milefizStore.sendRollDice();
}




</script>
<template>
    <div class="action-button">
        <img src="@/assets/hud/dice.png" class="action-icon" />
        <span class="hotkey">R</span>
    </div>
</template>

<style>
.action-button {
    position: relative;
    padding: 5px;
    width: 70px;
    height: 70px;
    border: 2px solid #c8a25d;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    background: #234420;
}

.action-icon {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    image-rendering: crisp-edges;
}

.hotkey {
    position: absolute;
    bottom: -3px;
    left: 3px;
    font-size: 18px;
    font-weight: bold;
    color: #ffffff;
    border-radius: 3px;
}
</style>