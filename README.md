# Flock Lab 🐦

### Modern Java Boids Simulation --- Desktop + Web

![Java](https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk&logoColor=white)
![LibGDX](https://img.shields.io/badge/LibGDX-Latest-red?style=for-the-badge)
![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Platform](https://img.shields.io/badge/platform-Desktop%20%7C%20Web-lightgrey?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

An interactive **Boids (flocking) simulation** built with modern **Java 21** and **LibGDX**, designed as a clean-architecture, portfolio-grade hobby project.

Flock Lab implements the classic flocking algorithm and extends it with real-time configuration, predator-prey mechanics, obstacle avoidance, and browser deployment.

---

## 📌 Project Status

**Step 4 / 12 Complete:** Simulation engine & config running:
- `Vec2` — Immutable 2D vector math
- `Boid`, `Obstacle`, `Attractor`, `Predator` — Core simulation entities
- `World` & `SpatialGrid` — Fast O(1) neighbor lookups and tick simulation
- `SimulationConfig` & `Preset` — Configurable flocking behaviors

**Step 5 / 12 Complete:** Core integration tests implemented. Tests verify vector math, edge wrapping, stabilization, and flocking rules (JUnit 5).

------------------------------------------------------------------------

## ✨ Features

### 🧠 Core Simulation

-   Separation, Alignment, Cohesion (classic Boids rules)
-   Obstacle avoidance
-   Predator behavior
-   Food / attractor mechanics
-   Edge wrapping world
-   Real-time adjustable simulation parameters

### 🎛 Live Controls

Adjustable at runtime: - Number of boids - Max speed - Perception
radius - Rule weights (separation / alignment / cohesion / avoidance /
predator / food)

UI includes: - Start / Pause / Reset - Preset configurations (tight
flock, chaotic swarm, etc.)

Mouse interactions: - Add boids - Place obstacles - Add attractors

------------------------------------------------------------------------

## 🧮 What Are Boids?

Boids is an artificial life simulation originally created by Craig
Reynolds (1986) to simulate flocking behavior.

Each boid follows three simple rules:

1.  **Separation** -- Avoid crowding nearby boids\
2.  **Alignment** -- Match velocity with neighbors\
3.  **Cohesion** -- Move toward the center of nearby boids

From these simple rules, complex emergent swarm behavior appears.

Flock Lab extends this model with:

-   Environmental obstacles
-   Predator avoidance
-   Food attraction
-   Runtime parameter tuning

------------------------------------------------------------------------

## 🏗 Architecture

    boids-core/
      Pure Java simulation logic (no rendering dependencies)
      Domain models
      Simulation engine
      Configuration
      Integration tests

    boids-desktop/
      Desktop launcher
      Rendering layer (LibGDX)
      Input handling
      UI & overlay

    boids-html/
      Web launcher (GWT)
      Browser-compatible rendering

Design principles: - Core simulation independent from rendering -
Minimal external libraries - Modern Java 21 - Clean, readable,
self-explanatory code - Configurable and extensible structure -
Integration-focused testing

------------------------------------------------------------------------

## 🚀 Running the Project

### Prerequisites

-   Java 21 (or latest LTS)
-   Gradle (or use wrapper)

### ▶ Run Desktop Version

``` bash
./gradlew :boids-desktop:run
```

### 🌐 Build Web Version

``` bash
./gradlew :boids-html:build
```

After build, serve the generated files via any static server.

Example:

``` bash
npx serve build/dist
```

------------------------------------------------------------------------

## 🎮 Controls

  Action           Input
  ---------------- ------------------
  Add boids        Click / Drag
  Place obstacle   Modifier + Click
  Add attractor    Modifier + Click
  Pause / Resume   UI Button
  Reset            UI Button
  Toggle stats     UI Toggle
  Zoom             Mouse wheel

------------------------------------------------------------------------

## 📊 Live Metrics

Toggleable overlay displaying: - FPS - Number of boids - Average speed -
Average neighbor count

------------------------------------------------------------------------

## 🧪 Testing

Core simulation logic includes: - Integration-style tests - Edge
wrapping verification - Stability checks (no NaN / explosion) - Rule
consistency validation

Testing framework: **JUnit 5**

Run tests:

``` bash
./gradlew test
```

------------------------------------------------------------------------

## 📚 Why This Project?

Flock Lab was built as: - A deep dive into emergent behavior
simulation - A clean architecture exercise - A Java game-dev
exploration - A browser-deployable interactive experiment

------------------------------------------------------------------------

## 🤖 Built With AI

This project was developed with the assistance of AI tools as a learning
and exploration exercise.

------------------------------------------------------------------------

## 📜 License

MIT License

------------------------------------------------------------------------

## 👤 Author

Eyal Shahaf
