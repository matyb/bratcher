def String call(url, branches = [env.BRANCH_NAME], curlArgs = '-X GET', continueFn = { branch, exception -> true}) {
  def val = new org.bratcher.Bratcher().curl(url, branches, curlArgs, continueFn)
  echo val
  echo val.toString()
  echo "HERE"
  return val
}
