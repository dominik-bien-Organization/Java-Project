# 🏥 ClinicApp – JavaFX Desktop Application

ClinicApp is a simple desktop application built with Java and JavaFX to simulate a basic clinic management system. It supports doctor registration and login and a MySQL 8.0 database managed via Docker.

## 🚀 Features

- 👨‍⚕️ **Doctor Registration & Login** – secure registration and login for doctors
- 🖥️ **JavaFX UI** – clean, responsive interface
- 🐬 **MySQL 8.0 via Docker** – portable, reproducible database
- 🔌 **JDBC Integration** – direct MySQL connectivity
- 📋 **Form Validation** – basic input checks in forms
- 📦 **Modular Codebase** – clear separation (GUI, model, service, etc.)
- 🧰 **Utility Classes** – helpers for encryption and DB connection
- ♻️ **Service Layer** – business logic separated from UI/data
- 🚫 **Error Handling with Alerts** – user-friendly JavaFX dialogs
- 📐 **MVC Design** – controllers, models, and services follow MVC

## ⚙️ Getting Started

### Prerequisites
- Java 23 or later (JavaFX-compatible)
- Docker & Docker Compose
- JavaFX SDK (if not bundled with your JDK)
- IDE like IntelliJ IDEA or Eclipse with JavaFX support

### Database Setup (optional)
1. Ensure Docker is running.
2. Create a `docker-compose.yml` file in the project root with the following content:

    ```yaml
    version: '3.8'

    services:
    mysql:
        image: mysql:8.0
        container_name: clinic-mysql
        restart: always
        environment:
        MYSQL_ALLOW_EMPTY_PASSWORD: "true"
        MYSQL_DATABASE: clinic
        ports:
            - "3306:3306"
        volumes:
            - clinic-db:/var/lib/mysql

    volumes:
        clinic-db:
    ```

### Running the App
1. Clone the repository:
    ```bash
    git clone https://github.com/dominik-bien-Organization/Java-Project.git
    cd Java-Project
    ```
2. Open the project in your IDE.
3. Launch the MySQL container with: (optional)
   ```bash
   docker-compose up -d
   ```
   This spins up a MySQL 8.0 container with Database: `clinic`
4. Update DB connection parameters in code if needed.
5. Run the application from your IDE or build it using Maven/Gradle.

## 🧪 Testing
Currently, the project does not include automated tests.

## ✅ TODO
- Add DAO layer for database abstraction
- Improve error handling and user feedback
- Add unit and integration tests
- Add session/logout support
- Switch DB config to use environment variables
- Improve GUI with consistent layout & icons

## 📝 License
This project is licensed under the MIT License. You are free to use, modify, and distribute it under the terms of the license.
