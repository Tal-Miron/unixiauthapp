"""
qr_generator.py

Generates a secure QR code containing a signed JWT (HS256).
Payload:
  - qrToken : plaintext, readable by the app, tamper-proof via JWT signature
  - proxy   : AES-256-GCM encrypted proxy address
  - jti     : unique UUID per QR, enables future revocation
  - iat     : issued-at timestamp (informational, no expiry enforced)

Dependencies:
    pip install python-jose[cryptography] qrcode python-dotenv cryptography

Usage from another module:
    from qr_generator import print_qr
    print_qr("my_token", "192.168.1.5:8080")
"""

import os
import base64
import uuid
import time

from dotenv import load_dotenv
from jose import jwt
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import qrcode

# ── Load secrets ──────────────────────────────────────────────────────────────

load_dotenv()

_AES_KEY_HEX = os.getenv("AES_KEY")
_HMAC_SECRET = os.getenv("HMAC_SECRET")

if not _AES_KEY_HEX or not _HMAC_SECRET:
    raise EnvironmentError(
        "Missing AES_KEY or HMAC_SECRET in environment. "
        "Run generate_keys.py first."
    )

_AES_KEY: bytes = bytes.fromhex(_AES_KEY_HEX)

if len(_AES_KEY) != 32:
    raise ValueError("AES_KEY must be exactly 32 bytes (64 hex chars).")


# ── Encryption ────────────────────────────────────────────────────────────────

def _encrypt_proxy(proxy_address: str) -> str:
    iv = os.urandom(12)
    aesgcm = AESGCM(_AES_KEY)
    encrypted = aesgcm.encrypt(iv, proxy_address.encode(), None)
    return base64.urlsafe_b64encode(iv + encrypted).rstrip(b"=").decode()


def _decrypt_proxy(encoded: str) -> str:
    padding = 4 - len(encoded) % 4
    raw = base64.urlsafe_b64decode(encoded + "=" * (padding % 4))
    iv, ciphertext_and_tag = raw[:12], raw[12:]
    return AESGCM(_AES_KEY).decrypt(iv, ciphertext_and_tag, None).decode()


# ── JWT building ──────────────────────────────────────────────────────────────

def _build_jwt(qr_token: str, proxy_address: str) -> str:
    claims = {
        "qrToken": qr_token,
        "proxy":   _encrypt_proxy(proxy_address),
        "jti":     str(uuid.uuid4()),
        "iat":     int(time.time()),
    }
    return jwt.encode(claims, _HMAC_SECRET, algorithm="HS256")


# ── Public API ────────────────────────────────────────────────────────────────

def print_qr(qr_token: str, proxy_address: str) -> None:
    """
    Build a secure JWT and print it as a QR code in the terminal.

    Args:
        qr_token:      Token the app reads directly from the JWT.
        proxy_address: Proxy address — AES-256-GCM encrypted, server-only.
    """
    jwt_string = _build_jwt(qr_token, proxy_address)

    print("\n" + "=" * 50)
    qr = qrcode.QRCode(
        error_correction=qrcode.constants.ERROR_CORRECT_M,
    )
    qr.add_data(jwt_string)
    qr.make(fit=True)
    qr.print_ascii(invert=True)
    print("=" * 50 + "\n")