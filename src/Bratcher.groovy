def String curl(url, branches, curlArgs = '-f -X GET', continueFn = { branch, exception -> true}){
  if(!branches?.empty){
    try {
      subUrl = url.replace('$branch', branches.head())
      def val = sh(returnStdout: true, script: "curl " + curlArgs + " '$subUrl'")
      if(!val || val.isEmpty()){
        if(continueFn(branches.head(), x)){
          echo "empty response... ${branches.tail()}"
          curl(url, branches.tail(), curlArgs, continueFn)
        }
      }
      return val
    } catch (Exception x) {
      if(continueFn(branches.head(), x)){
        echo "exception response... ${branches.tail()}"
        curl(url, branches.tail(), curlArgs, continueFn)
      }
    }
  }
}
