# Аналіз механізмів зберігання даних

## Огляд

У проекті використовується три механізми збереження даних:

| Механізм | Файл | Що зберігає |
|---|---|---|
| **Room** | `GymDb.db` | Весь основний контент: тренування, вправи, їжа, пульс, вага |
| **DataStore** | `CurrentUserStore.kt` | `current_user_id` як `Flow<String?>` |
| **SharedPreferences** | `UserPrefs.kt` + `SettingsRepositoryImpl` | `current_user_id` (дублікат) + версія апки |

---

## Room Database

**Версія:** 11  
**Стратегія міграції:** `fallbackToDestructiveMigration()` — при зміні схеми дані стираються  
**DI:** Koin, `DatabaseModule.kt`

### Entities (11 таблиць)

| Entity | Що зберігає |
|---|---|
| `UserEntity` | Профіль: ім'я, вік, стать, вага, зріст, пульс, streak, розклад тренувань |
| `TrainingsEntity` | Сесія тренування: час, тривалість, пульс, калорії, настрій |
| `TrainingExerciseEntity` | Зв'язок тренування ↔ вправа з порядком |
| `TrainingSetEntity` | Підхід: к-сть повторів, вага |
| `ExercisesEntity` | Каталог вправ: назва, категорія, складність |
| `ExercisePrEntity` | Особисті рекорди по кожній вправі |
| `SetsEntity` | Легасі-підходи (стара схема) |
| `BodyWeightEntity` | Історія ваги тіла |
| `PulseEntity` | Лог пульсу по типу активності |
| `MealInfoEntity` | Прийоми їжі: дата, тип, КБЖУ |
| `FoodEntity` | Каталог продуктів з нутрієнтами |

### Type Converters

Складні об'єкти серіалізуються в JSON через Gson:
- `PentagonConverter` — метрики `Pentagon`
- `TrainingDayConverter` — об'єкти днів
- `DayTypesConverter` — маппінг типів днів
- `DaySlotsConverter` — списки тренувальних слотів

---

## DataStore (Preferences)

**Файл:** `data/user/CurrentUserStore.kt`  
**DI:** `PreferencesModule.kt`  
**Ключ:** `current_user_id` (String)

### Навіщо DataStore, а не SharedPreferences

Єдина і головна причина — **реактивність**. ViewModels будують реактивний ланцюжок:

```kotlin
currentUserIdFlow           // Flow<String?> від DataStore
    .flatMapLatest { id ->
        userDao.observeById(id)  // Flow від Room
    }
```

DataStore повертає `Flow<String?>` нативно. SharedPreferences повертає значення синхронно — для аналогічного Flow довелося б обгортати через `callbackFlow` з `OnSharedPreferenceChangeListener`, що складніше і гірше підтримує корутини.

---

## SharedPreferences

Два незалежних екземпляри:

| Ім'я | Файл | Що зберігає |
|---|---|---|
| `"user_prefs"` | `UserPrefs.kt` | `current_user_id` |
| `"app_prefs"` | `AppModule.kt` + `SettingsRepositoryImpl` | Версія апки (`VERSION`) |

---

## Проблема: дублювання current_user_id

`UserPrefs.kt` (SharedPreferences) і `CurrentUserStore.kt` (DataStore) зберігають **одне й те саме** — `current_user_id`. Це виглядає як незавершений перехід зі старої реалізації на нову.

```
UserPrefs.kt        — стара реалізація (SharedPreferences, синхронна)
CurrentUserStore.kt — нова реалізація (DataStore, Flow-based)
```

---

## Рекомендації

| Компонент | Дія | Причина |
|---|---|---|
| `CurrentUserStore` (DataStore) | **Залишити** | Потрібен для реактивного Flow у ViewModels |
| `UserPrefs` (SharedPreferences) | **Прибрати** | Дублює DataStore, є легасі |
| `SettingsRepositoryImpl` (SharedPreferences) | **Залишити** | Зберігає тільки версію апки, Flow не потрібен |

### Чи варто замінювати DataStore на SharedPreferences?

**Ні.** Заміна вимагала б обгортки `callbackFlow` + `OnSharedPreferenceChangeListener` — більше коду, більше ризиків витоку пам'яті, гірша інтеграція з корутинами. DataStore вже вирішує задачу правильно.

---

## Схема потоку даних

```
Remote API (Firebase, OpenFoodFacts)
         ↓
    Repository Layer
         ↓
Room Database (GymDb.db)
         ↓
    ViewModel (Flow chains)
         ↓
      UI / Fragments

+ DataStore ──► current_user_id (Flow)
+ SharedPreferences ──► app version
```
