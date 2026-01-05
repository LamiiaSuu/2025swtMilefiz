<script setup lang="ts">
import { computed } from "vue"
import type { IBoardDTD } from "@/stores/IBoardDTD";




type Occupancy = "FREE" | "OCCUPIED" | "OWN_MEEPLE"



const props = defineProps<{
  board: IBoardDTD
  occupancyByFieldId: Record<string, Occupancy>
  selectedFieldId: string | null
}>()

console.log("occupancy keys:", Object.keys(props.occupancyByFieldId).length)
console.log("first field id:", props.board.fields[0]?.id)
/* console.log("occupancy for first:", props.occupancyByFieldId[props.board.fields[0]?.id])
 */
const emit = defineEmits<{
  (e: "select", fieldId: string): void
}>()

/**
 * Render-Parameter
 * - SPACING bestimmt Abstand zwischen Nodes
 * - R bestimmt Kreisradius
 * - PADDING sorgt für Rand im SVG
 */
const SPACING = 70
const R = 20
const PADDING = 28

/**
 * Bounds: Board-Positionen als Layout-Hints
 * Negative Koordinaten sind erlaubt ->  normalisieren über minX/minY.
 */
const bounds = computed(() => {
  const xs = props.board.fields.map(f => f.position?.x).filter(n => Number.isFinite(n)) as number[]
  const ys = props.board.fields.map(f => f.position?.y).filter(n => Number.isFinite(n)) as number[]

  // Fallback, falls Daten kaputt/leer sind
  if (xs.length === 0 || ys.length === 0) {
    return { minX: 0, maxX: 0, minY: 0, maxY: 0 }
  }

  return {
    minX: Math.min(...xs),
    maxX: Math.max(...xs),
    minY: Math.min(...ys),
    maxY: Math.max(...ys),
  }
})

/**
 * Mappt Board-Koordinaten -> SVG-Koordinaten (Zentren der Kreise).
 */
function cx(x: number) {
  // X-Achse spiegeln
  return PADDING + (bounds.value.maxX - x) * SPACING
}

function cy(y: number) {
  // Y-Achse spiegeln
  return PADDING + (bounds.value.maxY - y) * SPACING
}


/**
 * viewBox-Größe: genug Raum für Kreise + padding
 */
const svgSize = computed(() => {
  const w = (bounds.value.maxX - bounds.value.minX) * SPACING + PADDING * 2
  const h = (bounds.value.maxY - bounds.value.minY) * SPACING + PADDING * 2
  // Minimum, damit bei 1 Feld nicht 0x0 entsteht
  return { w: Math.max(w, 2 * (PADDING + R)), h: Math.max(h, 2 * (PADDING + R)) }
})

/**
 * Lookup Map für Nachbarn (für Edge-Linien)
 */
const fieldById = computed(() => {
  const m = new Map<string, IBoardDTD["fields"][number]>()
  for (const f of props.board.fields) m.set(f.id, f)
  return m
})

function occ(fieldId: string): Occupancy {
  return props.occupancyByFieldId[fieldId] ?? "FREE"
}

/**
 * UI-Regel: nur FREE darf ausgewählt werden
 */
function isFree(fieldId: string) {
  return occ(fieldId) === "FREE"
}

function onClickField(fieldId: string) {
  if (!isFree(fieldId)) return
  emit("select", fieldId)
}

/**
 * Zustände:
 * - OWN_MEEPLE: roter gefüllter Kreis
 * - SELECTED (Barriere-Ziel): schwarzer gefüllter Kreis
 * - OCCUPIED: leerer Kreis + X
 * - FREE: leerer Kreis
 */
function isOccupied(fieldId: string) {
  return occ(fieldId) === "OCCUPIED"
}
function isOwn(fieldId: string) {
  return occ(fieldId) === "OWN_MEEPLE"
}
function isSelected(fieldId: string) {
  return props.selectedFieldId === fieldId
}
</script>

<template>
  <svg
    class="minimap-svg"
    :viewBox="`0 0 ${svgSize.w} ${svgSize.h}`"
    width="100%"
    height="100%"
    preserveAspectRatio="xMidYMid meet"
  >
    <!-- Drop Shadow Filter (leichter "Wireframe"-Look) -->
    <defs>
      <filter id="nodeShadow" x="-50%" y="-50%" width="200%" height="200%">
        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#000" flood-opacity="0.25" />
      </filter>
    </defs>

    <!-- Kanten: nur east + south zeichnen, um Duplikate zu vermeiden -->
    <g class="edges">
      <template v-for="f in props.board.fields" :key="f.id">
        <line
          v-if="f.east && fieldById.get(f.east)"
          :x1="cx(f.position.x)"
          :y1="cy(f.position.y)"
          :x2="cx(fieldById.get(f.east)!.position.x)"
          :y2="cy(fieldById.get(f.east)!.position.y)"
          class="edge"
        />
        <line
          v-if="f.south && fieldById.get(f.south)"
          :x1="cx(f.position.x)"
          :y1="cy(f.position.y)"
          :x2="cx(fieldById.get(f.south)!.position.x)"
          :y2="cy(fieldById.get(f.south)!.position.y)"
          class="edge"
        />
      </template>
    </g>

    <!-- Nodes -->
    <g class="nodes">
      <g
        v-for="f in props.board.fields"
        :key="f.id"
        class="node"
        :class="{
          clickable: isFree(f.id),
          locked: !isFree(f.id),
        }"
        @click="onClickField(f.id)"
      >
        <!-- Grundkreis (Outline) -->
        <circle
          :cx="cx(f.position.x)"
          :cy="cy(f.position.y)"
          :r="R"
          class="node-circle"
          filter="url(#nodeShadow)"
        />

        <!-- OWN (roter Kreis) -->
        <circle
          v-if="occ(f.id) ==='OWN_MEEPLE'"
          :cx="cx(f.position.x)"
          :cy="cy(f.position.y)"
          :r="R - 3"
          class="node-own"
        />

        <!-- SELECTED (schwarzer Kreis) -->
        <circle
          v-else-if="isSelected(f.id)"
          :cx="cx(f.position.x)"
          :cy="cy(f.position.y)"
          :r="R - 3"
          class="node-selected"
        />

        <!-- OCCUPIED (X im Kreis) -->
        <g v-else-if="occ(f.id) === 'OCCUPIED'" class="node-x">
          <line
            :x1="cx(f.position.x) - (R - 6)"
            :y1="cy(f.position.y) - (R - 6)"
            :x2="cx(f.position.x) + (R - 6)"
            :y2="cy(f.position.y) + (R - 6)"
            class="x-line"
          />
          <line
            :x1="cx(f.position.x) + (R - 6)"
            :y1="cy(f.position.y) - (R - 6)"
            :x2="cx(f.position.x) - (R - 6)"
            :y2="cy(f.position.y) + (R - 6)"
            class="x-line"
          />
        </g>
      </g>
    </g>
  </svg>
</template>

<style scoped>
.minimap-svg {
  display: block;
}


.edge {
  stroke: rgba(0, 0, 0, 0.6);
  stroke-width: 4;
  stroke-linecap: round;
}


.node-circle {
  fill: #fff;
  stroke: rgba(0, 0, 0, 0.85);
  stroke-width: 4;
}

/* OWN Meeple*/
.node-own {
  fill: #e11;
  stroke: rgba(0, 0, 0, 0.85);
  stroke-width: 2;
}

/* Selected Barrier */
.node-selected {
  fill: #000;
  stroke: rgba(0, 0, 0, 0.85);
  stroke-width: 2;
}

/* X Mark */
.x-line {
  stroke: rgba(0, 0, 0, 0.85);
  stroke-width: 4;
  stroke-linecap: round;
}

/* Interaktion */
.node.clickable {
  cursor: pointer;
}
.node.locked {
  cursor: not-allowed;
}

/* Hover nur für freie Felder */
.node.clickable:hover .node-circle {
  stroke-width: 6;
}
</style>
