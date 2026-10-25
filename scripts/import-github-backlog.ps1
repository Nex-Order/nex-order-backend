<#
.SYNOPSIS
    Imports the Nex Order roadmap (labels, milestones, issues) into GitHub and,
    optionally, into a GitHub Projects (v2) board with Sprint, Priority and
    Estimate fields.

.DESCRIPTION
    Reads docs/roadmap/backlog.json and is idempotent: labels and milestones are
    matched by name, issues by a hidden marker (<!-- nexorder-backlog:NX-01 -->)
    or by exact title, so running it twice does not create duplicates.

    Items with "existing": <number> are NOT recreated: the existing issue gets
    the milestone, labels, a one-time comment with the proposed scope, and is
    added to the project.

    Requires a token in $env:GITHUB_TOKEN:
      - classic PAT with scopes "repo" and "project" (needed for user-owned projects), or
      - `gh auth token` after `gh auth refresh -s project`.

.EXAMPLE
    # Preview everything without calling GitHub
    .\scripts\import-github-backlog.ps1 -DryRun

.EXAMPLE
    # Labels, milestones and issues only
    $env:GITHUB_TOKEN = "<token>"
    .\scripts\import-github-backlog.ps1

.EXAMPLE
    # Also create a new project board and fill Sprint/Priority/Estimate
    .\scripts\import-github-backlog.ps1 -CreateProject

.EXAMPLE
    # Use an existing project (number from its URL: /users/<owner>/projects/<n>)
    .\scripts\import-github-backlog.ps1 -ProjectNumber 3
#>
[CmdletBinding()]
param(
    [string]$Owner = "martinssluis",
    [string]$Repo = "nex-order-backend",
    [string]$BacklogPath = "",
    [int]$ProjectNumber = 0,
    [string]$ProjectOwner = "",
    [switch]$CreateProject,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

if (-not $ProjectOwner) { $ProjectOwner = $Owner }
if (-not $BacklogPath) { $BacklogPath = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "..\docs\roadmap\backlog.json" }
$ApiBase = "https://api.github.com"
$Token = $env:GITHUB_TOKEN

if (-not $DryRun -and -not $Token) {
    throw "Set `$env:GITHUB_TOKEN (classic PAT with 'repo' and 'project' scopes) or run with -DryRun."
}

$backlog = Get-Content -Raw -Encoding UTF8 -Path $BacklogPath | ConvertFrom-Json
$headings = $backlog.bodyHeadings
$priorityNames = @{ critical = "Critical"; high = "High"; medium = "Medium"; low = "Low" }

# ---------------------------------------------------------------- helpers

function Get-Headers {
    @{
        Authorization          = "Bearer $Token"
        Accept                 = "application/vnd.github+json"
        "X-GitHub-Api-Version" = "2022-11-28"
        "User-Agent"           = "nexorder-backlog-import"
    }
}

function Invoke-GitHub {
    param([string]$Method, [string]$Path, $Body = $null)
    $params = @{ Method = $Method; Uri = "$ApiBase$Path"; Headers = (Get-Headers) }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 20
        $params.Body = [Text.Encoding]::UTF8.GetBytes($json)
        $params.ContentType = "application/json; charset=utf-8"
    }
    Invoke-RestMethod @params
}

function Get-AllPages {
    param([string]$Path)
    $results = @()
    $page = 1
    $separator = if ($Path.Contains("?")) { "&" } else { "?" }
    while ($true) {
        $batch = @(Invoke-GitHub -Method Get -Path "$Path${separator}per_page=100&page=$page")
        if ($batch.Count -eq 0 -or $null -eq $batch[0]) { break }
        $results += $batch
        if ($batch.Count -lt 100) { break }
        $page++
    }
    $results
}

function Invoke-GraphQL {
    param([string]$Query, [hashtable]$Variables = @{})
    $response = Invoke-GitHub -Method Post -Path "/graphql" -Body @{ query = $Query; variables = $Variables }
    if ($response.errors) {
        throw "GraphQL error: " + (($response.errors | ForEach-Object { $_.message }) -join "; ")
    }
    $response.data
}

function Write-Step { param([string]$Text) Write-Host "`n== $Text" -ForegroundColor Cyan }
function Write-Action { param([string]$Text) Write-Host "  + $Text" -ForegroundColor Green }
function Write-Skip { param([string]$Text) Write-Host "  = $Text" -ForegroundColor DarkGray }
function Write-Warn { param([string]$Text) Write-Host "  ! $Text" -ForegroundColor Yellow }

function Get-Marker { param([string]$Key) "<!-- nexorder-backlog:$Key -->" }

function Get-IssueLabels {
    param($Item)
    @($Item.labels) + @("priority:$($Item.priority)")
}

function New-IssueBody {
    param($Item)
    $lines = @()
    $lines += "## $($headings.context)"
    $lines += ""
    $lines += $Item.context
    if (@($Item.tasks).Count -gt 0) {
        $lines += ""
        $lines += "## $($headings.tasks)"
        $lines += ""
        foreach ($task in $Item.tasks) { $lines += "- [ ] $task" }
    }
    if (@($Item.acceptance).Count -gt 0) {
        $lines += ""
        $lines += "## $($headings.acceptance)"
        $lines += ""
        foreach ($criterion in $Item.acceptance) { $lines += "- [ ] $criterion" }
    }
    $lines += ""
    $lines += "---"
    $footer = "``$($Item.key)`` | $($Item.sprint) | $($headings.estimate): $($Item.estimate) $($headings.points)"
    if (@($Item.refs).Count -gt 0) { $footer += " | $($headings.refs): " + (@($Item.refs) -join ", ") }
    $lines += $footer
    $lines += ""
    $lines += (Get-Marker $Item.key)
    $lines -join "`n"
}

# ---------------------------------------------------------------- labels

Write-Step "Labels"
$existingLabels = @{}
if (-not $DryRun) {
    foreach ($label in (Get-AllPages "/repos/$Owner/$Repo/labels")) { $existingLabels[$label.name] = $true }
}
foreach ($label in $backlog.labels) {
    if ($existingLabels.ContainsKey($label.name)) { Write-Skip $label.name; continue }
    Write-Action $label.name
    if (-not $DryRun) {
        Invoke-GitHub -Method Post -Path "/repos/$Owner/$Repo/labels" -Body @{
            name = $label.name; color = $label.color; description = $label.description
        } | Out-Null
    }
}

# ---------------------------------------------------------------- milestones

Write-Step "Milestones"
$milestoneNumbers = @{}
$existingMilestones = @{}
if (-not $DryRun) {
    foreach ($m in (Get-AllPages "/repos/$Owner/$Repo/milestones?state=all")) { $existingMilestones[$m.title] = $m.number }
}
foreach ($m in $backlog.milestones) {
    if ($existingMilestones.ContainsKey($m.title)) {
        $milestoneNumbers[$m.key] = $existingMilestones[$m.title]
        Write-Skip $m.title
        continue
    }
    Write-Action "$($m.title) (due $($m.due_on))"
    if ($DryRun) { $milestoneNumbers[$m.key] = 0; continue }
    $created = Invoke-GitHub -Method Post -Path "/repos/$Owner/$Repo/milestones" -Body @{
        title = $m.title; description = $m.description; due_on = "$($m.due_on)T23:59:59Z"
    }
    $milestoneNumbers[$m.key] = $created.number
}

# ---------------------------------------------------------------- issues

Write-Step "Issues"
$byMarker = @{}
$byTitle = @{}
$byNumber = @{}
if (-not $DryRun) {
    foreach ($issue in (Get-AllPages "/repos/$Owner/$Repo/issues?state=all")) {
        if ($issue.pull_request) { continue }
        $byNumber[[int]$issue.number] = $issue
        $byTitle[$issue.title] = $issue
        if ($issue.body -match "<!-- nexorder-backlog:(NX-\d+) -->") { $byMarker[$Matches[1]] = $issue }
    }
}

$resolved = @()   # @{ item; nodeId; number }
foreach ($item in $backlog.issues) {
    $labels = Get-IssueLabels $item
    $milestone = $milestoneNumbers[$item.milestone]

    if ($item.existing) {
        $number = [int]$item.existing
        Write-Action "#$number $($item.title) -> $($item.milestone), $($item.sprint) (existing)"
        if ($DryRun) { continue }
        $issue = $byNumber[$number]
        if (-not $issue) { Write-Warn "#$number not found, skipped"; continue }
        Invoke-GitHub -Method Patch -Path "/repos/$Owner/$Repo/issues/$number" -Body @{ milestone = $milestone } | Out-Null
        Invoke-GitHub -Method Post -Path "/repos/$Owner/$Repo/issues/$number/labels" -Body @{ labels = $labels } | Out-Null
        $marker = Get-Marker $item.key
        $comments = @(Get-AllPages "/repos/$Owner/$Repo/issues/$number/comments")
        if (-not ($comments | Where-Object { $_.body -and $_.body.Contains($marker) })) {
            $comment = "**$($headings.existingComment)** ($($item.sprint), $($headings.estimate): $($item.estimate) $($headings.points))`n`n$($item.context)`n`n$marker"
            Invoke-GitHub -Method Post -Path "/repos/$Owner/$Repo/issues/$number/comments" -Body @{ body = $comment } | Out-Null
        }
        $resolved += @{ item = $item; nodeId = $issue.node_id; number = $number }
        continue
    }

    $found = $byMarker[$item.key]
    if (-not $found) { $found = $byTitle[$item.title] }
    if ($found) {
        Write-Skip "#$($found.number) $($item.title)"
        $resolved += @{ item = $item; nodeId = $found.node_id; number = [int]$found.number }
        continue
    }

    Write-Action "[$($item.key)] $($item.title) -> $($item.milestone), $($item.sprint), $($item.estimate) pts"
    if ($DryRun) { continue }
    $created = Invoke-GitHub -Method Post -Path "/repos/$Owner/$Repo/issues" -Body @{
        title = $item.title; body = (New-IssueBody $item); labels = $labels; milestone = $milestone
    }
    $resolved += @{ item = $item; nodeId = $created.node_id; number = [int]$created.number }
    Start-Sleep -Milliseconds 1200   # stay clear of GitHub's secondary rate limit on content creation
}

if (-not $CreateProject -and $ProjectNumber -eq 0) {
    Write-Step "Done (no project requested: use -CreateProject or -ProjectNumber <n> to fill a board)"
    return
}

# ---------------------------------------------------------------- project v2

Write-Step "Project"
if ($DryRun) {
    $target = if ($CreateProject) { "new project '$($backlog.project.title)'" } else { "project #$ProjectNumber" }
    Write-Action "$target owned by $ProjectOwner"
    Write-Action "fields: Sprint (single select or iteration), Priority (single select), Estimate (number)"
    Write-Action "add $(@($backlog.issues).Count) items and set their fields"
    Write-Step "Dry run finished: nothing was sent to GitHub"
    return
}

$ownerData = Invoke-GraphQL -Query @'
query($login: String!, $owner: String!, $repo: String!) {
  repositoryOwner(login: $login) { id __typename }
  repository(owner: $owner, name: $repo) { id }
}
'@ -Variables @{ login = $ProjectOwner; owner = $Owner; repo = $Repo }

if ($CreateProject) {
    $project = (Invoke-GraphQL -Query @'
mutation($ownerId: ID!, $title: String!, $repositoryId: ID!) {
  createProjectV2(input: { ownerId: $ownerId, title: $title, repositoryId: $repositoryId }) { projectV2 { id number url } }
}
'@ -Variables @{ ownerId = $ownerData.repositoryOwner.id; title = $backlog.project.title; repositoryId = $ownerData.repository.id }).createProjectV2.projectV2
    Write-Action "created $($project.url)"
} else {
    $ownerField = if ($ownerData.repositoryOwner.__typename -eq "Organization") { "organization" } else { "user" }
    $project = (Invoke-GraphQL -Query @"
query(`$login: String!, `$number: Int!) { $ownerField(login: `$login) { projectV2(number: `$number) { id number url } } }
"@ -Variables @{ login = $ProjectOwner; number = $ProjectNumber }).$ownerField.projectV2
    if (-not $project) { throw "Project #$ProjectNumber not found for $ProjectOwner" }
    Write-Skip "using $($project.url)"
}

function Get-ProjectFields {
    (Invoke-GraphQL -Query @'
query($id: ID!) {
  node(id: $id) {
    ... on ProjectV2 {
      fields(first: 50) {
        nodes {
          __typename
          ... on ProjectV2FieldCommon { id name dataType }
          ... on ProjectV2SingleSelectField { options { id name } }
          ... on ProjectV2IterationField { configuration { iterations { id title startDate } } }
        }
      }
    }
  }
}
'@ -Variables @{ id = $project.id }).node.fields.nodes
}

function New-ProjectField {
    param([string]$Name, [string]$DataType, $Options = $null)
    $fieldInput = @{ projectId = $project.id; dataType = $DataType; name = $Name }
    if ($Options) { $fieldInput.singleSelectOptions = $Options }
    Invoke-GraphQL -Query @'
mutation($input: CreateProjectV2FieldInput!) { createProjectV2Field(input: $input) { projectV2Field { ... on ProjectV2FieldCommon { id } } } }
'@ -Variables @{ input = $fieldInput } | Out-Null
    Write-Action "field $Name ($DataType)"
}

$fields = Get-ProjectFields
$fieldByName = @{}
foreach ($f in $fields) { if ($f.name) { $fieldByName[$f.name] = $f } }

if (-not $fieldByName.ContainsKey("Sprint")) {
    New-ProjectField -Name "Sprint" -DataType "SINGLE_SELECT" -Options @($backlog.sprints | ForEach-Object {
        @{ name = $_.name; color = "BLUE"; description = "$($_.start) - $($_.end)" }
    })
}
if (-not $fieldByName.ContainsKey("Priority")) {
    $colors = @{ Critical = "RED"; High = "ORANGE"; Medium = "YELLOW"; Low = "GREEN" }
    New-ProjectField -Name "Priority" -DataType "SINGLE_SELECT" -Options @("Critical", "High", "Medium", "Low" | ForEach-Object {
        @{ name = $_; color = $colors[$_]; description = "" }
    })
}
if (-not $fieldByName.ContainsKey("Estimate")) { New-ProjectField -Name "Estimate" -DataType "NUMBER" }

$fields = Get-ProjectFields
$fieldByName = @{}
foreach ($f in $fields) { if ($f.name) { $fieldByName[$f.name] = $f } }
$sprintField = $fieldByName["Sprint"]
$priorityField = $fieldByName["Priority"]
$estimateField = $fieldByName["Estimate"]

function Get-SprintValue {
    param([string]$SprintName)
    if ($sprintField.__typename -eq "ProjectV2IterationField") {
        $sprint = $backlog.sprints | Where-Object { $_.name -eq $SprintName }
        $iteration = $sprintField.configuration.iterations | Where-Object { $_.title -eq $SprintName -or $_.startDate -eq $sprint.start } | Select-Object -First 1
        if ($iteration) { return @{ iterationId = $iteration.id } }
        return $null
    }
    $option = $sprintField.options | Where-Object { $_.name -eq $SprintName } | Select-Object -First 1
    if ($option) { return @{ singleSelectOptionId = $option.id } }
    $null
}

function Set-ItemField {
    param([string]$ItemId, $Field, [hashtable]$Value)
    Invoke-GraphQL -Query @'
mutation($projectId: ID!, $itemId: ID!, $fieldId: ID!, $value: ProjectV2FieldValue!) {
  updateProjectV2ItemFieldValue(input: { projectId: $projectId, itemId: $itemId, fieldId: $fieldId, value: $value }) { projectV2Item { id } }
}
'@ -Variables @{ projectId = $project.id; itemId = $ItemId; fieldId = $Field.id; value = $Value } | Out-Null
}

$warnedSprints = @{}
foreach ($entry in $resolved) {
    $item = $entry.item
    $projectItem = (Invoke-GraphQL -Query @'
mutation($projectId: ID!, $contentId: ID!) { addProjectV2ItemById(input: { projectId: $projectId, contentId: $contentId }) { item { id } } }
'@ -Variables @{ projectId = $project.id; contentId = $entry.nodeId }).addProjectV2ItemById.item

    $sprintValue = Get-SprintValue $item.sprint
    if ($sprintValue) { Set-ItemField -ItemId $projectItem.id -Field $sprintField -Value $sprintValue }
    elseif (-not $warnedSprints.ContainsKey($item.sprint)) {
        Write-Warn "no Sprint option/iteration matching '$($item.sprint)'; set it manually"
        $warnedSprints[$item.sprint] = $true
    }

    $priorityOption = $priorityField.options | Where-Object { $_.name -eq $priorityNames[$item.priority] } | Select-Object -First 1
    if ($priorityOption) { Set-ItemField -ItemId $projectItem.id -Field $priorityField -Value @{ singleSelectOptionId = $priorityOption.id } }

    Set-ItemField -ItemId $projectItem.id -Field $estimateField -Value @{ number = [double]$item.estimate }
    Write-Action "#$($entry.number) on board ($($item.sprint), $($priorityNames[$item.priority]), $($item.estimate) pts)"
}

Write-Step "Done: $($project.url)"
