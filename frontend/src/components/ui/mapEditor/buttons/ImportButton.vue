<script setup lang="ts">
import { ref, inject, onMounted, onUnmounted } from 'vue'

const fileInput = ref<HTMLInputElement>()
const emitImport = inject<(file: File) => void>("emitImport")

function handleImport() {
    fileInput.value?.click()
}

function handleFileSelected(event: Event) {
    const target = event.target as HTMLInputElement
    const file = target.files?.[0]

    if (file && file.name.endsWith(".json")) {
        if (emitImport) {
            emitImport(file)
        }
    } else {
        alert("Board-Datei auswählen")
    }

    // Reset input
    target.value = ''
}


onMounted(() => {
    window.addEventListener("keydown", onKeypress)
});

onUnmounted(() => {
    window.removeEventListener("keydown", onKeypress)
})

// Hotkey O
const onKeypress = (e: KeyboardEvent) => {
    if (e.key.toLocaleLowerCase() === "o") {
        handleImport()
    }
}

</script>
<template>
    <input ref="fileInput" type="file" accept=".json" style="display: none" @change="handleFileSelected" />
    <button class="editor-action-button" @click="handleImport">
        <img src="/mapEditorIcons/folder.png" class="action-icon editor-invert-color" />
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