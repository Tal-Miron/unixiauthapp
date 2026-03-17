import subprocess
import sys
import time
import re
from qr_generator import print_qr

def install_if_missing(package):
    try:
        __import__(package)
    except ImportError:
        print(f"Installing {package}...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", package])

def start_containers():
    print("Starting containers...")
    subprocess.run(["docker", "compose", "up", "--build", "-d"], check=True)

def get_tunnel_url(timeout=60):
    print("Waiting for tunnel URL...")
    start = time.time()
    while time.time() - start < timeout:
        logs = subprocess.run(
            ["docker", "logs", "unixi-tunnel"],
            capture_output=True,
            text=True
        )
        output = logs.stdout + logs.stderr
        match = re.search(r'https://[a-zA-Z0-9-]+\.trycloudflare\.com', output)
        if match:
            return match.group(0)
        time.sleep(2)
    raise TimeoutError("Tunnel URL not found within timeout. Check docker logs unixi-tunnel.")

if __name__ == "__main__":
    install_if_missing("qrcode")
    start_containers()
    url = get_tunnel_url()
    print(url)
    print_qr("qr_demo_1", url)
    print_qr("qr_demo_2", url)
    print_qr("qr_non_existing", url)