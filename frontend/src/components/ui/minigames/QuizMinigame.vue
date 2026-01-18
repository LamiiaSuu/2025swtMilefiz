<script setup lang="ts">
import { tUI } from '@/i18n'
import { useMilefizStore } from '@/stores/milefizstore'
import { computed, onMounted, ref, watch } from 'vue'
import CountdownBar from './CountdownBar.vue'
import { currentLocale } from '@/i18n/index'

const props = defineProps<{
  duel: any
}>()


const emit = defineEmits<{
  (e: 'close'): void
}>()

const store = useMilefizStore()

const isFinished = computed(() => {
  return props.duel?.state?.finished ?? false
})

const isWinner = computed(() => {
  return props.duel?.state?.winner === store.gamedata.playerId
})

const question = computed(() => {
  const q = props.duel?.state?.question
  if (q === undefined) return
  return q[currentLocale.value]
})

const answers = computed(() => {
  const a = props.duel?.state?.answers

  if (a === undefined) return
  return a[currentLocale.value]
})

const selectedAnswer = ref<number | null>(null)

const colorMap: Record<string, string> = {
  RED: '#ff4d4d',
  BLUE: '#4d79ff',
  GREEN: '#4dff6e',
  YELLOW: '#ffd54d',
}


const selectedColor = computed(() => {
  const me = store.getOwnPlayer?.()
  const c = me?.color ?? 'RED'
  return colorMap[c.toUpperCase()] ?? colorMap['RED']
})


const correctAnswer = computed(() => {
  return props.duel?.state?.correctAnswer ?? -1
})


onMounted(() => {
  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/quiz/getQuestion`,
    { id: store.gamedata.playerId }
  )

})


const selectAnswer = (idx: string | number) => {

  if (typeof selectedAnswer.value === 'number') return

  selectedAnswer.value = typeof idx === 'number' ? idx : parseInt(String(idx))

  store.sendLobbyMessage(
    `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/quiz/sendAnswer/${idx}`,
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
    <h2 class="dice-title">{{ tUI('MINIGANE_QUIZ_TITLE') }}</h2>

    <CountdownBar :seconds="duel.timeOut" />

    <div class="quiz-question">{{ question }}</div>

    <div class="quiz-answers" role="list">
      <button class="quiz-answer-button" v-for="(ans, idx) in answers" :key="idx" @click="selectAnswer(idx)" :class="{
        selected: selectedAnswer === Number(idx),
        correct: isFinished && Number(idx) === correctAnswer,
        incorrect: isFinished && correctAnswer !== -1 && Number(idx) !== correctAnswer
      }" :style="selectedAnswer === Number(idx) ? { '--player-color': selectedColor } : {}" role="listitem">
        <span class="answer-letter">{{ String.fromCharCode(65 + Number(idx)) }}</span>
        <span class="answer-text">{{ ans }}</span>
      </button>
    </div>

    <div v-if="isFinished" class="winner-big">
      <span v-if="isWinner" class="winner-text">{{ tUI('DUEL_WON') }}</span>
      <span v-else class="loser-text">{{ tUI('DUEL_LOST') }}</span>
    </div>
  </div>
</template>

<style scoped>
.dice-card {
  width: 520px;
  max-width: calc(100vw - 48px);
  background: linear-gradient(180deg, #0f1220 0%, #151724 60%);
  border-radius: 14px;
  padding: 20px 22px;
  box-shadow: 0 18px 48px rgba(8, 10, 18, 0.7);
  border: 2px solid rgba(255, 215, 100, 0.06);
  color: #f6f6f6;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: stretch;
}

.dice-title {
  font-family: 'Acme', sans-serif;
  font-size: 1.6rem;
  letter-spacing: 1px;
  color: #ffd66b;
  text-align: center;
  margin: 0;
}

.countdown-placeholder {
  height: 28px;
}

.quiz-question {
  font-family: 'Acme', sans-serif;
  font-weight: 700;
  font-size: 1.5rem;
  color: #fff;
  text-align: center;
  padding: 10px 12px;
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(0, 0, 0, 0.04));
  box-shadow: inset 0 -6px 18px rgba(0, 0, 0, 0.45);
}

.quiz-answers {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 6px;
}

.quiz-answer-button {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  background: linear-gradient(180deg, #11131a 0%, #0c0f14 100%);
  border-radius: 12px;
  padding: 14px 16px;
  border: 2px solid rgba(255, 215, 100, 0.09);
  cursor: pointer;
  color: #f3f3f3;
  transition: transform 150ms ease, box-shadow 150ms ease, background 150ms ease;
  box-shadow: 0 6px 20px rgba(2, 6, 12, 0.6);
  text-align: left;
  overflow: hidden;
}

.quiz-answer-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 30px rgba(2, 6, 12, 0.7), 0 0 18px rgba(255, 215, 100, 0.06) inset;
}

.answer-letter {
  min-width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: linear-gradient(180deg, #ffd66b 0%, #e6b23a 100%);
  color: #111;
  font-weight: 900;
  font-size: 1.05rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.45);
}

.answer-text {
  flex: 1;
  font-size: 1.05rem;
  font-weight: 700
}

.quiz-answer-button.selected {
  position: relative;
  border-color: rgba(255, 255, 255, 0.06);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0.01));
}

.quiz-answer-button.selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 8px;
  background: var(--player-color, #ff4d4d);
  border-top-left-radius: 12px;
  border-bottom-left-radius: 12px;
}

.quiz-answer-button.correct {
  border-color: #ffd700;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  animation: pulse-border 1.5s ease-in-out infinite;
  position: relative;
  z-index: 2;
}

.quiz-answer-button.incorrect {
  opacity: 0.55;
}

@keyframes pulse-border {

  0%,
  100% {
    border-color: #ffd700;
    box-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
  }

  50% {
    border-color: #ffed4e;
    box-shadow: 0 0 20px rgba(255, 215, 0, 0.9);
  }
}

.winner-big {
  margin-top: 18px;
  text-align: center;
  font-size: 1.8rem;
  font-weight: 900;
  font-family: 'Acme', sans-serif;
}

.winner-text,
.loser-text {
  font-family: 'Acme', sans-serif;
}

@media (max-width: 640px) {
  .dice-card {
    width: auto;
    padding: 14px
  }

  .answer-letter {
    min-width: 36px;
    height: 36px;
    font-size: 0.95rem
  }

  .quiz-question {
    font-size: 1.15rem
  }
}
</style>