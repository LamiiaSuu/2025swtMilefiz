<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router'
import BackButton from '../pages/BackButton.vue';

const router = useRouter()

const newGameStart = () => {
  router.push({ name: 'game-start' })
}

</script>

<template>
	<div class="popup">
		


		<div class="button-container">
			<BackButton :to="{ name: 'Homepage' }">&lt; Hauptmenü</BackButton>
			
      		<button class="menu-button" @click="newGameStart">Neues Spiel</button>
		</div>
	</div>

</template>

<style scoped></style>