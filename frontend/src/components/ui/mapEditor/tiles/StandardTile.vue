<script setup lang="ts">
import { computed } from 'vue'

type Connections = {
  up: boolean
  down: boolean
  left: boolean
  right: boolean
}

const props = defineProps<{
  x: number
  y: number
  type: 'start' | 'goal' | 'tile' | 'barrier'
  selected: boolean
  connections: Connections
}>()

type Direction = 'up' | 'down' | 'left' | 'right'

const emit = defineEmits<{
  (e: 'add', dir: Direction): void
  (e: 'select'): void
}>()

const TILE_SIZE = 80

const style = computed(() => ({
  left: `${props.x * TILE_SIZE}px`,
  top: `${props.y * TILE_SIZE}px`,
}))
</script>

<template>
  <div
    class="tile"
    :class="{ selected }"
    :style="style"
    @click.stop="emit('select')"
  >
    <div class="tile-icon">
      <img v-if="type === 'start'" src="/mapEditorIcons/base.png" class="tile-icon-img" />
      <img v-else-if="type === 'goal'" src="/mapEditorIcons/goal.png" class="tile-icon-img" />
      <img v-else-if="type === 'barrier'" src="/mapEditorIcons/barrier.png" class="tile-icon-img" />
    </div>

    <!-- Plus Buttons -->
    <div v-if="!connections.up"    class="plus up"    @click.stop="emit('add', 'up')">+</div>
    <div v-if="!connections.down"  class="plus down"  @click.stop="emit('add', 'down')">+</div>
    <div v-if="!connections.left"  class="plus left"  @click.stop="emit('add', 'left')">+</div>
    <div v-if="!connections.right" class="plus right" @click.stop="emit('add', 'right')">+</div>

    <!-- Verbindungslinien -->
    <div v-if="connections.up"    class="connection up"></div>
    <div v-if="connections.down"  class="connection down"></div>
    <div v-if="connections.left"  class="connection left"></div>
    <div v-if="connections.right" class="connection right"></div>
  </div>
</template>

<style scoped>
.tile {
  position: absolute;
  width: 80px;
  height: 80px;
  background: #2c2c2c;
  border: 2px solid #777;
  box-sizing: border-box;
  cursor: pointer;
  border-radius: 50%;
}

.tile.selected {
  border-color: #ffd36a;
  box-shadow: 0 0 10px rgba(255, 211, 106, 0.6);
}

.tile-icon {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.tile-icon-img {
  width: 60%;
  height: 60%;
  object-fit: contain;
  filter: invert(1);
}

/* Plus Buttons */
.plus {
  position: absolute;
  width: 22px;
  height: 22px;
  background: #00552d;
  border-radius: 50%;
  color: white;
  font-size: 18px;
  line-height: 25px;
  text-align: center;
  cursor: pointer;
  user-select: none;
}

.plus.up    { top: -35px; left: 50%; transform: translateX(-50%); }
.plus.down  { bottom: -35px; left: 50%; transform: translateX(-50%); }
.plus.left  { left: -35px; top: 50%; transform: translateY(-50%); }
.plus.right { right: -35px; top: 50%; transform: translateY(-50%); }

.connection {
  position: absolute;
  background: #ffd36a;
}

.connection.up {
  width: 4px;
  height: 40px;
  top: -45px;
  left: 50%;
  transform: translateX(-50%);
}

.connection.down {
  width: 4px;
  height: 40px;
  bottom: -45px;
  left: 50%;
  transform: translateX(-50%);
}

.connection.left {
  width: 40px;
  height: 4px;
  left: -45px;
  top: 50%;
  transform: translateY(-50%);
}

.connection.right {
  width: 40px;
  height: 4px;
  right: -45px;
  top: 50%;
  transform: translateY(-50%);
}
</style>
