<template>
    <div class="component-list">
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

    .component-list > * {
        width: 25%;
        min-width: 400px;
    }
</style>