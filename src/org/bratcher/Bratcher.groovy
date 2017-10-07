package org.bratcher

class Bratcher {

  def curl(url, branches, curlArgs = '-X GET', continueFn = { branch, exception -> true}){
    if(!branches?.empty){
      try {
        url = url.replace('$branch', branches.head())
        def val = sh(returnStdout: true, script: "curl " + curlArgs + " '$url'")
        echo  "curling '$url' returns '$val'".toString()
        echo url
        echo val
        echo "OMG IM HERE"
        return val
      } catch (Exception x) {
        if(continueFn(branches.head(), x)){
          curl(url, branches.tail(), curlArgs, continueFn)
        }
      }
    }
  }

}
