(ns hozumi.mongodb-session
  (:use ring.middleware.session.store)
  (:require [somnium.congomongo :as mongo])
  (:import [java.util UUID Date]))

(defn mongodb-store
  ([] (mongodb-store :ring_sessions))
  ([collection-name]
     (reify
      SessionStore
      (read-session
       [_ key]
       (if-let [entity (and key
			    (mongo/fetch-one collection-name :where {:_id key}))]
	 entity {}))
      (write-session
       [_ key data]
       (let [data (zipmap (map #(if (keyword? %) ;;work around for ::keyword -> "keyword"
				  (-> % str (.substring 1)) %)
			       (keys data))
			  (vals data))]
	 (if-let [entity (and key (mongo/fetch-one collection-name :where {:_id key}))]
	   (do (mongo/update! collection-name {:_id key} (assoc data :_date (:_date entity)))
	       key)
	   (let [key (str (UUID/randomUUID))]
	     (mongo/insert! collection-name (assoc data :_id key :_date (Date.)))
	     key))))
      (delete-session
       [_ key]
       (mongo/destroy! collection-name {:_id key})
       nil))))
