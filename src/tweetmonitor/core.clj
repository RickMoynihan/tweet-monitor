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

(defn get-url
  ([url] (let [response (->> (HttpGet. url)
                        (.execute (DefaultHttpClient.)))]
    (-> response .getEntity .getContent read-lines)))
  ([url username password]
    (let [http-client (DefaultHttpClient.)
          local-context (BasicHttpContext.)
          basic-auth (BasicScheme.)
          target-host (HttpHost. "stream.twitter.com" 80 "http")
          http-get (HttpGet. "/1/statuses/firehose.json")]
      (.. http-client getCredentialsProvider (setCredentials (AuthScope. "stream.twitter.com" 80)
                                                             (UsernamePasswordCredentials. username password )))
      (. local-context (setAttribute "preemptive-auth" basic-auth))
      (. http-client (addRequestInterceptor (request-interceptor) 0))
      (dotimes [_ 3]
        (let [response (. http-client (execute target-host http-get local-context))
              entity (.getEntity response)]
        (prn (.getStatusLine response))
      (when entity
          (.consumeContent entity))))
      (.. http-client getConnectionManager shutdown))))


(defn- request-interceptor []
  (proxy [HttpRequestInterceptor] []
    (process [request context]
      (let [auth-state (. context (getAttribute ClientContext/TARGET_AUTH_STATE))]
        (when-not (.getAuthScheme auth-state)
          (let [auth-scheme (. context (getAttribute "preemptive-auth"))
                creds-provider (. context (getAttribute ClientContext/CREDS_PROVIDER))
                target-host (. context (getAttribute ExecutionContext/HTTP_TARGET_HOST))]
            (when auth-scheme
              (let [creds (. creds-provider (getCredentials (AuthScope. (.getHostName target-host)
                                                            (.getPort target-host))))]
                (if-not creds
                  (throw (HttpException. "No credentials for preemptive authentication"))
                  (doto auth-state
                    (.setAuthScheme auth-scheme)
                    (.setCredentials creds)))))))))))