<script setup lang="ts">
import { inject, onMounted, onUnmounted } from 'vue'

const emitSave = inject<() => Promise<void> | void>('emitSave')

async function handleSave() {
    if (emitSave) {
        await emitSave()
    }
}

onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});

onUnmounted(() => {
    window.removeEventListener("keydown", onKeypress)
})

// Hotkey S
const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "s") {
        handleSave()
    }
}
</script>
<template>
    <button class="editor-action-button" @click="handleSave">
        <img src="/mapEditorIcons/save.png" class="action-icon editor-invert-color" />
    </button>
</template>

<style scoped>
.editor-action-button {
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

.editor-invert-color {
    filter: invert(1);
}


.editor-action-button:hover {
    transform: scale(1.05);
}
</style>