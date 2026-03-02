# Boids Studio 🐦

### Modern Java Boids Simulation --- Desktop + Web

<div align="center">
  <img src="images/banner.png" alt="Boids Studio — Boids Simulation" width="100%">
</div>

![Java](https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk&logoColor=white)
![LibGDX](https://img.shields.io/badge/LibGDX-Latest-red?style=for-the-badge)
[![Live Demo](https://img.shields.io/badge/Live_Demo-Play_Now-green?style=for-the-badge&logo=google-chrome&logoColor=white)](https://eyalshahaf.github.io/boids-studio/)
![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Platform](https://img.shields.io/badge/platform-Desktop%20%7C%20Web-lightgrey?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

An interactive **Boids (flocking) simulation** built with modern **Java 21** and **LibGDX**, designed as a clean-architecture, portfolio-grade hobby project.

Boids Studio implements the classic flocking algorithm and extends it with real-time configuration, predator-prey mechanics, obstacle avoidance, and browser deployment.

---

## 📌 Project Status

**Performance-optimized for 1000–2000+ boids in browser!** 🚀

- ✅ **Core Physics & Simulation:** Domain models (`Vec2`, `Boid`), flocking rules, spatial grids.
- ✅ **Desktop & Web Builds:** Runs locally via LWJGL3 and in browser via GWT/HTML.
- ✅ **Interactive UI Controls:** Real-time property tweaking with Scene2D sliders & HUD.
- ✅ **Visual Polish:** Dynamic HSL coloring, additive blending, and fading motion trails.
- ✅ **CI Pipeline:** Automated builds and GitHub Pages deployment via GitHub Actions.
- ✅ **Performance Overhaul:** Full three-phase optimization pass (see below).

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

Boids Studio extends this model with:

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

Boids Studio was built as: - A deep dive into emergent behavior
simulation - A clean architecture exercise - A Java game-dev
exploration - A browser-deployable interactive experiment

------------------------------------------------------------------------

## ⚡ Performance Optimizations

To break the ~1000 boid limit in the browser, three phases of optimization
were applied:

### Phase 1 — Simulation lifecycle fixes
- **Spatial grid reuse:** `SpatialGrid` is now cleared and reused each frame instead of being
  re-instantiated, eliminating a full `HashMap` allocation per frame. The grid is only recreated
  when the perception radius or world dimensions change.
- **Perception radius caching:** `perceptionRadius` and `perceptionRadius²` are computed once
  per `World.update` call and threaded through all rule calculations.
- **UI update throttling:** `ControlPanel` and `StatsOverlay` label updates are throttled to
  ~8 Hz instead of 60 Hz, eliminating ~52 string allocations per second per label.

### Phase 2 — Hot-loop allocation elimination
- **Single-pass flock rule:** `BoidRules.flock()` combines separation, alignment, and cohesion
  in one neighbor iteration instead of three, cutting loop overhead by 3×.
- **Raw float accumulation:** The inner loop uses `float` math throughout and creates exactly
  one `Vec2` per boid per frame (the combined force), down from 4+ Vec2s per neighbor. With
  1000 boids and ~20 neighbors each this eliminates ~80,000 short-lived allocations per frame.
- **No-sqrt separation:** `diff.normalize().scale(500/d)` reduces to `diff * 500/d²`, removing
  a `Math.sqrt` call per in-range neighbor.
- **Neighbor buffer reuse:** A single `ArrayList<Boid>` is pre-allocated per `World` and cleared
  before each boid's spatial query, eliminating 1000+ `ArrayList` allocations per frame.
- **Pre-allocated predator proxy:** The temporary `Boid(-1, ...)` created per predator per frame
  is replaced with a single pre-allocated proxy whose position is updated in-place.
- **Index-based list iteration:** All hot-path list traversals use index loops to avoid
  `Iterator` allocation overhead.

### Phase 3 — Adaptive rendering
- **Trail adaptive quality:** Trails automatically reduce length at 600+ boids, shorten
  further at 1000+, and disable entirely at 1500+ boids.
- **Trail decimation:** At 1000+ boids, every second trail segment is skipped, halving
  line draw calls.
- **Trail ring buffer:** `LinkedList<Vec2>` replaced with `ArrayDeque<Vec2>` for better
  cache locality and no per-node allocation.
- **LOD boid rendering:** Boid triangle size scales down from 6px → 4px → 3px at 1000 and
  1500 boid thresholds, slightly reducing fragment fill rate.
- **Min-speed clamp removed:** The 20%-of-maxSpeed velocity floor was causing incorrect
  edge-wrapping behavior and has been removed; boids now only enforce a maximum speed.

### Expected results
| Target | Boid count |
|--------|-----------|
| 60 FPS | 800–1200 (trails adaptive) |
| 30+ FPS | 1500–2000+ (trails disabled) |

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
