# Hemospec Project

This repository contains the code for the Hemospec system, including the backend API, frontend Android/Web application, and prediction service.

## Project Structure

*   `hemospec-api/`: NestJS Backend API.
*   `hemospec-app/`: Ionic/React Frontend (Android & Web).
*   `old_app/predict/`: Python Prediction Service.

## Deployment

The project is configured to run using Docker Compose.

### Prerequisites

*   Docker
*   Docker Compose
*   Node.js (for building frontend assets locally)

### Deployment Steps

1.  **Build Frontend Assets**

    The frontend needs to be built locally (or in a CI/CD pipeline) before starting the containers, as the static files are mounted into the Caddy container.

    ```bash
    echo "Building Frontend Assets..."
    cd hemospec-app
    # Install dependencies and build the static files
    npm install
    npm run build
    cd ..
    ```

2.  **Start Docker Containers**

    Run the following command to build the backend services and start the stack.

    ```bash
    echo "Starting Docker Containers..."
    # We use sudo here to ensure permissions work immediately if needed
    # COMPOSE_PARALLEL_LIMIT=1 helps prevent crashes on smaller instances
    sudo COMPOSE_PARALLEL_LIMIT=1 docker compose up -d --build
    ```

3.  **Verify Deployment**

    *   Ensure your DNS A Record points to your PUBLIC IP.
    *   Visit `https://www.labinlight.dev`.
    *   Monitor logs with: `sudo docker compose logs -f`.

## Configuration

*   **API URL**: The frontend connects to the API via `https://www.labinlight.dev/api`.
*   **Environment Variables**:
    *   Backend configuration is set in `docker-compose.yml`.
    *   Frontend local development configuration is in `hemospec-app/.env`.

## Services

*   **caddy**: Web server and reverse proxy. Serves the frontend and forwards `/api` requests to the backend. handles SSL automatically.
*   **api**: NestJS backend service.
*   **predict**: Python service for spectral analysis.
*   **postgres**: Database for the API.
