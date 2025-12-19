<script setup lang="ts">

</script>
<template>
    <button class="action-button">
        <img src="@/assets/hud/JumpingMeeple.png" class="action-icon" />
    </button>
</template>

<style>


.action-button {
    position: relative;
    padding: 5px;
    width: 100px;
    height: 100px;
    border: 2px solid #c8a25d;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    background: #234420;
    transition: filter 120ms ease-out, transform 120ms ease-out;
}

.action-button:hover {
  transform: scale(1.05);
}

</style>