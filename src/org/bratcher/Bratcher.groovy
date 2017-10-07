package org.bratcher

class Bratcher {

  def curl(url, branches = [env.BRANCH_NAME], curlArgs = '-X GET', continueFn = { branch, exception -> true}){
    if(!branches?.empty){
      try {
        sh(returnStdout: true, script: "curl " + curlArgs + " '${url.replace('$branch', branches.head())}'")
      } catch (Exception x) {
        if(continueFn(branches.head(), x)){
          curl(url, branches.tail(), curlArgs, continueFn)
        }
      }
    }
  }

}
