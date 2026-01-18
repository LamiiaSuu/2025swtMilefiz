<script setup lang="ts">
import { inject, onMounted, onUnmounted, ref } from 'vue'

const emitSave = inject<() => Promise<void> | void>('emitSave')

const isSaving = ref(false)

async function handleSave() {
    if (!emitSave || isSaving.value) return
    try {
        isSaving.value = true
        await emitSave()
    } finally {
        isSaving.value = false
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
    <div class="save-button-wrapper">
        <button class="editor-action-button" @click="handleSave" :disabled="isSaving">
            <img v-if="!isSaving" src="/mapEditorIcons/save.png" class="action-icon editor-invert-color" />
            <span v-else class="lds-roller">
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
            </span>
        </button>
    </div>
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


.lds-roller,
.lds-roller div,
.lds-roller div:after {
    box-sizing: border-box;
}

.lds-roller {
    display: inline-block;
    position: relative;
    width: 80px;
    height: 80px;
}

.lds-roller div {
    animation: lds-roller 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
    transform-origin: 40px 40px;
}

.lds-roller div:after {
    content: " ";
    display: block;
    position: absolute;
    width: 7.2px;
    height: 7.2px;
    border-radius: 50%;
    background: white;
    margin: -3.6px 0 0 -3.6px;
}

.lds-roller div:nth-child(1) {
    animation-delay: -0.036s;
}

.lds-roller div:nth-child(1):after {
    top: 62.62742px;
    left: 62.62742px;
}

.lds-roller div:nth-child(2) {
    animation-delay: -0.072s;
}

.lds-roller div:nth-child(2):after {
    top: 67.71281px;
    left: 56px;
}

.lds-roller div:nth-child(3) {
    animation-delay: -0.108s;
}

.lds-roller div:nth-child(3):after {
    top: 70.90963px;
    left: 48.28221px;
}

.lds-roller div:nth-child(4) {
    animation-delay: -0.144s;
}

.lds-roller div:nth-child(4):after {
    top: 72px;
    left: 40px;
}

.lds-roller div:nth-child(5) {
    animation-delay: -0.18s;
}

.lds-roller div:nth-child(5):after {
    top: 70.90963px;
    left: 31.71779px;
}

.lds-roller div:nth-child(6) {
    animation-delay: -0.216s;
}

.lds-roller div:nth-child(6):after {
    top: 67.71281px;
    left: 24px;
}

.lds-roller div:nth-child(7) {
    animation-delay: -0.252s;
}

.lds-roller div:nth-child(7):after {
    top: 62.62742px;
    left: 17.37258px;
}

.lds-roller div:nth-child(8) {
    animation-delay: -0.288s;
}

.lds-roller div:nth-child(8):after {
    top: 56px;
    left: 12.28719px;
}

@keyframes lds-roller {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}
</style>