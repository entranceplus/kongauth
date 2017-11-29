(ns kongauth.db.util
  (:require [clojure.java.jdbc :as jdbc]
            [conman.core :as conman]
            [honeysql.core :as sql]
            [environ.core :refer [env]]
            [honeysql.helpers :as helpers :refer :all]
            ;; [clojure.spec.alpha :as s]
            [system.repl :refer [system]]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn make-vec-if-not [maybe-vec]
  (if ((complement seq?) maybe-vec)
    (conj []  maybe-vec)
(vec maybe-vec)))

(defn query [db sqlmap]
  (jdbc/query db (-> sqlmap sql/build sql/format)))

(defn execute! [db sqlmap]
  (jdbc/execute! db (let [sql (sql/format sqlmap)]
                                (println sql)
                                sql)))

(defn- prep-insert-data
  "if you have not set id it will"
  [data]
  (mapv (fn [data]
         (cond-> data
           (nil? (:id data)) (assoc :id (uuid))))
       (make-vec-if-not data)))

(defn add
  "insert data into table"
  [table data]
  (if-let [rows (prep-insert-data data)]
    (when ((complement empty?) rows)
      (execute! (-> (insert-into table)
                    (values rows)))
      rows)))

(def db-spec
  {:classname   "org.postgresql.Driver"
   :dbtype "postgres"
   :subprotocol "postgresql"})

(defn get-db-spec-from-env [& {:keys [config]}]
  (let [{:keys [dbuser db password host]} config]
    (println "config passed is " config)
    (println "Trying environment variables" (env :dbuser))
    (merge db-spec {:user (or (env :dbuser) dbuser)
                    :dbname (or (env :db) db)
                    :password (or (env :password) password)
                    :host (or (env :host)
                              host
                              "127.0.0.1")})))
