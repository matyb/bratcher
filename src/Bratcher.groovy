def String curl(url, branches = [], curlArgs = '-f -X GET', continueFn = { branch, exception -> true}){
  def branchName
  try {
    branchName = branches.size() > 0 ? branches.head() : ""
    subUrl = url.replace('$branch', branchName)
    def val = sh(returnStdout: true, script: "curl $curlArgs '$subUrl'")
    return val
  } catch (Exception x) {
    if(continueFn(branchName, x)){
      echo """file not found on branch '$branchName',
             |trying next branch '${branches.size() > 1 ? branches.tail().head() : null}'""".stripMargin().toString()
      if(branches.size() > 1){
        curl(url, branches.tail(), curlArgs, continueFn)
      }
    }
  }
}

def checkout(repoName, branches, remotePath = 'https://github.com') {
  def output = sh(returnStdout: true, script: 'ls -la')
  def folder = output.split("\n").findAll { it.startsWith("d") && it.endsWith(" $repoName") }
  if(folder.size() > 0) {
    cwd repoName
    def co = { return sh(returnStatus: true, script: "git checkout $it".toString()) }
    def status = co(branches.head())
    while(status != 0) {
      branches = branches.tail()
      status = co(branches.head())
    }
  } else {
    sh "git clone $remotePath/$repoName"
    return checkout(repoName, branches)
  }
}
