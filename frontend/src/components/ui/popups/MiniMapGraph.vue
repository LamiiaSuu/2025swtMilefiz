<script setup lang="ts">
import { computed, ref } from "vue"
import type { IBoardDTD } from "@/stores/IBoardDTD";




type Occupancy = "FREE" | "OCCUPIED" | "OWN_MEEPLE" | "INVALID_END" | "INVALID_START"



const props = defineProps<{
  board: IBoardDTD
  occupancyByFieldId: Record<string, Occupancy>
  selectedFieldId: string | null
}>()

// Zoom-State
const zoom = ref(1)
const MIN_ZOOM = 0.5
const MAX_ZOOM = 2.5
const ZOOM_STEP = 0.1

const panX = ref(0)
const panY = ref(0)

function onWheel(e: WheelEvent) {
  e.preventDefault()

  const svg = e.currentTarget as SVGSVGElement | null
  if (!svg) return
  
  // Zoomrichtung
  const direction = e.deltaY > 0 ? -1:1
  let newZoom = zoom.value + direction * ZOOM_STEP
  newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom))

  // Wenn am Limit
  if (newZoom === zoom.value){
    return // nichts tun
  }

  // Bounding Rectangle des SVG in CSS Pixeln
  const rect = svg.getBoundingClientRect()

  // Mauspostion relativ zum SVG in CSS-Pixeln
  const mousePxX = e.clientX - rect.left
  const mousePxY = e.clientY - rect.top

  // Umrechnung in SVG-Koordinaten (in viewbox-Einheiten)
  const scaleX = svgSize.value.w / rect.width
  const scaleY = svgSize.value.h / rect.height

  const mouseSvgX = mousePxX * scaleX
  const mouseSvgY = mousePxY * scaleY

  // Weltkoordinaten vor Zoom 
  const worldX = (mouseSvgX - panX.value) / zoom.value
  const worldY = (mouseSvgY - panY.value) / zoom.value

  // pan so anpassen, dass worldX/worldY nahc dem Zom wieder unter der Muas liegen
  panX.value = mouseSvgX - worldX * newZoom
  panY.value = mouseSvgY - worldY * newZoom

  // Zoom setzen
  zoom.value = newZoom


}

const transform = computed(() => {
  return `translate(${panX.value} ${panY.value}) scale(${zoom.value})`
})


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
  console.log("Selected Field: " + fieldId)
}

/**
 * Zustände:
 * - OWN_MEEPLE: in Meeple Farbe gefärbter Kreis
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
function isInvalidStart(fieldId: string) {
  return occ(fieldId) === "INVALID_START"
}
function isInvalidEnd(fieldId: string) {
  return occ(fieldId) === "INVALID_END"
}
</script>

<template>
  <svg class="minimap-svg" :viewBox="`0 0 ${svgSize.w} ${svgSize.h}`" width="100%" height="100%"
    preserveAspectRatio="xMidYMid meet" @wheel.prevent="onWheel">
    <!-- Drop Shadow Filter -->
    <defs>
      <filter id="nodeShadow" x="-50%" y="-50%" width="200%" height="200%">
        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#000" flood-opacity="0.25" />
      </filter>
    </defs>

    <g :transform="transform">
      <!-- Edges: nur east + south zeichnen, um Duplikate zu vermeiden -->
      <g class="edges">
        <template v-for="f in props.board.fields" :key="f.id">
          <line v-if="f.east && fieldById.get(f.east)" :x1="cx(f.position.x)" :y1="cy(f.position.y)"
            :x2="cx(fieldById.get(f.east)!.position.x)" :y2="cy(fieldById.get(f.east)!.position.y)" class="edge" />
          <line v-if="f.south && fieldById.get(f.south)" :x1="cx(f.position.x)" :y1="cy(f.position.y)"
            :x2="cx(fieldById.get(f.south)!.position.x)" :y2="cy(fieldById.get(f.south)!.position.y)" class="edge" />
        </template>
      </g>

      <!-- Nodes -->
      <g class="nodes">
        <g v-for="f in props.board.fields" :key="f.id" class="node" :class="{
          clickable: isFree(f.id),
          locked: !isFree(f.id),
        }">
          <!-- Grundkreis -->
          <circle :cx="cx(f.position.x)" :cy="cy(f.position.y)" :r="R" class="node-circle" filter="url(#nodeShadow)"
            @click="onClickField(f.id)" />

          <!-- OWN meeple-farbener Kreis -->
          <circle v-if="isOwn(f.id)" :cx="cx(f.position.x)" :cy="cy(f.position.y)" :r="R - 3" class="node-own" />

          <!-- SELECTED schwarzer Kreis)-->
          <circle v-else-if="isSelected(f.id)" :cx="cx(f.position.x)" :cy="cy(f.position.y)" :r="R - 3"
            class="node-selected" />


          <!-- OCCUPIED X -->
          <g v-else-if="isOccupied(f.id) && !isInvalidStart(f.id)" class="node-x">
            <line :x1="cx(f.position.x) - (R - 6)" :y1="cy(f.position.y) - (R - 6)" :x2="cx(f.position.x) + (R - 6)"
              :y2="cy(f.position.y) + (R - 6)" class="x-line" />
            <line :x1="cx(f.position.x) + (R - 6)" :y1="cy(f.position.y) - (R - 6)" :x2="cx(f.position.x) - (R - 6)"
              :y2="cy(f.position.y) + (R - 6)" class="x-line" />
          </g>
          <!-- INVALID_START -->
          <image v-else-if="isInvalidStart(f.id)" href="/mapEditorIcons/base.png" :x="cx(f.position.x) - (R)"
            :y="cy(f.position.y) - (R)" :width="R * 2" :heigth="R * 2" />

          <!-- INVALID_END -->



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
  fill: var(--own-color, #e11);
  stroke: rgba(0, 0, 0, 0.85);
  stroke-width: 2;
}

/* Selected Barrier */
.node-selected {
  fill: #000;
  stroke: rgba(0, 0, 0, 0.85);
  stroke-width: 2;
}

.x-line {
  stroke: rgba(0, 0, 0, 0.85);
  stroke-width: 8;
  stroke-linecap: round;
}

.node-circle {
  pointer-events: all;
}

.node.clickable .node-circle {
  cursor: pointer;
}

.node.locked .node-circle {
  cursor: not-allowed;
}

.node.clickable .node-circle:hover {
  stroke-width: 6;
}
</style>
