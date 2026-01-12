<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect, watch, ref, computed, onMounted } from 'vue'
import { useMilefizStore } from '@/stores/milefizstore'
import { getPlayerColors } from '@/types/colorsAssets';
import { audioEngine } from '@/composables/audioEngine'
import type { TresObject } from '@tresjs/core'

// Zugriff auf globalen PiniaStore
const milefizStore = useMilefizStore()

// Für Move-Sound am Anfang des Spiels
const hasInitializedMoved = ref(false)

//Definierte Props für Augen, Körperfarbe und Position
const props = defineProps<{
  bodyColor?: string | number
  eyeColor?: string | number
  position?: [number, number, number]
  meepleId: string
  barrier?: boolean
  playerColor?:  string
  hidden?: boolean
}>()

const characterRotation = ref(0)
const characterPosition = ref<TresObject | null>(null)

//Variablen für Anpassung des Sprungs definiert
const jumpOffset = ref(0)
const isJumping = ref(false)
const isJumpAllowed = computed(() => milefizStore.energy.isEnergyFull)

// Standard-Sprunghöhe (wird für große Sprünge verwendet)
const defaultJumpHeight = 4
const defaultUpDuration = 300
const defaultFallDuration = 2000

// Kleine Hüpfer (bei Bewegung)
const smallJumpHeight = 0.7
const smallUpDuration = 120
const smallFallDuration = 170

// Animation-Variablen
const mixer = ref<any>(null)
const jumpAction = ref<any>(null)

// NEU: Y-Offset für unterschiedliche Modelle
const yOffset = computed(() => (props.barrier ? 0.85 : 0.135))

// Berechne aktuelle Position (inklusive jumpOffset)
const currentPosition = computed<[number, number, number]>(() => [
  animatedPosition.value[0],
  animatedPosition.value[1] + jumpOffset.value + yOffset.value,
  animatedPosition.value[2],
])

//Rock by Poly by Google [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/dmRuyy1VXEv)
// Block Character by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/ozSIyRIcIj)

const modelPath = computed(() => (props.barrier ? '/Rock.glb' : '/Block Character.glb'))
const { state } = useGLTF(modelPath, { draco: true })

// Unterschiedliche Scale für Barriere und Character
const scale = computed(() => (props.barrier ? 1.5 : 0.55))


const meepleColors = computed(() => {
   // Spieler nutzen playerColor
  const playerColors = getPlayerColors(props.playerColor)

  return {
    body: props.bodyColor ?? playerColors.body,
    eyes: props.eyeColor ?? playerColors.eyes
  }

})

watchEffect(async () => {
  if (!state.value?.scene) return

  state.value.scene.scale.set(scale.value, scale.value, scale.value)

  const userData = (state.value.scene as any).userData
  if (!userData?.colorsApplied) {
    state.value.scene.traverse((child: any) => {
      if (!child.isMesh || !child.material) return

      const mats = Array.isArray(child.material) ? child.material : [child.material]

      mats.forEach((mat: any) => {
        if (!mat || !mat.color) return

        // Body
        if (mat.name === 'body' || child.name?.includes('body')) {
          mat.color.set(meepleColors.value.body)
          mat.needsUpdate = true
        }
        // Eyes
        else if (mat.name === 'eye_color' || child.name?.includes('eye')) {
          mat.color.set(meepleColors.value.eyes) 
          mat.needsUpdate = true
        }
      })
    })

    ;(state.value.scene as any).userData = {
      ...userData,
      colorsApplied: true
    }
  }

    // Animation Mixer einrichten
    if (state.value.animations && state.value.animations.length > 0) {
      const THREE = await import('three')
      mixer.value = new THREE.AnimationMixer(state.value.scene)

      // Suche nach Jump-Animation
      const jumpAnimation = state.value.animations.find((anim: any) =>
        anim.name.toLowerCase().includes('jump'),
      )

      if (jumpAnimation) {
        jumpAction.value = mixer.value.clipAction(jumpAnimation)
        jumpAction.value.setLoop(THREE.LoopOnce, 1) // Nur einmal abspielen
        jumpAction.value.clampWhenFinished = true

        console.log('Jump animation found:', jumpAnimation.name)
      } else {
        console.log(
          'Available animations:',
          state.value.animations.map((a) => a.name),
        )
      }
    }
  }
)

// Animation updaten
const clock = ref<any>(null)
watchEffect(async () => {
  if (mixer.value && !clock.value) {
    const THREE = await import('three')
    clock.value = new THREE.Clock()

    const animate = () => {
      if (mixer.value) {
        const delta = clock.value.getDelta()
        mixer.value.update(delta)
        requestAnimationFrame(animate)
      }
    }
    animate()
  }
})

watch(
  () => props.hidden,
  (hidden) => {
    if (characterPosition.value) {
      characterPosition.value.visible = !hidden
    }
  },
  { immediate: true }
)

let targetRotation = 0
let isRotating = false

const lerp = (a: number, b: number, t: number) => {
  return a + (b - a) * t
}

const animateRotation = () => {
  if (!isRotating) return

  characterRotation.value = lerp(
    characterRotation.value,
    targetRotation,
    0.065 // smoothing factor (niedriger = langsamer, smoother)
  )

  // Stop wenn nah genug am Wert
  if (Math.abs(characterRotation.value - targetRotation) < 0.001) {
    characterRotation.value = targetRotation
    isRotating = false
    return
  }

  requestAnimationFrame(animateRotation)
}

const setRotation = (yRotation: number) => {
  targetRotation = yRotation
  if (!isRotating) {
    isRotating = true
    requestAnimationFrame(animateRotation)
  }
}


// Sprung-Animation
const animateCustomJump = (
  height = defaultJumpHeight,
  upMs = defaultUpDuration,
  downMs = defaultFallDuration,
  onComplete?: () => void,
) => {
  const startTime = performance.now()

  // Easing-Funktionen
  const easeOutCubic = (t: number) => 1 - Math.pow(1 - t, 3)
  const easeInCubic = (t: number) => t * t * t

  const total = upMs + downMs

  const animate = (now: number) => {
    const elapsed = now - startTime

    if (elapsed < upMs) {
      const progress = elapsed / upMs
      jumpOffset.value = height * easeOutCubic(progress)
    } else if (elapsed < total) {
      const progress = (elapsed - upMs) / downMs
      jumpOffset.value = height * (1 - easeInCubic(progress))
    } else {
      jumpOffset.value = 0
      isJumping.value = false
      milefizStore.gamedata.isJumping = false
      if (onComplete) onComplete()
      return
    }

    requestAnimationFrame(animate)
  }

  requestAnimationFrame(animate)
}

const jump = () => {
  console.log("jump wird erreicht")
  if (isJumping.value) return
  audioEngine.play3D('meepleJump', {
    x: currentPosition.value[0],
    y: currentPosition.value[1],
    z: currentPosition.value[2]
  })
  isJumping.value = true
  milefizStore.gamedata.isJumping = true

  // Spiele GLB-Animation ab (falls verfügbar)
  if (jumpAction.value) {
    jumpAction.value.reset()
    jumpAction.value.play()
  }

  // Führe immer Custom-Animation für Höhe aus
  animateCustomJump(defaultJumpHeight, defaultUpDuration, defaultFallDuration)
}

// Position für Animation
const animatedPosition = ref<[number, number, number]>([...(props.position ?? [0, 0, 0])])

// auf Änderung der Position reagieren (nur bei tatsächlicher Positionsänderung)
const _lastPropPosition = ref<[number, number, number] | null>(null)
watch(
  () => props.position,
  (newPos) => {
    if (!newPos) return
    const last = _lastPropPosition.value
    if (
      last &&
      Math.abs(last[0] - newPos[0]) < 1e-6 &&
      Math.abs(last[1] - newPos[1]) < 1e-6 &&
      Math.abs(last[2] - newPos[2]) < 1e-6
    ) {
      // no meaningful change -> do nothing
      return
    }

    // record and animate
    _lastPropPosition.value = [newPos[0], newPos[1], newPos[2]]
    animateTo(newPos)
  },
  { deep: true },
)

const speed = 0.08
let moveAnimationFrame: number | null = null

/**
 * Animiert die Bewegung des Charakters zu einer Zielposition auf dem Spielfeld.
 *
 * Ablauf:
 * 1. Vorherige Bewegungsanimation (falls vorhanden) wird abgebrochen.
 * 2. Ein kurzer Sprung wird ausgeführt, während sich die Figur bewegt.
 * 3. Die Position wird frameweise geglättet interpoliert, bis das Ziel erreicht ist.
 *
 * @param target - Zielkoordinaten im 3D-Raum [x, y, z], zu denen sich der Charakter bewegen soll
 */
const animateTo = (target: [number, number, number]) => {
  if (moveAnimationFrame) cancelAnimationFrame(moveAnimationFrame)

  if (!isJumping.value) {
    isJumping.value = true
    if(hasInitializedMoved.value) {
      setTimeout(() => {
        audioEngine.play3D('meepleMove', {
          x: target[0],
          y: target[1],
          z: target[2]
        })
      }, 200)
      }else{
        hasInitializedMoved.value = true
      }
    // starte kleinen Sprung und binde Bewegungsende an das Sprungende
    animateCustomJump(smallJumpHeight, smallUpDuration, smallFallDuration, () => {
      // Nach der Landung Position fixieren
      animatedPosition.value = target
      moveAnimationFrame = null
    })
  }

  const speed = 0.08
  const animate = () => {
    const [x, y, z] = animatedPosition.value
    const [tx, ty, tz] = target

    const nx = x + (tx - x) * speed
    const ny = y + (ty - y) * speed
    const nz = z + (tz - z) * speed

    animatedPosition.value = [nx, ny, nz]

    const dist = Math.sqrt((tx - nx) ** 2 + (tz - nz) ** 2)
    if (dist > 0.02 && isJumping.value) {
      moveAnimationFrame = requestAnimationFrame(animate)
    } else {
      // Wenn Sprung beendet oder das Ziel erreicht ist, Position fixieren
      animatedPosition.value = target
      moveAnimationFrame = null
    }
  }

  animate()
}

/**
 * Setzt die Position des Charakters
 *
 * Wird als Fallback verwendet, falls kein `animateTo` verfügbar ist
 * oder wenn ein sofortiger Snap auf die Zielposition erwünscht ist.
 *
 * @param {[number, number, number]} pos - Zielposition [x, y, z]
 */
const setPositionImmediate = (pos: [number, number, number]) => {
  animatedPosition.value = [pos[0], pos[1], pos[2]]
}

/**
 * Gibt Methoden und reactive Refs für Elternkomponenten frei.
 *
 * - `setRotation(yRotation)` : setzt die Y-Rotation des Charakters
 * - `jump()` : startet die Sprung-Animation
 * - `characterPosition` : Ref auf das `TresGroup`-Referenzobjekt
 * - `meepleId` : identifier des Meeple
 * - `getPosition()` : liefert die aktuelle Weltposition (inkl. Jump-Offset)
 * - `animateTo(target)` : animiert den Charakter zu `target` (smooth)
 * - `setPositionImmediate(pos)` : setzt Position ohne Animation
 */
defineExpose({
  setRotation,
  jump,
  characterPosition,
  meepleId: props.meepleId,
  getPosition: () => currentPosition.value,
  // Expose animateTo so parent can trigger movement directly
  animateTo,
  setPositionImmediate,
})

//Debug: Logging wenn GameCharacter gemounted werden
onMounted(() => {
  console.log('GameCharacter mounted:', props.meepleId, '| Barrier:', props.barrier)
})
</script>

<template>
  <TresGroup
    ref="characterPosition"
    :position="currentPosition"
    :rotation="[0, characterRotation, 0]"
  >
    <primitive v-if="state" :object="state?.scene" />
  </TresGroup>
</template>
