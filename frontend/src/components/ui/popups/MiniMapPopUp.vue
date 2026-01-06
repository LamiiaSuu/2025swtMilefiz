<script setup lang="ts">
import { computed, onMounted, onUnmounted } from "vue"

type Occupancy = "FREE" | "OCCUPIED" | "OWN_MEEPLE"

const props = defineProps<{
  isOpen: boolean
  selectedFieldId: string | null
  occupancyByFieldId: Record<string, Occupancy>
}>()

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
        <h2 class="title">Sperre umplatzieren</h2>
      </header>

      <section class="content">
        <!-- Map Bereich -->
        <div class="map-area">
          <div class="map-slot">
            <slot name="map" />
          </div>

          <!-- Legende unten links -->
          <aside class="legend">
            <div class="legend-row">
              <span class="legend-swatch swatch-barrier" aria-hidden="true"></span>
              <span class="legend-label">Sperre</span>
            </div>

            <div class="legend-row">
              <span class="legend-swatch swatch-own" aria-hidden="true"></span>
              <span class="legend-label">Eigene Figur</span>
            </div>

            <div class="legend-row">
              <span class="legend-swatch swatch-occupied" aria-hidden="true">X</span>
              <span class="legend-label">Besetzt</span>
            </div>

            <div class="legend-row">
              <span class="legend-swatch swatch-free" aria-hidden="true"></span>
              <span class="legend-label">frei</span>
            </div>
          </aside>
        </div>

        <!-- Bestätigen Button -->
        <div class="actions">
          <button type="button" class="btn primary" @click="console.log('CONFIRM CLICK'); emit('confirm')">
            Bestätigen
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
  background: #f7f7f7;
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
  overflow: hidden;
  width: min(520px, 100%);
  height: 340px;
  border: 1px solid rgba(0, 0, 0, 0.25);
  border-radius: 8px;
  background: #fff;
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

.legend-swatch {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 1px solid #111;
  display: grid;
  place-items: center;
  font-size: 11px;
  line-height: 1;
  font-weight: 700;
}

.swatch-barrier {
  background: #000;
  border-color: #000;
}

.swatch-own {
  background: #e11;
  border-color: #e11;
}

.swatch-occupied {
  background: #fff;
}

.swatch-free {
  background: #fff;
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
  cursor: not-allowed;
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
