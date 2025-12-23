<script lang="ts" setup>
import EditorHUD from '@/components/ui/mapEditor/EditorHUD.vue';
import EditorFileHUD from '@/components/ui/mapEditor/EditorFileHUD.vue';
import BackButton from '@/components/ui/pages/BackButton.vue'
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import StandardTile from '@/components/ui/mapEditor/tiles/StandardTile.vue';
import { useAudioStore } from '@/stores/audioStore';

type Direction = 'up' | 'down' | 'left' | 'right'
type ToolType = 'start' | 'goal' | 'tile' | 'barrier'

const selectedTool = ref<'start' | 'goal' | 'tile' | 'barrier'>('tile')
const audio = useAudioStore()


const DIR_OFFSET: Record<Direction, { x: number; y: number }> = {
  up:    { x: 0, y: -2 },
  down:  { x: 0, y:  2 },
  left:  { x: -2, y: 0 },
  right: { x:  2, y: 0 },
}

const selectedTile = computed<TileData | null>(() =>
  tiles.find(t => key(t.x, t.y) === selectedKey.value) ?? null
)

type Connections = {
  up: boolean
  down: boolean
  left: boolean
  right: boolean
}

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

type BoardExport = {
  id: string
  name: string
  fields: BackendTile[]
}

const tiles = reactive<TileData[]>([
  {
    id: crypto.randomUUID(),
    type: 'tile',
    x: 0,
    y: 0,
    connections: { up: false, down: false, left: false, right: false }
  }
])

const selectedKey = ref<string>('0,0')
  

/* Kamera */
const offset = reactive({ x: 0, y: 0 })
let dragging = false
let lastMouse = { x: 0, y: 0 }

function onMouseDown(e: MouseEvent) {
  dragging = true
  lastMouse = { x: e.clientX, y: e.clientY }
}

function onMouseMove(e: MouseEvent) {
  if (!dragging) return
  offset.x += e.clientX - lastMouse.x
  offset.y += e.clientY - lastMouse.y
  lastMouse = { x: e.clientX, y: e.clientY }
}

function onMouseUp() {
  dragging = false
}

function key(x: number, y: number) {
  return `${x},${y}`
}

const viewport = ref({
  width: 0,
  height: 0,
})

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

function addTile(fromTile: TileData, dir: Direction) {
  const offset = {
    up:    { x: 0, y: -2 },
    down:  { x: 0, y:  2 },
    left:  { x: -2, y: 0 },
    right: { x:  2, y: 0 }
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

type BackendTile = {
  id: string
  north: string | null
  east: string | null
  south: string | null
  west: string | null
  type: 'NORMAL' | 'START' | 'GOAL'
  position: { x: number; y: number }
  barrier: boolean
}

function exportTiles(): BackendTile[] {
  return tiles.map(tile => {
    const north = tile.connections.up
      ? findNeighbor(tile, 'up')?.id ?? null
      : null

    const south = tile.connections.down
      ? findNeighbor(tile, 'down')?.id ?? null
      : null

    const west = tile.connections.left
      ? findNeighbor(tile, 'left')?.id ?? null
      : null

    const east = tile.connections.right
      ? findNeighbor(tile, 'right')?.id ?? null
      : null

    return {
      id: tile.id,
      north,
      east,
      south,
      west,
      type:
        tile.type === 'start' ? 'START' :
        tile.type === 'goal'  ? 'GOAL'  :
        'NORMAL',
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
    name: 'Custom Board',
    fields
  }
}

function debugExport() {
  const board = exportBoard(exportTiles())
  console.log(board)
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

</script>

<template>
    <div class="editor-mapeditor">
        <div
          class="editor"
          @mousedown.left="onMouseDown"
          @mousemove="onMouseMove"
          @mouseup="onMouseUp"
          @mouseleave="onMouseUp"
        >
          <div
            class="editor-map"
            :style="{
              transform: `translate(${offset.x + viewport.width / 2}px, ${offset.y + viewport.height / 2}px)`
            }"
          >
            <StandardTile
              v-for="tile in tiles"
              :key="key(tile.x, tile.y)"
              :x="tile.x"
              :y="tile.y"
              :selected="selectedKey === key(tile.x, tile.y)"
              @select="selectedKey = key(tile.x, tile.y)"
              @add="(dir: Direction) => addTile(tile, dir)"
              :connections="tile.connections"
              :type="tile.type"
            />
          </div>
        </div>
        <EditorHUD   style="bottom: 20px;"
          :selectedTool="selectedTile?.type ?? 'tile'"
          @toolSelected="onHudToolSelected"
          @deleteSelected="deleteSelectedTile"/>
        <EditorFileHUD style="bottom: 20px;" @click="debugExport()"/>
        <div class="editor-form-row">
            <div class="editor-button-container">
                <BackButton @mouseenter="onHover" :to="{ name: 'Homepage' }" />
            </div>
        </div>
    </div>
</template>

<style scoped>
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
  box-shadow: 7.5px 7.5px 15px rgba(0,0,0,0.5), -7.5px -7.5px 15px rgba(0,0,0,0.5);
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