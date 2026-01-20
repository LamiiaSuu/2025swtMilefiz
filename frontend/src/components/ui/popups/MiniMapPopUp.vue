<script setup lang="ts">
import { computed, onMounted, onUnmounted } from "vue"
import { tUI } from '@/i18n'


const props = defineProps<{
  isOpen: boolean
  selectedFieldId: string | null
}>()

const disabled = computed(() => props.selectedFieldId === "")

const emit = defineEmits<{
  (e: "confirm"): void
}>()

function onKeyDown(e: KeyboardEvent) {
  if (!props.isOpen) return
  if (e.key === "Enter") {
    e.preventDefault()
    emit("confirm")
  }
}


onMounted(() => {
  window.addEventListener("keydown", onKeyDown)
})

onUnmounted(() => {
  window.removeEventListener("keydown", onKeyDown)
})
</script>


<template>
  <div v-if="props.isOpen" class="backdrop">
    <div class="dialog" role="dialog" aria-modal="true">
      <header class="header">
        <h2 class="title">{{ tUI('MINIMAP_BARRIER_MOVE') }}</h2>
      </header>

      <section class="content">
        <!-- Map Bereich -->
        <div class="map-area">
          <div class="map-slot">
            <slot name="map" />
          </div>

          <!-- Legende -->
          <aside class="legend">
            <div class="legend-row">
              <svg class="legend-icon legend-icon--barrier" viewBox="0 0 24 24" aria-hidden="true">
                <image href="/mapEditorIcons/barrier.png" x="4" y="4" width="16" height="16" />
              </svg>
              <span class="legend-label">{{ tUI('BARRIER') }}</span>
            </div>

            <div class="legend-row">
              <svg class="legend-icon legend-icon--own" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="12" r="8" class="legend-circle legend-circle--own" />
              </svg>
              <span class="legend-label">{{ tUI('MINIMAP_OWN_MEEPLE') }}</span>
            </div>

            <div class="legend-row">
              <svg class="legend-icon legend-icon--occupied" viewBox="0 0 24 24" aria-hidden="true">
                <!-- leerer Kreis -->
                <circle cx="12" cy="12" r="8" class="legend-circle legend-circle--occupied" />
                <!-- X -->
                <line x1="5" y1="5" x2="19" y2="19" class="legend-x-line" />
                <line x1="19" y1="5" x2="5" y2="19" class="legend-x-line" />
              </svg>
              <span class="legend-label">{{ tUI('MINIMAP_OCCUPIED') }}</span>
            </div>

            <div class="legend-row">
              <svg class="legend-icon legend-icon--free" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="12" r="8" class="legend-circle legend-circle--free" />
              </svg>
              <span class="legend-label">{{ tUI('MINIMAP_AVAILABLE') }}</span>
            </div>
          </aside>
        </div>

        <!-- Bestätigen Button -->
        <div class="actions">
          <button type="button" class="btn primary" :disabled="disabled" @click="emit('confirm')">
            {{ tUI('CONFIRM') }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: grid;
  place-items: center;
  padding: 24px;
  z-index: 9999;
  pointer-events: auto;
}

.dialog {
  position: relative;
  width: min(980px, 100%);
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.35);
  border-radius: 10px;
  box-shadow: 0 18px 60px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.header {
  padding: 18px 20px 10px 20px;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  text-align: center;
  letter-spacing: 0.2px;
}

.content {
  padding: 10px 20px 20px 20px;
}

.map-area {
  position: relative;
  padding: 12px 0 70px 0;
  display: flex;
  justify-content: center;
  z-index: 1;
  pointer-events: none;
}

.map-slot {
  position: relative;
  overflow: auto;
  width: min(520px, 100%);
  height: 340px;
  border: 1px solid rgba(0, 0, 0, 0.25);
  border-radius: 8px;
  background: rgba(255, 243, 226, 0.5);
  display: grid;
  place-items: center;
  z-index: 1;
  pointer-events: auto;
}

.map-slot :deep(svg) {
  display: block;
  width: 100%;
  height: 100%;
}

.legend {
  position: absolute;
  left: 0;
  bottom: 0;
  padding: 8px 0 0 0;
  display: grid;
  gap: 10px;
}

.legend-row {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 10px;
  align-items: center;
}

.legend-icon {
  padding: 0px;
  width: 18px;
  height: 18px;
  display: block;
}

.legend-circle {
  fill: #ffffff00;
  stroke: #111;
  stroke-width: 1.5;
}

.legend-circle--own {
  fill: var(--own-color, #e11);
  stroke: #000;
}



.legend-x-line {
  stroke: rgba(0, 0, 0);
  stroke-width: 2;
  stroke-linecap: round;
}

.legend-label {
  font-size: 13px;
  color: #111;
}


.actions {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  z-index: 1000;
}

.btn {
  width: min(300px, 100%);
  padding: 12px 18px;
  border-radius: 0;
  border: 1px solid #111;
  font-size: 14px;
  cursor: pointer;
}

.btn.primary {
  background: #234420;
  color: #fff;
}

.btn.primary:disabled {
  opacity: 0.5;
  pointer-events: none;
}


@media (max-width: 700px) {
  .map-area {
    padding-bottom: 12px;
  }

  .legend {
    position: static;
    margin-top: 12px;
  }

  .map-slot {
    height: 300px;
  }
}
</style>
