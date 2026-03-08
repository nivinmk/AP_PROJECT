from __future__ import annotations

from itertools import product
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.preprocessing import LabelEncoder
from sklearn.tree import DecisionTreeRegressor

DATASET_PATH = Path(__file__).resolve().parent / "dataset.csv"


def _encode_level(value: object) -> float:
    text = str(value).strip().lower()
    mapping = {"low": 1, "medium": 2, "high": 3}
    return mapping.get(text, np.nan)


def _normalize_name(value: object) -> str:
    text = str(value).strip()
    if not text:
        return ""
    # Use the first alias as canonical name for matching/recommendations.
    return text.split(",")[0].strip()


def _survival_score(req_water: int, req_space: int, req_sunlight: int, water: int, space: int, sunlight: int) -> float:
    penalty = (
        abs(req_water - water) * 12
        + abs(req_space - space) * 12
        + abs(req_sunlight - sunlight) * 12
    )
    return float(max(0, 100 - penalty))


def _validate_level(value: int, field_name: str) -> int:
    try:
        parsed = int(value)
    except (TypeError, ValueError) as exc:
        raise ValueError(f"{field_name} must be an integer between 1 and 3") from exc

    if parsed not in (1, 2, 3):
        raise ValueError(f"{field_name} must be between 1 and 3")
    return parsed


def _load_and_prepare_dataset() -> pd.DataFrame:
    df = pd.read_csv(DATASET_PATH)
    required_cols = ["common", "watering", "space", "ideallight"]
    missing_cols = [col for col in required_cols if col not in df.columns]
    if missing_cols:
        raise ValueError(f"dataset.csv is missing required columns: {missing_cols}")

    clean_df = df[required_cols].copy()
    clean_df["req_water"] = clean_df["watering"].apply(_encode_level)
    clean_df["req_space"] = clean_df["space"].apply(_encode_level)
    clean_df["req_sunlight"] = clean_df["ideallight"].apply(_encode_level)
    clean_df["plant_name"] = clean_df["common"].apply(_normalize_name)

    clean_df = clean_df.dropna(subset=["plant_name", "req_water", "req_space", "req_sunlight"])
    clean_df = clean_df[clean_df["plant_name"] != ""].copy()

    for col in ["req_water", "req_space", "req_sunlight"]:
        clean_df[col] = clean_df[col].astype(int)

    clean_df = clean_df.drop_duplicates(subset=["plant_name", "req_water", "req_space", "req_sunlight"])
    clean_df = clean_df.reset_index(drop=True)
    return clean_df


def _build_training_set(base_df: pd.DataFrame) -> pd.DataFrame:
    records: list[dict[str, float | int]] = []

    for _, row in base_df.iterrows():
        req_water = int(row["req_water"])
        req_space = int(row["req_space"])
        req_sunlight = int(row["req_sunlight"])
        plant_encoded = int(row["plant_encoded"])

        for water, space, sunlight in product((1, 2, 3), repeat=3):
            records.append(
                {
                    "plant_encoded": plant_encoded,
                    "req_water": req_water,
                    "req_space": req_space,
                    "req_sunlight": req_sunlight,
                    "water": water,
                    "space": space,
                    "sunlight": sunlight,
                    "survival_score": _survival_score(
                        req_water=req_water,
                        req_space=req_space,
                        req_sunlight=req_sunlight,
                        water=water,
                        space=space,
                        sunlight=sunlight,
                    ),
                }
            )

    return pd.DataFrame.from_records(records)


def _initialize() -> tuple[pd.DataFrame, LabelEncoder, DecisionTreeRegressor]:
    base_df = _load_and_prepare_dataset()

    encoder = LabelEncoder()
    base_df["plant_encoded"] = encoder.fit_transform(base_df["plant_name"])

    train_df = _build_training_set(base_df)
    feature_cols = [
        "plant_encoded",
        "req_water",
        "req_space",
        "req_sunlight",
        "water",
        "space",
        "sunlight",
    ]

    model = DecisionTreeRegressor(random_state=42)
    model.fit(train_df[feature_cols], train_df["survival_score"])

    return base_df, encoder, model


_BASE_DF, _ENCODER, _MODEL = _initialize()
_FEATURE_COLS = [
    "plant_encoded",
    "req_water",
    "req_space",
    "req_sunlight",
    "water",
    "space",
    "sunlight",
]


def recommend_plants(water: int, space: int, sunlight: int, top_k: int = 5) -> list[dict[str, float | str]]:
    water = _validate_level(water, "water")
    space = _validate_level(space, "space")
    sunlight = _validate_level(sunlight, "sunlight")

    candidates = _BASE_DF.copy()
    candidates["water"] = water
    candidates["space"] = space
    candidates["sunlight"] = sunlight

    candidates["predicted_survival"] = _MODEL.predict(candidates[_FEATURE_COLS])

    top = (
        candidates.groupby("plant_name", as_index=False)["predicted_survival"]
        .max()
        .sort_values("predicted_survival", ascending=False)
        .head(int(top_k))
    )

    return [
        {
            "plant": row["plant_name"],
            "survival_score": round(float(row["predicted_survival"]), 2),
        }
        for _, row in top.iterrows()
    ]


def predict_survival(plant: str, water: int, space: int, sunlight: int) -> dict[str, float | str]:
    if plant is None or not str(plant).strip():
        raise ValueError("plant is required")

    water = _validate_level(water, "water")
    space = _validate_level(space, "space")
    sunlight = _validate_level(sunlight, "sunlight")

    query = str(plant).strip().lower()
    matched = _BASE_DF[_BASE_DF["plant_name"].str.lower() == query]

    if matched.empty:
        raise ValueError(f"Plant '{plant}' not found")

    row = matched.iloc[0]
    feature_row = pd.DataFrame(
        [
            {
                "plant_encoded": int(row["plant_encoded"]),
                "req_water": int(row["req_water"]),
                "req_space": int(row["req_space"]),
                "req_sunlight": int(row["req_sunlight"]),
                "water": water,
                "space": space,
                "sunlight": sunlight,
            }
        ]
    )

    score = float(_MODEL.predict(feature_row[_FEATURE_COLS])[0])
    return {
        "plant": row["plant_name"],
        "survival_score": round(score, 2),
    }
