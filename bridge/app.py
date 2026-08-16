"""Minecraft LLM Companion AI - Local Bridge Server (MVP Step 2)

FastAPI server that receives context state payloads from the Java mod and
responds with an action command. For now it returns a fixed dummy response;
LLM integration comes in a later step.
"""

from typing import List, Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(title="Aibots Bridge Server", version="0.1.0")


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


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/context")
def handle_context(payload: ContextPayload) -> ActionResponse:
    """
    Receive a context payload and return a fixed dummy action response.
    Validates the incoming JSON against the spec schema.
    """
    trigger = payload.trigger
    if trigger == "CHAT_MESSAGE":
        chat = payload.chat_message or "(no message)"
        return ActionResponse(
            dialogue=f"話を聞いたよ！「{chat}」について考えてる…（ダミー応答・LLM連携は次のステップ）",
            action=ActionCommand(type="IDLE", urgency="LOW"),
        )
    return ActionResponse(
        dialogue="いつでもそばにいるよ！（ダミー応答・追従はJava側で管理）",
        action=ActionCommand(type="FOLLOW", urgency="LOW"),
    )