<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { useMilefizStore } from '@/stores/milefizstore'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'

const milefizStore = useMilefizStore()
const { disconnectAndReset } = milefizStore

/**
 * Props:
 * - to -> Zielroute
 * - confirm -> optionales Bestätigungs-Popup
 * - confirmText -> anpassbarer Text im Popup
 */
const props = defineProps<{
  to: string | { name: string }
  confirm?: boolean
  confirmText?: string
}>()

const router = useRouter()
const audio = useAudioStore()

const showPopup = ref(false)

function goBack() {
  if (!props.to) return

  audio.playSfx('click')
  router.push(props.to)
  disconnectAndReset()
}

function handleClick() {
  if (props.confirm) {
    showPopup.value = true
  } else {
    goBack()
  }
}
</script>

<template>
  <button class="back-button" @click="handleClick">
    <slot>&lt; {{ tUI('BACK') }}</slot>
  </button>

  <!-- Popup -->
  <div v-if="showPopup" class="overlay">
    <div class="popup">
      <p class="message">
        {{ props.confirmText ?? tUI('BACK_TO_MAIN_MENU_CONFIRMATION') }}
      </p>

      <div class="actions">
        <button class="cancel" @click="showPopup = false">
          {{ tUI('CANCEL') }}
        </button>

        <button class="confirm" @click="goBack">
          {{ tUI('BACK_TO_MAIN_MENU') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.back-button {
  background: none;
  border: none;
  color: white;
  font-size: 4vh;
  font-weight: bold;
  cursor: pointer;
  font-family: "AcmeFont", sans-serif;
  transition: all 0.2s;
  -webkit-text-stroke: 6px black;
  text-shadow:
    2px 2px 4px rgba(0, 0, 0, 0.8),
    0 0 8px rgba(0, 0, 0, 0.5);
  paint-order: stroke fill;
}

.back-button:hover {
  transform: scale(1.05);
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.popup {
  background: #141414;
  padding: 28px 34px;
  border-radius: 14px;

  border: 1px solid rgba(76, 175, 80, .6);
  box-shadow:
    0 22px 45px rgba(0,0,0,.6),
    0 0 18px rgba(76,175,80,.25);

  max-width: 750px;       
  width: 100%;            
  margin: 0 auto;         

  animation: popupIn .18s ease-out;
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

.message {
  margin: 0 0 52px 0;
  font-family: "AcmeFont", sans-serif;
  font-size: 1.5rem;
  font-weight: bold;
  word-wrap: break-word;
  line-height: 1.35;
  
  color: #ffffff;
  text-align: center;

  text-shadow:
    0 2px 4px rgba(0,0,0,.6);
}

.actions {
  display: flex;
  justify-content: center;
  gap: 14px;
}

.cancel,
.confirm {
  font-family: "AcmeFont", sans-serif;
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-size: 1.25rem;
  font-weight: 600;
  transition: transform .12s ease, opacity .15s, background .15s;
}

.cancel {
  background: #3f3f3f;
  color: #eee;
  border: 1px solid rgba(255,255,255,.12);
}

.cancel:hover {
  transform: translateY(-1px);
  opacity: .95;
}

.confirm {
  background: #2e7d32; 
  border: 2px solid #4caf50;
  color: #ffffff;
}

.confirm:hover {
  transform: translateY(-1px);
  opacity: .95;
}
</style>
