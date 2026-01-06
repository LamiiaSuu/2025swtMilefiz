<script lang="ts" setup>
import EditorHUD from '@/components/ui/mapEditor/EditorHUD.vue';
import EditorFileHUD from '@/components/ui/mapEditor/EditorFileHUD.vue';
import BackButton from '@/components/ui/pages/BackButton.vue'
import { ref, reactive, onMounted, onUnmounted, computed, provide } from 'vue'
import StandardTile from '@/components/ui/mapEditor/tiles/StandardTile.vue';
import { useErrorHandler } from '@/composables/useErrorHandler'
import { useAudioStore } from '@/stores/audioStore';
import type { IBoardDTD } from '@/stores/IBoardDTD'
import type { IFieldDTD } from '@/stores/IFieldDTD'
import { tUI } from '@/i18n'

/**
 * Richtungen im Editor (Grid bewegt sich in 2er-Schritten).
 */
type Direction = 'up' | 'down' | 'left' | 'right'

/**
 * Werkzeuge/Tile-Arten, die der Benutzer wählen kann.
 */
type ToolType = 'start' | 'goal' | 'tile' | 'barrier'

/** Aktuell ausgewähltes Tool */
const selectedTool = ref<'start' | 'goal' | 'tile' | 'barrier'>('tile')
const audio = useAudioStore()
const { showError, showWarning, showCriticalError, showSuccess } = useErrorHandler()

/**
 * Offset für Nachbar-Felder je Richtung.
 */
const DIR_OFFSET: Record<Direction, { x: number; y: number }> = {
  up: { x: 0, y: -2 },
  down: { x: 0, y: 2 },
  left: { x: -2, y: 0 },
  right: { x: 2, y: 0 },
}

/**
 * Liefert das aktuell ausgewählte Tile oder null.
 */
const selectedTile = computed<TileData | null>(() =>
  tiles.find(t => key(t.x, t.y) === selectedKey.value) ?? null
)

/** Repräsentiert ein einzelnes Tile im Editor. */
type TileData = {
  id: string
  type: ToolType
  x: number
  y: number
  connections: {
    up: boolean
    down: boolean
    left: boolean
    right: boolean
  }
}

type BoardExport = IBoardDTD & { id: string; name: string }

/**
 * Alle Tiles auf dem Board (reaktiv).
 */
const tiles = reactive<TileData[]>([
  {
    id: crypto.randomUUID(),
    type: 'tile',
    x: 0,
    y: 0,
    connections: { up: false, down: false, left: false, right: false }
  }
])

/** Key des aktuell ausgewählten Tiles */
const selectedKey = ref<string>('0,0')


/* Kamera */
/** Position des Canvas relativ zum Viewport */
const offset = reactive({ x: 0, y: 0 })
let dragging = false
let lastMouse = { x: 0, y: 0 }

/**
 * Startet Dragging.
 */
function onMouseDown(e: MouseEvent) {
  dragging = true
  lastMouse = { x: e.clientX, y: e.clientY }
}

/**
 * Verschiebt den Editor während Dragging.
 */
function onMouseMove(e: MouseEvent) {
  if (!dragging) return
  offset.x += e.clientX - lastMouse.x
  offset.y += e.clientY - lastMouse.y
  lastMouse = { x: e.clientX, y: e.clientY }
}

/**
 * Stoppt Dragging.
 */
function onMouseUp() {
  dragging = false
}

/**
 * Erstellt einen eindeutigen Key aus Koordinaten.
 */
function key(x: number, y: number) {
  return `${x},${y}`
}

/** Aktuelle Fenstergröße */
const viewport = ref({
  width: 0,
  height: 0,
})

/**
 * Aktualisiert die Viewport-Daten basierend auf Fenstergröße.
 */
function updateViewport() {
  viewport.value.width = window.innerWidth
  viewport.value.height = window.innerHeight
}

onMounted(() => {
  updateViewport()
  window.addEventListener('resize', updateViewport)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateViewport)
})


/**
 * Fügt ein neues Tile in der angegebenen Richtung an ein bestehendes Tile an.
 * Existiert dort bereits ein Tile, wird nur eine Verbindung hergestellt.
 *
 * @param fromTile Tile, von dem aus erweitert wird
 * @param dir Richtung, in die das Tile platziert werden soll
 */
function addTile(fromTile: TileData, dir: Direction) {
  const offset = {
    up: { x: 0, y: -2 },
    down: { x: 0, y: 2 },
    left: { x: -2, y: 0 },
    right: { x: 2, y: 0 }
  }[dir]

  const newX = fromTile.x + offset.x
  const newY = fromTile.y + offset.y


  const existing = tiles.find(t => t.x === newX && t.y === newY)
  if (existing) {

    connectTiles(fromTile, existing, dir)
    return
  }

  const newTile: TileData = {
    id: crypto.randomUUID(),
    type: selectedTool.value,
    x: newX,
    y: newY,
    connections: { up: false, down: false, left: false, right: false }
  }

  tiles.push(newTile)

  connectTiles(fromTile, newTile, dir)

  selectedKey.value = key(newX, newY)
}

/**
 * Sucht das Nachbar-Tile in der gewünschten Richtung.
 *
 * @param tile Ausgangs-Tile
 * @param dir Richtung, in der gesucht werden soll
 * @returns Gefundenes Tile oder null, falls keines existiert
 */
function findNeighbor(tile: TileData, dir: Direction): TileData | null {
  const off = DIR_OFFSET[dir]
  return tiles.find(t => t.x === tile.x + off.x && t.y === tile.y + off.y) ?? null
}

function onHudToolSelected(tool: ToolType) {
  if (!selectedTile.value) return
  selectedTile.value.type = tool
}

function onHover() {
  audio.playSfx('hover')
}

type BackendTile = IFieldDTD

/**
 * Findet die nächste verfügbare Startfarbe.
 * Rückgabe: START_RED, START_YELLOW, START_BLUE, START_GREEN oder NORMAL falls alle belegt.
 */
function getNextAvailableStartColor(usedColors: Set<string>): string {
  const colors = ["START_RED", "START_YELLOW", "START_BLUE", "START_GREEN"]
  for (const color of colors) {
    if (!usedColors.has(color)) {
      return color
    }
  }
  return "NORMAL"
}

/**
 * Konvertiert alle Editor-Tiles in das Backend-Format.
 * Dabei werden Verbindungen und Feldtypen korrekt abgebildet.
 *
 * @returns Liste von BackendTile-Objekten
 */
function exportTiles(): BackendTile[] {
  const usedStartColors = new Set<string>()

  return tiles.map(tile => {
    const north = tile.connections.up
      ? findNeighbor(tile, 'up')?.id ?? undefined
      : undefined

    const south = tile.connections.down
      ? findNeighbor(tile, 'down')?.id ?? undefined
      : undefined

    const west = tile.connections.left
      ? findNeighbor(tile, 'left')?.id ?? undefined
      : undefined

    const east = tile.connections.right
      ? findNeighbor(tile, 'right')?.id ?? undefined
      : undefined

    const fieldType = tile.type === 'start'
      ? getNextAvailableStartColor(usedStartColors)
      : tile.type === 'goal'
        ? 'END'
        : 'NORMAL'

    if (fieldType.startsWith("START_")) {
      usedStartColors.add(fieldType)
    }

    return {
      id: tile.id,
      north,
      east,
      south,
      west,
      type: fieldType,
      position: {
        x: tile.x,
        y: tile.y
      },
      barrier: tile.type === 'barrier'
    }
  })
}

function exportBoard(fields: BackendTile[]): BoardExport {
  return {
    id: crypto.randomUUID(),
    name: 'Board',
    fields,
    trees: []
  }
}

const OPPOSITE: Record<Direction, Direction> = {
  up: 'down',
  down: 'up',
  left: 'right',
  right: 'left',
}

function opposite(dir: Direction): Direction {
  return OPPOSITE[dir]
}

function connectTiles(a: TileData, b: TileData, dir: Direction) {
  a.connections[dir] = true
  b.connections[opposite(dir)] = true
}


/**
 * Löscht ein Tile und entfernt alle bestehenden Verbindungen
 * zu angrenzenden Tiles. Das Startfeld (0,0) kann nicht gelöscht werden.
 *
 * @param tile Tile, das gelöscht werden soll
 */
function deleteTile(tile: TileData) {

  if (tile.x === 0 && tile.y === 0) return

  (Object.keys(tile.connections) as Direction[]).forEach(dir => {
    if (!tile.connections[dir]) return

    const offset = DIR_OFFSET[dir]
    const nx = tile.x + offset.x
    const ny = tile.y + offset.y

    const neighbor = tiles.find(t => t.x === nx && t.y === ny)
    if (!neighbor) return

    neighbor.connections[opposite(dir)] = false
  })

  const index = tiles.indexOf(tile)
  if (index !== -1) {
    tiles.splice(index, 1)
  }

  if (selectedKey.value === key(tile.x, tile.y) && tiles[0] != null) {
    selectedKey.value = tiles.length
      ? key(tiles[0].x, tiles[0].y)
      : ''
  }
}

function deleteSelectedTile() {
  const tile = tiles.find(t => key(t.x, t.y) === selectedKey.value);
  if (tile) {
    deleteTile(tile);
  }
}

/**
 * Speichert das Board. Vorher wird das Backend aufgerufen zur Validierung des Boards
 */
async function handleSave() {
  const fields = exportTiles()
  const board = exportBoard(fields)

  // Validierung serverseitig
  try {
    const response = await fetch("/api/game/board/validate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(board),
    })

    if (!response.ok) {
      const msg = await response.text()
      console.error(msg || "Board ist nicht valide (Serverseitige Validierung fehlgeschlagen)")
      showError(msg)
      return
    }
  } catch (err) {
    console.error("Fehler bei der Validierung: ", err)
    showError("Fehler bei der Validierung: " + err)
    return
  }

  const json = JSON.stringify(board, null, 2)
  const blob = new Blob([json], { type: "application/json" })
  const url = URL.createObjectURL(blob)
  const link = document.createElement("a")
  link.href = url
  link.download = `${board.name}.json`
  link.click()
  URL.revokeObjectURL(url)
}

/**
 * Liest eine importierte JSON-file ein und baut die Tiles im Editor nach
 */
function handleImport(file: File) {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const content = e.target?.result as string
      const boardData = JSON.parse(content) as Partial<BoardExport>

      // Tiles resetten
      tiles.length = 0

      // Tiles vom File holen
      if (boardData.fields && Array.isArray(boardData.fields)) {
        const importedTiles: TileData[] = boardData.fields.map((field: IFieldDTD) => {
          const baseType = mapFieldTypeToTool(field.type)
          const tileType: ToolType = field.barrier ? "barrier" : baseType

          return {
            id: field.id,
            type: tileType,
            x: field.position.x,
            y: field.position.y,
            connections: {
              up: Boolean(field.north),
              down: Boolean(field.south),
              left: Boolean(field.west),
              right: Boolean(field.east)
            }
          }
        })

        tiles.push(...importedTiles)

        if (tiles.length > 0 && tiles[0]) {
          selectedKey.value = key(tiles[0].x, tiles[0].y)
        }
        console.log("Board erfolgreich importiert!")
      }
    } catch (error) {
      console.error("Fehler beim Import: ", error)
    }
  }
  reader.readAsText(file)
}

/**
 * Wandelt Field-DTO-Typen (START/GOAL/END/...) in Editor-Tooltypen um
 */
function mapFieldTypeToTool(fieldType: string): ToolType {
  const fieldTypeUpper = fieldType.toUpperCase()
  if (fieldTypeUpper.startsWith("START")) return "start"
  else if (fieldTypeUpper === "GOAL" || fieldTypeUpper === "END") return "goal"
  return "tile"
}

// Provide functions to child components
provide("emitSave", handleSave)
provide("emitImport", handleImport)

</script>

<template>
  <div class="editor-mapeditor no-select">
    <div class="editor" @mousedown.left="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp"
      @mouseleave="onMouseUp">
      <div class="editor-map" :style="{
        transform: `translate(${offset.x + viewport.width / 2}px, ${offset.y + viewport.height / 2}px)`
      }">
        <StandardTile v-for="tile in tiles" :key="key(tile.x, tile.y)" :x="tile.x" :y="tile.y"
          :selected="selectedKey === key(tile.x, tile.y)" @select="selectedKey = key(tile.x, tile.y)"
          @add="(dir: Direction) => addTile(tile, dir)" :connections="tile.connections" :type="tile.type" />
      </div>
    </div>
    <EditorHUD style="bottom: 20px;" :selectedTool="selectedTile?.type ?? 'tile'" @toolSelected="onHudToolSelected"
      @deleteSelected="deleteSelectedTile" />
    <EditorFileHUD style="bottom: 20px;" />
    <div class="editor-form-row">
      <div class="editor-button-container">
        <BackButton
          @mouseenter="onHover"
          :to="{ name: 'Homepage' }"
          :confirm="true"
          :confirmText="tUI('BACK_TO_MAIN_MENU_CONFIRMATION_MAP_EDITOR')"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-select {
  user-select: none;
  -webkit-user-drag: none;
}

.editor-mapeditor {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  overflow: hidden;
  padding-bottom: 6vh;
}

.editor-mapeditor::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("/backgrounds/BackgroundTest.webp");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  filter: blur(4px);
  z-index: -1;
}

.editor {
  top: 3vh;
  width: 97vw;
  height: 98vh;
  overflow: hidden;
  background: #1a1a1ae1;
  position: relative;
  border-radius: 8px;
  box-shadow: 7.5px 7.5px 15px rgba(0, 0, 0, 0.5), -7.5px -7.5px 15px rgba(0, 0, 0, 0.5);
}

.editor-map {
  position: relative;
  bottom: 5vw;
  right: 8.5vh;
}

form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  width: 100%;
  justify-content: center;
  margin-top: -5vh;
}

.editor-form-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.editor-form-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  align-items: center;
  gap: 15px;
  z-index: 20;
}

.editor-form-row label {
  text-align: right;
}

.editor-button-container {
  position: absolute;
  top: 38px;
  left: 43px;

  display: flex;
  flex-direction: column;
  gap: 2vh;
}

.editor-menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}
</style>