import os
import glob
import numpy as np
import pandas as pd
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv1D, MaxPooling1D, GRU, Dense, Dropout, BatchNormalization, Input
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint 
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score
import joblib
from tqdm import tqdm

#
SEQUENCE_LENGTH = 40
STEP = 4
DATA_DIR = 'data'
TEST_DIR = 'test-data'
MODEL_PATH = 'model.keras'
SCALER_PATH = 'scaler.gz'
BATCH_SIZE = 512
EPOCHS = 150
PATIENCE = 10 
EVALUATION_THRESHOLD = 0.9
MONITORING_PRECISION_THRESHOLD = 0.1

ALL_FEATURES_IN_ORDER = [
    'delta_yaw', 'delta_pitch', 'accel_yaw', 'accel_pitch', 'jerk_yaw',
    'jerk_pitch', 'gcd_error_yaw', 'gcd_error_pitch', 'is_on_ground',
    'is_sprinting', 'is_sneaking', 'is_using_item', 'player_speed_horizontal',
    'player_fall_distance', 'is_attacking', 'ticks_since_attack'
]
FEATURES_TO_USE = [
    'delta_yaw', 'delta_pitch', 'accel_yaw', 'accel_pitch', 'jerk_yaw',
    'jerk_pitch', 'gcd_error_yaw', 'gcd_error_pitch', #'is_attacking',
   # 'ticks_since_attack'
]

try:
    indices_to_keep = [ALL_FEATURES_IN_ORDER.index(feature) for feature in FEATURES_TO_USE]
    num_features_to_use = len(FEATURES_TO_USE)
except ValueError as e:
    exit()

def load_data_with_sliding_window(directory):
    all_sequences, all_labels = [], []
    csv_files = glob.glob(os.path.join(directory, '*.csv'))
    if not csv_files:
        print(f"В директории {directory} не найдено CSV файлов.")
        return np.array([]), np.array([])
    
    column_names = ['is_cheating'] + ALL_FEATURES_IN_ORDER
    for file_path in tqdm(csv_files, desc=f"Обработка файлов из {directory}"):
        try:
            df = pd.read_csv(file_path, header=0, names=column_names)
            label = df['is_cheating'].iloc[0]
            df_features = df[ALL_FEATURES_IN_ORDER].dropna()
        except Exception as e:
            print(f"Ошибка чтения или обработки файла {file_path}: {e}")
            continue
        if len(df_features) < SEQUENCE_LENGTH:
            continue
        data_values = df_features.values
        for i in range(0, len(data_values) - SEQUENCE_LENGTH + 1, STEP):
            all_sequences.append(data_values[i:i + SEQUENCE_LENGTH])
            all_labels.append(label)

    if not all_sequences:
        return np.array([]), np.array([])
    
    X_full = np.array(all_sequences, dtype=np.float32)
    X_sliced = X_full[:, :, indices_to_keep]
    return X_sliced, np.array(all_labels, dtype=np.float32)

X_train, y_train = load_data_with_sliding_window(DATA_DIR)
X_test, y_test = load_data_with_sliding_window(TEST_DIR)

if len(X_train) == 0 or len(X_test) == 0:
    exit()

num_features = num_features_to_use
print(f"Количество используемых признаков: {num_features}")

scaler = StandardScaler()
X_train_reshaped = X_train.reshape(-1, num_features)
scaler.fit(X_train_reshaped)
joblib.dump(scaler, SCALER_PATH)
X_train_scaled = scaler.transform(X_train_reshaped).reshape(X_train.shape)
X_test_scaled = scaler.transform(X_test.reshape(-1, num_features)).reshape(X_test.shape)

train_dataset = tf.data.Dataset.from_tensor_slices((X_train_scaled, y_train))
train_dataset = train_dataset.cache().shuffle(buffer_size=len(X_train)).batch(BATCH_SIZE).prefetch(buffer_size=tf.data.AUTOTUNE)

test_dataset = tf.data.Dataset.from_tensor_slices((X_test_scaled, y_test))
test_dataset = test_dataset.batch(BATCH_SIZE).cache().prefetch(buffer_size=tf.data.AUTOTUNE)


class_weights_dict = {0: 2.0, 1: 1.0} 
print(f"\nВеса классов {class_weights_dict}")

model = Sequential([
    Input(shape=(SEQUENCE_LENGTH, num_features)),
    Conv1D(filters=128, kernel_size=5, activation='relu', padding='same'),
    BatchNormalization(),
    MaxPooling1D(pool_size=2),
    Dropout(0.5), 
    Conv1D(filters=128, kernel_size=3, activation='relu', padding='same'),
    BatchNormalization(),
    MaxPooling1D(pool_size=2),
    Dropout(0.4), 
    GRU(128, return_sequences=False),
    BatchNormalization(),
    Dropout(0.5),
    Dense(128, activation='relu'),
    Dropout(0.5), 
    Dense(1, activation='sigmoid')
])


precision_at_eval_threshold = tf.keras.metrics.Precision(thresholds=EVALUATION_THRESHOLD, name=f'precision_at_{EVALUATION_THRESHOLD}')
recall_at_eval_threshold = tf.keras.metrics.Recall(thresholds=EVALUATION_THRESHOLD, name=f'recall_at_{EVALUATION_THRESHOLD}')
precision_at_monitoring_threshold = tf.keras.metrics.Precision(thresholds=MONITORING_PRECISION_THRESHOLD, name=f'precision_at_{MONITORING_PRECISION_THRESHOLD}')


initial_learning_rate = 0.0005
lr_schedule = tf.keras.optimizers.schedules.ExponentialDecay(
    initial_learning_rate,
    decay_steps=len(train_dataset) * 15,
    decay_rate=0.5,
    staircase=True)

optimizer = tf.keras.optimizers.AdamW(learning_rate=lr_schedule, weight_decay=0.001)
loss_function = tf.keras.losses.BinaryCrossentropy(label_smoothing=0.1)

model.compile(
    optimizer=optimizer,
    loss=loss_function,
    metrics=['accuracy', precision_at_eval_threshold, recall_at_eval_threshold, precision_at_monitoring_threshold]
)
model.summary()


METRIC_TO_MONITOR = f'val_{recall_at_eval_threshold.name}'
checkpoint = ModelCheckpoint(filepath=MODEL_PATH, monitor=METRIC_TO_MONITOR, mode='max', save_best_only=True, verbose=1)
early_stopping = EarlyStopping(monitor=METRIC_TO_MONITOR, patience=PATIENCE, mode='max', restore_best_weights=True, verbose=1)

history = model.fit(
    train_dataset,
    epochs=EPOCHS,
    validation_data=test_dataset,
    callbacks=[checkpoint, early_stopping], 
    class_weight=class_weights_dict,
    verbose=1
)


try:
    custom_objects = {
        precision_at_eval_threshold.name: precision_at_eval_threshold,
        recall_at_eval_threshold.name: recall_at_eval_threshold,
        precision_at_monitoring_threshold.name: precision_at_monitoring_threshold
    }
    model = tf.keras.models.load_model(MODEL_PATH, custom_objects=custom_objects)
    print("Модель успешно загружена со всеми кастомными метриками.")
except Exception as e:
    print(f"Не удалось загрузить модель с диска, используется модель из памяти. Ошибка: {e}")

print(f"\n--- Финальная оценка лучшей модели на тестовых данных ---")
results = model.evaluate(test_dataset, verbose=0)
print(f"  Потери (Loss): {results[0]:.4f}")
print(f"  Точность (Accuracy): {results[1]*100:.2f}%")
print(f"\n  --- Основные метрики (Порог {EVALUATION_THRESHOLD}) ---")
print(f"  Точность предсказания (Precision): {results[2]*100:.2f}%")
print(f"  Полнота (Recall): {results[3]*100:.2f}%")
print(f"\n  --- Мониторинговая метрика (Порог {MONITORING_PRECISION_THRESHOLD}) ---")
print(f"  Точность предсказания (Precision): {results[4]*100:.2f}%")

print(f"\n--- Финальный отчет по классификации (с порогом {EVALUATION_THRESHOLD}) ---")
y_pred_proba = model.predict(X_test_scaled, verbose=0).flatten()
y_pred_class = (y_pred_proba > EVALUATION_THRESHOLD).astype(int)

print(f"\nОтчет по классификации:")
print(classification_report(y_test, y_pred_class, target_names=['LEGIT', 'CHEAT']))
print("\nМатрица ошибок:")
print(confusion_matrix(y_test, y_pred_class))

try:
    roc_auc = roc_auc_score(y_test, y_pred_proba)
    print(f"\nROC-AUC Score: {roc_auc:.4f}")
except ValueError as e:
    print(f"\nНе удалось рассчитать ROC-AUC: {e}")
