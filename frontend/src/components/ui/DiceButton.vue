<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";


onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});

const onRollDice = () => {
    //TODO
}

const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "t") {
        console.log("Würfeln angestoßen")
        document.getElementById("dice-button")?.classList.toggle("move")
        //onRollDice()
    }
}

const isBumping = ref(false);

function doBump() {
    isBumping.value = true;

    setTimeout(() => {
        isBumping.value = false;
    }, 500);
}

</script>
<template>
    <button :class="{ bump: isBumping }" id="dice-button" class="dice-button" type="button">
        <img src="@/assets/hud/diceButton.svg" class="dice-icon" alt="dice" />
    </button>
</template>

<style>
.dice-icon {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
    image-rendering: crisp-edges;
}

.dice-button {
    width: 100px;
    height: 100px;
    padding: 0;
    border: none;
    background: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

}

</style>