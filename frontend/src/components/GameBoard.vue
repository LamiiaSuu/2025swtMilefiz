<script setup lang="ts">
import { ref, shallowRef, onMounted, onUnmounted, computed, watchEffect } from 'vue'
import { TresCanvas, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import GameCharacter from './GameCharacter.vue'
import { useBoardStore } from '@/stores/boardStore'
import Tile from './Tile.vue'
import Camera from './Camera.vue'
import { useMilefizStore } from "@/stores/milefizstore"
import type { Direction } from "@/types/movement"
import type { Object3D } from 'three'
import { Raycaster, Vector3 } from 'three'
import { watch } from 'vue'

const milefizStore = useMilefizStore();
const gameCharRef = shallowRef<TresObject | null>(null)
const fpsCamera = shallowRef<any | null>(null)
const boardStore = useBoardStore()
const raycaster = new Raycaster()
const rayOrigin = new Vector3()
const rayDirection = new Vector3()
const crosshairColor = ref<'white' | 'red' | 'green'>('white')

/**
 * Liste aller klickbaren Tile-Objekte im Spielfeld.
 * Wird beim Rendern jedes Tiles über das `@tile-ready`-Event befüllt.
 */
const clickableTiles = ref<{ id: string; object: Object3D }[]>([])

/**
 * Frame-Zähler, um Raycast-Checks zu throttlen (z. B. nur jedes zweite Frame prüfen).
 */
let frameCounter = 0

/**
 * Wird aufgerufen, wenn ein Tile in der Szene bereit ist.
 * 
 * Fügt das zugehörige 3D-Objekt zur Liste der klickbaren Tiles hinzu, 
 * sodass es später für Raycasting (Klick- und Hover-Erkennung) verwendet werden kann.
 * 
 * @param payload - Objekt mit Tile-ID und zugehörigem 3D-Objekt
 */
const onTileReady = (payload: { id: string; object: Object3D }) => {
  clickableTiles.value.push(payload)
}

// Board-Daten laden wenn die App startet
onMounted(async () => {
  console.log('App mounted - loading board data...')
  await boardStore.getBoard()
})

/**
 * Berechnet die aktuelle 3D-Position des Spielcharakters auf dem Spielfeld.
 * 
 * Nutzt die gespeicherte Meeple-Position aus dem BoardStore und 
 * wandelt sie in Three.js-Koordinaten um. 
 * Wird automatisch neu berechnet, wenn sich das Board oder die Meeple-Position ändert.
 * 
 * @returns [x, y, z] - Weltkoordinaten des Spielcharakters
 */
const gameCharPosition = computed<[number, number, number]>(() => {
  const board = boardStore.board
  const fieldId = boardStore.meeplePositions[boardStore.testMeepleId]

  if (!board || !fieldId) {
    return [0, 0, 0]
  }

  const field = board.fields.find(f => f.id === fieldId)
  if (!field) {
    return [0, 0, 0]
  }

  return [field.position.x, 0, field.position.y]
})

const useFirstPerson = ref(true) // Kamera-Mode-Flag

//Methode um alle Keyboard Events zu verwalten
const handleKeydown = (e: KeyboardEvent) => {
  toggleCamera(e)
  handleJump(e)
  handleMoveKeys(e)
}

const handleJump = (e: KeyboardEvent) => {
  if (e.code === 'Space') {
    e.preventDefault()
    if (gameCharRef.value && gameCharRef.value.jump) {
      gameCharRef.value.jump()
    }
  }
}

// Keyboard toggle listener
const toggleCamera = (e: KeyboardEvent) => {
  if (e.key.toLowerCase() === 'o') {
    useFirstPerson.value = !useFirstPerson.value
  }
}

/**
 * Die Richtung wird relativ zur aktuellen Kamerasicht berechnet.
 * 
 * Ablauf:
 * 1. Prüft, ob sich der Spieler im First-Person-Modus befindet.
 * 2. Ermittelt, welche Bewegungstaste gedrückt wurde (`W`, `A`, `S`, `D` oder Pfeiltasten).
 * 3. Wandelt diese lokale Richtung (z. B. „vorwärts“) über die Kamerarotation (`Quaternion`)
 *    in eine Richtung im Weltkoordinatensystem um.
 * 4. Analysiert, ob sich die resultierende Richtung überwiegend entlang der X- oder Z-Achse bewegt:
 *    - X-Achse → EAST oder WEST
 *    - Z-Achse → NORTH oder SOUTH
 * 5. Sendet die berechnete Himmelsrichtung und übrigbleibenden Züge als Spielzug an den Server (`milefizStore`).
 *
 * @param {KeyboardEvent} e - Das Tastatur-Event, das die Eingabe auslöst.
 */
const handleMoveKeys = (e: KeyboardEvent) => {
  if (!useFirstPerson.value) return

  const cam = fpsCamera.value?.camera
  const meepleId = gameCharRef.value?.meepleId
  if (!cam || !meepleId) return

  let localDir = new Vector3()

  switch (e.code) {
    case ("ArrowUp"):
    // case "KeyW":
      localDir.set(0, 0, 1) 
      break
    case "ArrowDown":
    // case "KeyS":
      localDir.set(0, 0, -1) 
      break
    case "ArrowLeft":
    // case "KeyA":
      localDir.set(1, 0, 0) 
      break
    case "ArrowRight":
    // case "KeyD":  
      localDir.set(-1, 0, 0) 
      break
    default:
      return
  }
  
  e.preventDefault()

  const worldDir = localDir.clone().applyQuaternion(cam.quaternion).setY(0).normalize()

  const absX = Math.abs(worldDir.x)
  const absZ = Math.abs(worldDir.z)

  let direction: Direction | null = null
  if (absX > absZ) {
    direction = worldDir.x > 0 ? "EAST" : "WEST"
  } else {
    direction = worldDir.z > 0 ? "SOUTH" : "NORTH"
  }

  if (!direction) return

  let remainingMoves = milefizStore.gamedata.currentDiceRoll

  milefizStore.sendMove(meepleId, direction, remainingMoves)
}


/**
 * Wird ausgelöst, wenn ein Spielfeld-Tile angeklickt wurde.
 * 
 * Ermittelt anhand der Tile-ID, ob das Ziel-Feld ein Nachbarfeld des Charakters ist.
 * Falls ja, wird der Spielzug (Richtung und übrigbleibende Züge) über den Milefiz-Store an den Server gesendet.
 * 
 * @param targetFieldId - ID des angeklickten Ziel-Feldes
 */
const onTileClicked = (targetFieldId: string) => {
  console.log(targetFieldId)
  const meepleId = gameCharRef.value?.meepleId
  if (!meepleId) {
    console.warn("Meeple ID missing — cannot move")
    return
  }

  const board = boardStore.board
  if (!board) return

  const targetField = board.fields.find(f => f.id === targetFieldId)
  if (!targetField) return

  const [cx, , cz] = gameCharRef.value?.getPosition() ?? [0, 0, 0]
  const currentField = board.fields.find(f => f.position.x === cx && f.position.y === cz)

  if (!currentField) {
    console.warn("Cannot find current field for character")
    return
  }

  const dx = targetField.position.x - currentField.position.x
  const dy = targetField.position.y - currentField.position.y

  let direction: Direction | null = null

  if (dx === -2 && dy === 0) direction = "EAST"
  else if (dx === 2 && dy === 0) direction = "WEST"
  else if (dx === 0 && dy === -2) direction = "SOUTH"
  else if (dx === 0 && dy === 2) direction = "NORTH"

  if (!direction) {
    console.log("not a neighbor — no move.")
    return
  }

  let remainingMoves = milefizStore.gamedata.currentDiceRoll
  milefizStore.sendMove(meepleId, direction, remainingMoves)
}

/**
 * Globale Mausklick-Handler-Funktion für den First-Person-Modus.
 * 
 * Wird bei jedem Mausklick im Fenster aufgerufen und prüft, 
 * ob der Spieler mit der Kamera auf ein klickbares Spielfeld (Tile) zielt.
 * 
 * Funktionsweise:
 * 1. Nur im First-Person-Modus und bei linkem Mausklick aktiv.  
 * 2. Ermittelt die Weltposition und Blickrichtung der Kamera.  
 * 3. Führt einen Raycast entlang der Blickrichtung aus.  
 * 4. Erkennt das zuerst getroffene Tile-Objekt.  
 * 5. Ruft `onTileClicked(tile.id)` auf, um die Spiellogik auszulösen (z. B. Bewegung).
 * 
 * @param e - Das auslösende MouseEvent (wird global vom Fenster empfangen)
 */
const handleGlobalClick = (e: MouseEvent) => {

  if (!useFirstPerson.value) return
  if (e.button !== 0) return

  const cam = fpsCamera.value?.camera
  if (!cam) {
    console.warn("Keine Kamera gefunden")
    return
  }

  cam.getWorldPosition(rayOrigin)

  rayDirection.set(0, 0, -1)
  rayDirection.applyQuaternion(cam.quaternion)
  raycaster.set(rayOrigin, rayDirection)

  const objects = clickableTiles.value.map(t => t.object)
  const hits = raycaster.intersectObjects(objects, true)
  const firstHit = hits[0]

  if (!firstHit) {
    console.log('Kein Tile getroffen')
    return
  }

  let hitRoot = firstHit.object as any

  while (hitRoot.parent && !objects.includes(hitRoot)) {
    hitRoot = hitRoot.parent
  }

  const tile = clickableTiles.value.find(t => t.object === hitRoot)
  if (!tile) {
    console.warn('Treffer, aber kein zugehöriges Tile gefunden')
    return
  }

  onTileClicked(tile.id)
}


// Updated Rotation vom Charakter für First Person Kamera
const onRotateCharacter = (yRotation: number) => {
  if (gameCharRef.value) {
    gameCharRef.value.setRotation(yRotation)
  }
}

/**
 * Überprüft in regelmäßigen Abständen (per requestAnimationFrame),
 * ob der Spieler im First-Person-Modus mit dem Fadenkreuz auf ein Spielfeld zeigt.
 * 
 * - Default ist 60fps
 * - durch frameCounter % 2 wird nur in jedem zweiten Frame überprüft, was die Performance deutlich verbessert.
 * - Führt einen Raycast aus Sicht der Kamera aus.
 * - Erkennt, ob das getroffene Feld ein gültiger Nachbar ist.
 * - Aktualisiert die Fadenkreuzfarbe dynamisch:
 *   - Weiß: kein Treffer  
 *   - Grün: gültiger Nachbar  
 *   - Rot: ungültig, keine Züge mehr übrig oder letztes Feld
 */
const checkHoverTile = () => {

  frameCounter++
  if (frameCounter % 1 !== 0) {
    requestAnimationFrame(checkHoverTile)
    return
  }

  if (!useFirstPerson.value) {
    crosshairColor.value = 'white'
    requestAnimationFrame(checkHoverTile)
    return
  }

  const cam = fpsCamera.value?.camera

  if (!cam) {
    requestAnimationFrame(checkHoverTile)
    return
  }

  cam.getWorldPosition(rayOrigin)
  rayDirection.set(0, 0, -1)
  rayDirection.applyQuaternion(cam.quaternion)
  raycaster.set(rayOrigin, rayDirection)

  const objects = clickableTiles.value.map(t => t.object)
  const hits = raycaster.intersectObjects(objects, true)
  const firstHit = hits[0]

  if (!firstHit) {
    crosshairColor.value = 'white'
    requestAnimationFrame(checkHoverTile)
    return
  }

  let hitRoot = firstHit.object as any
  while (hitRoot.parent && !objects.includes(hitRoot)) hitRoot = hitRoot.parent
  const tile = clickableTiles.value.find(t => t.object === hitRoot)

  if (!tile) {
    crosshairColor.value = 'white'
    requestAnimationFrame(checkHoverTile)
    return
  }

  const board = boardStore.board
  const meeplePos = gameCharRef.value?.getPosition()
  if (!board || !meeplePos) {
    crosshairColor.value = 'white'
    requestAnimationFrame(checkHoverTile)
    return
  }

  const [cx, , cz] = meeplePos
  const targetField = board.fields.find(f => f.id === tile.id)
  const currentField = board.fields.find(f => f.position.x === cx && f.position.y === cz)
  if (!currentField || !targetField) {
    crosshairColor.value = 'white'
    requestAnimationFrame(checkHoverTile)
    return
  }

  const dx = targetField.position.x - currentField.position.x
  const dy = targetField.position.y - currentField.position.y

  const isNeighbor =
    (dx === -2 && dy === 0) ||
    (dx === 2 && dy === 0) ||
    (dx === 0 && dy === -2) ||
    (dx === 0 && dy === 2)

  const lastFieldId = boardStore.lastFields[boardStore.testMeepleId]
  const remainingMoves = milefizStore.gamedata.currentDiceRoll
  const isLastField = targetField.id === lastFieldId

  if (isNeighbor && !isLastField && remainingMoves != 0 && remainingMoves != undefined) {
    crosshairColor.value = 'green'
  } else {
    crosshairColor.value = 'red'
  }

  requestAnimationFrame(checkHoverTile)
}

onMounted(() => {

  /**
   * Wartet, bis die First-Person-Kamera vollständig initialisiert ist.
   * 
   * Sobald die Kamera verfügbar ist:
   * - werden globale Event-Listener für Tastatur und Mausklicks aktiviert
   * - startet die Hover-Erkennung (checkHoverTile)
   * 
   * Diese Schleife verhindert Fehler, falls die Kamera-Referenz
   * beim Mounten der Komponente noch nicht gesetzt wurde.
   */
  const waitForCamera = () => {
    const cam = fpsCamera.value?.camera
    if (!cam) {
      requestAnimationFrame(waitForCamera)
      return
    }
    window.addEventListener("keydown", handleKeydown)
    window.addEventListener("click", handleGlobalClick)
    requestAnimationFrame(checkHoverTile)
  }

  waitForCamera()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('click', handleGlobalClick)
})

</script>

<template>
  <!-- 3D-Canvas Element das den ganzen Bildschirm ausfüllt-->
  <TresCanvas window-size style="width: 100vw; height: 100vh" clear-color="#87CEEB">
    <!-- Kameraposition und Kamerasteuerung via OrbitControls -->
    <TresPerspectiveCamera v-if="!useFirstPerson" ref="orbitCam" :position="[0, 8, 15]" :fov="60" />
    <OrbitControls v-if="!useFirstPerson" />

    <!-- First Person Kamera (Folgt dem Charakter) -->
    <Camera ref="fpsCamera" :gameCharRef="gameCharRef" :use-first-person="useFirstPerson"
      @rotate-character="onRotateCharacter" />

    <!-- 3D-Objekt für den Spielfeld-Boden rotation dreht den boden, damit er horizontal und nicht
     vertikal ist -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[20, 20]" />
      <TresMeshBasicMaterial :color="0x7cfc00" />
    </TresMesh>

    <!-- Grundbeleuchtung der Szene (80% Intensität) -->
    <TresAmbientLight :intensity="0.8" />

    <!-- Game Character includiert (position - Position auf Plane), (bodyColor - Farbe der Figur), (eyeColor - Farbe der Augen) -->
    <GameCharacter ref="gameCharRef" :position="gameCharPosition" bodyColor="pink" eyeColor="white"
      :meepleId="boardStore.testMeepleId" />

    <!-- Spielfeldtiles rendern -->
    <Tile v-for="field in boardStore.board?.fields" :key="field.id" :id="field.id"
      :position="[field.position.x, 0, field.position.y]" :type="field.type" @tile-click="onTileClicked"
      @tile-ready="onTileReady" />
  </TresCanvas>

  <!-- Fadenkreuz -->
  <div v-if="useFirstPerson" class="crosshair">
    <div class="dot" :style="{ background: crosshairColor }"></div>
  </div>
</template>

<style scoped>
.crosshair {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 9999;
}

.crosshair .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);
  transition: background 0.1s ease, transform 0.1s ease;
}
</style>
