def String call(url, branches = [env.BRANCH_NAME], curlArgs = '-X GET', continueFn = { branch, exception -> true}) {
  return new org.bratcher.Bratcher().curl(url, branches, curlArgs, continueFn)
}
