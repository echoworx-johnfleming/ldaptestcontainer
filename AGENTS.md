# Running Tests with Testcontainers and Docker

This document outlines the steps to run this project's tests using Testcontainers with Docker as the container runtime.

## IMPORTANT: Docker Hub Authentication

The tests in this project rely on Docker images that are subject to Docker Hub's pull rate limits. To avoid test failures due to these limits, you should authenticate with Docker Hub if you hit these limits.

Provide your Docker Hub credentials via the following environment variables if needed:

-   `DOCKER_HUB_USER`: Your Docker Hub username.
-   `DOCKER_HUB_PASSWORD`: Your Docker Hub password or Personal Access Token (PAT).

## Setup and Test Execution

1.  **Ensure Docker is installed and running:**
    Ensure that the Docker daemon is active on your system.

2.  **Make Gradle Wrapper Executable:**
    Before running any Gradle commands, ensure the wrapper script is executable:
    ```bash
    chmod +x gradlew
    ```

3.  **(Optional) Disable Ryuk:**
    If you encounter issues with the `ryuk` container in restricted environments, you can disable it with the following command:
    ```bash
    export TESTCONTAINERS_RYUK_DISABLED=true
    ```

4.  **Run the Tests:**
    Execute the tests using the Gradle wrapper:
    ```bash
    ./gradlew test
    ```

## Troubleshooting

- **Storage Driver:** If Docker fails to start containers with "invalid argument" during mount operations in some virtualized environments, ensure you are using a compatible storage driver (e.g., `vfs` or `fuse-overlayfs`).
- **API Version:** If you see "client version is too old" errors, you may need to force a newer Docker API version:
  ```bash
  export COM_GITHUB_DOCKERJAVA_API_VERSION=1.44
  ```
