<script setup lang="ts">
import { ref, shallowRef, onMounted, onUnmounted, computed, watchEffect, type ShallowRef, type ComputedRef, type ComponentPublicInstance } from 'vue'
import { TresCanvas, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import GameCharacter from './GameCharacter.vue'
import { useBoardStore } from '@/stores/boardStore'
import Tile from './Tile.vue'
import Path from './Path.vue'
import Foliage from './Foliage.vue'
import Camera from './Camera.vue'
import { useMilefizStore } from "@/stores/milefizstore"
import type { Direction } from "@/types/movement"
import { Vector3 } from 'three'
import { watch } from 'vue'
import { useErrorHandler } from '@/composables/useErrorHandler';
import { TreesGeometry } from 'three/examples/jsm/Addons.js'

const milefizStore = useMilefizStore();
const fpsCamera = shallowRef<any | null>(null)
const boardStore = useBoardStore()
let started: boolean = false

const { showError, showWarning, showCriticalError, showSuccess } = useErrorHandler()

// record: meepleID -> gameCharRef
const gameCharRefs: Record<string, ShallowRef<TresObject | null, TresObject | null>> = {}

/**
 * Liefert eine Liste aller bekannten MeepleIDs
 *
 * Diese Liste wird verwendet, um genau ein `GameCharacter`-Component
 * pro Meeple zu rendern, sodass Komponenten nicht
 * bei jeder Positionsänderung neu erstellt werden.
 *
 * @returns {string[]} Array mit allen Meeple-IDs aus dem Lobby-Objekt
 */
const allMeepleIds = computed(() => {
  const lobby = milefizStore.gamedata.lobby
  if (!lobby) return [] as string[]
  return lobby.players.flatMap(p => p.meeples.map(m => m.id))
})

/**
 * Berechnet die 3D-Zielposition für jeden Meeple anhand von
 * `boardStore.meeplePositions` und der aktuellen Board-Daten.
 *
 * Anstatt ein Array zu erzeugen (was bei Vue zu Neu-Rendern führen kann),
 * liefert diese Compute-Funktion eine `Map<meepleId, [x,y,z]>`,
 * damit die Komponenten pro Meeple stabil bleiben und Positionen
 * gezielt auf die existierenden Instanzen angewendet werden können.
 *
 * @returns {Map<string, [number, number, number]>} Map von Meeple-ID → 3D-Position
 */
const meeplePositions3D = computed(() => {
  const out = new Map<string, [number, number, number]>()
  const board = boardStore.board

  const groups = new Map<string, string[]>()
  for (const id of Object.keys(boardStore.meeplePositions)) {
    const fieldID = boardStore.meeplePositions[id] ?? ''
    const arr = groups.get(fieldID) || []
    arr.push(id)
    groups.set(fieldID, arr)
  }

  for (const [fieldID, meepleIDs] of groups.entries()) {
    let center: [number, number, number] = [0, 0, 0]
    if (board && fieldID) {
      const field = board.fields.find((f) => f.id === fieldID)
      if (field) center = [field.position.x, 0, field.position.y]
    }

    const count = meepleIDs.length
    const baseRadius = 0.5
    const radius = count <= 1 ? 0 : baseRadius

    for (let i = 0; i < count; i++) {
      const meepleID = meepleIDs[i]
      if (!meepleID) continue
      if (count === 1) {
        out.set(meepleID, center)
      } else {
        const angle = (i / count) * Math.PI * 2
        const x = center[0] + radius * Math.cos(angle)
        const z = center[2] + radius * Math.sin(angle)
        out.set(meepleID, [x, center[1], z])
      }
    }
  }

  return out
})

/**
 * Registriert die Ref einer `GameCharacter`-Instanz.
 *
 * Zweck:
 * - Speichert eine `shallowRef` pro MeepleID in `gameCharRefs`.
 * - Vermeidet wiederholte Neuzuweisungen desselben Objekts, um
 *   unnötige Logs und Nebenwirkungen zu verhindern.
 *
 * @param {string} id - Die Meeple-ID
 * @param {TresObject | null} el - Die Komponenteninstanz oder `null`
 */
function registerGameCharRef(id: string, el: TresObject | null) {
  if (!gameCharRefs[id]) {
    gameCharRefs[id] = shallowRef<TresObject | null>(null)
  }

  const prev = gameCharRefs[id].value
  if (prev !== el) {
    gameCharRefs[id].value = el
    if (el) {
      console.log('GameCharacter created:', id, el)
    }
  }
}


/**
 * Überträgt die berechneten 3D-Positionen auf die existierenden
 * `GameCharacter`-Instanzen.
 *
 * Anstatt Positionen per Prop zu übergeben (was zu Re-Renders führen
 * kann), ruft der Watcher die Methoden `animateTo`
 * oder `setPositionImmediate` des GameCharacters auf.
 * Dadurch bleiben die Komponenten erhalten und nur die Three.js-Objekte
 * werden bewegt.
 *
 * @param {Map<string,[number,number,number]>} map - Map von MeepleID → Position
 */

// Vorherige Positionen merken, um nur geänderte Positionen zu animieren
const _prevMeeplePositions = new Map<string, [number, number, number]>()
watch(meeplePositions3D, (map) => {
  // map is a Map<string, [number,number,number]>
  for (const [id, pos] of map.entries()) {
    const prev = _prevMeeplePositions.get(id)
    const changed = !prev || Math.abs(prev[0] - pos[0]) > 1e-6 || Math.abs(prev[1] - pos[1]) > 1e-6 || Math.abs(prev[2] - pos[2]) > 1e-6
    if (!changed) continue

    const ref = gameCharRefs[id]
    const inst: any = ref?.value
    if (inst && typeof inst.animateTo === 'function') {
      inst.animateTo(pos)
    } else if (inst && typeof inst.setPositionImmediate === 'function') {
      // fallback: snap into position if animateTo not present
      inst.setPositionImmediate(pos)
    }

    _prevMeeplePositions.set(id, [pos[0], pos[1], pos[2]])
  }
}, { deep: true })

watch(() => boardStore.meeplePositions, (val) => {
  console.log('boardStore.meeplePositions changed:', JSON.stringify(val))
}, { deep: true })

// Rotation-Updates aus dem Store auf die GameCharacter anwenden
const _prevMeepleRotations = new Map<string, number>()
watch(
  () => boardStore.meepleRotations,
  (rots) => {
    for (const [id, rot] of Object.entries(rots)) {
      const prev = _prevMeepleRotations.get(id)
      if (prev !== undefined && Math.abs(prev - rot) < 1e-6) continue

      const ref = gameCharRefs[id]
      const inst: any = ref?.value
      if (inst && typeof inst.setRotation === 'function') {
        inst.setRotation(rot)
      }

      _prevMeepleRotations.set(id, rot)
    }
  },
  { deep: true, immediate: true }
)

watchEffect(() => {
  if (milefizStore.gameFinished) {
    useFirstPerson.value = false
  }
})

watch(
  () => milefizStore.jumpTrigger,
  (t) => {
    const meepleId = t?.meepleId
    console.log("Meeple in jump:")
    console.log(meepleId)
    if (!meepleId) return

    const ref = gameCharRefs[meepleId]
    const inst: any = ref?.value
    if (inst?.jump) inst.jump()
    console.log("instanz im watcher: " + inst)
  },
  { deep: true }
)

function registerGameCharRefFromTemplate(id: string, el: Element | ComponentPublicInstance | null) {
  // Cast the template ref value to TresObject | null in a type-safe place
  registerGameCharRef(id, el as unknown as TresObject | null)

  if (!started) {
    cycleSelection(0)
    started = true
  }
}

// Board erst laden, wenn Meeples verfügbar sind
const meeplesReady = computed(() => {
  const lobby = milefizStore.gamedata.lobby
  console.log('meeplesReady check:', {
    hasLobby: !!lobby,
    playersLength: lobby?.players?.length,
    players: lobby?.players,
    playersIsArray: Array.isArray(lobby?.players)
  })
  if (!lobby || !lobby.players?.length) return false
  const ready = lobby.players.some(p => {
    const hasMeeples = Array.isArray(p.meeples) && p.meeples.length > 0
    console.log(`Player ${p.id}: meeples=${p.meeples?.length}, hasMeeples=${hasMeeples}`)
    return hasMeeples
  })
  console.log('→ meeplesReady result:', ready)
  return ready
})

// Lädt Board automatisch, sobald Meeples da sind
watch(meeplesReady, async (ready) => {
  console.log('meeplesReady changed to:', ready, 'boardStore.ok:', boardStore.ok)
  if (ready && !boardStore.ok) {
    console.log("Meeples detected — loading board data now...")
    await boardStore.getBoard()
  }
}, { immediate: true })

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
    if (me.meeples[0]) {
      me.activeMeeple = me.meeples[0]
      console.log('Set initial activeMeeple to', me.activeMeeple.id)
    }
  }
})


const useFirstPerson = ref(true) // Kamera-Mode-Flag

//Methode um alle Keyboard Events zu verwalten
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' || e.key === 'm') {
    e.preventDefault()

    // Schließt das PopUp-Menu, wenn es offen sind
    if (milefizStore.popUpMenuOpen) {
      milefizStore.closePopUpMenu()
      return
    }
    else { // Oeffnet das PopUp-Menu
      milefizStore.openPopUpMenu()
      return
    }
  }

  // Wenn ein Duell aktiv ist → alle Steuerungen blockieren
  if (Object.keys(milefizStore.activeDuels).length > 0) {
    e.preventDefault()
    return
  }

  // Tab zum wechseln verwenden + default verhalten verhindern
  if (e.key === 'Tab') {
    e.preventDefault()
    if (milefizStore.gamedata.moved) {
      showWarning('MEEPLE_SELECTION_REJECTED')
      return
    }
    cycleSelection(e.shiftKey ? -1 : 1)
    return
  }

  toggleCamera(e)
  handleMoveKeys(e)
  handleMeepleSelectionKeydown(e)
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

/**
 * Verwaltet die Tastatureingabe zum Wechseln zwischen den eigenen Meeplen
 * @param e Zahlentasten 1 bis 5
 */
function handleMeepleSelectionKeydown(e: KeyboardEvent) {
  if (e.key < '1' || e.key > '5') return

  if (milefizStore.gamedata.moved) {
    showWarning('MEEPLE_SELECTION_REJECTED')
    return
  }

  e.preventDefault()
  const index = Number(e.key) - 1
  selectMeepleByIndex(index)
}

/**
 * Versetzt den Spieler in den gewählten Meeple als den aktiven, steuerbaren Meeple
 * @param index Index und Id des gewählten Meeples
 */
function selectMeepleByIndex(index: number) {
  const ids = ownMeepleIds.value

  if (!ids.length) return
  if (!ids[0]) return

  const lobby = milefizStore.gamedata.lobby
  const myId = milefizStore.gamedata.playerId
  if (!lobby || !myId) return

  const me = lobby.players.find((p) => p.id === myId)
  if (!me) return

  const meeple = me.meeples.find(m => m.id === ids[index])
  if (!meeple) return

  me.activeMeeple = meeple
  console.log('Selected meeple ->', meeple.id)
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


let lastRotSent = 0
const ROT_SEND_MS = 80

// Updated die Rotation vom Meeple
const onRotateCharacter = (yRotation: number) => {
  const id = selectedMeepleId.value
  if (!id) return
  const ref = gameCharRefs[id]
  if (!ref || !ref.value) return
  boardStore.updateMeepleRotation(id, yRotation)
  // in bestimmten Zeitabständen an alle clients senden
  const now = performance.now()
  if (now - lastRotSent >= ROT_SEND_MS) {
    lastRotSent = now
    milefizStore.sendMeepleRotation(id, yRotation)
  }
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
    globalThis.addEventListener("keydown", handleKeydown)
  }

  waitForCamera()
})

onUnmounted(() => {
  globalThis.removeEventListener('keydown', handleKeydown)
})

// Computed Property für Meeple → PlayerColor Mapping
const meepleColorMap = computed(() => {
  const lobby = milefizStore.gamedata.lobby
  if (!lobby) return new Map<string, string>()

  const map = new Map<string, string>()

  // Iteriere über alle Spieler
  for (const player of lobby.players) {
    // Alle Meeples dieses Spielers bekommen seine Farbe
    for (const meeple of player.meeples) {
      map.set(meeple.id, player.color) // player.color = "RED" | "GREEN" | "YELLOW" | "BLUE"
    }
  }

  return map
})


const connectionSegments = computed(() => {
  const board = boardStore.board
  if (!board) return [] as Array<{ x: number; y: number; z: number; length: number; rotY: number; key: string }>

  const out: Array<{ x: number; y: number; z: number; length: number; rotY: number; key: string }> = []
  const seen = new Set<string>()

  for (const f of board.fields) {
    for (const dir of ['east', 'north']) {
      const neighborId = (f as any)[dir] as string | undefined
      if (!neighborId) continue
      const n = board.fields.find((ff) => ff.id === neighborId)
      if (!n) continue

      const key = [f.id, n.id].sort().join('-')
      if (seen.has(key)) continue
      seen.add(key)

      const x1 = f.position.x
      const z1 = f.position.y
      const x2 = n.position.x
      const z2 = n.position.y

      const dx = x2 - x1
      const dz = z2 - z1
      const length = Math.hypot(dx, dz)
      const midX = (x1 + x2) / 2
      const midZ = (z1 + z2) / 2

      const rotY = Math.atan2(dz, dx)

      out.push({ x: midX, y: 0, z: midZ, length, rotY, key })
    }
  }

  return out
})

</script>

<template>

  <!-- 3D-Canvas Element das den ganzen Bildschirm ausfüllt-->
  <TresCanvas window-size style="width: 100vw; height: 100vh" clear-color="#87CEEB">
    <!-- Kameraposition und Kamerasteuerung via OrbitControls -->
    <TresPerspectiveCamera v-if="!useFirstPerson" ref="orbitCam" :position="[0, 8, 15]" :fov="60" />
    <OrbitControls v-if="!useFirstPerson" />

    <!-- First Person Kamera (Folgt dem Charakter) -->
    <Camera ref="fpsCamera" :gameCharRef="(gameCharRefs[selectedMeepleId ?? '']?.value) ?? null"
      :use-first-person="useFirstPerson" @rotate-character="onRotateCharacter" />

    <!-- 3D-Objekt für den Spielfeld-Boden rotation dreht den boden, damit er horizontal und nicht
     vertikal ist -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]">
      <TresPlaneGeometry :args="[500, 500]" />
      <TresMeshStandardMaterial :color="0x4FA200" />

    </TresMesh>

    <!-- Grundbeleuchtung der Szene (75% Intensität) -->
    <TresAmbientLight :intensity=".75" />

    <!-- Himmel- und Bodenlicht der Szene (75% Intensität)-->
    <TresHemisphereLight :intensity=".75" skyColor="#ffffff" groundColor="#888888" />

    <!-- Directional Licht von "vorne rechts" 200%-->
    <TresDirectionalLight :position="[10, 15, 10]" :intensity="2" />

    <!--Spawnen der Meeple (one persistent component per meeple id) -->
    <GameCharacter v-for="id in allMeepleIds" :key="id" :ref="el => registerGameCharRefFromTemplate(id, el)"
      :meepleId="id" :playerColor="meepleColorMap.get(id)" :hidden="useFirstPerson && id === selectedMeepleId && ownMeepleIds.includes(id)"/>

    <!--Spawnen von Barrieren-->
    <GameCharacter v-for="barrier in boardStore.barriersWithPositions" :key="barrier.fieldId"
      :position="barrier.position" bodyColor="gray" eyeColor="red" :meepleId="barrier.fieldId" :barrier="true" />

    <!-- Verbindungspfade zwischen verbundenen Tiles -->
    <Path v-for="seg in connectionSegments" :key="seg.key" :position="[seg.x, 0, seg.z]" :rotationY="seg.rotY"
      :length="seg.length" />

    <!-- Spielfeldtiles rendern -->
    <Tile v-for="field in boardStore.board?.fields" :key="field.id" :id="field.id"
      :position="[field.position.x, 0, field.position.y]" :type="field.type" />

    <!-- Pflanzen und Bäume -->
    <Foliage :elements="boardStore.board?.trees.map(tree => ({ position: [tree.treePosition.x, 0, tree.treePosition.y], type: tree.treeType }))" />
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
