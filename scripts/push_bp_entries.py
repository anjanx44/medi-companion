#!/usr/bin/env python3
"""Bulk-push BP chart entries from 7.bp-chart-15-day.md into Firestore.

Usage:
    pip install firebase-admin
    python push_bp_entries.py --sa firebase-adminsdk.json --device-id <DEVICE_ID>

The device ID comes from the app: tap the (i) icon in the top-right corner.
Document IDs are deterministic (date+slot), so re-running overwrites instead
of duplicating.

"""
import argparse
import sys
from datetime import datetime

from firebase_admin import credentials, firestore, initialize_app

# date -> {"MORNING": (sys, dia, pulse) | None, "EVENING": (sys, dia, pulse) | None}
DATA = {
    "2026-08-22": {"MORNING": None,          "EVENING": (120, 80, None)},
    "2026-08-23": {"MORNING": (110, 80, None), "EVENING": (120, 80, None)},
    "2026-08-24": {"MORNING": (110, 70, None), "EVENING": (100, 70, None)},
    "2026-08-25": {"MORNING": (120, 90, None), "EVENING": (100, 70, None)},
    "2026-08-26": {"MORNING": (100, 60, None), "EVENING": (100, 70, None)},
    "2026-08-27": {"MORNING": (115, 80, None), "EVENING": (100, 70, None)},
    "2026-08-28": {"MORNING": (120, 80, None), "EVENING": (115, 75, None)},
    "2026-08-29": {"MORNING": (110, 70, None), "EVENING": None},
    "2026-08-30": {"MORNING": (110, 80, None), "EVENING": (115, 80, None)},
}

SLOT_TIME = {"MORNING": (9, 15), "EVENING": (21, 45)}


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--sa", required=True, help="path to service account JSON")
    p.add_argument("--device-id", required=True, help="device id shown in the app")
    a = p.parse_args()

    app = initialize_app(credentials.Certificate(a.sa))
    db = firestore.client(app)
    col = db.collection("users").document(a.device_id).collection("bp_entries")

    written = 0
    skipped = 0
    for date, slots in DATA.items():
        for slot, reading in slots.items():
            if reading is None:
                continue
            sys_v, dia_v, pulse = reading
            hh, mm = SLOT_TIME[slot]
            created = datetime.strptime(f"{date} {hh:02d}:{mm:02d}", "%Y-%m-%d %H:%M")
            doc_id = f"{date}-{slot}"
            entry = {
                "id": doc_id,
                "date": date,
                "timeSlot": slot,
                "systolic": sys_v,
                "diastolic": dia_v,
                "pulse": pulse,
                "createdAt": int(created.timestamp() * 1000),
            }
            doc = col.document(doc_id)
            ref = doc.get()
            if ref.exists:
                skipped += 1
                print(f"  skip  [{date} {slot}: {sys_v}/{dia_v}] already exists")
                continue
            doc.set(entry)
            written += 1
            print(f"  write [{date} {slot}: {sys_v}/{dia_v}]")

    same_day = sum(1 for s in DATA.values() if s["MORNING"] is not None)
    print(f"\nDone. Wrote {written}, skipped {skipped} existing. "
          f"Path: users/{a.device_id}/bp_entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())