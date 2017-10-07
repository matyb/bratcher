package org.bratcher

class Bratcher {

  def curl(url, branches = [env.BRANCH_NAME], curlArgs = '-X GET', continueFn = { branch, exception -> true}){
    if(!branches?.empty){
      try {
        url = url.replace('$branch', branches.head())
        def val = sh(returnStdout: true, script: "curl " + curlArgs + " '$url'")
        throw new RuntimeException( "curling '$url' returns '$val'")
        val
      } catch (Exception x) {
        if(continueFn(branches.head(), x)){
          curl(url, branches.tail(), curlArgs, continueFn)
        }
      }
    }
  }

}
