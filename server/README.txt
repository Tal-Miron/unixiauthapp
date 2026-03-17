# Unixi Mobile Assignment – Mock API

A mock REST API for the Unixi mobile assignment, served over HTTPS via a Cloudflare tunnel with zero configuration required.

## Architecture

Android App
    │
    ▼ HTTPS (trusted certificate)
[ Cloudflare Tunnel ]  ← reverse proxy
    │
    ▼ HTTP
[ FastAPI container ]


## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Python 3.x

## How to Run

1. Open a terminal in the project folder
2. Run (bash):
python run.py

3. A QR code will print in the terminal with the live HTTPS URL
4. Scan it with the Android app — it will connect automatically

## Stopping the server
Run (bash):
docker compose down

## Project structure

unixi-mobile-mock-api/
├── main.py
├── Dockerfile
├── docker-compose.yml
├── run.py
├── qr_generator.py
├── .env
└── README.md