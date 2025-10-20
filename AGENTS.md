# Running Tests with Testcontainers and Podman

This document outlines the steps to run this project's tests using Testcontainers with Podman as the container runtime.

## IMPORTANT: Docker Hub Authentication

The tests in this project rely on Docker images that are subject to Docker Hub's pull rate limits. To avoid test failures due to these limits, you **must** authenticate with Docker Hub.

Provide your Docker Hub credentials via the following environment variables:

-   `DOCKER_HUB_USER`: Your Docker Hub username.
-   `DOCKER_HUB_PASSWORD`: Your Docker Hub password or Personal Access Token (PAT).

The agent will use these credentials to authenticate `podman` before running the tests.

## Setup and Test Execution

1.  **Install Podman:**
    If you are on a Debian-based system, you can install it with:
    ```bash
    sudo apt-get install -y podman
    ```
    For other systems, follow the official [Podman installation guide](https://podman.io/getting-started/installation).

2.  **Make Gradle Wrapper Executable:**
    Before running any Gradle commands, ensure the wrapper script is executable:
    ```bash
    chmod +x gradlew
    ```

3.  **Configure Testcontainers for Podman:**
    Create a file at `~/.testcontainers.properties` with the following content to tell Testcontainers where to find the Podman socket.
    ```properties
    # The path may vary depending on your system.
    # This path is for a user-level Podman service on Linux.
    docker.host=unix:///run/user/1001/podman/podman.sock
    ```
    You may need to replace `1001` with your user's UID, which you can find by running `id -u`.

4.  **Start Podman Service:**
    Run the Podman service in the background:
    ```bash
    podman system service --time=0 &
    ```

5.  **(Optional) Disable Ryuk:**
    If you encounter issues with the `ryuk` container, you can disable it with the following command. This is often necessary when running Podman in rootless mode.
    ```bash
    export TESTCONTAINERS_RYUK_DISABLED=true
    ```

6.  **Run the Tests:**
    Execute the tests using the Gradle wrapper:
    ```bash
    ./gradlew test
    ```
