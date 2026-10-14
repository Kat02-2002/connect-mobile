"""AWS smoke test. Credentials are entered interactively or read from environment.

Writes a reservation ONLY with --create-test-reservation; it remains in AWS because
the supplied API has no cancellation endpoint. Never stores the password in output.
"""
import argparse
import datetime as dt
import getpass
import json
import os
from pathlib import Path
import urllib.error
import urllib.parse
import urllib.request

BASE = "https://ujh0myzrbb.execute-api.us-east-1.amazonaws.com/"


def request(method, path, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method,
                                 headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=30) as response:
            return response.status, json.load(response)
    except urllib.error.HTTPError as error:
        return error.code, json.load(error)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--email", required=True)
    parser.add_argument("--output", default="docs/qa/aws-verification.json")
    parser.add_argument("--create-test-reservation", action="store_true")
    args = parser.parse_args()
    password = os.environ.get("ROOMLINK_TEST_PASSWORD") or getpass.getpass("Contraseña de prueba: ")
    report = {"checkedAtPeru": dt.datetime.now(dt.timezone(dt.timedelta(hours=-5))).isoformat(), "checks": []}

    def check(method, path, expected, body=None):
        status, result = request(method, path, body)
        report["checks"].append({"method": method, "path": path, "status": status, "expected": expected,
                                 "passed": status == expected})
        if status != expected:
            raise RuntimeError(f"{method} {path}: expected {expected}, received {status}")
        return result

    try:
        login = check("POST", "login", 200, {"correo": args.email, "password": password})
        assert login["success"] is True
        user = login["usuario"]["usuarioId"]
        report["usuarioId"] = user
        catalog = check("GET", "salas", 200)["salas"]
        active = [s for s in catalog if s["activa"]]
        assert active, "No active rooms available"
        room = active[0]
        check("GET", "salas/" + urllib.parse.quote(room["salaId"]), 200)
        history_path = "reservas?" + urllib.parse.urlencode({"usuarioId": user})
        history = check("GET", history_path, 200)["reservas"]
        if history:
            check("GET", "reservas/" + urllib.parse.quote(history[0]["reservaId"]), 200)
        check("GET", "salas/00000000-0000-0000-0000-000000000000", 404)
        if args.create_test_reservation:
            tomorrow = (dt.datetime.now(dt.timezone(dt.timedelta(hours=-5))).date() + dt.timedelta(days=1)).isoformat()
            filters = {"sede": room["sede"], "fecha": tomorrow, "horaInicio": "16:00", "horaFin": "16:30",
                       "capacidad": "1", "equipamiento": ",".join(room["equipamiento"])}
            available_path = "salas?" + urllib.parse.urlencode(filters)
            rooms = check("GET", available_path, 200)["salas"]
            assert any(s["salaId"] == room["salaId"] for s in rooms), "Test slot unavailable; no write performed"
            body = {"usuarioId": user, "salaId": room["salaId"], "fecha": tomorrow,
                    "horaInicio": "16:00", "horaFin": "16:30"}
            created = check("POST", "reservas", 201, body)
            assert created["success"] is True
            booking = created["reserva"]
            report["testReservation"] = booking
            report["testCriteria"] = filters
            check("POST", "reservas", 409, body)
            refreshed = check("GET", history_path, 200)["reservas"]
            assert sum(r["reservaId"] == booking["reservaId"] for r in refreshed) == 1
            detail = check("GET", "reservas/" + urllib.parse.quote(booking["reservaId"]), 200)
            assert detail["usuarioId"] == user and detail["salaId"] == room["salaId"]
            after = check("GET", available_path, 200)["salas"]
            assert all(s["salaId"] != room["salaId"] for s in after)
            report["conflictingRoomExcludedAfterBooking"] = True
        report["passed"] = True
    finally:
        password = None
        target = Path(args.output)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"passed": report["passed"], "checks": len(report["checks"]), "report": args.output}))


if __name__ == "__main__":
    main()
