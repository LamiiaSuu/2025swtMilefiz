# MI’lefiz

MI’lefiz is a multiplayer game developed as part of the **Software Engineering module (Project 2025/26)**. It is based on the classic board game **Malefiz**, extended with additional mechanics and modern multiplayer features.  
The project focuses on clean, extensible architecture, continuous development using Scrum, and a fully functional full-stack application with a dedicated game server.

---

## Project Concept

While classic Malefiz is strictly turn-based, MI’lefiz introduces a more interactive, real-time-oriented approach:

- **Multiplayer game sessions** managed on a server-side game map
- **3D game view** from a first-person perspective (switchable between owned pieces)
- **Movement along a defined path network** (fields/nodes)
- **Not turn-based**: players can roll the dice every *n* seconds and move a piece
- **Limited visibility**: no complete overview of opponent positions (no traditional minimap)
- **Duel system**: entering a field occupied by an opponent triggers a duel as a minigame
- **Block mechanic**: blocks can be repositioned; occupied fields are clearly highlighted during selection

The goal is to be the first player to move one of their pieces to the target field.

---

## System Overview

MI’lefiz consists of a web-based client (frontend) and a Spring-based game server (backend).  
Communication is handled via **REST** and **WebSocket (STOMP / PubSub)** for real-time game state synchronization.

### Components

### Game Client
- User interface for game state, interactions, and minigames
- 3D visualization of the game board and pieces

### Game Server
- Management of game sessions
- Centralized game rules and validation
- Client synchronization via events and state updates

### Map Editor
- Creation and modification of game maps (bases, goal, blocks)
- Map import and export functionality

---

## Tech Stack

### Backend
- Java 21  
- Spring Boot  
- WebSocket (STOMP)  
- REST API  
- JUnit  

### Frontend
- Vue 3 (Composition API)  
- TypeScript  
- three.js  

---

## Architecture Overview

- **Frontend:** Vue 3 client with 3D rendering using three.js
- **Backend:** Spring Boot game server managing sessions and game logic
- **Communication:** REST for commands and WebSocket for real-time updates
- **Architecture Goal:** Modular, extensible, and scalable multiplayer system

---

## Development Process

- Scrum-based development
- Iterative feature implementation
- Focus on clean architecture and maintainability
- Continuous integration of frontend and backend components

---

## Educational Context

This project was developed as part of the **Software Engineering course in the Media Computer Science program**.
It demonstrates full-stack development, real-time communication, multiplayer synchronization, and modern software engineering practices like Scrum.
