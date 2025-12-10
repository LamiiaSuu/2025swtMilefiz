<template>
    <div class="header">
        <h1>MI'lefiz</h1>
        <h2><slot>&nbsp;</slot></h2>
    </div>
</template>

<style scoped>
    .header {
        font-family: "MainFont", sans-serif;
        background-image: url("/backgrounds/HeaderPlate.png");
        background-size: contain;
        background-repeat: no-repeat;
        background-position: center;
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        flex-direction: column;

        font-weight: bolder;
        color: white;
        -webkit-text-stroke: 6px black;
        text-shadow:
            3px 3px 6px rgba(0, 0, 0, 0.8),
            0 0 10px rgba(0, 0, 0, 0.5);

        width: 100vw;
        min-height: 54%;
        padding-bottom: 6vh;
        margin-top: 3vh;
        margin-bottom: -6vh;  /* Verkleinert Abstand zum Header */
        paint-order: stroke fill;

        pointer-events: none;

        z-index: 1;

        h1 {
            font-size: 8vh;
        }

        h2 {
            font-size: 4vh;
            line-height: 0.6;
        }
    }
</style>
