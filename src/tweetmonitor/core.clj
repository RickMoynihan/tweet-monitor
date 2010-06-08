(ns tweetmonitor.core
 (:import (org.apache.http.impl.client DefaultHttpClient)
          (org.apache.http.client.methods HttpGet))

    (:import (org.apache.http HttpEntity)
             (org.apache.http HttpException)
             (org.apache.http HttpHost)
    (org.apache.http HttpRequest)
    (org.apache.http HttpRequestInterceptor)
    ( org.apache.http HttpResponse)
    ( org.apache.http.auth AuthScheme)
    ( org.apache.http.auth AuthScope)
    ( org.apache.http.auth AuthState)
    ( org.apache.http.auth Credentials)
    ( org.apache.http.auth UsernamePasswordCredentials)
    ( org.apache.http.client CredentialsProvider)
    ( org.apache.http.client.protocol ClientContext)
    ( org.apache.http.impl.auth BasicScheme)
    ( org.apache.http.protocol BasicHttpContext)
    ( org.apache.http.protocol ExecutionContext)
    ( org.apache.http.protocol HttpContext))
    (:use (clojure.contrib
            [duck-streams :only [read-lines]]))
 )

"http://stream.twitter.com/1/statuses/firehose.json"

(defn get-url [url]
  (let [response (->> (HttpGet. url)
                      (.execute (DefaultHttpClient.)))]
  (-> response .getEntity .getContent read-lines)))