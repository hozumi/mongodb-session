(ns hozumi.test-mongodb-session
  (:use [clojure.test]
	[ring.middleware.session.store]
	[hozumi.mongodb-session]
	[somnium.congomongo]))

(def test-db-host "127.0.0.1")
(def test-db "hozumi-test-mongodb-sessions")
(defn setup! [] (mongo! :db test-db :host test-db-host))
(defn teardown! []
  (drop-database! test-db))

(defmacro with-test-mongo [& body]
  `(do
     (setup!)
     ~@body
     (teardown!)))

(deftest mongo-session-read-not-exist
  (with-test-mongo
    (let [store (mongodb-store)]
      (is (read-session store "non-existent")
	  {}))))

(deftest mongo-session-create
  (with-test-mongo
    (let [store    (mongodb-store)
	  sess-key (write-session store nil {:foo "bar"})
	  entity   (read-session store sess-key)]
      (is (not (nil? sess-key)))
      (is (and (:_id entity) (:_date entity)))
      (is (= (dissoc entity :_id :_date)
	     {:foo "bar"})))))

(deftest mongo-session-update
  (with-test-mongo
    (let [store     (mongodb-store)
	  sess-key  (write-session store nil {:foo "bar"})
	  sess-key* (write-session store sess-key {:bar "baz"})
	  entity    (read-session store sess-key)]
      (is (= sess-key sess-key*))
      (is (and (:_id entity) (:_date entity)))
      (is (= (dissoc entity :_id :_date)
	     {:bar "baz"})))))

(deftest mongo-session-delete
  (with-test-mongo
    (let [store    (mongodb-store)
	  sess-key (write-session store nil {:foo "bar"})]
      (is (nil? (delete-session store sess-key)))
      (is (= (read-session store sess-key)
	     {})))))