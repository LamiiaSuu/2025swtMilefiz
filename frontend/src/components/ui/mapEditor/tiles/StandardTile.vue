<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  x: number
  y: number
  selected: boolean
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
    <!-- Plus Buttons -->
    <div class="plus up"    @click.stop="emit('add', 'up')">+</div>
    <div class="plus down"  @click.stop="emit('add', 'down')">+</div>
    <div class="plus left"  @click.stop="emit('add', 'left')">+</div>
    <div class="plus right" @click.stop="emit('add', 'right')">+</div>
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
</style>
