# Migrating from the Pebble theme

| Pebble | Casual Yumi |
|---|---|
| `PebbleTheme` | `YumiTheme` |
| `PebbleTokens` | `YumiTokens` (raw values) / `YumiTheme.shapes`, `.depth`, `.motion` |
| `LocalPebbleDepth.current` | `YumiTheme.depth` |
| `PebbleButton(variant = PebbleVariant.Accent)` | `YumiButton(variant = YumiButtonVariant.Accent)` |
| `PebbleIconButton` | `YumiIconButton` |
| `PebbleTray` | `YumiTray` |
| `PebbleLed(armed)` | `YumiLed(on = armed, contentDescription = …)` |
| `MaterialTheme.colorScheme.tertiary` for orange text | `YumiTheme.colors.accentInk` (still mapped to `tertiary`) |

App-specific pieces (sky window, weather, outfits) stay in the app.
