<script lang="ts" setup>

defineProps({
  row: Boolean
})

</script>

<template>
    <div class="component-list" :class="{ 'row': row }">
        <slot></slot>
    </div>
</template>

<style scoped>
    .component-list {
        display: flex;
        max-height: 50%;
        flex-direction: column;
        flex-wrap: nowrap;
        align-items: center;
        gap: 2vh;
    }

    .component-list.row {
        flex-direction: row;
        justify-content: center;
        gap: 5vh;
        max-height: 50%;
    }

    .component-list > * {
        width: 25%;
        min-width: 400px;
    }

    .component-list.row > * {
        max-height: 100%;
        width: 100%;
    }
</style>