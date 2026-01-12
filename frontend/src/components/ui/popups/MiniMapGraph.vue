<script setup lang="ts">
import { computed, ref, onMounted, nextTick } from "vue"
import type { IBoardDTD } from "@/stores/IBoardDTD";

type Occupancy = "FREE" | "OCCUPIED" | "OWN_MEEPLE" | "INVALID_END" | "INVALID_START"

const props = defineProps<{
  board: IBoardDTD
  occupancyByFieldId: Record<string, Occupancy>
  selectedFieldId: string | null
}>()

const emit = defineEmits<{
  (e: "select", fieldId: string): void
}>()

const svgRef = ref<SVGSVGElement | null>(null)

// 
// STATES UND KONSTANTEN für zoom, pan und drag
// 

// Zoom-State
const zoom = ref(1)
const MIN_ZOOM = 1.0
const maxZoom = ref(3)
const ZOOM_STEP = 0.1
const INTIAL_ZOOM = 10.0

// Pan-State
const panX = ref(0)
const panY = ref(0)

// Drag-State
const isDragging = ref(false)
let lastMouseX = 0
let lastMouseY = 0
let dragMoved = false


// RENDER PARAMETER
// - SPACING bestimmt Abstand zwischen Nodes
// - R bestimmt Kreisradius
// - PADDING sorgt für Rand im SVG

const SPACING = 100
const R = 80
const PADDING = 200



// BOARD GEOMETRIE / LAYOUT

/**
 * Berechnet min/max Grenzen aller Feldkoordinaten.
 * @returns {{minX:number, maxX:number, minY:number, maxY:number}}
 */

const bounds = computed(() => {
  const xs = props.board.fields.map(f => f.position?.x).filter((n): n is number => isFinite(n as number))

  const ys = props.board.fields.map(f => f.position?.y).filter((n): n is number => isFinite(n as number))


  return {
    minX: Math.min(...xs),
    maxX: Math.max(...xs),
    minY: Math.min(...ys),
    maxY: Math.max(...ys),
  }
})

/**
 * Wandelt Board-X in SVG-X um (mit Spiegelung + Padding + Spacing).
 * @param x Board-Koordinate X
 * @returns {number}
 */
function cx(x: number) {
  // X-Achse spiegeln
  return PADDING + (bounds.value.maxX - x) * SPACING
}

/**
 * Wandelt Board-Y in SVG-Y um (mit Spiegelung + Padding + Spacing).
 * @param y Board-Koordinate Y
 * @returns {number}
 */
function cy(y: number) {
  // Y-Achse spiegeln
  return PADDING + (bounds.value.maxY - y) * SPACING
}

/**
 * Berechnet die ViewBox-Gesamtgröße der MiniMap.
 * @returns {{w:number, h:number}}
 */
const svgSize = computed(() => {
  const w = (bounds.value.maxX - bounds.value.minX) * SPACING + PADDING * 2
  const h = (bounds.value.maxY - bounds.value.minY) * SPACING + PADDING * 2
  // Minimum, damit bei 1 Feld nicht 0x0 entsteht
  return { w: Math.max(w, 2 * (PADDING + R)), h: Math.max(h, 2 * (PADDING + R)) }
})

/**
 * Erstellt ein Lookup-Objekt für Spielfelder anhand ihrer IDs.
 */
const fieldById: Record<string, IBoardDTD["fields"][number]> = {}
for (const f of props.board.fields) {
  fieldById[f.id] = f
}



/**
 * Liefert die kombinierte SVG-Transformation (translate + scale).
 * @returns {string}
 */
const transform = computed(() => {
  return `translate(${panX.value} ${panY.value}) scale(${zoom.value})`
})

// 
// OCCUPANCY + SELECTION HELPERS
// 

/**
 * Gibt die Occupancy eines Feldes zurück.
 * @param fieldId ID des Feldes
 * @returns {Occupancy}
 */
function occ(fieldId: string): Occupancy {
  return props.occupancyByFieldId[fieldId] ?? "FREE"
}

/**
 * Prüft ob ein Feld auswählbar ist.
 * @param fieldId ID des Feldes
 * @returns {boolean}
 */
function isFree(fieldId: string) {
  return occ(fieldId) === "FREE"
}


// Zustände:
// - OWN_MEEPLE: in Meeple Farbe gefärbter Kreis
// - SELECTED (Barriere-Ziel): schwarzer gefüllter Kreis
// - OCCUPIED: leerer Kreis + X
// - FREE: leerer Kreis
 
/**
 * Prüft ob ein Feld als besetzt markiert ist.
 * @param fieldId ID des Feldes
 * @returns {boolean}
 */  
function isOccupied(fieldId: string) {
  return occ(fieldId) === "OCCUPIED"
}

/**
 * Prüft ob sich ein eigenes Meeple auf dem Feld befindet.
 * @param fieldId ID des Feldes
 * @returns {boolean}
 */
function isOwn(fieldId: string) {
  return occ(fieldId) === "OWN_MEEPLE"
}

/**
 * Prüft ob das Feld aktuell ausgewählt ist.
 * @param fieldId ID des Feldes
 * @returns {boolean}
 */
function isSelected(fieldId: string) {
  return props.selectedFieldId === fieldId
}

/**
 * Prüft ob das Feld ein Startfeld ist.
 * @param fieldId ID des Feldes
 * @returns {boolean}
 */
function isInvalidStart(fieldId: string) {
  return occ(fieldId) === "INVALID_START"
}

/**
 * Prüft ob das Feld ein Zielfeld ist.
 * @param fieldId ID des Feldes
 * @returns {boolean}
 */
function isInvalidEnd(fieldId: string) {
  return occ(fieldId) === "INVALID_END"
}

/**
 * Liefert das Feld auf dem das eigene aktive Meeple steht.
 * @returns {IBoardDTD['fields'][number] | undefined}
 */
const currentField = computed(() => {
  const own = props.board.fields.find(f => isOwn(f.id))
  if (own) return own
})

// 
// CENTERING / ZOOM-BERECHNUNG
// 

/**
 * Zentriert die MiniMap so, dass das Feld mittig dargestellt wird.
 * @param field Feld das fokussiert werden soll
 * @param targetZoom Gewünschter Zoomfaktor
 */
function centerOnField(field: IBoardDTD["fields"][number], targetZoom: number) {
  const z = Math.max(MIN_ZOOM, Math.min(maxZoom.value, targetZoom))

  const viewW = svgSize.value.w
  const viewH = svgSize.value.h

  const fieldCx = cx(field.position.x)
  const fieldCy = cy(field.position.y)

  panX.value = viewW / 2 - fieldCx * z
  panY.value = viewH / 2 - fieldCy * z

  zoom.value = z
}

/**
 * Berechnet das maximale Zoom-Level basierend auf Node-Pixeldimensionen.
 */
function recomputeMaxZoom() {
  const svg = svgRef.value
  if (!svg) return

  const rect = svg.getBoundingClientRect()

  const viewH = svgSize.value.h

  const scaleAt1 = rect.height / viewH

  const desiredMaxNodeRadiusPx = 20

  const maxZoomLocal = desiredMaxNodeRadiusPx / (R * scaleAt1)

  maxZoom.value = Math.max(MIN_ZOOM, maxZoomLocal)
}


// 
// LIFE-CYLCE
//

onMounted(() => {
  nextTick(() => {
    recomputeMaxZoom()

    const field = currentField.value
    field && centerOnField(field, INTIAL_ZOOM)
  })
})

// 
// INPUT HANDLER
// 

/**
 * Zoomt in/aus der MiniMap um Mausposition oder Kartenmitte.
 * @param e WheelEvent
 */
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

    const scaleX = viewW / rect.width
    const scaleY = viewH / rect.height

    const mouseSvgX = mousePxX * scaleX
    const mouseSvgY = mousePxY * scaleY

    const worldX = (mouseSvgX - panX.value) / zoom.value
    const worldY = (mouseSvgY - panY.value) / zoom.value

    panX.value = mouseSvgX - worldX * newZoom
    panY.value = mouseSvgY - worldY * newZoom

  } else {// ZOOM OUT -> um tatsächliche Mitte des SVG-Graphen
    const centerSvgX = viewW / 2
    const centerSvgY = viewH / 2

    const worldCenterX = (centerSvgX - panX.value) / zoom.value
    const worldCenterY = (centerSvgY - panY.value) / zoom.value

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

/**
 * Startet einen Drag-Vorgang bei linker Maustaste.
 * @param e MouseEvent
 */
function onMouseDown(e: MouseEvent) {
  if (e.button !== 0) return //linke Maustaste

  e.preventDefault()

  isDragging.value = true
  dragMoved = false
  lastMouseX = e.clientX
  lastMouseY = e.clientY
}

/**
 * Verschiebt die MiniMap proportional zur Mausbewegung.
 * @param e MouseEvent
 */
function onMouseMove(e: MouseEvent) {
  if (!isDragging.value) return

  const svg = svgRef.value
  if (!svg) return

  const rect = svg.getBoundingClientRect()
  const viewW = svgSize.value.w
  const viewH = svgSize.value.h

  const dxPx = e.clientX - lastMouseX
  const dyPx = e.clientY - lastMouseY

  lastMouseX = e.clientX
  lastMouseY = e.clientY

  const distanceSq = dxPx * dxPx + dyPx * dyPx
  if (distanceSq < 3 * 3) {
    return
  }

  dragMoved = true

  const scale = Math.min(rect.width / viewW, rect.height / viewH)
  // Faktor, damit sich der Inhalt 1:1 zur Maus in Pixeln bewegt
  const factor = 3 / (scale * zoom.value)

  panX.value += dxPx * factor
  panY.value += dyPx * factor
}

/**
 * Beendet einen aktiven Drag-Vorgang.
 */
function onMouseUp() {
  isDragging.value = false
}

function onMiddleClick(e: MouseEvent) {
  e.preventDefault()

  const field = currentField.value
  if(!field) return

  centerOnField(field, INTIAL_ZOOM)
}


/**
 * Selektiert ein Feld, wenn kein Drag stattgefunden hat.
 * @param fieldId ID des geklickten Feldes
 */
function onClickField(fieldId: string) {
  if (dragMoved) {
    dragMoved = false
    return
  }
  if (!isFree(fieldId)) return
  emit("select", fieldId)
  console.log("Selected Field: " + fieldId)
}


</script>

<template>
  <svg ref="svgRef" class="minimap-svg" :viewBox="`0 0 ${svgSize.w} ${svgSize.h}`" width="100%" height="100%"
    preserveAspectRatio="xMidYMid meet" @wheel.prevent="onWheel" @mousedown="onMouseDown" @mousemove="onMouseMove"
    @mouseup="onMouseUp" @mouseleave="onMouseUp" @click.middle.stop.prevent="onMiddleClick">

    <defs>
      <filter id="nodeShadow" x="-50%" y="-50%" width="200%" height="200%">
        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#000" flood-opacity="0.25" />
      </filter>
    </defs>

    <g :transform="transform">
      <!-- Edges: nur east + south zeichnen, um Duplikate zu vermeiden -->
      <g class="edges">
        <template v-for="f in props.board.fields" :key="f.id">
          <line v-if="f.east && fieldById[f.east]" :x1="cx(f.position.x)" :y1="cy(f.position.y)"
            :x2="cx(fieldById[f.east]!.position.x)" :y2="cy(fieldById[f.east]!.position.y)" class="edge" />
          <line v-if="f.south && fieldById[f.south]" :x1="cx(f.position.x)" :y1="cy(f.position.y)"
            :x2="cx(fieldById[f.south]!.position.x)" :y2="cy(fieldById[f.south]!.position.y)" class="edge" />
        </template>
      </g>

      <!-- Nodes -->
      <g class="nodes">
        <g v-for="f in props.board.fields" :key="f.id" class="node" :class="{
          clickable: isFree(f.id),
          // locked: !isFree(f.id),
        }">
          <!-- Grundkreis -->
          <circle :cx="cx(f.position.x)" :cy="cy(f.position.y)" :r="R" class="node-circle" filter="url(#nodeShadow)"
            @click="onClickField(f.id)" />

          <!-- OWN meeple-farbener Kreis -->
          <circle v-if="isOwn(f.id)" :cx="cx(f.position.x)" :cy="cy(f.position.y)" :r="R - 3" class="node-own" />

          <!-- SELECTED Barrier Icon -->
          <image v-else-if="isSelected(f.id)" href="/mapEditorIcons/barrier.png" :x="cx(f.position.x) - (R)"
            :y="cy(f.position.y) - (R)" :width="R * 2" :heigth="R * 2" />
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
  fill: rgb(154, 90, 37);
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
  stroke: #8b6f47;
  stroke-width: 20;
}
</style>
