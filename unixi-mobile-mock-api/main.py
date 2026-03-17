import os
from datetime import datetime, timezone
from typing import Dict, Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Unixi Mobile Assignment - Mock API", version="1.0.0")

USERS_BY_QR: Dict[str, Dict] = {
    "qr_demo_1": {
        "user_id": "u_1001",
        "full_name": "Shadeh Levi",
        "email": "shadeh.levi@unixi.io",
        "company": "Unixi",
        "account_creation_date": "2024-10-01T09:12:00Z",
        "department": "Engineering",
        "account_status": "ACTIVE",
        "last_login_time": "2026-03-04T19:25:00Z",
    },
    "qr_demo_2": {
        "user_id": "u_1002",
        "full_name": "Noa Cohen",
        "email": "noa.cohen@unixi.io",
        "company": "Unixi",
        "account_creation_date": "2025-06-12T14:05:00Z",
        "department": "Security",
        "account_status": "ACTIVE",
        "last_login_time": "2026-03-03T08:10:00Z",
    },
}

PASSWORD_BY_USER_ID: Dict[str, str] = {
    "u_1001": "unixi123",
    "u_1002": "password",
}

class QrResolveRequest(BaseModel):
    qr_token: str = Field(..., description="Token extracted from scanned QR code")

class UserResponse(BaseModel):
    user_id: str
    full_name: str
    email: str
    company: str
    account_creation_date: str
    department: str
    account_status: str
    last_login_time: str

class ValidateRequest(BaseModel):
    user_id: str
    password: str

class ValidateResponse(BaseModel):
    authenticated: bool
    error: Optional[str] = None

@app.get("/health")
def health():
    return {"ok": True, "ts": datetime.now(timezone.utc).isoformat()}

@app.post("/qr/resolve", response_model=UserResponse)
def qr_resolve(body: QrResolveRequest):
    user = USERS_BY_QR.get(body.qr_token)
    if not user:
        raise HTTPException(status_code=404, detail="QR token not found")
    return user

@app.post("/auth/validate", response_model=ValidateResponse)
def auth_validate(body: ValidateRequest):
    expected = PASSWORD_BY_USER_ID.get(body.user_id)
    if expected is None:
        raise HTTPException(status_code=404, detail="User not found")

    if body.password == expected:
        return ValidateResponse(authenticated=True)

    raise HTTPException(status_code=401, detail={"authenticated": False, "error": "Invalid credentials"})

@app.get("/demo/qr-tokens")
def demo_qr_tokens():
    return {"tokens": list(USERS_BY_QR.keys())}

if __name__ == "__main__":
    import uvicorn
    host = os.environ.get("HOST", "0.0.0.0")
    port = int(os.environ.get("PORT", "8080"))
    uvicorn.run("main:app", host=host, port=port, log_level="info")