#!/usr/bin/env python3
"""
Startup script for SkillSync AI Insights Service
"""
import uvicorn
from app.config import settings

if __name__ == "__main__":
    print("=" * 60)
    print("🚀 Starting SkillSync AI Insights Service")
    print("=" * 60)
    print(f"Host: {settings.service_host}")
    print(f"Port: {settings.service_port}")
    print(f"OpenAI Configured: {bool(settings.openai_api_key)}")
    print(f"Backend URL: {settings.backend_url}")
    print("=" * 60)
    print("\n📚 API Documentation will be available at:")
    print(f"   http://localhost:{settings.service_port}/docs")
    print("\n")

    uvicorn.run(
        "app.main:app",
        host=settings.service_host,
        port=settings.service_port,
        reload=True,
        log_level="info"
    )
