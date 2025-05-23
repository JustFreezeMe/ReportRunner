@echo off
setlocal enabledelayedexpansion

:: Имя входного CSV и выходного YAML файла
set INPUT_FILE=profile.csv
set OUTPUT_FILE=profile.yml

:: Чистим выходной файл
echo load_profile: > "%OUTPUT_FILE%"

:: Запускаем встроенный PowerShell скрипт для преобразования
powershell -Command ^
    $csv = Import-Csv -Path '%INPUT_FILE%' -Delimiter ','; ^
    $currentOperation = $null; ^
    foreach ($row in $csv) { ^
        if ($row.'Описание операции') { ^
            if ($currentOperation) { ^
                Add-Content -Path '%OUTPUT_FILE%' -Value ' '; ^
            } ^
            Add-Content -Path '%OUTPUT_FILE%' -Value '  - operation: "' + $row.'Описание операции' + '"'; ^
            Add-Content -Path '%OUTPUT_FILE%' -Value '    requests:'; ^
        } ^
        Add-Content -Path '%OUTPUT_FILE%' -Value '      - script: "' + $row.'Название скрипта' + '"'; ^
        Add-Content -Path '%OUTPUT_FILE%' -Value '        intensity: ' + $row.'Интенсивность'; ^
        Add-Content -Path '%OUTPUT_FILE%' -Value '        sla: ' + $row.'SLA'; ^
    }

echo Done. Output: %OUTPUT_FILE%
pause