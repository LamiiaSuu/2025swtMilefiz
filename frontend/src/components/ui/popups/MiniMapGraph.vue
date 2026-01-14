<script setup lang="ts">
import { computed, ref, onMounted, nextTick, watch } from "vue"
import { useMilefizStore } from "@/stores/milefizstore";
import type { IBoardDTD } from "@/stores/IBoardDTD";


const milefizStore = useMilefizStore()

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

// Zwei Ansichten: 'overview' und 'detail'
type ViewMode = 'OVERVIEW' | 'DETAIL'
const viewMode = ref<ViewMode>('OVERVIEW')

// Zoom-Faktoren
const ZOOM_FACTOR = 1.0
const zoom = ref(ZOOM_FACTOR)

// Pan-State
const panX = ref(0)
const panY = ref(0)

// Drag-State
const isDragging = ref(false)
let dragStartX = 0
let dragStartY = 0
let dragStartPanX = 0
let dragStartPanY = 0
let isDragMoved = false

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
  if (viewMode.value === 'OVERVIEW') {
    // In Übersicht: Kein Pan, nur Zoom
    return `scale(${zoom.value})`
  } else {
    // In Detail: Pan + Zoom
    return `translate(${panX.value} ${panY.value}) scale(${zoom.value})`
  }
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
  // currentField aus dem milefizStore holen
  const currentFieldId = milefizStore.gamedata.currentField
  
  if (!currentFieldId) return undefined
  
  // Feld-Objekt anhand der ID finden
  return props.board.fields.find(f => f.id === currentFieldId)
})

/**
 * Liefert das aktuell ausgewählte Feld
 * @return {IBoardDTD['fields'][number] | undefined}
 */
const selectedField = computed(() => {
  if (!props.selectedFieldId) return undefined
  return fieldById[props.selectedFieldId]
})

// VIEW MANAGEMENT
/**
 * Wechselt zur Übersichtsansicht (gesamte Map)
 */
function showOverview() {
  viewMode.value = 'OVERVIEW'
  zoom.value = ZOOM_FACTOR
  panX.value = 0
  panY.value = 0
}


/**
 * Berechnet den optimalen Detail-Zoom basierend auf der Map-Größe
 */
const computedDetailZoom = computed(() => {
  const svg = svgRef.value
  if (!svg) return 2.0

  const rect = svg.getBoundingClientRect()
  const viewW = svgSize.value.w
  const viewH = svgSize.value.h
  
  // Skalierung bei 100% Zoom
  const scaleAt100 = Math.min(rect.width / viewW, rect.height / viewH)
  
  // Node-Durchmesser in Pixeln bei 100% Zoom
  const nodePxAt100 = R * 2 * scaleAt100
  
  // Maximal erlaubter Zoom basierend auf Node-Größe (max 60px)
  const MAX_NODE_PX = 50
  const maxZoomByNodeSize = MAX_NODE_PX / nodePxAt100
  
  // Zoom für doppelte Größe der größeren Dimension
  const maxDimension = Math.max(viewW, viewH)
  // Berechne Zoom so, dass die größere Dimension etwa doppelt so groß dargestellt wird
  const baseZoom = Math.max(2.0, maxDimension / 500)
  const maxZoom = 10.0
  
  // Der kleinere Wert ist der limitierende Faktor
  const limitedZoom = Math.min(Math.min(baseZoom, maxZoom), maxZoomByNodeSize)
  
  // Mindestens 100% Zoom
  return Math.max(1.0, limitedZoom)
})

/**
 * Wechselt zur Detailansicht auf ein bestimmtes Feld
 */
function showDetail(field: IBoardDTD["fields"][number]) {
  viewMode.value = 'DETAIL'
  zoom.value = ZOOM_FACTOR * computedDetailZoom.value
  
  const viewW = svgSize.value.w
  const viewH = svgSize.value.h
  const fieldCx = cx(field.position.x)
  const fieldCy = cy(field.position.y)
  
  // Zentriere auf das Feld
  panX.value = viewW / 2 - fieldCx * ZOOM_FACTOR * computedDetailZoom.value
  panY.value = viewH / 2 - fieldCy * ZOOM_FACTOR * computedDetailZoom.value
}

/**
 * Wechselt zur Detailansicht auf das aktuelle Feld
 */
function showDetailOnCurrentField() {
  const field = currentField.value
  if (field) {
    showDetail(field)
  }
}



// INPUT HANDLER
function onMouseDown(e: MouseEvent) {
  // Nur in Detail-Ansicht darf gedraggt werden
  if (viewMode.value !== 'DETAIL') return
  if (e.button !== 0) return // Nur linke Maustaste

  e.preventDefault()

  const svg = e.currentTarget as SVGSVGElement
  
  // Startposition in Pixel
  dragStartX = e.clientX
  dragStartY = e.clientY
  dragStartPanX = panX.value
  dragStartPanY = panY.value
  isDragging.value = true
  isDragMoved = false
}

function onMouseMove(e: MouseEvent) {
  // Nur in Detail-Ansicht
  if (!isDragging.value || viewMode.value !== 'DETAIL') return

  const dx = e.clientX - dragStartX
  const dy = e.clientY - dragStartY

  // 1:1 Pixel-Drag
  const svg = svgRef.value
  if (!svg) return

  const rect = svg.getBoundingClientRect()
  const viewW = svgSize.value.w
  const viewH = svgSize.value.h

  const scale = (viewW + viewH) / (rect.width +rect.height) * 1.5

  panX.value = dragStartPanX + dx * scale
  panY.value = dragStartPanY + dy * scale

  // Setze isDragMoved wenn Maus bewegt wurde (Deadzone)
  const distance = Math.sqrt(dx * dx + dy * dy)
  if (distance > 3) { // 3 Pixel Deadzone
    isDragMoved = true
  }
}

function onMouseUp(e: MouseEvent) {
  if (!isDragging.value) return

  isDragging.value = false
  const svg = e.currentTarget as SVGSVGElement
  svg.style.userSelect = ''

  isDragMoved = false
}

function onMiddleClick(e: MouseEvent) {
  e.preventDefault()
  
  if (viewMode.value !== 'DETAIL') {
    // In Übersicht: Wechsel zu Detail auf aktuellem Feld
    showDetailOnCurrentField()
  } else {
    // In Detail: Wechsel zurück zu Übersicht
    showOverview()
  }
}

function onClickField(fieldId: string) {
  if (isDragMoved) {
    isDragMoved = false
    return
  }
  
  if (!isFree(fieldId)) return
  emit("select", fieldId)
}

function onRightClick(e: MouseEvent) {
  e.preventDefault()
  
  if(selectedField.value){
    showDetail(selectedField.value)
  }
}

// LIFE-CYCLE
onMounted(() => {
  nextTick(() => {
    const field = currentField.value
    if (field) {
        showDetail(field)
    }
  })
})
</script>

<template>
  <svg ref="svgRef" class="minimap-svg" :viewBox="`0 0 ${svgSize.w} ${svgSize.h}`" width="100%" height="100%"
    preserveAspectRatio="xMidYMid meet"  @mousedown="onMouseDown" @mousemove="onMouseMove"
    @mouseup="onMouseUp" @mouseleave="onMouseUp" @click.middle.stop.prevent="onMiddleClick" @click.right.stop.prevent="onRightClick">

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
