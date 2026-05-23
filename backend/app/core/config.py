from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    env: str = "local"
    api_host: str = "0.0.0.0"
    api_port: int = 8000
    allowed_origins: list[str] = []

    model_config = SettingsConfigDict(env_prefix="DRUK_", env_file=".env")


@lru_cache
def get_settings() -> Settings:
    return Settings()

