

<script setup>
import { storeToRefs } from 'pinia'
import { useAudioStore } from '@/stores/audioStore'
import { tUI } from '@/i18n'

const audioStore = useAudioStore()
const { channels } = storeToRefs(audioStore)

const audioSettings = [
  { key: 'music', label: tUI('MUSIC') },
  { key: 'ambient', label: tUI('AMBIENCE') },
  { key: 'sfx', label: tUI('SFX') },
]

</script>

<template>

    <!-- AUDIO SETTINGS -->
    <div class="settings-panel">

        <div class="audio-setting" v-for="setting in audioSettings" :key="setting.key">
            <div class="audio-header">
                <span class="audio-title">{{ setting.label }}</span>

                <button
                class="mute-button"
                :class="{ muted: channels[setting.key].muted }"
                @click="audioStore.toggleMute(setting.key)"
                >
                🔈
                </button>
            </div>

            <input
                type="range"
                min="0"
                max="100"
                step="1"
                :disabled="channels[setting.key].muted"
                v-model="channels[setting.key].volume"
            />
        </div>

    </div>
</template>

<style scoped>
.settings {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;

  align-items: center;
  overflow: hidden;
  padding-bottom: 4rem;
}

.settings::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("/backgrounds/BackgroundTest.webp");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  filter: blur(4px);
  z-index: -1;
}

.settings-button-container {
  display: flex;
  flex-direction: column;
  gap: 2vh;
  align-items: center;
  margin-top: 1.2vh;
}

.settings-menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

.settings-panel {
  margin-top: 0.75vw;
  width: 36vw;
  max-width: 520px;
  min-width: 300px;
  padding: 1.6vw;
  border-radius: 18px;

  background:
    linear-gradient(
      rgba(40, 60, 35, 0.92),
      rgba(25, 40, 25, 0.92)
    );

  box-shadow:
    inset 0 0 0 2px rgba(255, 255, 255, 0.06),
    0 12px 30px rgba(0, 0, 0, 0.45);

  border: 3px solid rgba(120, 160, 110, 0.25);
}


.audio-setting {
  display: flex;
  flex-direction: column;
  gap: 0.6vh;
  margin-bottom: 1vh;
}

.audio-setting:last-child {
  margin-bottom: 0;
}

.audio-setting:not(:last-child)::after {
  content: "";
  height: 2px;
  margin-top: 1vw;
  background: linear-gradient(
    to right,
    transparent,
    rgba(140, 190, 120, 0.4),
    transparent
  );
}

.audio-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.audio-title {
  font-size: 1.85em;
  font-weight: 700;
  letter-spacing: 0.07em;
  color: #e6f5dc;
  text-shadow: 0 2px 0 rgba(0,0,0,0.4);
}

.mute-button {
  background: none;
  border: none;
  font-size: 1.75em;
  cursor: pointer;
  opacity: 0.85;
}

.mute-button.muted {
  filter: grayscale(0);
}

.mute-button.muted::after {
  content: "✖";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -55%);
  font-size: 1.2em;
  color: #c43a3a; /* dunkles, edles Rot */
  text-shadow:
    0 1px 2px rgba(0,0,0,0.6);
  pointer-events: none;
}

input[type="range"] {
  -webkit-appearance: none;
  appearance: none;
  height: 14px;
  border-radius: 999px;
  background: linear-gradient(
    to right,
    #7ecb6f,
    #b5e48c
  );
  gap: 5vw;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 30px;
  width: 30px;
  border-radius: 50%;
  background: #5b3a1e; /* Holz-Farbe */
  border: 2px solid #caa472;
  box-shadow: 0 3px 6px rgba(0,0,0,0.5);
  cursor: pointer;
}

input[type="range"]:disabled {
  opacity: 0.4;
  filter: grayscale(0.8);
}

input[type="range"]:disabled::-webkit-slider-thumb {
  box-shadow: none;
}
</style>
