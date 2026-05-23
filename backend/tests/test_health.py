from app.main import health


def test_health_returns_ok() -> None:
    response = health()

    assert response.status == "ok"
