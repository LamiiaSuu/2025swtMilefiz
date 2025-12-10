<template>
    <div class="component-list">
        <slot></slot>
    </div>
</template>

<style scoped>
    .component-list {
        display: flex;
        height: 50%;
        flex-direction: column;
        flex-wrap: wrap;
        align-items: center;
        gap: 2vh;
    }

    .component-list > * {
        flex: 1;
    }
</style>