<script setup>
import { useRouter } from 'vue-router'
import Header from '@/components/ui/pages/Header.vue'
import BackButton from '@/components/ui/pages/BackButton.vue'

const router = useRouter()

const goHome = () => {
  router.push({ name: 'Homepage' })
}
</script>

<template>
  <div class="not-found">
    <Header overlay>404 - Seite nicht gefunden</Header>

    <div class="content">
      <h1>404</h1>
      <p>
        Diese Seite existiert nicht.<br />
        Vielleicht haben wir sie auch noch nicht implementiert.
      </p>

      <div class="button-container">

        <BackButton :to="{ name: 'Homepage' }" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  padding-bottom: 4rem;
}

.not-found::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image: url("/backgrounds/BackgroundTest.webp");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  filter: blur(4px);
  z-index: -1;
}

.content {
  margin-top: 10vh;
  text-align: center;
  color: var(--ui-text, #f0f0f0);
}

.content h1 {
  font-size: 6rem;
  margin-bottom: 0.5rem;
  text-shadow: 0 0 12px rgba(0,0,0,0.6);
}

.content p {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 3vh;
}

.button-container {
  display: flex;
  flex-direction: column;
  gap: 2vh;
  align-items: center;
}

.menu-button {
  padding: 0.8rem 2.2rem;
  font-size: 1rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  background: rgba(20, 20, 20, 0.7);
  color: #fff;
  backdrop-filter: blur(6px);
}

.menu-button:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}
</style>
