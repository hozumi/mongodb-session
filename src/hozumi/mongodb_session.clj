(ns hozumi.mongodb-session
  (:require [somnium.congomongo :as mongo])
  (:import [java.util UUID]))

(defn mongodb-store
  "Creates a mongodb session storage engine."
  [& [collection-name]]
  (let [collection-name (if collection-name collection-name :ring_sessions)]
  {:read (fn [session-key]
	   (if-let [s (mongo/fetch-one collection-name :where {:_id session-key})]
	     s {}))
   :write (fn [session-key session]
	    (let [session (zipmap (map #(if (keyword? %) ;;work around for ::keyword -> "keyword"
					  (-> % str (.substring 1)) %)
				       (keys session))
				  (vals session))]
	      (if (and session-key (mongo/fetch-one collection-name :where {:_id session-key}))
		(do (mongo/update! collection-name {:_id session-key} session)
		    session-key)
		(let [session-key (str (UUID/randomUUID))]
		  (mongo/insert! collection-name (assoc session :_id session-key))
		  session-key))))
   :delete (fn [session-key]
	     (mongo/destroy! collection-name {:_id session-key})
	     nil)}))
