# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**Lịch Âm** — a Vietnamese lunar/perpetual calendar Android app (`com.utc.cal`). Single-module Gradle project (`:app`), Kotlin with **XML/View-based UI** (Material Components / Material 3 theme, view binding). All user-facing strings are in Vietnamese. Originally scaffolded from Google AI Studio (hence the `GEMINI_API_KEY` plumbing), but the shipping app does no network/AI calls — it computes everything locally.

The app UI was migrated from Jetpack Compose to classic XML layouts + Views; there is no Compose code left. If you see Compose references anywhere, they are stale.

## Commands

Uses the Gradle wrapper (`./gradlew`). **Requires JDK 17+** (AGP 9.x) — if the default JDK is 11, set `JAVA_HOME` to a 17/21 JDK. Two build variants: `debug` (signed with a local `debug.keystore`) and `release` (signed via `KEYSTORE_PATH`/`STORE_PASSWORD`/`KEY_PASSWORD` env vars).

```bash
./gradlew assembleDebug                # build debug APK
./gradlew installDebug                 # build + install on connected device/emulator
./gradlew testDebugUnitTest            # run JVM unit tests
./gradlew connectedDebugAndroidTest    # run instrumented tests (needs device/emulator)
./gradlew lint                         # Android lint

# Run a single unit test class or method:
./gradlew testDebugUnitTest --tests "com.utc.cal.CalendarNavigationTest"
./gradlew testDebugUnitTest --tests "com.utc.cal.CalendarNavigationTest.lunarDatesResolveAcrossTwoYears"
```

`debug.keystore` is git-ignored and not committed; generate one before the first debug build (or point `debugConfig` at your own). `ExampleRobolectricTest` is configured for `@Config(sdk=[36])`, which requires Robolectric to download the SDK 36 image — it fails in offline environments.

## Setup notes

- Dependencies are declared through the version catalog `gradle/libs.versions.toml` (aliases like `libs.androidx.room.ktx`), not inline versions. Add/upgrade deps there.
- `buildFeatures { viewBinding = true }` is on — generated `*Binding` classes are the standard way to reach views (e.g. `ActivityMainBinding`). An included layout is reachable only when its `<include>` has an `android:id`.
- The **Secrets Gradle Plugin** reads `.env` (fallback `.env.example`) and exposes keys via `BuildConfig`. Create a local `.env` from `.env.example` if a Gemini key is ever needed; `.env` is git-ignored.
- KSP is used for annotation processing (Room, Moshi codegen). Several deps (camera, coil, datastore, firebase-ai) are present in the catalog but commented out in `app/build.gradle.kts` — uncomment to re-enable rather than re-adding.

## Architecture

**Lunar date engine — `LunarUtils.kt`** is the heart of the app. It's a Kotlin port of the Hồ Ngọc Đức astronomical algorithm (Julian day numbers, new-moon and sun-longitude calculations) converting solar → Vietnamese lunar dates, always at **timezone +7**. It also derives Can-Chi (Heavenly Stem / Earthly Branch) names for day/month/year. Treat this as the single source of truth for calendar math — both the UI and the widgets call into it. Changing its output ripples into screenshot tests.

**App UI (XML/Views)** — `MainActivity` (an `AppCompatActivity`) inflates `activity_main.xml`. Infinite month scrolling uses a `ViewPager2` backed by `MonthPagerAdapter`, which keeps the old anchor trick: `MonthPagerAdapter.ANCHOR = 6000` is the current month and each page is that many months away (`PAGE_COUNT = 12000`). Each page (`item_month_page.xml`) is built programmatically as a 6×7 grid of weighted `item_day_cell.xml` cells so it fills the pager area — the same layout the Compose grid had. The selected-date panel is `view_date_details.xml`, `<include>`d with id `details` and populated in `MainActivity.bindDetails()`. The weekday header row is generated in code (`buildWeekdayHeader()`) because its labels/weekend coloring depend on `startOnMonday`. Theming comes from the `Theme.Material3.DynamicColors.DayNight` app theme (`res/values/themes.xml`) — Material You dynamic colors on Android 12+, Material 3 baseline otherwise; cell/text colors are resolved at runtime from theme attrs via `MaterialColors.getColor`.

**Home-screen widgets** — two `AppWidgetProvider`s in `widget/`, both built with classic **`RemoteViews` + XML layouts** (NOT Compose/Glance):
- `CalendarWidgetProvider` → `widget_calendar.xml`: a single large "today" card.
- `MonthWidgetProvider` → `widget_month.xml`: a full month grid (6×7 cells addressed by generated `cell_r_c` / `cell_r_c_bg` IDs).

Widgets read the same computed lunar dates from `LunarUtils`. Settings changes broadcast `ACTION_APPWIDGET_UPDATE` to both providers to force a redraw (see `MainActivity`'s `onSave`).

**Settings — `WidgetSettingsManager.kt`** is a thin `SharedPreferences` wrapper (`"widget_settings"`) shared by the app and both widgets: `showLunarDate`, `useDynamicColor`, `startOnMonday`, `widgetThemeColor`. The editor is a `MaterialAlertDialogBuilder` dialog (`dialog_settings.xml` + `MainActivity.showSettingsDialog()`); its color-swatch picker is built in code. Saving broadcasts `ACTION_APPWIDGET_UPDATE` to both widget providers and refreshes the in-app grid.

### Widget theming gotcha

Widget colors are resolved by matching `widgetThemeColor` (an ARGB `Int`) against hardcoded constants (e.g. `0xFF8C1D18` → `widget_bg_0`, `0xFFFFFFFF` → the white/blue theme). `useDynamicColor` on Android S+ overrides these with Material You `system_accent1`/`system_neutral1` resources. When adding a theme color you must add both the constant→drawable mapping in **both** widget providers and the corresponding `widget_bg_*` drawable. The white theme (`0xFFFFFFFF`) is a special case with its own text colors and a blue accent.

### Widget layout generation

`fix_layout.py` / `fix_layout_2.py` are one-off scripts that regenerated the repetitive `cell_r_c` `TextView`/`FrameLayout` blocks in `widget_month.xml`. They are not part of the build; use them as a reference if you need to bulk-edit that layout's cells. The loose `test_*.kt` files at the repo root are scratch snippets, not part of the source set.
