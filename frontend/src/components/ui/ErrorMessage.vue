<!-- frontend/src/components/ui/ErrorMessage.vue -->
<template>
  <!-- Normale Error/Info Box als pop up -->
  <Transition name="error-popup">
    <div v-if="errorState.show && errorState.type !== 'warning'" class="error-popup-inline">
        <div class="error-header" :class="`error-${errorState.type}`">
          <h3>{{ getErrorTitle() }}</h3>
        </div>
        
        <div class="error-content">
          <p>{{ errorState.message }}</p>
        </div>
    </div>
  </Transition>

  <!-- Warning als zentriertes Overlay -->
  <Transition name="warning-overlay">
    <div v-if="errorState.show && errorState.type === 'warning'" class="warning-text-overlay">
        {{ errorState.message }}
    </div>
  </Transition>
</template>


<script setup>
import { useErrorHandler } from '@/composables/useErrorHandler'

const { errorState, hideError } = useErrorHandler()

function getErrorTitle() {
  switch (errorState.type) {
    case 'error': return 'Fehler'
    case 'warning': return 'Warnung'
    case 'info': return 'Information'
    default: return 'Nachricht'
  }
}
</script>

<style scoped>
/* Normale Error/Info Box als pop up */
.error-popup-inline {
  background: linear-gradient(145deg, #2a2a2a, #1a1a1a);
  border: 2px solid #444;
  border-radius: 1vh;
  min-width: 25vw;
  max-width: 35vw;
  box-shadow: 0 1vh 3vh rgba(0, 0, 0, 0.8);
  font-family: "MainFont", sans-serif;
  overflow: hidden;
  position: relative;
  z-index: 999;
}

.error-header {
  padding: 2vh 3vw;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.error-header.error-error {
  background: linear-gradient(45deg, #d32f2f, #c62828);
}
.error-header.error-info {
  background: linear-gradient(45deg, #1976d2, #1565c0);
}

.error-header h3 {
  margin: 0;
  font-size: 3vh;
  font-weight: bold;
  text-shadow: 0 0.2vh 0.5vh rgba(0, 0, 0, 0.8);
}

.error-content {
  padding: 3vh 3vw;
  color: #e0e0e0;
  font-size: 2.5vh;
  line-height: 1.4;
  text-align: center;
}

/*Warning Overlay - Zentriert im Bildschirm */
.warning-text-overlay {
  position: fixed;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
  pointer-events: none;
  
  color: #f57c00;
  font-family: "MainFont", sans-serif;
  font-size: 4vh;
  font-weight: bold;
  text-align: center;
  
-webkit-text-stroke: 1px rgba(0, 0, 0, 0.8);


  
  max-width: 80vw;
  word-wrap: break-word;
  
  animation: warningPulse 2s ease-in-out infinite;
}

/*Animation für Warnings */
@keyframes warningPulse {
  0%, 100% { 
    opacity: 0.9; 
    transform: translate(-50%, -50%) scale(1); 
  }
  50% { 
    opacity: 1; 
    transform: translate(-50%, -50%) scale(1.05); 
  }
}

.warning-overlay-enter-active {
  transition: all 0.4s ease;
}

.warning-overlay-leave-active {
  transition: all 0.3s ease;
}

.warning-overlay-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.5);
}

.warning-overlay-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(1.2);
}

/* Error Box Animations */
.error-popup-enter-active {
  transition: all 0.3s ease;
}

.error-popup-leave-active {
  transition: all 0.2s ease;
}

.error-popup-enter-from {
  opacity: 0;
  transform: scale(0.8) translateX(-10vw);
}

.error-popup-leave-to {
  opacity: 0;
  transform: scale(0.9) translateX(-5vw);
}
</style>