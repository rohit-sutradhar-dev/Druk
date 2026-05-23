from fastapi import FastAPI

from app.core.config import get_settings
from app.schemas.health import HealthResponse

settings = get_settings()

app = FastAPI(
    title="Druk API",
    version="0.1.0",
    description="Session sync and account API for the Druk Android app.",
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok", environment=settings.env)

