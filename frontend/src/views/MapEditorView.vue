<script lang="ts" setup>
import EditorHUD from '@/components/ui/mapEditor/EditorHUD.vue';
import EditorFileHUD from '@/components/ui/mapEditor/EditorFileHUD.vue';
import BackButton from '@/components/ui/pages/BackButton.vue'
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import StandardTile from '@/components/ui/mapEditor/tiles/StandardTile.vue';

type TileData = {
  type: string
  x: number
  y: number
}

const tiles = reactive<TileData[]>([
  { type: "standard", x: 0, y: 0 } // Start-Tile
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

</script>

<template>
    <div class="mapeditor">
        <div
          class="editor"
          @mousedown.left="onMouseDown"
          @mousemove="onMouseMove"
          @mouseup="onMouseUp"
          @mouseleave="onMouseUp"
        >
          <div
            class="map"
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
            />
          </div>
        </div>
        <EditorHUD style="bottom: 20px;"/>
        <EditorFileHUD style="bottom: 20px;"/>
        <div class="form-row">
            <div class="button-container">
                <BackButton :to="{ name: 'Homepage' }" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.mapeditor {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  overflow: hidden;
  padding-bottom: 4rem;
}

.mapeditor::before {
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
  height: 95vh;
  overflow: hidden;
  background: #1a1a1ae1;
  position: relative;
}

.map {
  position: relative;
}

form {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 40px;
    width: 100%;
    justify-content: center;
    margin-top: -5vh;
}

.form-column {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-row {
    display: grid;
    grid-template-columns: 150px 1fr;
    align-items: center;
    gap: 15px;
    z-index: 20;
}

.form-row label {
    text-align: right;
}

.button-container {
  position: absolute;
  bottom: 10px;
  left: 30px;

  display: flex;
  flex-direction: column;
  gap: 2vh;
}

.menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}
</style>