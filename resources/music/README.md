# Background music

Drop **`.wav`** files in this directory and the game plays them in rotation,
sorted by filename.

```
resources/music/
  01-nocturne.wav
  02-arabesque.wav
```

Nothing else is needed — `MusicService` scans this folder at startup. With no
tracks present the music feature quietly switches itself off; the game is fully
playable without it.

## Why WAV only

Java's stock `javax.sound.sampled` cannot decode MP3, and pulling in JLayer to
fix that would mean a hard third-party dependency for a cosmetic feature. So
`MusicService` filters for `.wav` and ignores everything else — an MP3 dropped
in here is silently skipped, not played.

Convert with ffmpeg:

```bash
ffmpeg -i "track.mp3" -acodec pcm_s16le -ar 44100 "track.wav"
```

Expect roughly 10x the file size. A 4-minute track lands around 40 MB as
16-bit 44.1 kHz PCM.

## Why these files aren't in git

Audio is deliberately untracked (see `.gitignore`) — the repo previously carried
45 MB of MP3s that the game could not play, which is dead weight in every clone
forever, since git keeps blobs in history. Host large audio outside the repo and
copy it in, or ship it alongside the installer.

`swing/build.gradle.kts` also excludes `music/**` from the jar for the same
reason. The game reads this directory from disk next to the application, not
from the packaged resources.

## Controls

| Where | What |
|-------|------|
| Options ▸ Mute music | Toggle playback |
| Options ▸ Next track | Skip to the next file |
| Options ▸ Preferences… | Music volume, and whether music starts enabled |

Volume and the enabled flag persist to `settings.properties` in your per-user
data directory.
