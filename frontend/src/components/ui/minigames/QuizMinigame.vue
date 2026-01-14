<script setup lang="ts">
import { tUI } from '@/i18n'
import { useMilefizStore } from '@/stores/milefizstore'
import { computed, onMounted, ref, watch } from 'vue'
import CountdownBar from './CountdownBar.vue'
import { currentLocale } from '@/i18n/index'

const timerStarted = ref(false) 

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

// const showInstructions = ref(true) 

onMounted(() => {
    store.sendLobbyMessage(
        `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/quiz/getQuestion`,
        { id: store.gamedata.playerId }
    )
  setTimeout(() => {
    // showInstructions.value = false
    timerStarted.value = true
  }, 2000)

})



const selectAnswer = (idx: string | number) => {
    store.sendLobbyMessage(
        `/app/milefiz/lobby/${store.gamedata.lobby?.id}/duel/${props.duel.duelId}/quiz/sendAnswer/${idx}`,
        { id: store.gamedata.playerId}
    )
}

watch(isFinished, (finished) => {
  if (finished) {
    setTimeout(() => {
      emit('close')
    }, 1000)
  }
})

</script>

<template>
    <div class="dice-card no-select">
        <h2 class="dice-title">
            {{ tUI('MINIGANE_QUIZ_TITLE') }}
        </h2>
        <CountdownBar v-if="timerStarted" :seconds="duel.timeOut" />

        <div v-else class="countdown-placeholder"></div>
        
        <div class="quiz-question">
          {{ question }}
        </div>
        
        <div class="quiz-answers">
          <button class="quiz-answer-button" v-for="(ans, idx) in answers" @click="selectAnswer(idx)">
            {{ ans }}
          </button>
        </div>

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

<style scoped>

.quiz-answer-button {
  font-family: "Acme", sans-serif;
  font-weight: 900;
  font-size: 1.6rem;

  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}
</style>