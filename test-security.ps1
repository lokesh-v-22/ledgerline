$base = "http://localhost:8080"

function Send-Req($method, $path, $token, $body) {
    $headers = @{}
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    try {
        if ($body) {
            $r = Invoke-WebRequest -UseBasicParsing -Method $method -Uri "$base$path" -Headers $headers -ContentType "application/json" -Body ($body | ConvertTo-Json)
        } else {
            $r = Invoke-WebRequest -UseBasicParsing -Method $method -Uri "$base$path" -Headers $headers
        }
        return @{ status = [int]$r.StatusCode; body = $r.Content }
    } catch {
        $resp = $_.Exception.Response
        if ($resp) { return @{ status = [int]$resp.StatusCode; body = "" } }
        return @{ status = 0; body = $_.Exception.Message }
    }
}

function Get-Token($email) {
    $r = Send-Req "POST" "/api/auth/login" $null @{ email = $email; password = "Demo@12345" }
    return ($r.body | ConvertFrom-Json).token
}

function Check($name, $expected, $actual) {
    if ($expected -eq $actual) { Write-Host "PASS  $name (got $actual)" -ForegroundColor Green }
    else { Write-Host "FAIL  $name (expected $expected, got $actual)" -ForegroundColor Red }
}

$alice = Get-Token "alice@ledgerline.test"
$bob   = Get-Token "bob@ledgerline.test"
$admin = Get-Token "admin@ledgerline.test"
$aud   = Get-Token "auditor@ledgerline.test"

if (-not $alice -or -not $bob -or -not $admin -or -not $aud) {
    Write-Host "Could not log in a seeded user. Was the backend started with SEED_PASSWORD set?" -ForegroundColor Red
    exit 1
}

$aliceAcc = @((Send-Req "GET" "/api/accounts" $alice $null).body | ConvertFrom-Json | ForEach-Object { $_ } | Select-Object -First 1).id
$bobAcc   = @((Send-Req "GET" "/api/accounts" $bob $null).body | ConvertFrom-Json | ForEach-Object { $_ } | Select-Object -First 1).id

Check "Health is open"                          200 (Send-Req "GET" "/api/health" $null $null).status
Check "No token on /api/me is refused"          401 (Send-Req "GET" "/api/me" $null $null).status
Check "Alice reads her own account"             200 (Send-Req "GET" "/api/accounts/$aliceAcc" $alice $null).status
Check "Alice reads BOB's account"               404 (Send-Req "GET" "/api/accounts/$bobAcc" $alice $null).status
Check "Bob reads ALICE's account"               404 (Send-Req "GET" "/api/accounts/$aliceAcc" $bob $null).status
Check "No token on an account is refused"       401 (Send-Req "GET" "/api/accounts/$bobAcc" $null $null).status
Check "Tampered token is refused"               401 (Send-Req "GET" "/api/me" ($alice.Substring(0, $alice.Length - 3) + "abc") $null).status

$newUser = @{ email = "mallory$(Get-Random)@test.com"; password = "password123"; role = "ADMIN" }
Check "Holder cannot create users (admin only)" 403 (Send-Req "POST" "/api/admin/users" $alice $newUser).status
Check "Auditor cannot create users"             403 (Send-Req "POST" "/api/admin/users" $aud $newUser).status
Check "Admin can create users"                  201 (Send-Req "POST" "/api/admin/users" $admin $newUser).status

$adminList = @((Send-Req "GET" "/api/accounts" $admin $null).body | ConvertFrom-Json | ForEach-Object { $_ })
Check "Admin sees all customer accounts (2+)"   $true ($adminList.Count -ge 2)
$audList = @((Send-Req "GET" "/api/accounts" $aud $null).body | ConvertFrom-Json | ForEach-Object { $_ })
Check "Auditor sees all customer accounts (2+)" $true ($audList.Count -ge 2)

Check "Admin cannot open an account"            403 (Send-Req "POST" "/api/accounts" $admin $null).status
Check "Holder can open an account"              201 (Send-Req "POST" "/api/accounts" $alice $null).status

