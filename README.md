# DB-Controller (Backend)

This is the core engine of the DB-Controller application. It handles API requests, processes data routing, and manages structural interactions with the database layer.

## 🛠️ Architecture & Design Principles
* **SOLID Design:** The codebase closely aligns with SOLID principles, ensuring a modular, maintainable structure that allows new functionalities to be integrated or modified seamlessly.
* **Type-Safe Foundation:** Programmed entirely in **Java** to leverage its strict syntax, strong type-safety, and robust memory management.
* **Decoupled Execution (DB-Agent):** To maximize security, the backend does not execute SQL queries directly. Instead, it processes requests and delegates execution to a secondary microservice/module, **DB-Agent**. This separation isolates the database connection and mitigates risks such as direct SQL Injection (SQLi) vulnerabilities.

## 🔒 Security & Data Management
* **Role-Based Access Control (RBAC):** Integrated **Spring Security** to enforce strict user roles and permissions. Unauthorized API calls are restricted, ensuring users can only access endpoints matching their explicitly granted privileges.
* **Data Layer & Optimization:** Utilizes **Spring Boot** for core framework architecture, **PostgreSQL** for reliable relational data storage, and **Redis** as a high-performance caching layer to optimize data flow and system responsiveness.
