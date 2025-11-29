#!/usr/bin/env python3
"""
SkillSync AI Service Runner
Starts the FastAPI application for AI-powered career advice
"""

import subprocess
import sys

if __name__ == "__main__":
    # Run the FastAPI app with uvicorn
    subprocess.run([
        sys.executable, "-m", "uvicorn",
        "app.main:app",
        "--host", "0.0.0.0",
        "--port", "8001",
        "--reload"
    ])
