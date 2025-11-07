from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional
from pathlib import Path


class Settings(BaseSettings):
    """Application settings loaded from environment variables"""

    gemini_api_key: str = ""
    service_port: int = 8001
    service_host: str = "0.0.0.0"
    backend_url: str = "http://localhost:8080"

    model_config = SettingsConfigDict(
        env_file=str(Path(__file__).parent.parent / ".env"),
        case_sensitive=False
    )


settings = Settings()
