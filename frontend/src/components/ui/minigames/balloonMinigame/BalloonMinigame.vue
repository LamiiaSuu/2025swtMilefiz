<script setup lang="ts">
import { tUI } from '@/i18n'
import { useMilefizStore } from '@/stores/milefizstore'
import { computed, onMounted, ref, watch } from 'vue'
import CountdownBar from '../CountdownBar.vue'

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
    if (player.meeples?.some((m) => m.id === meepleId)) {
      return player.playerName ?? '?'
    }
  }
  return '?'
}

function getPlayerColorByMeeple(meepleId: string) {
  const lobby = store.gamedata.lobby
  if (!lobby) return 'RED'

  for (const player of lobby.players) {
    if (player.meeples?.some((m) => m.id === meepleId)) {
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

    <!-- Spieler & Ballons -->
    <div class="players">
      <!-- Spieler 1 -->
      <div class="player">
        <div class="balloon-wrapper">
          <img
            :src="getBalloonImage(duel.firstMeeple, isPlayer1 ? myPhase : rivalPhase)"
            :alt="`Balloon Phase ${isPlayer1 ? myPhase : rivalPhase}`"
            class="balloon-image"
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
          />
        </div>
        <h3 :style="{ color: getPlayerColorByMeeple(duel.secondMeeple) }">
          {{ getPlayerNameByMeeple(duel.secondMeeple) }}
        </h3>
      </div>
    </div>

    <!-- Klick-Button -->
    <button
      v-if="!isFinished"
      class="dice-roll-button"
      :disabled="showInstructions"
      @click="handleClick"
    >
      {{ tUI('MINIGAME_BALLOON_CLICK') }}
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

    <!-- INSTRUCTIONS OVERLAY -->
    <div v-if="showInstructions" class="instructions-overlay">
      <div class="instructions-popup">
        <p class="instruction-text">{{ tUI('MINIGAME_BALLOON_INSTRUCTION') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.instructions-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(3px);
}

.instructions-popup {
  background: rgba(20, 20, 20, 0.95);
  border: 3px solid #ffcc00;
  border-radius: 15px;
  padding: 30px 40px;
  box-shadow: 0 0 30px rgba(255, 204, 0, 0.6);
}

.instruction-text {
  font-family: 'Acme', sans-serif;
  font-size: 2rem;
  font-weight: 900;
  color: #ffcc00;
  text-shadow:
    0 0 5px rgba(0, 0, 0, 0.95),
    2px 2px 5px rgba(0, 0, 0, 0.95);
  margin: 0;
}

.players {
  display: grid;
  grid-template-columns: 1fr 1fr;
  justify-content: space-between;
  margin: 0px 0 15px;
  gap: 12px;
}

.player {
  text-align: center;
}

.player h3 {
  font-size: 1.8rem;
  font-weight: 900;
  margin: 0px 0 6px 0;

  text-shadow:
    0 0 1px rgba(0, 0, 0, 0.95),
    1px 0 1px rgba(0, 0, 0, 0.9),
    -1px 0 1px rgba(0, 0, 0, 0.9),
    0 1px 1px rgba(0, 0, 0, 0.9),
    0 -1px 1px rgba(0, 0, 0, 0.9);

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'Acme', sans-serif;
}

.balloon-wrapper {
  position: relative;
  width: 125px;
  height: 150px;
  margin: 10px auto 0;
}

.balloon-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.winner-big {
  margin-top: 18px;
  text-align: center;
  font-size: 1.8rem;
  font-weight: 900;
  font-family: 'Acme', sans-serif;
}

.dice-title {
  font-family: 'Acme', sans-serif;
  font-size: 1.6rem;
  font-weight: 900;
  text-align: center;
  margin: 0 0 8px 0;
}

.dice-roll-button {
  font-family: 'Acme', sans-serif;
  font-weight: 900;
  font-size: 1.6rem;

  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

.dice-roll-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.winner-text,
.loser-text {
  font-family: 'Acme', sans-serif;
}

.no-select {
  -webkit-user-select: none;
  -ms-user-select: none;
  user-select: none;

  -webkit-user-drag: none;
}

.dice-card {
  position: relative;
}
</style>