def call(repoName, branches, remotePath){
  new Bratcher().curl(repoName, branches, remotePath)
}
