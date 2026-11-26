$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$dishFile = Join-Path $root "app/src/main/java/com/smartnutrient/app/DishCatalog.kt"
$ingredients = [System.Collections.Generic.HashSet[string]]::new()
Get-Content -LiteralPath $dishFile -Encoding utf8 | ForEach-Object {
    $cells = $_.Split("|")
    if ($cells.Count -eq 9 -and -not $_.StartsWith(" ")) {
        $cells[7].Split(",") | ForEach-Object { [void]$ingredients.Add($_) }
    }
}
$cache = @{}
$headers = @{ "User-Agent" = "SmartNutrientAudit/1.0"; "Referer" = "https://nlc.chinanutri.cn/fq/foodlist_0_0_0_0_0_1.htm" }
$i = 0
foreach ($ingredient in $ingredients) {
    $i++
    $encoded = [Uri]::EscapeDataString($ingredient)
    $body = "categoryOne=0&categoryTwo=0&foodName=$encoded&pageNum=1&field=0&flag=0"
    try {
        $response = Invoke-WebRequest -Uri "https://nlc.chinanutri.cn/fq/FoodInfoQueryAction!queryFoodInfoList.do" -Method Post -Body $body -ContentType "application/x-www-form-urlencoded; charset=UTF-8" -Headers $headers -UseBasicParsing -TimeoutSec 20
        $json = $response.Content | ConvertFrom-Json
        if ($json.list -and $json.list.Count -gt 0) {
            $cache[$ingredient] = $json.list
        }
    } catch {
        Write-Warning "$ingredient : $($_.Exception.Message)"
    }
    if ($i % 25 -eq 0) { Write-Output "China food lookup $i/$($ingredients.Count)" }
    Start-Sleep -Milliseconds 120
}
# Common dish wording differs from the food-composition names.
$aliases = @{
    "青椒" = "甜椒"; "鸡胸肉" = "鸡胸"; "五花肉" = "猪肉(肋条肉)";
    "牛肉末" = "牛肉(瘦)"; "猪大排" = "猪肉(里脊)"; "猪脚" = "猪蹄";
    "土豆" = "马铃薯"; "番茄" = "番茄"; "鸡蛋" = "蛋（鸡蛋，均值)";
    "豆腐" = "豆腐(均值)"; "面条" = "面条(均值)"; "鸡肉" = "鸡胸";
    "五花肉" = "猪肉(肋条肉)"; "牛肉末" = "牛肉(瘦)"
}
foreach ($alias in $aliases.GetEnumerator()) {
    if ($cache.ContainsKey($alias.Key)) { continue }
    $aliasFile = Join-Path $root ".audit_data/china_aliases/$($alias.Key).json"
    if (Test-Path -LiteralPath $aliasFile) { continue }
    $encoded = [Uri]::EscapeDataString($alias.Value)
    $body = "categoryOne=0&categoryTwo=0&foodName=$encoded&pageNum=1&field=0&flag=0"
    try {
        $response = Invoke-WebRequest -Uri "https://nlc.chinanutri.cn/fq/FoodInfoQueryAction!queryFoodInfoList.do" -Method Post -Body $body -ContentType "application/x-www-form-urlencoded; charset=UTF-8" -Headers $headers -UseBasicParsing -TimeoutSec 20
        $json = $response.Content | ConvertFrom-Json
        if ($json.list -and $json.list.Count -gt 0) { $cache[$alias.Key] = $json.list }
    } catch { Write-Warning "alias $($alias.Key): $($_.Exception.Message)" }
}
$out = Join-Path $root ".audit_data/china_food_cache.json"
$cache | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $out -Encoding utf8
Write-Output "cached=$($cache.Count) path=$out"
