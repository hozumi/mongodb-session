# mongodb-session

Mongodb-session use mongodb as a Clojure/Ring's http session storage.

## Usage
### functional way
**ring.middleware.session** version. Following examples are originated from [sandbar-examples](https://github.com/brentonashworth/sandbar-examples/blob/master/sessions/src/sandbar/examples/session_demo.clj).
    (ns hello
      (:use [ring.middleware.session]
            [somnium.congomongo]
            [hozumi.mongodb-session]))
	    
    (mongo! :db "mydb" :host "127.0.0.1")
    
    (def app (-> handler
               (wrap-session {:store (mongodb-store)})))

Then, you can use mongodb session in the same way as in-memory one.
    (defn functional-handler
      "Functional style of working with a session."
      [request]
      (let [counter (if-let [counter (-> request :session :counter)]
                      (+ counter 1)
                      1)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (html
                (layout "Functional" counter (link-to "/stateful" "Stateful")))
         :session {:counter counter}}))

### statuful way
[sandbar.stateful-session](https://github.com/brentonashworth/sandbar) version.
    (ns hello
      (:use [sandbar.stateful-session]
            [somnium.congomongo]
            [hozumi.mongodb-session]))
	    
    (mongo! :db "mydb" :host "127.0.0.1")
    
    (def app (-> handler
               (wrap-stateful-session {:store (mongodb-store)})))

    (defn stateful-handler
      "Stateful style of working with a session."
      []
      (let [counter (+ 1 (session-get :counter 0))]
        (do (session-put! :counter counter)
            (html
                 (layout "Stateful" counter (link-to "/functional" "Functional"))))))

## Installation
Leiningen
    [org.clojars.hozumi/mongodb-session "1.0.0-SNAPSHOT"]

