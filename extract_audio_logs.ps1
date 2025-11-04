# 音声再生関連のログを抽出するスクリプト

Write-Host "ログキャッシュをクリアしています..." -ForegroundColor Yellow
adb logcat -c

Write-Host ""
Write-Host "音声再生関連のログを監視しています..." -ForegroundColor Green
Write-Host "アプリを起動してスプラッシュ画面を表示してください" -ForegroundColor Cyan
Write-Host "停止するには Ctrl+C を押してください" -ForegroundColor Gray
Write-Host ""

# 音声・動画再生関連のログを抽出
adb logcat -v time ^
    VideoSplashScreen:* ^
    LoadingAnimationVideoPlayer:* ^
    ExoPlayer:* ^
    AudioManager:* ^
    MediaPlayer:* ^
    android.media:* ^
    androidx.media3:* ^
    *:E | Select-String -Pattern "VideoSplashScreen|LoadingAnimationVideoPlayer|ExoPlayer|Audio|Media|ERROR|error|Exception|exception|volume|Volume|音声|動画|playback|Playback|prepare|Prepare|state|State"



