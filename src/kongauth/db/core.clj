(ns kongauth.db.core
  (:gen-class)
  (:require [kongauth.db.util :as dbutil]
            [honeysql.helpers :as helpers :refer :all]))

(defprotocol Authdb
  "Persistence protocol"
  (create-user [db user])
  (get-users [db {:keys [username]}]))

(deftype Postgres [db]
  Authdb
  (create-user [this user]
    (dbutil/execute! db (-> (insert-into :users)
                            (values [user]))))
  (get-users [this {:keys [username]}]
    (dbutil/query db {:select [:id :pass :username]
                      :from [:users]
                      :where [:= :users.username username]})))
