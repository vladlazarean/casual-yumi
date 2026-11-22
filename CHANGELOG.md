# Changelog

All notable changes to Casual Yumi. Versions follow [Semantic Versioning](https://semver.org); see [docs/API_POLICY.md](docs/API_POLICY.md).

## 0.0.9

First public preview. Everything below is new. While the version starts with 0, the API may still change between releases.

### yumi-tokens
- Putty and Graphite palettes (`YumiTokens`, `lightYumiColors()`, `darkYumiColors()`), mapped one-to-one from the Pebble theme.
- `YumiAccent` with Orange, Amber, Sage, Sky and Coral presets plus `YumiAccent.custom(...)`. Every preset passes WCAG AA.
- `YumiDepth`, `YumiShapes`, `YumiMotion`, and the `YumiContrast` WCAG helper.

### yumi-foundation
- `YumiTheme`: Material 3 colour scheme, shapes and Manrope type scale. No ripples or tonal tint in the subtree.
- `Modifier.pebble()` and `Modifier.tray()`, with draw-phase press animation.
- `animateSinkAsState`, `Modifier.pebbleClickable`, `YumiHaptics`, and automatic reduced motion.

### yumi-components
- Buttons: `YumiButton` (Accent / Neutral / Charcoal), `YumiIconButton`, `YumiTextButton`, `YumiToggleButton`, `YumiFab`.
- Selection: `YumiCheckbox`, `YumiTriStateCheckbox`, `YumiRadioButton`, `YumiRadioGroup`, `YumiSwitch`.
- Sliders: `YumiSlider`, `YumiRangeSlider`, `YumiCircularSlider`, `YumiTimeDial`.
- Inputs: `YumiTextField`, `YumiSecureTextField`, `YumiSearchField`, `YumiStepper`.
- Chips and segments: `YumiFilterChip`, `YumiAssistChip`, `YumiInputChip`, `YumiSegmentedButtonRow`.
- Surfaces: `YumiTray`, `YumiCard` (static and clickable), `YumiLed`.
- Feedback: `YumiToastHostState`, `YumiToastHost`, `YumiToast`, `YumiSnackbar`, `YumiLinearProgress`, `YumiCircularProgress`.
- Dialogs and sheets: `YumiDialog`, `YumiAlertDialog`, `YumiTimePickerDialog`, `YumiDatePickerDialog`, `YumiModalBottomSheet`.
- Navigation: `YumiTopAppBar`, `YumiNavigationBar` + `YumiNavigationBarItem`, `YumiNavigationRail` + `YumiNavigationRailItem`, `YumiTabRow` + `YumiTab`, `YumiDropdownMenu` + `YumiDropdownMenuItem`.
- Lists: `YumiListItem`, `YumiSwipeToDismiss`.

### Extras
- From the brief: `YumiBadge` + `YumiBadgedBox` (the badge sits on the icon's corner, clear of the glyph), `YumiTooltipBox`, `YumiRichTooltipBox`, `YumiHorizontalDivider`, `YumiVerticalDivider`.
- Structure: `YumiScaffold`, `YumiModalNavigationDrawer` + `YumiNavigationDrawerItem`, `YumiBottomAppBar`, `YumiModalSideSheet` (over the whole window, or inside its parent with `fullScreen = false`), `YumiBottomSheetScaffold`.
- Input: `YumiSelectField`, `YumiDateRangePickerDialog`, `YumiMultiChoiceSegmentedButtonRow`, `YumiIconToggleButton`, `YumiSplitButton`, `YumiRatingBar`.
- Content: `YumiAvatar`, `YumiSkeleton`, `YumiSkeletonLines`, `YumiEmptyState`, `YumiExpandableSection`, `YumiCarousel`, `YumiPagerIndicator`.
- Gestures and feedback: `YumiPullToRefreshBox`, `YumiReorderableColumn` (rows 8 dp apart, `itemSpacing` to change) + `YumiDragHandle`, `YumiSwipeActions`, `YumiBanner`.

### Known limitations
- `YumiReorderableColumn` composes rows eagerly; use it for short lists, not long `LazyColumn`s.
- `YumiModalSideSheet` is Yumi's own (Material 3 for Compose has none) and may be replaced by Material's when it ships.
- `YumiModalBottomSheet`, `YumiBottomSheetScaffold` and `YumiPullToRefreshBox` take Material state types (`SheetState`, `BottomSheetScaffoldState`, `PullToRefreshState`) that Material still marks experimental.
- On Graphite, the tray's inner shadow (14 %) is faint; the recess reads mostly from the colour step.
