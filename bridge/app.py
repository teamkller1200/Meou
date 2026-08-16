"""Minecraft LLM Companion AI - Local Bridge Server (MVP Step 4)

FastAPI server that receives context state payloads from the Java mod,
calls LM Studio (OpenAI-compatible local API), and returns an action command.
"""

import json
import logging
import os
from typing import List, Optional

import httpx
from fastapi import FastAPI
from pydantic import BaseModel, Field

logger = logging.getLogger("bridge")

LM_STUDIO_URL = os.environ.get("LM_STUDIO_URL", "http://127.0.0.1:1234/v1")

PREFERRED_MODELS = ["qwen2.5-7b-instruct", "qwen2.5-coder-14b-instruct", "gemma4-12b-qat-uncensored-hauhaucs-balanced"]

def resolve_model() -> str:
    env_model = os.environ.get("LM_MODEL", "")
    if env_model:
        return env_model
    try:
        with httpx.Client(timeout=5) as hc:
            resp = hc.get(f"{LM_STUDIO_URL}/models")
            models = resp.json().get("data", [])
            model_ids = [m["id"] for m in models if m["id"] != "text-embedding-nomic-embed-text-v1.5"]
            for preferred in PREFERRED_MODELS:
                if preferred in model_ids:
                    logger.info(f"Selected preferred model: {preferred}")
                    return preferred
            if model_ids:
                selected = model_ids[0]
                logger.info(f"Auto-detected LM Studio model: {selected}")
                return selected
    except Exception as e:
        logger.warning(f"Failed to detect LM Studio model: {e}")
    return "local-model"

LM_MODEL = resolve_model()

app = FastAPI(title="Aibots Bridge Server", version="0.2.0")

SYSTEM_PROMPT = """You are a loyal companion AI living inside Minecraft with the player. \
You watch the player's surroundings and status, and respond with helpful, \
friendly advice in Japanese. Keep responses to 1-2 short sentences.

You must ALWAYS output valid JSON in exactly this format (no markdown, no extra text):
{
  "dialogue": "response text here",
  "action": {
    "type": "FOLLOW",
    "urgency": "LOW"
  }
}

action.type must be one of: FOLLOW, FLEE, ATTACK, DEFEND_PLAYER, IDLE
action.urgency must be one of: LOW, MEDIUM, HIGH

Decide the action based on context: if the player is low HP with nearby hostile mobs at night, use FLEE or DEFEND_PLAYER with HIGH urgency. Otherwise use FOLLOW or IDLE."""


class NearbyMob(BaseModel):
    type: str
    distance: float


class PlayerState(BaseModel):
    name: str
    hp: int
    max_hp: int
    food_level: int
    main_hand_item: str


class EnvironmentState(BaseModel):
    dimension: str
    biome: str
    time_of_day: str
    light_level: int
    nearby_mobs: Optional[List[NearbyMob]] = Field(default_factory=list)


class ContextPayload(BaseModel):
    timestamp: int
    trigger: str = Field(..., pattern="^(PERIODIC_TICK|CHAT_MESSAGE)$")
    player: PlayerState
    environment: EnvironmentState
    chat_message: Optional[str] = None


class ActionCommand(BaseModel):
    type: str = Field(..., pattern="^(FOLLOW|FLEE|ATTACK|DEFEND_PLAYER|IDLE)$")
    target_position: Optional[List[float]] = None
    urgency: str = Field("LOW", pattern="^(LOW|MEDIUM|HIGH)$")


class ActionResponse(BaseModel):
    dialogue: str
    action: ActionCommand


def build_user_message(payload: ContextPayload) -> str:
    parts = [
        f"Player: {payload.player.name}",
        f"HP: {payload.player.hp}/{payload.player.max_hp}",
        f"Food: {payload.player.food_level}",
        f"Item: {payload.player.main_hand_item}",
        f"Dimension: {payload.environment.dimension}",
        f"Biome: {payload.environment.biome}",
        f"Time: {payload.environment.time_of_day}",
        f"Light: {payload.environment.light_level}",
    ]
    if payload.environment.nearby_mobs:
        mobs = ", ".join(
            f"{m.type} ({m.distance}m)" for m in payload.environment.nearby_mobs
        )
        parts.append(f"Nearby mobs: {mobs}")
    else:
        parts.append("Nearby mobs: none")
    if payload.chat_message:
        parts.append(f"Player said: {payload.chat_message}")
    return "\n".join(parts)


def call_lm_studio(payload: ContextPayload) -> ActionResponse:
    user_msg = build_user_message(payload)

    request_body = {
        "model": LM_MODEL,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_msg},
        ],
        "temperature": 0.7,
        "max_tokens": 256,
    }

    try:
        with httpx.Client(timeout=120.0) as hc:
            resp = hc.post(
                f"{LM_STUDIO_URL}/chat/completions",
                json=request_body,
            )
            resp.raise_for_status()
            data = resp.json()
            raw = data["choices"][0]["message"]["content"].strip()
            if raw.startswith("```"):
                raw = raw.strip("`").strip()
                if raw.startswith("json"):
                    raw = raw[4:].strip()
            parsed = json.loads(raw)
            return ActionResponse(
                dialogue=parsed.get("dialogue", "..."),
                action=ActionCommand(
                    type=parsed.get("action", {}).get("type", "IDLE"),
                    urgency=parsed.get("action", {}).get("urgency", "LOW"),
                ),
            )
    except Exception as e:
        logger.warning(f"LM Studio call failed: {e}")
        return fallback_response(payload)


def fallback_response(payload: ContextPayload) -> ActionResponse:
    if payload.trigger == "CHAT_MESSAGE":
        return ActionResponse(
            dialogue="話を聞いてるよ！",
            action=ActionCommand(type="IDLE", urgency="LOW"),
        )
    return ActionResponse(
        dialogue="そばにいるよ！",
        action=ActionCommand(type="FOLLOW", urgency="LOW"),
    )


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/context")
def handle_context(payload: ContextPayload) -> ActionResponse:
    return call_lm_studio(payload)