# 音声再生問題のログ監視スクリプト

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  音声再生問題のログ監視" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ログキャッシュをクリア
Write-Host "ログキャッシュをクリアしています..." -ForegroundColor Yellow
adb logcat -c
Start-Sleep -Seconds 1

Write-Host ""
Write-Host "監視を開始します..." -ForegroundColor Green
Write-Host "アプリを起動してスプラッシュ画面を表示してください" -ForegroundColor Cyan
Write-Host "停止するには Ctrl+C を押してください" -ForegroundColor Gray
Write-Host ""
Write-Host "監視中のタグ:" -ForegroundColor Yellow
Write-Host "  - VideoSplashScreen" -ForegroundColor White
Write-Host "  - LoadingAnimationVideoPlayer" -ForegroundColor White
Write-Host "  - ExoPlayer" -ForegroundColor White
Write-Host "  - AudioManager" -ForegroundColor White
Write-Host "  - MediaPlayer" -ForegroundColor White
Write-Host "  - すべてのERROR" -ForegroundColor Red
Write-Host ""

# ログ監視を開始
adb logcat -v time `
    VideoSplashScreen:* `
    LoadingAnimationVideoPlayer:* `
    ExoPlayer:* `
    AudioManager:* `
    MediaPlayer:* `
    android.media:* `
    androidx.media3:* `
    *:E | ForEach-Object {
        $line = $_
        # エラー行を強調表示
        if ($line -match "ERROR|Exception|FATAL") {
            Write-Host $line -ForegroundColor Red
        }
        # ExoPlayer関連を強調表示
        elseif ($line -match "ExoPlayer|Media3") {
            Write-Host $line -ForegroundColor Yellow
        }
        # オーディオ関連を強調表示
        elseif ($line -match "Audio|volume|Volume|mute|Mute") {
            Write-Host $line -ForegroundColor Cyan
        }
        # その他
        else {
            Write-Host $line
        }
    }

