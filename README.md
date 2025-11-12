# Java Preview Server

This folder contains a minimal Java project that serves the pre-built Wise Wallet Gems web application without using Maven, Gradle, or Spring.

## Requirements
- JDK 11 or newer (only the standard `javac` and `java` tools are used)

## First-Time Setup
The `web/` directory already includes the static build generated from the React/Vite project. If you modify the React code later, rebuild it from the project root:

```powershell
npm install
npm run build
Copy-Item -Recurse -Force dist java-preview\web
```

## Compile
From inside the `java-preview` folder:

```powershell
javac Main.java
```

This will create `Main.class` alongside the source file.

## Run
Still inside `java-preview`, start the preview server:

```powershell
java Main
```

By default the site is served at `http://localhost:8080`. You can choose another port:

```powershell
java Main 9090
```

Press `Ctrl + C` in the terminal to stop the server.

## Project Layout
- `Main.java` – lightweight HTTP server using only JDK built-ins
- `web/` – static assets produced by Vite (`index.html`, JavaScript bundles, CSS)

This approach keeps everything in plain Java, making it easy to present the existing web project in a classroom setting without additional build tools.

