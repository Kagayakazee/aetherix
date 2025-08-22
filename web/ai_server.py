from fastapi import FastAPI, HTTPException, Request, Response, Header
import numpy as np
import joblib
import os
import tensorflow as tf
from tensorflow.keras.models import load_model
from tensorflow.keras.metrics import Precision, Recall
import msgpack
import asyncio
from functools import partial
import logging
from typing import Optional

logging.basicConfig(level=logging.INFO, format='%(levelname)s:%(name)s:%(message)s')
logger = logging.getLogger(__name__)

MODEL_PATH = 'model.keras'
SCALER_PATH = 'scaler.gz'
EXPECTED_SECRET_KEY = "JKsjdfkjsd(*!@&*d8synbczjxi*&A*&1i2jhnzxc"

ALL_FEATURES_IN_ORDER = [
    'delta_yaw', 'delta_pitch', 'accel_yaw', 'accel_pitch', 'jerk_yaw',
    'jerk_pitch', 'gcd_error_yaw', 'gcd_error_pitch', 'is_on_ground',
    'is_sprinting', 'is_sneaking', 'is_using_item', 'player_speed_horizontal',
    'player_fall_distance', 'is_attacking', 'ticks_since_attack'
]
FEATURES_TO_USE = [
    'delta_yaw', 'delta_pitch', 'accel_yaw', 'accel_pitch', 'jerk_yaw',
    'jerk_pitch', 'gcd_error_yaw', 'gcd_error_pitch',
]
indices_to_keep = [ALL_FEATURES_IN_ORDER.index(feature) for feature in FEATURES_TO_USE]
num_features_to_use = len(FEATURES_TO_USE)


app = FastAPI(title="huyna")
model = None
scaler = None


@app.on_event("startup")
def load_model_and_scaler():
    global model, scaler
    if not os.path.exists(MODEL_PATH) or not os.path.exists(SCALER_PATH):
        raise RuntimeError(f"Файлы модели или скейлера не найдены! Проверьте пути: {MODEL_PATH}, {SCALER_PATH}")

    custom_objects = {
        'precision_at_0.9': Precision(thresholds=0.9),
        'recall_at_0.9': Recall(thresholds=0.9),
        'precision_at_0.1': Precision(thresholds=0.1)
    }
    model = load_model(MODEL_PATH, custom_objects=custom_objects)
    scaler = joblib.load(SCALER_PATH)

    dummy_input = np.zeros((1, 40, num_features_to_use), dtype=np.float32)
    model.predict(dummy_input)

def clean_data(data_list: list) -> list:
    cleaned_list = []
    for row in data_list:
        cleaned_row = [float(v) if isinstance(v, (int, float)) else 0.0 for v in row]
        cleaned_list.append(cleaned_row)
    return cleaned_list

def run_blocking_inference(data_list: list) -> dict:
    cleaned_sequence_data = clean_data(data_list)
    sequence_np = np.array(cleaned_sequence_data, dtype=np.float32)

    expected_shape = (40, len(ALL_FEATURES_IN_ORDER))
    if sequence_np.shape != expected_shape:
        return {"cheat_probability": 0.0, "prediction_result": 0.0}

    sequence_sliced = sequence_np[:, indices_to_keep]
    scaled_sequence = scaler.transform(sequence_sliced)
    input_for_model = np.expand_dims(scaled_sequence, axis=0)
    prediction_proba = model.predict(input_for_model, verbose=0)
    
    prob = float(prediction_proba[0][0])
    return {"cheat_probability": prob, "prediction_result": prob}

@app.post("/predict")
async def predict(request: Request, x_secret_key: Optional[str] = Header(None)):
    if x_secret_key != EXPECTED_SECRET_KEY:
        raise HTTPException(status_code=403, detail="Forbidden: Invalid or missing secret key")

    if not model or not scaler:
        raise HTTPException(status_code=503, detail="Модель еще не загружена.")

    if request.headers.get('content-type') != 'application/x-msgpack':
        return Response(status_code=415, content="Unsupported Media Type: must be application/x-msgpack")

    try:
        body = await request.body()
        payload = msgpack.unpackb(body, raw=False)

        if not isinstance(payload, dict):
            return Response(status_code=400, content="Bad Request: Payload must be a dictionary")

        sequence_data = payload.get('ticks_data')

        if not isinstance(sequence_data, list):
            return Response(status_code=400, content="Bad Request: 'ticks_data' must be a list")
        
        logger.info(f'{request.client.host} - "POST /predict HTTP/1.1" 200 OK')

        loop = asyncio.get_running_loop()
        func_to_run = partial(run_blocking_inference, sequence_data)
        result = await loop.run_in_executor(None, func_to_run)

        return result

    except msgpack.exceptions.UnpackException:
        return Response(status_code=400, content="Bad Request: Malformed MessagePack payload")
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}")
        raise HTTPException(status_code=500, detail="Internal Server Error")


@app.get("/")
def root():
    return {"message": "huyna123123asdzxc123qweqwe"}