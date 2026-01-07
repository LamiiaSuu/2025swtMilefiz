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
        <img class="close-icon" src="@/assets/buttons/close_button_icon.png" />
    </button>
</template>

<style scoped>
.close-button {
    position: absolute;
    margin-top: -40%;
    margin-right: -112.5%;
    display: flex;
    align-items: center; 

    background: transparent;
    cursor: pointer;
    border: none;
    padding: 0;

    
    color: white;
    font-size: 3vh;
}

.close-icon {
    width: 15%;
    height: 15%;
    filter: brightness(0) invert(1);
    aspect-ratio: 1 / 1;
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
