(ns hozumi.test-mongodb-session
  (:use [clojure.test]
	[hozumi.mongodb-session]
	[somnium.congomongo]))

(def test-db-host "127.0.0.1")
(def test-db "hozumi-test-mongodb-sessions")
(defn setup! [] (mongo! :db test-db :host test-db-host))
(defn teardown! []
  (drop-database! test-db))
;;(insert! :ring-sessions {:_id 3 :a 2})
;;(-> (fetch :ring-sessions) )
;;(update! :ring-sessions {:_id 2} {:d 2 :c "hello"})
;;
(defmacro with-test-mongo [& body]
  `(do
     (setup!)
     ~@body
     (teardown!)))

(deftest mongodb-session-read-not-exist
  (with-test-mongo
    (let [store (mongodb-store)]
      (is ((:read store) "non-existent")
	  {}))))

(deftest mongodb-session-create
  (with-test-mongo
    (let [store    (mongodb-store)
	  sess-key ((:write store) nil {:foo "bar"})]
      (is (not (nil? sess-key)))
      (is (= (dissoc ((:read store) sess-key) :_id)
	     {:foo "bar"})))))

(deftest mongodb-session-update
  (with-test-mongo
    (let [store     (mongodb-store)
	  sess-key  ((:write store) nil {:foo "bar"})
	  sess-key* ((:write store) sess-key {:bar "baz"})]
      (is (= sess-key sess-key*))
      (is (= (dissoc ((:read store) sess-key) :_id)
	     {:bar "baz"})))))

(deftest mongodb-session-delete
  (with-test-mongo
    (let [store    (mongodb-store)
	  sess-key ((:write store) nil {:foo "bar"})]
      (is (nil? ((:delete store) sess-key)))
      (is (= ((:read store) sess-key)
	     {})))))
