(ns hozumi.mongodb-session
  (:require [somnium.congomongo :as congo]
	    [ring.middleware.session.store :as ringstore])
  (:import [java.util UUID Date]))

(deftype MongodbStore [collection-name auto-key-change?]
  ringstore/SessionStore
  (read-session [_ key]
		(if-let [entity (and key
				     (congo/fetch-one collection-name
						      :where {:_id key}))]
		  entity {}))
  (write-session [_ key data]
		 ;;work around for (name ::keyword) -> "keyword"
		 (let [data (zipmap (map #(if (keyword? %)
					    (-> % str (.substring 1)) %)
					 (keys data))
				    (vals data))
		       entity (and key (congo/fetch-one collection-name :where {:_id key}))
		       key-change? (or (not entity) auto-key-change?)
		       newkey (if key-change?
			     (str (UUID/randomUUID)) key)]
		   (if entity
		     (do (if key-change?
			   (do (congo/destroy! collection-name {:_id key})
			       (congo/insert! collection-name
					      (assoc data :_id newkey :_date (:_date entity))))
			   (congo/update! collection-name {:_id newkey}
					  (assoc data :_date (:_date entity))))
			 newkey)
		     (do (congo/insert! collection-name (assoc data :_id newkey :_date (Date.)))
			 newkey))))
  (delete-session [_ key]
		  (congo/destroy! collection-name {:_id key})
		  nil))

(defn mongodb-store
  ([] (mongodb-store {}))
  ([opt]
     (let [collection-name (opt :collection-name :ring_sessions)
	   auto-key-change? (opt :auto-key-change? false)]
       (MongodbStore. collection-name auto-key-change?))))
