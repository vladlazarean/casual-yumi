# API policy

## Versioning

Casual Yumi follows Semantic Versioning. All artifacts share one version, aligned by `yumi-bom`.

**Now: 0.0.x previews.** Any release may change the public API. Every change is listed in [CHANGELOG.md](../CHANGELOG.md).

Once the API is declared stable, each part of the version means:

- **Patch (last number):** bug fixes and visual fixes that keep the documented look. No public API changes.
- **Minor (middle number):** new components, new parameters with defaults, new tokens. Binary-compatible.
- **Major (first number):** anything that breaks source or binary compatibility, or changes a design rule the README lists.

## What "public API" means

Everything `public` in `yumi-tokens`, `yumi-foundation` and `yumi-components`. Explicit API mode makes every public symbol a deliberate choice. Automated ABI dumps (KGP's `abiValidation`) are not available yet under AGP 9's built-in Kotlin, so public API changes are caught in review.

Not public API:
- anything in a `dev.yumi.*.internal` package, or marked `internal`;
- the `catalog` app;
- exact pixels. Screenshot tests guard looks, but a refined shadow or spacing is a patch.

## Changing the API

1. Make the change. Prefer adding a parameter with a default, or a new overload, to changing an existing signature.
2. To retire something, deprecate it first (`@Deprecated` with `ReplaceWith`) and keep it for at least one minor version.
3. Add a line to `CHANGELOG.md`.

## Rules that are part of the contract

These are design rules, not implementation details. Changing one is a major version:

- Raised = pressable, recessed = read-only.
- One accent action per screen. Selected states use accent ink, never an accent fill.
- Press = sink + shadow swap. No ripples.
- Swipe-to-dismiss behaves identically everywhere and is not configurable.

## Dependencies on experimental Material APIs

Some components are built on Material 3 APIs that Material still marks experimental: the slider slots, `RangeSlider`, `DatePicker`, `DateRangePicker`, `ModalBottomSheet`, `BottomSheetScaffold`, `BasicAlertDialog`, `TooltipBox` and `PullToRefreshBox`. Yumi opts in internally. Three components expose such a type in their signature: `YumiModalBottomSheet` (`SheetState`), `YumiBottomSheetScaffold` (`BottomSheetScaffoldState`) and `YumiPullToRefreshBox` (`PullToRefreshState`). If Material changes these, Yumi absorbs the change in a minor release where it can. Where it can't, it follows the deprecation steps above.
