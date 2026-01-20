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
        <img class="close-icon" src="@/assets/buttons/close_button_icon.png" alt="X" />
        <span class="close-hotkey">[ M ]</span>
    </button>
</template>

<style scoped>
.close-button {
    position: fixed;
    top: 2vh;
    right: 3vw;

    display: flex;
    flex-direction: column;
    align-items: center;
    gap: -1vh;
    justify-content: center;

    background: transparent;
    cursor: pointer;
    border: none;
    padding: 0;

    color: white;
    font-size: 4vh;
}


.close-icon {
    position: relative;
    width: 6vh;
    height: 6vh;

    filter: brightness(0) invert(1);
    /* flex-shrink: 0; */
}

.close-hotkey {
    font-size: 2vh;
    font-weight: bold;
    color: #ffffff;
    font-family: "AcmeFont", sans-serif;
    line-height: 1;
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
