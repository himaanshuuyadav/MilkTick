$paths = git status --short | ForEach-Object {
  $_.Substring(3).Trim()
}

foreach ($path in $paths) {
  if (-not $path) { continue }

  $name = [IO.Path]::GetFileNameWithoutExtension($path)
  $type = 'chore'
  if ($path -like 'app/src/main/java/com/prantiux/milktick/ui/screens/*' -or $path -like 'app/src/main/java/com/prantiux/milktick/ui/components/*') {
    $type = 'refactor-ui'
  } elseif ($path -like 'app/src/main/java/com/prantiux/milktick/viewmodel/*') {
    $type = 'refactor-viewmodel'
  } elseif ($path -like 'app/src/main/java/com/prantiux/milktick/repository/*' -or $path -like 'app/src/main/java/com/prantiux/milktick/sync/*') {
    $type = 'feat-sync'
  } elseif ($path -like 'app/src/main/java/com/prantiux/milktick/data/local/*') {
    $type = 'feat-data'
  } elseif ($path -like 'app/src/main/java/com/prantiux/milktick/notification/*' -or $path -like 'app/src/main/java/com/prantiux/milktick/utils/Notification*') {
    $type = 'feat-notifications'
  } elseif ($path -like '*gradle*' -or $path -eq 'app/build.gradle.kts') {
    $type = 'chore-build'
  } elseif ($path -like 'app/src/main/res/drawable/*') {
    $type = 'feat-icons'
  }

  git add -- "$path"
  git commit -m "${type}: update ${name}" -m "Apply update changes in ${path}."
}

git status --short --branch
git log --oneline -n 20