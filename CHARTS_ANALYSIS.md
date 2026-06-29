# Аналіз графіків та візуалізацій

## Залежності

```kotlin
// app/build.gradle.kts
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
```

Основна бібліотека — **MPAndroidChart v3.1.0**. Частина графіків реалізована через кастомний **Canvas**.

---

## Зведена таблиця

| Графік | Фрагмент | Реалізація | Дані |
|---|---|---|---|
| Line Chart (вага тіла) | `StatsFragment` | MPAndroidChart | `BodyWeightDao` |
| Bar Chart (макс. вага по категорії) | `StatsFragment` | MPAndroidChart | `observeCategoryStats()` |
| Bar Chart (к-сть підходів по категорії) | `StatsFragment` | MPAndroidChart | `observeCategoryStats()` |
| Лінія пульсу в реальному часі | `PulseFragment` | Custom Canvas / TextureView | `OutputAnalyzer` (камера) |
| Хвиляста анімація | `WaveView` | Custom Canvas | Ручні гармоніки |
| Календар тренувань | `CalendarView` | Custom RecyclerView | Дані тренувань |

---

## 1. StatsFragment — три графіки MPAndroidChart

**Файли:**
- `ui/home/stats/StatsFragment.kt`
- `ui/home/stats/viewmodel/StatsViewModel.kt`
- `res/layout/fragment_stats.xml`

### 1.1 Line Chart — прогрес ваги тіла

- Тип: лінійний з кубічними кривими Безьє
- Дані: останні 30 вимірювань ваги тіла
- Компактний режим: показує 5 останніх точок, клік розгортає до повного
- Особливості: кругові маркери точок, плавна інтерполяція, висота 220dp

### 1.2 Bar Chart — максимальна вага по категорії

- Тип: вертикальні стовпці
- Дані: максимальна підніята вага в розрізі груп м'язів
- Осі: X — назва категорії, Y — вага
- Особливості: кольорова схема Material, підписи повернуті на -25°, анімація входу 400ms

### 1.3 Bar Chart — к-сть підходів по категорії

- Тип: вертикальні стовпці
- Дані: загальна к-сть підходів в розрізі груп м'язів
- Особливості: кольорова схема Joyful, підписи повернуті на -25°, анімація входу

---

## 2. PulseFragment — кастомний Canvas графік пульсу

**Файли:**
- `ui/home/pulse/heartrate/ChartDrawer.kt`
- `ui/home/pulse/PulseFragment.kt`
- `res/layout/fragment_pulse.xml`

- Тип: лінійний графік в реальному часі
- Рендеринг: `TextureView` + `Canvas` + `Path` — без сторонніх бібліотек
- Дані: BPM, що знімаються через камеру (`OutputAnalyzer`, алгоритм valley detection)
- Особливості: синій колір лінії, антіаліасінг, нормалізація по min/max, оновлюється в реальному часі
- Layout: два `TextureView` (80dp і 200dp), `LottieAnimationView` (серце, що б'ється)

---

## 3. WaveView — кастомна хвиляста анімація

**Файл:** `utils/view/WaveView.kt`

- Тип: анімована хвиля на Canvas
- Реалізація: синусоїдальні гармоніки з настроюваними амплітудою, частотою та фазою
- Колір заливки: напівпрозорий червоний (`#33FF0000`)
- Призначення: декоративна візуалізація

---

## 4. CalendarView — календар тренувань

**Файли:**
- `utils/view/calendarview/CalendarView.kt`
- `res/layout/view_calendar.xml`

- Тип: горизонтальний RecyclerView з днями місяця
- Функціонал: навігація по місяцях, маркери виконаних/запланованих тренувань
- Не є графіком у класичному сенсі, але відображає дані візуально

---

## Дані для графіків

### DAO запити (`TrainingsDao.kt`)

```kotlin
// Агрегована статистика по днях
@Query("""
    SELECT date(startTime/1000, 'unixepoch') as day,
           SUM(activeDuration) as totalMinutes,
           AVG(avgBPM) as avgBpm,
           SUM(calories) as calories
    FROM TrainingsEntity
    GROUP BY day
""")
fun observeDailyStats(): Flow<List<DailyStatsRow>>

// Статистика по категоріях вправ
@Query("""
    SELECT e.baseCategory,
           MAX(ts.weight) as maxWeight,
           COUNT(ts.id) as totalSets
    FROM ExercisesEntity e
    JOIN TrainingExerciseEntity te ON e.id = te.exerciseId
    JOIN TrainingSetEntity ts ON te.id = ts.trainingExerciseId
    GROUP BY e.baseCategory
    ORDER BY totalSets DESC
""")
fun observeCategoryStats(): Flow<List<CategoryStatRow>>
```

### Repository (`TrainingRepository.kt`)

```kotlin
fun observeDailyStats(): Flow<List<DailyStatsRow>>
fun observeMoodTimeline(): Flow<List<MoodRow>>
fun observeCategoryStats(): Flow<List<CategoryStatRow>>
fun observeBodyWeight(): Flow<List<BodyWeightEntity>>
```

### Моделі даних

| Модель | Файл | Призначення |
|---|---|---|
| `ChartPoint` | `domain/models/ChartPoint.kt` | Координати точки (x, y) |
| `DailyStatsRow` | `domain/models/DailyStatsRow.kt` | Агрегована статистика за день |
| `DailyStatsUi` | `domain/models/DailyStatsUi.kt` | UI-представлення денної статистики |
| `CategoryStatRow` | `domain/models/CategoryStatRow.kt` | Статистика по категорії вправ |
| `MoodRow` | `domain/models/MoodRow.kt` | Дані настрою по дню |
| `MoodDayUi` | `domain/models/MoodDayUi.kt` | UI enum: BAD / NEUTRAL / GOOD / AMAZING |

---

## Потік даних

```
Room Database
    ↓
TrainingsDao / BodyWeightDao
    ↓
TrainingRepository (Flow)
    ↓
StatsViewModel (маппінг у ChartPoint / Entry)
    ↓
StatsFragment → MPAndroidChart

Камера (TextureView)
    ↓
OutputAnalyzer (BPM detection)
    ↓
ChartDrawer (Canvas)
    ↓
PulseFragment → TextureView
```
