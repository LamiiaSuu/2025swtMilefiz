<script setup lang="ts">
// https://cientos.tresjs.org/guide/loaders/use-gltf
import { useGLTF } from '@tresjs/cientos'
import { watchEffect, watch, ref, computed, onMounted } from 'vue'
import { useMilefizStore } from "@/stores/milefizstore";

// Zugriff auf globalen PiniaStore
const milefizStore = useMilefizStore()

//Definierte Props für Augen, Körperfarbe und Position
const props = defineProps<{
  bodyColor?: string | number
  eyeColor?: string | number
  position?: [number, number, number]
  meepleId: string
  barrier?: boolean
}>()

const characterRotation = ref(0)
const characterPosition = ref(null)

//Variablen für Anpassung des Sprungs definiert
const jumpOffset = ref(0)
const isJumping = ref(false)

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
const yOffset = computed(() => props.barrier ? 0.85 : 0.135)

// Berechne aktuelle Position (inklusive jumpOffset)
const currentPosition = computed<[number, number, number]>(() => [
  animatedPosition.value[0],
  animatedPosition.value[1] + jumpOffset.value + yOffset.value,
  animatedPosition.value[2]
])

//Rock by Poly by Google [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/dmRuyy1VXEv)
// Block Character by J-Toastie [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/ozSIyRIcIj)

const modelPath = computed(() => props.barrier ? '/Rock.glb' : '/Block Character.glb')
const { state } = useGLTF(modelPath, { draco: true })

// Unterschiedliche Scale für Barriere und Character
const scale = computed(() => props.barrier ? 1.5 : 0.55)
watchEffect(async () => {
  if (state.value?.scene) {
    state.value.scene.scale.set(scale.value, scale.value, scale.value)

    //Geht über CharacterMesh und unterscheidet nach Körper und Eyes
    state.value.scene.traverse((child: any) => {
      if (child.material) {
        // Unterscheidung zwischen body und eye_color Material
        if (child.material.name === 'body' || child.name?.includes('body')) {
          if (props.bodyColor) {
            child.material.color.set(props.bodyColor)
          }
        } else if (child.material.name === 'eye_color' || child.name?.includes('eye')) {
          if (props.eyeColor) {
            child.material.color.set(props.eyeColor)
          }
        }
      }
    })

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
})

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

// Updated die Rotation des Charakters
const setRotation = (yRotation: number) => {
  characterRotation.value = yRotation
}

// Sprung-Animation 
const animateCustomJump = (
  height = defaultJumpHeight,
  upMs = defaultUpDuration,
  downMs = defaultFallDuration,
  onComplete?: () => void
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
      milefizStore.isJumping = false
      if (onComplete) onComplete()
      return
    }

    requestAnimationFrame(animate)
  }

  requestAnimationFrame(animate)
}

const jump = () => {
  if (isJumping.value) return

  isJumping.value = true
  milefizStore.isJumping = true

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
  { deep: true }
)

const speed = 0.08
let moveAnimationFrame: number | null = null

/**
 * Animiert die Bewegung des Charakters zu einer Zielposition auf dem Spielfeld.
 * 
 * - inkl. Sprung und Drehung
 * 
 * Ablauf:
 * 1. Vorherige Bewegungsanimation (falls vorhanden) wird abgebrochen.  
 * 2. Charakter wird in Richtung des Ziels gedreht (`rotateToward`).  
 * 3. Ein kurzer Sprung wird ausgeführt, während sich die Figur bewegt.  
 * 4. Die Position wird frameweise geglättet interpoliert, bis das Ziel erreicht ist.
 * 
 * @param target - Zielkoordinaten im 3D-Raum [x, y, z], zu denen sich der Charakter bewegen soll
 */
const animateTo = (target: [number, number, number]) => {
  if (moveAnimationFrame) cancelAnimationFrame(moveAnimationFrame)

  rotateToward(target)

  if (!isJumping.value) {
    isJumping.value = true
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
 * Dreht den Charakter sanft in Richtung einer Zielposition.
 * 
 * Berechnet den Winkel zwischen der aktuellen Position und der Zielposition 
 * und interpoliert die Y-Rotation über eine kurze Zeitspanne, um 
 * eine fließende Drehbewegung zu erzeugen.
 * 
 * - wählt immer den kürzesten Drehweg
 * - Verwendet `Math.atan2()` zur Winkelberechnung im XZ-Raum.  
 * - Normalisiert Winkel auf den Bereich [-π, π], um Sprünge zu vermeiden.  
 * - Führt die Drehung innerhalb von ~200 ms aus (Ease-in/Ease-out Kurve).  
 * 
 * @param target - Zielkoordinaten [x, y, z], in deren Richtung der Charakter schauen soll
 */
const rotateToward = (target: [number, number, number]) => {
  const [x, , z] = animatedPosition.value
  const [tx, , tz] = target

  const dx = tx - x
  const dz = tz - z

  const targetRotation = Math.atan2(dx, dz)
  let startRotation = characterRotation.value

  // --- beide Winkel normalisieren auf [-π, π] ---
  const normalize = (angle: number) => ((angle + Math.PI) % (2 * Math.PI)) - Math.PI
  startRotation = normalize(startRotation)
  const normalizedTarget = normalize(targetRotation)

  // --- Differenz auf kürzesten Weg ---
  let diff = normalizedTarget - startRotation
  if (diff > Math.PI) diff -= 2 * Math.PI
  if (diff < -Math.PI) diff += 2 * Math.PI

  const duration = 200
  const startTime = Date.now()

  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)

    // weiches Interpolieren (Ease in/out optional)
    const easedProgress = 0.5 - 0.5 * Math.cos(progress * Math.PI)
    characterRotation.value = startRotation + diff * easedProgress

    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      characterRotation.value = normalizedTarget
    }
  }

  animate()
}

// Gibt Rotation und Position frei
defineExpose({ setRotation, jump, characterPosition, meepleId: props.meepleId, getPosition: () => currentPosition.value, })

//Debug: Logging wenn GameCharacter gemounted werden
onMounted(() => {
  console.log('GameCharacter mounted:', props.meepleId, '| Barrier:', props.barrier)
})
</script>

<template>
  <TresGroup ref="characterPosition" :position="currentPosition" :rotation="[0, characterRotation, 0]">
    <primitive v-if="state" :object="state?.scene" />
  </TresGroup>
</template>
