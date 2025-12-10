<script lang="ts" setup>
import { ref } from 'vue';
import BackButton from '../pages/BackButton.vue';


</script>

<template>
	<div class="popup">
		<div class="popup-inner">
            <BackButton :to="{ name: 'Homepage' }" class="popup-close"/>
		</div>
	</div>
</template>

<style scoped>
</style>