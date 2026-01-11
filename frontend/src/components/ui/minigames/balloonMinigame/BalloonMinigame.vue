<script setup lang="ts">
import { tUI } from '@/i18n'
import { useMilefizStore } from '@/stores/milefizstore'
import { computed, onMounted, ref, watch } from 'vue'

const props = defineProps<{
  duel: any
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const store = useMilefizStore()
const myLocalClicks = ref(0)
const showInstructions = ref(true)

onMounted(() => {
  setTimeout(() => {
    showInstructions.value = false
  }, 3000)
})

/**
 * Findet die PlayerId zu einer gegebenen MeepleId
 */
function getPlayerIdByMeeple(meepleId: string): string | null {
  const lobby = store.gamedata.lobby
  if (!lobby) return null

  for (const player of lobby.players) {
    if (player.meeples?.some((m) => m.id === meepleId)) {
      return player.id
    }
  }
  return null
}

const isPlayer1 = computed(() => {
  const firstPlayerId = getPlayerIdByMeeple(props.duel?.firstMeeple)
  return firstPlayerId === store.gamedata.playerId
})

const myPhase = computed(() => {
  const state = props.duel?.state
  if (!state) return 0

  return isPlayer1.value ? state.phasePlayer1 : state.phasePlayer2
})

const rivalPhase = computed(() => {
  const state = props.duel?.state
  if (!state) return 0

  return isPlayer1.value ? state.phasePlayer2 : state.phasePlayer1
})

const isWinner = computed(() => {
  const winnerPlayerId = getPlayerIdByMeeple(props.duel?.state?.winner)
  return winnerPlayerId === store.gamedata.playerId
})

const isFinished = computed(() => {
  return props.duel?.state?.finished ?? false
})

function getPlayerNameByMeeple(meepleId: string) {
  const lobby = store.gamedata.lobby
  if (!lobby) return '?'

  for (const player of lobby.players) {
    if (player.meeples?.some(m => m.id === meepleId)) {
      return player.playerName ?? '?'
    }
  }
  return '?'
}

function getPlayerColorByMeeple(meepleId: string) {
  const lobby = store.gamedata.lobby
  if (!lobby) return 'RED'

  for (const player of lobby.players) {
    if (player.meeples?.some(m => m.id === meepleId)) {
      return player.color || 'RED'
    }
  }
  return 'RED'
}

function getBalloonImage(meepleId: string, phase: number) {
  const color = getPlayerColorByMeeple(meepleId)
  return `/balloonMinigame/${color}_LVL${phase}.jpg`
}

const handleClick = () => {
  if (isFinished.value || showInstructions.value) return

  myLocalClicks.value++

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/balloon/click`,
    { id: store.gamedata.playerId }
  )
}

watch(isFinished, (finished) => {
  if (finished) {
    setTimeout(() => {
      emit('close')
    }, 2000)
  }
})
</script>

<template>
   <div class="dice-card no-select">
    <h2 class="dice-title">
      {{ tUI('MINIGAME_BALLOON_TITLE') }}
    </h2>

    <!-- COUNTDOWN -->
    <CountdownBar :seconds="duel.timeOut" />

    <!-- 3-Sekunden Instruktion -->
    <div v-if="showInstructions" class="instructions">
      <p class="instruction-text">{{ tUI('MINIGAME_BALLOON_INSTRUCTION') }}</p>
    </div>

    <!-- Spieler & Ballons -->
    <div v-else class="players">
      <!-- Spieler 1 -->
      <div class="player">
        <div class="balloon-wrapper">
          <img
            :src="getBalloonImage(duel.firstMeeple, isPlayer1 ? myPhase : rivalPhase)"
            :alt="`Balloon Phase ${isPlayer1 ? myPhase : rivalPhase}`"
            class="balloon-image"
            :class="{ exploded: (isPlayer1 ? myPhase : rivalPhase) === 4 }"
          />
        </div>
        <h3 :style="{ color: getPlayerColorByMeeple(duel.firstMeeple) }">
          {{ getPlayerNameByMeeple(duel.firstMeeple) }}
        </h3>
      </div>

      <!-- Spieler 2 -->
      <div class="player">
        <div class="balloon-wrapper">
          <img
            :src="getBalloonImage(duel.secondMeeple, isPlayer1 ? rivalPhase : myPhase)"
            :alt="`Balloon Phase ${isPlayer1 ? rivalPhase : myPhase}`"
            class="balloon-image"
            :class="{ exploded: (isPlayer1 ? rivalPhase : myPhase) === 4 }"
          />
        </div>
        <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
          {{ getPlayerNameByMeeple(duel.secondMeeple) }}
        </h3>
      </div>
    </div>

    <!-- Klick-Button -->
    <button
      v-if="!isFinished && !showInstructions"
      class="dice-roll-button"
      @click="handleClick"
    >
      {{ tUI('MINIGAME_BALLOON_CLICK') }}!
    </button>

    <!-- GEWINNER -->
    <div v-if="isFinished" class="winner-big">
      <span v-if="isWinner" class="winner-text">
        {{ tUI('DUEL_WON') }}
      </span>

      <span v-else class="loser-text">
        {{ tUI('DUEL_LOST') }}
      </span>
    </div>
  </div>
</template>