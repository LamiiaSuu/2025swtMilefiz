<script setup lang="ts">
import { computed, ref, onMounted, nextTick } from "vue"
import type { IBoardDTD } from "@/stores/IBoardDTD";




type Occupancy = "FREE" | "OCCUPIED" | "OWN_MEEPLE" | "INVALID_END" | "INVALID_START"

const props = defineProps<{
  board: IBoardDTD
  occupancyByFieldId: Record<string, Occupancy>
  selectedFieldId: string | null
}>()

const currentField = computed(() => {
  const own = props.board.fields.find(f => isOwn(f.id))
  if (own) return own
})

function centerOnField(field: IBoardDTD["fields"][number], targetZoom:number) {
  const z = Math.max(MIN_ZOOM, Math.min(maxZoom.value, targetZoom))
  
  const viewW = svgSize.value.w
  const viewH = svgSize.value.h

  const fieldCx = cx(field.position.x)
  const fieldCy = cy(field.position.y)

  panX.value = viewW / 2 - fieldCx * z
  panY.value = viewH / 2 - fieldCy * z

  zoom.value = z
}

const INTIAL_ZOOM = 15.0
onMounted(async () => {
  await nextTick()

  const field = currentField.value
  if(!field) return

  centerOnField(field, INTIAL_ZOOM)
})

const svgRef = ref<SVGSVGElement | null>(null)

// Zoom-State
const zoom = ref(1)
const MIN_ZOOM = 1.0
const maxZoom = ref(3)
const ZOOM_STEP = 1.5

const panX = ref(0)
const panY = ref(0)

function recomputeMaxZoom() {
  const svg = svgRef.value
  if (!svg) return

  const rect = svg.getBoundingClientRect()

  const viewH = svgSize.value.h

  // Skala bei zoom = 1 (Pixel pro SVG-Einheit, hier Höhe als Basis)
  const scaleAt1 = rect.height / viewH

  // Wie groß darf ein Node-Radius maximal in Pixeln werden?
  const desiredMaxNodeRadiusPx = 20

  const maxZoomLocal = desiredMaxNodeRadiusPx / (R * scaleAt1)

  // nie kleiner als MIN_ZOOM
  maxZoom.value = Math.max(MIN_ZOOM, maxZoomLocal)
}

onMounted(() => {
  nextTick(() => {
    recomputeMaxZoom()
  })
})


function onWheel(e: WheelEvent) {
  e.preventDefault()

  const svg = e.currentTarget as SVGSVGElement | null
  if (!svg) return

  // deltaY > 0  => rauszoomen
  // deltaY <= 0 => reinzoomen
  const direction = e.deltaY > 0 ? -1 : 1

  let newZoom = zoom.value + direction * ZOOM_STEP

  if (newZoom < MIN_ZOOM) newZoom = MIN_ZOOM
  if (newZoom > maxZoom.value) newZoom = maxZoom.value

  if (newZoom === zoom.value) {
    return
  }

  // Sichtbare Größe des SVG
  const rect = svg.getBoundingClientRect()

  // viewBox-Größe
  const viewW = svgSize.value.w
  const viewH = svgSize.value.h

  if (direction > 0) {// ZOOM IN -> um Mausposition
    const mousePxX = e.clientX - rect.left
    const mousePxY = e.clientY - rect.top

    // Umrechnungsfaktoren von CSS-Pixeln -> viewBox-Koordinaten
    const scaleX = viewW / rect.width
    const scaleY = viewH / rect.height

    const mouseSvgX = mousePxX * scaleX
    const mouseSvgY = mousePxY * scaleY

    // Weltkoordinaten des Punkts unter der Maus vor dem Zoom
    const worldX = (mouseSvgX - panX.value) / zoom.value
    const worldY = (mouseSvgY - panY.value) / zoom.value

    // pan so anpassen, dass worldX/worldY nach dem neuen Zoom wieder unter der Maus liegen
    panX.value = mouseSvgX - worldX * newZoom
    panY.value = mouseSvgY - worldY * newZoom

  } else {// ZOOM OUT -> um tatsächliche Mitte des SVG-Graphen
   // Logisches Zentrum des Graphen in SVG-Koordinaten
    const centerSvgX = viewW / 2
    const centerSvgY = viewH / 2

    // Weltkoordinaten dieses Zentrums vor dem Zoom
    const worldCenterX = (centerSvgX - panX.value) / zoom.value
    const worldCenterY = (centerSvgY - panY.value) / zoom.value

    // pan so anpassen, dass dieses Zentrum in SVG-Koordinaten
    // am selben Ort relativ zum Graphen skaliert wird
    panX.value = centerSvgX - worldCenterX * newZoom
    panY.value = centerSvgY - worldCenterY * newZoom
  }

  // komplett rausgezoomt -> zurück zur initialen Zentrierung
  if (newZoom === MIN_ZOOM) {
    panX.value = 0
    panY.value = 0
  }

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
const SPACING = 100
const R = 80
const PADDING = 200

/**
 * Bounds: Board-Positionen als Layout-Hints
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
  <svg ref="svgRef" class="minimap-svg" :viewBox="`0 0 ${svgSize.w} ${svgSize.h}`" width="100%" height="100%"
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
  stroke: rgba(0, 0, 0);
  stroke-width: 10;
  stroke-linecap: round;
}


.node-circle {
  fill: #fff;
  stroke: rgba(0, 0, 0);
  stroke-width: 10;
}

/* OWN Meeple*/
.node-own {
  fill: var(--own-color, #e11);
  stroke: rgba(0, 0, 0);
  stroke-width: 2;
}

/* Selected Barrier */
.node-selected {
  fill: rgb(87, 40, 2);
  stroke: rgb(87, 40, 2);
  stroke-width: 2;
}

.x-line {
  stroke: rgba(0, 0, 0);
  stroke-width: 20;
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
