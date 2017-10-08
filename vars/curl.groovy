def String call(url, branches = [env.BRANCH_NAME], curlArgs = '-f -X GET', continueFn = { branch, exception -> true}) {
  return new Bratcher().curl(url, branches, curlArgs, continueFn)
}
