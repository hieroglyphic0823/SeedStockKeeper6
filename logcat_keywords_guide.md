# logcat 音声再生問題の調査キーワードガイド

## 🔍 基本検索キーワード

### 1. アプリ固有のタグ（必須）
```
VideoSplashScreen
LoadingAnimationVideoPlayer
```

### 2. ExoPlayer関連（最重要）
```
ExoPlayer
Media3
ExoPlayerImpl
Player
PlaybackParameters
```

### 3. オーディオ関連
```
AudioManager
AudioTrack
AudioAttributes
AudioFocus
AudioSystem
```

### 4. メディア再生全般
```
MediaPlayer
MediaCodec
MediaExtractor
MediaMetadataRetriever
```

### 5. エラー・例外（必須チェック）
```
ERROR
Exception
FATAL
AndroidRuntime
crash
```

### 6. 再生状態・ボリューム関連
```
volume
Volume
playback
Playback
state
State
prepare
Prepare
```

## 📋 実用的なlogcatコマンド例

### 基本コマンド（すべての関連ログ）
```bash
adb logcat | grep -E "VideoSplashScreen|LoadingAnimationVideoPlayer|ExoPlayer|AudioManager|Audio|Media|ERROR|Exception"
```

### 詳細ログ（時間付き、エラーも含む）
```bash
adb logcat -v time VideoSplashScreen:* LoadingAnimationVideoPlayer:* ExoPlayer:* AudioManager:* *:E
```

### PowerShell用（Windows）
```powershell
adb logcat -v time VideoSplashScreen:* LoadingAnimationVideoPlayer:* ExoPlayer:* AudioManager:* *:E | Select-String -Pattern "VideoSplashScreen|ExoPlayer|Audio|ERROR|Exception|volume|playback"
```

### エラーのみを抽出
```bash
adb logcat *:E | grep -E "VideoSplashScreen|ExoPlayer|Audio"
```

## 🎯 調査のポイント

### 1. まずエラーを確認
```
adb logcat *:E
```

### 2. ExoPlayerの初期化を確認
```
adb logcat | grep -i "ExoPlayer"
```

### 3. オーディオ属性の設定を確認
```
adb logcat | grep -i "AudioAttributes"
```

### 4. ボリューム設定を確認
```
adb logcat | grep -i "volume"
```

### 5. リソース読み込みエラーを確認
```
adb logcat | grep -iE "resource|uri|tanesukemovie"
```

## 🔑 具体的なキーワードリスト

### 検索すべきキーワード（優先順位順）
1. **ERROR** - すべてのエラー
2. **Exception** - 例外情報
3. **VideoSplashScreen** - アプリ固有のログ
4. **ExoPlayer** - プレイヤーの状態
5. **AudioManager** - オーディオシステム
6. **prepare** - 準備状態
7. **playback** - 再生状態
8. **volume** - 音量関連
9. **MediaItem** - メディアアイテム
10. **RawResource** - リソース読み込み

## 💡 よくある問題の検索パターン

### 音が全く出ない場合
```bash
adb logcat | grep -E "AudioManager|AudioTrack|volume|mute|AudioAttributes"
```

### リソース読み込みエラーの場合
```bash
adb logcat | grep -E "resource|uri|RawResource|FileNotFoundException|IOException"
```

### ExoPlayer初期化エラーの場合
```bash
adb logcat | grep -E "ExoPlayer|MediaItem|prepare|STATE"
```

### オーディオフォーカス問題の場合
```bash
adb logcat | grep -E "AudioFocus|audioFocus|AUDIOFOCUS"
```

## 📝 実際の調査手順

1. **ログキャッシュをクリア**
   ```bash
   adb logcat -c
   ```

2. **エラーとアプリログを同時監視**
   ```bash
   adb logcat -v time VideoSplashScreen:* ExoPlayer:* AudioManager:* *:E
   ```

3. **アプリを起動してスプラッシュ画面を表示**

4. **ログを確認**
   - ERROR で始まる行を確認
   - Exception が含まれる行を確認
   - ExoPlayer の状態変化を確認
   - AudioManager のメッセージを確認



