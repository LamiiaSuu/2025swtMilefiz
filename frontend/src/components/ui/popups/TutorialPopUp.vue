<script setup>
import { useRouter } from 'vue-router'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'
import LanguageSelection from '@/components/ui/LanguageSelection.vue'
import Tutorial from '../Tutorial.vue'
import PopUpCloseButton from './PopUpCloseButton.vue'

const router = useRouter()
const audio = useAudioStore()

</script>

<template>
    <div class="overlay">

        <!-- Close Button -->
        <PopUpCloseButton></PopUpCloseButton>

        <div class="content">
            <!-- Tutorial-->
            <Tutorial></Tutorial>
        </div>


    </div>
</template>

<style scoped>
.overlay {
    position: fixed;
    inset: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 99991;
    pointer-events: auto;
    flex-direction: column;

    background-color: rgba(0, 0, 0, 0.6);

    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
}

.content {
    margin-top: -10vh;
}
</style>