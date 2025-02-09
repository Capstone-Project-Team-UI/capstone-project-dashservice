# Base URL for the API
$baseUrl = "http://localhost:8090/users"

# Function to display response status and body
function Show-Response {
    param (
        [Parameter(Mandatory = $true)] $Response,
        [Parameter(Mandatory = $true)] $StatusCode
    )
    Write-Host "Status Code: $StatusCode"
    Write-Host "Response Body: $($Response | ConvertTo-Json -Depth 3)"
}

# Function to handle errors
function Handle-Error {
    param (
        [Parameter(Mandatory = $true)] [System.Management.Automation.ErrorRecord] $ErrorRecord
    )
    $response = $ErrorRecord.Exception.Response
    if ($response -ne $null) {
        $statusCode = $response.StatusCode
        Write-Host "Error Status Code: $statusCode"
        Write-Host "Error Message: $($ErrorRecord.Exception.Message)"
    } else {
        Write-Host "Unexpected Error: $($ErrorRecord.Exception.Message)"
    }
}

# 1. Test GET endpoint: Get all users
Write-Host "Testing GET /users (Fetch All Users)..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl" -Method Get -ErrorAction Stop
    Show-Response -Response $response -StatusCode 200
} catch {
    Handle-Error -ErrorRecord $_
}
Write-Host ""

# 2. Test POST endpoint: Add a new user
Write-Host "Testing POST /users (Add User)..."
$newUser = @{
    userID = "user2"
    organization = "Company B"
    serialNumber = "Device45"
    uniqueID = "uniqueID456"
    emailAddress = "support@companyb.com"
}
try {
    $response = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body ($newUser | ConvertTo-Json -Depth 10) -ContentType "application/json" -ErrorAction Stop
    Show-Response -Response $response -StatusCode 201
} catch {
    Handle-Error -ErrorRecord $_
}
Write-Host ""

# Optional: Test POST request with missing data (invalid data)
Write-Host "Testing POST /users (Add User with Missing Data)..."
$invalidUser = @{
    userID = "user4"
    # Missing organization, serialNumber, uniqueID, and emailAddress
}
try {
    $response = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body ($invalidUser | ConvertTo-Json -Depth 10) -ContentType "application/json" -ErrorAction Stop
    Show-Response -Response $response -StatusCode 400
} catch {
    Handle-Error -ErrorRecord $_
}
Write-Host ""

Write-Host "All tests complete."
