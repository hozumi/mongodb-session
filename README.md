# mongodb-session

mongodb-session store ring's http session in mongodb.

## Usage
### functional way
Using **ring.middleware.session**.
Following example originated from [sandbar-examples](https://github.com/brentonashworth/sandbar-examples/blob/master/sessions/src/sandbar/examples/session_demo.clj).
    (ns hello
      (:use [ring.middleware.session]
            [hozumi.mongodb-session]))

    (def app (-> handler
               (wrap-session {:store (mongodb-store)})))

Then, you can use session like in-memory session.
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
Using [sandbar.stateful-session](https://github.com/brentonashworth/sandbar).
    (ns hello
      (:use [sandbar.stateful-session]
            [hozumi.mongodb-session]))

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

