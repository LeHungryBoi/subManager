# SubScale: High-Density Power-User Interface Overhaul

Complete redesign of the SubScale subtitle fine-tuning interface for power users, maximizing screen real estate and information density while streamlining the workflow.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following layout and interaction choices have been synthesized directly from your power-user requirements:
> - **Top Bar Apply Action**: The high-frequency "Apply to System" action is placed directly in the `TopAppBar` as a prominent action button, eliminating scroll fatigue.
> - **Single Vertical Scroll View**: All tab rows are removed. Subtitle styling controls (scale, text, background, edges, layout) flow in a single, continuous, compact vertical list.
> - **Total Elimination of Redundant Presets & Math Cards**: Removed the "Quick Presets & OS Steps" chip cloud, the style presets list, the viewport math card, and the opacity preset buttons.
> - **Photoshop-Style Custom Color Picker Modal**: An interactive 2D Saturation/Value gradient canvas with a Hue slider, Hex/RGB readout, and a single-line preset swatch strip at the bottom.
> - **Ultra-Compact Preview with Overlay Controls**: Default preview height reduced to a sleek ~110dp caption-focused strip with overlay buttons for aspect-ratio expansion, backdrop switching, and OS comparison.
> - **Dedicated Tools Sheet for Non-Styling Options**: ADB commands, system write permission status, system caption sync, and custom preview dialogue editing are consolidated in a dedicated "System & Tools" bottom sheet accessed from the top bar.

---

## 1. Overview & Core Concept

- **What It Does**: SubScale provides granular, sub-step control over Android system caption sizes and styles for YouTube and media apps. This overhaul replaces the multi-tab, spaced-out layout with an ultra-dense, single-scroll workspace designed for rapid parameter adjustment.
- **Target Audience / Persona**: Power users, accessibility customizers, and video enthusiasts who want precise subtitle calibration without wading through generic cards, tabs, or bloated preset panels.
- **Key Value**: Immediate 1-tap system application, zero wasted vertical space, continuous scroll access to all parameters, and precision Photoshop-grade color tuning.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Granular Size & Style Tuning**:
   - The user opens the app and immediately sees the compact preview displaying their current subtitle config.
   - In a single continuous scroll, they can adjust Scale (`-` / `+` steppers and slider), Text styling, Background box, Edge/Shadow bevels, and Window placement.
   - Tapping any color swatch opens the Photoshop-style popup to select exact hues or hex codes, or pick from the single-line bottom palette.
2. **Instant System Application**:
   - At any moment while tuning, the user taps the prominent **Apply** button pinned in the top app bar to write settings directly to Android OS.
3. **Preview Mode & Backdrop Switching**:
   - Preview overlays allow 1-tap switching between the compact 110dp banner and the full 16:9 player, changing backdrop scenes (beach sunset, daytime city, dark studio), and toggling the OS comparison badge.
4. **System Tools & ADB Access**:
   - Tapping the Tools/Terminal icon in the top bar slides up the System & ADB Sheet to inspect `WRITE_SECURE_SETTINGS` permission, copy the ADB grant command, or edit custom sample text dialogue.

### Visual Identity & Theme

- **Aesthetic Direction**: High-density utility / pro tool aesthetic. Clean, crisp borders, dark-mode elevated surfaces, high-contrast typography, and compact interactive touchpoints.
- **Color Palette & Mood**:
  - Pinned Apply Action: High-visibility Emerald Green (`#2E7D32` / `#4CAF50`) container for instant discovery.
  - Surfaces: Deep obsidian (`#121316`) and slate surface containers (`#1C1D22`).
  - Accents: Vibrant Electric Blue (`#2979FF`) active track indicators and crisp White typography.
- **Typography & Hierarchy**:
  - Section headers: 12sp Bold uppercase labels with subtle divider lines.
  - Values: Monospace/numeric badges (`0.75x`, `85%`, `#FFFF00`) for quick scanning.
  - Sliders & Steppers: Integrated inline `-` and `+` buttons flanking the continuous slider.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Pinned Top Bar Apply Button vs Floating Action Button**
  - *Chosen Approach*: Pinned right into `TopAppBar.actions` as a high-contrast filled pill button.
  - *Why*: Floating action buttons obscure list content in high-density scroll views. Pinned top-bar placement guarantees 1-tap availability from any scroll position without blocking sliders.
- **Decision 2: 2D Saturation-Value Canvas vs RGB Sliders for Custom Color**
  - *Chosen Approach*: 2D Saturation/Value gradient canvas with draggable reticle + Hue spectrum slider bar + Hex input + 1-line preset bar.
  - *Why*: Matches Photoshop's visual color box paradigm requested by the user, providing intuitive 2D gamut exploration in a compact dialog.
- **Decision 3: Separation of Styling vs System/ADB Tools**
  - *Chosen Approach*: Modal bottom sheet triggered via a top-bar wrench/terminal icon.
  - *Why*: Eliminates clutter from the main tuning workflow while keeping system permission diagnostics and ADB copy helpers 1 tap away.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        TopAppBar (Fixed Pinned)                        │
│ [SubScale Pro]             [System Tools] [Sync] [★ APPLY TO SYSTEM]   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│           Compact YouTube Preview (Default ~110dp / Expandable)         │
│  Overlays: [⛶ Full/Compact] [🌄 Backdrop Scene] [⚖ Compare OS]         │
│  Live Subtitle Rendering with dynamic pill padding, shadow & fontScale │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│             Single Continuous Vertical ScrollView (High Density)       │
│                                                                        │
│ ┌─ 1. SUBTITLE SCALE & STEPPER ──────────────────────────────────────┐ │
│ │  [-]  ══════════●════════════  [+]       [ 0.75x ]                 │ │
│ └────────────────────────────────────────────────────────────────────┘ │
│ ┌─ 2. TEXT APPEARANCE ───────────────────────────────────────────────┐ │
│ │  Color: [Swatch] -> Opens Photoshop Picker | Opacity: [ 100% ]     │ │
│ │  Bold: [Switch]  | Font: [Default | Sans | Serif | Mono | Casual]  │ │
│ └────────────────────────────────────────────────────────────────────┘ │
│ ┌─ 3. BACKGROUND CONTAINER ──────────────────────────────────────────┐ │
│ │  Color: [Swatch] -> Opens Photoshop Picker | Opacity: [ 80% ]      │ │
│ │  Radius: [ 4 dp ] | Horizontal Padding: [ 6 dp ]                   │ │
│ └────────────────────────────────────────────────────────────────────┘ │
│ ┌─ 4. EDGE & SHADOW BEVELS ──────────────────────────────────────────┐ │
│ │  Type: [None | Drop Shadow | Outline | Raised | Depressed]         │ │
│ │  Edge Color: [Swatch] | Thickness: [ 1.5x ]                        │ │
│ └────────────────────────────────────────────────────────────────────┘ │
│ ┌─ 5. WINDOW & VERTICAL POSITION ────────────────────────────────────┐ │
│ │  Window Color: [Swatch] | Window Opacity: [ 0% ]                   │ │
│ │  Vertical Position: [ 12% ] | Line Height: [ 1.25x ]               │ │
│ └────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Popups & Secondary Surfaces                       │
│                                                                        │
│ 1. PhotoshopColorPickerDialog:                                         │
│    - 2D Canvas (Saturation x Value) with touch indicator               │
│    - 1D Hue Spectrum Slider (0..360)                                   │
│    - Hex Code Field with instant preview                               │
│    - 1-Line Horizontal Preset Swatch Bar (11 high-contrast colors)     │
│                                                                        │
│ 2. SystemToolsBottomSheet:                                             │
│    - WRITE_SECURE_SETTINGS status badge                                │
│    - One-click copyable ADB terminal command                           │
│    - Refresh status & Open Android OS Caption Settings                 │
│    - Custom Preview dialogue text editor                               │
└────────────────────────────────────────────────────────────────────────┘
```

### Component & State Mapping

- **`CaptionViewModel`**: Holds `customConfig` (`CaptionConfig`), `systemConfig`, `hasWritePermission`, `previewText`, `selectedBackdrop`, and handles `applyToSystem()`.
- **`PhotoshopColorPickerDialog`**: Self-contained Compose modal managing HSV (Hue, Saturation, Value) conversions to ARGB Long, Hex string parsing, and touch-drag reticle calculation.
- **`CompactPreviewBanner`**: Renders caption styling with configurable height (compact 110dp vs expanded 16:9), embedding all switcher actions as unobtrusive overlay chips.
- **`HighDensityControls`**: Inline rows combining labels, numeric badges, steppers, and sliders with tight 4dp–8dp vertical margins.
