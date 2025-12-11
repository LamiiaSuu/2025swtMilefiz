<script setup lang="ts">
import { ref, shallowRef, onMounted, onUnmounted, computed, watchEffect, type ShallowRef, type ComputedRef, type ComponentPublicInstance } from 'vue'
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
const fpsCamera = shallowRef<any | null>(null)
const boardStore = useBoardStore()
let started: boolean = false

//TODO 
// Refs richtig setzen ✓
// Meeple auf Feld versetzt anzeigen ✓
// Zischen meeple switchen ✓
// Fix: camera init x
// Fix: alle hüpfen beim laufen x
// Fix: movement issues

// record: meepleID -> gameCharRef
const gameCharRefs: Record<string, ShallowRef<TresObject | null, TresObject | null>> = {}

/**
 * Berechnet die aktuelle 3D-Position des Spielcharakters auf dem Spielfeld.
 * 
 * Nutzt die gespeicherte Meeple-Position aus dem BoardStore und 
 * wandelt sie in Three.js-Koordinaten um. 
 * Wenn mehrere Meeple auf einem Feld stehen werden sie auf einem Kreis platziert
 * Wird automatisch neu berechnet, wenn sich das Board oder die Meeple-Position ändert.
 * 
 * @returns {id, [x, y, z]} - Key: Id des Meeple, Value: Weltkoordinaten des Spielcharakters
 */
const meepleEntries = computed(() => {
  const out: { id: string; position: [number, number, number] }[] = []
  const board = boardStore.board

  //field -> meeple[]
  const groups = new Map<string, string[]>()

  for (const [meepleID, posID] of Object.entries(boardStore.meeplePositions)) {
    const fieldID = posID ?? ''
    const meeples = groups.get(fieldID) || []
    meeples.push(meepleID)
    groups.set(fieldID, meeples)

  }

  for (const [fieldID, meepleIDs] of groups.entries()) {
    let center: [number, number, number] = [0, 0, 0]

    if (board && fieldID) {
      const field = board.fields.find( (f) => f.id === fieldID)
      if (field) {
        center = [field.position.x , 0, field.position.y]
      }
    }

    const count = meepleIDs.length
    const baseRadius = 0.5
    const radius = count <= 1 ? 0 : baseRadius

    for (let i = 0; i < count; i++) {
      const meepleID = meepleIDs[i]

      if (!meepleID) continue

      if (count === 1) {
        out.push({ id: meepleID, position: center})
      } else {
        const angle = (i / count) * Math.PI * 2
        const x = center[0] + radius * Math.cos(angle)
        const z = center[2] + radius * Math.sin(angle)
        out.push({ id: meepleID, position: [x, center[1], z] })
      }
    }
  }
  console.log("Computed Meeple Entries", out)
  return out
})

function registerGameCharRef(id: string, el: TresObject | null) {
  if (!gameCharRefs[id]) {
    gameCharRefs[id] = shallowRef<TresObject | null>(null)
  }
  gameCharRefs[id].value = el
  if (el) {
    console.log('GameCharacter created:', id, el)
  }
}

watch(meepleEntries, (val) => {
  console.log('meepleEntries changed:', val)
}, { deep: true })

watch(() => boardStore.meeplePositions, (val) => {
  console.log('boardStore.meeplePositions changed:', JSON.stringify(val))
}, { deep: true })

function registerGameCharRefFromTemplate(id: string, el: Element | ComponentPublicInstance | null) {
  // Cast the template ref value to TresObject | null in a type-safe place
  registerGameCharRef(id, el as unknown as TresObject | null)

  if(!started) {
    cycleSelection(1)
    started = true
  }
}

// Board-Daten laden wenn die App startet
// onMounted(async () => {
//   console.log('App mounted - loading board data...')
//   await boardStore.getBoard()
// })

// Board erst laden, wenn Meeples verfügbar sind
const meeplesReady = computed(() => {
  const lobby = milefizStore.gamedata.lobby
  if (!lobby || !lobby.players?.length) return false
  return lobby.players.some(p => Array.isArray(p.meeples) && p.meeples.length > 0)
})

// Lädt Board automatisch, sobald Meeples da sind
watch(meeplesReady, async (ready) => {
  if (ready && !boardStore.ok) {
    console.log("🎯 Meeples detected — loading board data now...")
    await boardStore.getBoard()
  }
})

//eigene Meeple aus der Lobby merken 
const ownMeepleIds = computed(() => {
  const lobby = milefizStore.gamedata.lobby
  const myId = milefizStore.gamedata.playerId
  if (!lobby || !myId) return [] as string[]
  const me = lobby.players.find((p) => p.id === myId)
  if (!me) return [] as string[]
  return me.meeples.map((m) => m.id)
})

// activeMeeple merken
const selectedMeepleId = computed(() => {
  const lobby = milefizStore.gamedata.lobby
  const myId = milefizStore.gamedata.playerId
  if (!lobby || !myId) return null
  const me = lobby.players.find((p) => p.id === myId)
  return (me?.activeMeeple?.id) ?? null
})

// falls kein activeMeeple gesetzt ist, wird hier das erste gesetzt
watch(ownMeepleIds, (ids) => {
  const lobby = milefizStore.gamedata.lobby
  const myId = milefizStore.gamedata.playerId
  if (!lobby || !myId) return
  const me = lobby.players.find((p) => p.id === myId)
  if (!me) return
  if ((!me.activeMeeple || !me.activeMeeple.id) && me.meeples.length > 0) {
    // erste Meeple als active setzen
    if(me.meeples[0]) {
      me.activeMeeple = me.meeples[0]
      console.log('Set initial activeMeeple to', me.activeMeeple.id)
    }
  }
})

const useFirstPerson = ref(true) // Kamera-Mode-Flag

//Methode um alle Keyboard Events zu verwalten
const handleKeydown = (e: KeyboardEvent) => {
  // Tab zum wechseln verwenden + default verhalten verhindern
  if (e.key === 'Tab') {
    e.preventDefault()
    cycleSelection(e.shiftKey ? -1 : 1)
    return
  }

  toggleCamera(e)
  handleJump(e)
  handleMoveKeys(e)
}

/**
 * rotiert durch die eigenen Meeple durch
 */
function cycleSelection(offset: number = 1) {
  const ids = ownMeepleIds.value
  if (!ids.length) return
  if (!ids[0]) return
  const current = selectedMeepleId.value ?? ids[0]
  const idx = Math.max(0, ids.indexOf(current))
  const next = (idx + offset + ids.length) % ids.length

  const lobby = milefizStore.gamedata.lobby
  const myId = milefizStore.gamedata.playerId
  if (!lobby || !myId) return
  const me = lobby.players.find((p) => p.id === myId)
  if (!me) return

  // activeMeeple setzen
  const nextMeeple = me.meeples.find((m) => m.id === ids[next])
  if (nextMeeple) {
    me.activeMeeple = nextMeeple
    console.log('Cycled activeMeeple ->', nextMeeple.id)
  }
}

const handleJump = (e: KeyboardEvent) => {
  if (e.code === 'Space') {
    e.preventDefault()
    const id = selectedMeepleId.value
    if (!id) return
    const ref = gameCharRefs[id]
    if (!ref || !ref.value) return
    if (ref.value && ref.value.jump) {
      ref.value.jump()
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
 * 5. Sendet die berechnete Himmelsrichtung als Spielzug an den Server (`milefizStore`).
 *
 * @param {KeyboardEvent} e - Das Tastatur-Event, das die Eingabe auslöst.
 */
const handleMoveKeys = (e: KeyboardEvent) => {
  if (!useFirstPerson.value) return

  const cam = fpsCamera.value?.camera
  // const meepleId = gameCharRef.value?.meepleId
  const meepleId = selectedMeepleId.value
  if (!cam || !meepleId) return

  // Blickrichtung der Kamera holen
  const lookDir = new Vector3()
  cam.getWorldDirection(lookDir)
  lookDir.setY(0).normalize() // nur horizontale Richtung
  lookDir.multiplyScalar(-1)

  // Vektor für Bewegung
  const moveDir = new Vector3()

  switch (e.code) {
    case "ArrowUp":
    case "KeyW":
      moveDir.copy(lookDir)
      break
    case "ArrowDown":
    case "KeyS":
      moveDir.copy(lookDir).negate()
      break
    case "ArrowLeft":
    case "KeyA":
      // Links = Kreuzprodukt von Up-Vektor × Blickrichtung
      moveDir.crossVectors(new Vector3(0, 1, 0), lookDir).normalize()
      break
    case "ArrowRight":
    case "KeyD":
      // Rechts = Kreuzprodukt von Blickrichtung × Up-Vektor
      moveDir.crossVectors(lookDir, new Vector3(0, 1, 0)).normalize()
      break
    default:
      return
  }

  e.preventDefault()

  // Richtung auf Hauptachsen runterbrechen (X/Z)
  const absX = Math.abs(moveDir.x)
  const absZ = Math.abs(moveDir.z)

  let direction: Direction
  if (absX > absZ) {
    direction = moveDir.x > 0 ? "EAST" : "WEST"
  } else {
    direction = moveDir.z > 0 ? "SOUTH" : "NORTH"
  }

  milefizStore.sendMove(meepleId, direction)
}







// Updated Rotation vom Charakter für First Person Kamera
const onRotateCharacter = (yRotation: number) => {
  const id = selectedMeepleId.value
  if (!id) return
  const ref = gameCharRefs[id]
  if (!ref || !ref.value) return
  ref.value.setRotation(yRotation)
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
    if (!cam || !meeplesReady.value || !boardStore.ok) {
      requestAnimationFrame(waitForCamera)
      return
    }
    window.addEventListener("keydown", handleKeydown)
  }

  waitForCamera()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

</script>

<template>
  <!-- 3D-Canvas Element das den ganzen Bildschirm ausfüllt-->
  <TresCanvas window-size style="width: 100vw; height: 100vh" clear-color="#87CEEB">
    <!-- Kameraposition und Kamerasteuerung via OrbitControls -->
    <TresPerspectiveCamera v-if="!useFirstPerson" ref="orbitCam" :position="[0, 8, 15]" :fov="60" />
    <OrbitControls v-if="!useFirstPerson" />

    <!-- First Person Kamera (Folgt dem Charakter) -->
    <Camera ref="fpsCamera" :gameCharRef="(gameCharRefs[selectedMeepleId ?? '']?.value) ?? null" :use-first-person="useFirstPerson"
      @rotate-character="onRotateCharacter" />

    <!-- 3D-Objekt für den Spielfeld-Boden rotation dreht den boden, damit er horizontal und nicht
     vertikal ist -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[500, 500]" />
      <TresMeshStandardMaterial :color="0x4FA200" />

    </TresMesh>

    <!-- Grundbeleuchtung der Szene (75% Intensität) -->
    <TresAmbientLight :intensity=".75" />

    <!-- Directional Licht von "vorne rechts" 200%-->
    <TresHemisphereLight :intensity=".75" skyColor="#ffffff" groundColor="#888888" />
    
    <GameCharacter v-for="entry in meepleEntries" :key="entry.id"
      :ref="el => registerGameCharRefFromTemplate(entry.id, el)"
      :position="entry.position" :meepleId="entry.id" bodyColor="pink" eyeColor="white" />

    <GameCharacter v-for="barrier in boardStore.barriersWithPositions" :key="barrier.fieldId"
      :position="barrier.position" bodyColor="gray" eyeColor="red" :meepleId="barrier.fieldId" :barrier="true" />

    <!-- Spielfeldtiles rendern -->
    <Tile v-for="field in boardStore.board?.fields" :key="field.id" :id="field.id"
      :position="[field.position.x, 0, field.position.y]" :type="field.type" />
  </TresCanvas>

  <!-- Fadenkreuz -->
  <div v-if="useFirstPerson" class="crosshair">
    <div class="dot"></div>
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
  background: white;
  /* <-- immer weiß */
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.5);
  transition: background 0.1s ease, transform 0.1s ease;
}
</style>
