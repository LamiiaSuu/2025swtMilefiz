<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { useMilefizStore } from '@/stores/milefizstore'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'

const milefizStore = useMilefizStore()

const router = useRouter()
const audio = useAudioStore()

const showPopup = ref(false)

function closePopUps() {

    audio.playSfx('click')
    milefizStore.closePopUpMenu()
    milefizStore.closePopUpSettings()
    milefizStore.closePopUpTutorial()
}

</script>

<template>
    <!-- Close icons created by Rutmer Zijlstra - Flaticon: https://www.flaticon.com/free-icon/cross_9675141?term=close&page=2&position=64&origin=search&related_id=9675141 -->
    <button class="close-button" @click="closePopUps()">
        <slot></slot>
        <img class="close-icon" src="@/assets/buttons/close_button_icon.png" alt="X"/>
    </button>
</template>

<style scoped>
.close-button {
    position: fixed;
    top: 5vh;
    right: 1vw;

    display: flex;
    align-items: center;
    gap: -1vh;

    background: transparent;
    cursor: pointer;
    border: none;
    padding: 0;

    color: white;
    font-size: 4vh;
}


.close-icon {
    width: 10vh;
    height: 10vh;

    filter: brightness(0) invert(1);
    flex-shrink: 0;
}


@keyframes popupIn {
    from {
        opacity: 0;
        transform: scale(.93);
    }

    to {
        opacity: 1;
        transform: scale(1);
    }
}
</style>
