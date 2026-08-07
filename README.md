# 11_105_CIPHERSQUAD
# 🚨 Transaction Monitoring & Alert Management System

## 📌 Overview

The **Transaction Monitoring & Alert Management System** is a full-stack application designed to detect suspicious financial activities by monitoring transactions against predefined rules.

The system automatically evaluates transactions, generates alerts for suspicious behavior, and provides an interface for operators to investigate and manage alerts.

---

# 🎯 Objectives

- Monitor financial transactions in real time
- Detect suspicious transaction patterns
- Generate and manage fraud alerts
- Maintain alert history and status tracking
- Provide a dashboard for monitoring activities

---

# 🏗️ System Architecture

             React Dashboard
                   |
                   |
            REST API Layer
           (Spring Boot)
                   |
    --------------------------------
    |              |               |

Transaction Rule Engine Alert Management
Service Service Service
| | |
--------------------------------
|
Database


---

# 🛠️ Technology Stack

### Frontend
- React.js + Vite
- Tailwind CSS
- Axios

### Backend
- Java
- Spring Boot
- Spring Data JPA
- Mockito (Testing)

### Database
- MySQL/PostgreSQL

### DevOps
- Git & GitHub
- Docker
- CI/CD Pipeline

---

# ✨ Key Features

## Transaction Management
- Create and view transactions
- Search and filter transaction records
- Maintain transaction history


## Rule-Based Monitoring

The system supports multiple fraud detection rules:

### 💰 Amount Threshold Rule
Triggers an alert when transaction amount exceeds a defined limit.

Example:

Transaction Amount > $10,000
→ Generate Alert


### ⚡ Velocity Rule
Detects multiple transactions within a short time period.

Example:

More than 5 transactions
within 10 minutes
→ Generate Alert


### 👤 New Payee Rule
Detects transactions made to previously unused payees.

### 📅 Daily Limit Rule
Triggers alerts when daily transaction limits are exceeded.

---

# 🚨 Alert Lifecycle


OPEN
|
ACKNOWLEDGED
|
INVESTIGATING
|
CLOSED

OPEN → DISMISSED
INVESTIGATING → DISMISSED


### Alert Status

| Status | Description |
|---|---|
| OPEN | Newly generated alert |
| ACKNOWLEDGED | Alert reviewed by operator |
| INVESTIGATING | Investigation in progress |
| CLOSED | Issue resolved |
| DISMISSED | False positive alert |

---

# 📊 Dashboard Features

The dashboard allows users to:

- View transactions
- View active alerts
- Check alert details
- Update alert status
- Manage monitoring rules
- View alert history

---

# 🔌 API Endpoints

## Transactions

| Method | Endpoint | Description |
|---|---|---|
| POST | `/transactions` | Create transaction |
| GET | `/transactions` | Get all transactions |
| GET | `/transactions/{id}` | Get transaction details |


## Alerts

| Method | Endpoint | Description |
|---|---|---|
| GET | `/alerts` | View alerts |
| GET | `/alerts/{id}` | Alert details |
| PUT | `/alerts/{id}/status` | Update alert status |


## Rules

| Method | Endpoint | Description |
|---|---|---|
| GET | `/rules` | View rules |
| POST | `/rules` | Add new rule |
| PUT | `/rules/{id}` | Update rule |
| DELETE | `/rules/{id}` | Delete rule |

---

# 🧪 Testing

Testing is performed using:

- JUnit
- Mockito

Test cases include:

- Rule evaluation
- Alert generation
- Alert status changes
- Transaction validation

---

# 🔄 CI/CD Workflow


Code Commit
|
GitHub Repository
|
Build & Test Pipeline
|
Docker Image Creation
|
Deployment


---

# 🚀 Future Enhancements

- Real-time alert updates using WebSockets
- Machine Learning based fraud detection
- Email/SMS notifications
- Advanced configurable rule engine
- Alert prioritization (High/Medium/Low)
- Audit trail and reporting

---

# 👥 Team Workflow

Development process:


Feature Branch
|
Code Review
|
Pull Request
|
Merge to Main


---

## Developed By

**CypherSquad Team**  
Transaction Monitoring & Alert Management System
