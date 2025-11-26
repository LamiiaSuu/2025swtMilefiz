

<script setup lang="ts">
    import DiceButton from './DiceButton.vue'
</script>
<template>
    <div class="hud-container">
        <div class="bottom-right">
            <DiceButton/>
        </div>
    </div>
</template>
<style scoped>
.hud-container {
  position: fixed;   /* HUD über allem */
  inset: 0;          /* nimmt gesamte Fläche ein */
  pointer-events: none;  /* HUD blockiert Canvas nicht */
  z-index: 99999
}

.bottom-right {
  position: absolute;
  bottom: 20px;      /* Abstand vom unteren Rand */
  right: 20px;       /* Abstand vom rechten Rand */
  pointer-events: auto; /* Button bleibt klickbar */
}
</style>